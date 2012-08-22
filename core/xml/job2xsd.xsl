<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="xs" version="2.0" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:simmit="https://proj.5nord.org/simmit" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="path2specs">/Users/phia/akt_source/simmit/trunk/code/core/xml</xsl:param>
	<xsl:param name="path2modules">/Users/phia/akt_source/simmit/trunk/code/extensions/modules</xsl:param>
	<xsl:param name="path2modulesXsd" />
	<xsl:output indent="yes" method="xml"/>
	<xsl:template match="simmit:job">
		<schema>
			<xsl:variable name="simmit" select="'https://proj.5nord.org/simmit'"/>
			<xsl:attribute name="targetNamespace" select="$simmit"/>
			<xsl:namespace name="simmit" select="$simmit"/>
			<xsl:variable as="xs:string*" name="includedModules">
				<xsl:for-each-group group-by="." select="graph/nodes/node/module">
					<xsl:sequence select="concat($path2modules, '/', .,  '/instances/', ., '.xsd')"/>
				</xsl:for-each-group>
			</xsl:variable>
			<xsl:variable as="node()*" name="includedDocs">
				<xsl:for-each select="$includedModules">
					<xsl:sequence select="doc(.)" />
				</xsl:for-each>
			</xsl:variable>
			<xsl:for-each select="distinct-values(graph/nodes/node/module)">
				<xsl:namespace name="{.}" select="concat($simmit, '/modules/', .)"/>
			</xsl:for-each>
			<xsl:for-each select="distinct-values(graph/nodes/node/module)">
				<import namespace="{concat($simmit, '/modules/', .)}" schemaLocation="{concat($path2modules, '/', ., '/instances/', ., '.xsd')}"/>
			</xsl:for-each>
			<include schemaLocation="{concat($path2specs, '/moduleDescription.xsd')}"/>
			<element name="job">
				<complexType>
					<sequence>
						<group ref="simmit:headerInformationWithName"/>
						<element name="core">
							<complexType>
								<sequence>
									<xsl:if test="core/server">
										<element  name="server">
											<complexType>
												<sequence>
													<element fixed="{core/server/host}" name="host" />
													<element fixed="{core/server/port}" name="port" />
												</sequence>
											</complexType>
										</element>
									</xsl:if>
									<element name="{name(core/*[last()])}"/>
								</sequence>
							</complexType>
						</element>
						<element name="graph" type="simmit:graph"/>
						<element name="settings" type="simmit:job_settings"/>
					</sequence>
				</complexType>
			</element>
			<complexType name="graph">
				<sequence>
					<element name="nodes" type="simmit:nodes"/>
					<element name="edges" type="simmit:edges"/>
					<element name="phases" type="simmit:phases"/>
				</sequence>
			</complexType>
			<complexType name="nodes">
				<all>
					<xsl:for-each select="graph/nodes/node">
						<element name="node">
							<complexType>
								<sequence>
									<element fixed="{name}" name="name"/>
									<element fixed="{description}" name="description" />
									<element fixed="{module}" name="module"/>
									<element fixed="{instance}" name="instance"/>
									<element name="location">
										<complexType>
											<sequence>
											<xsl:if test="location/server">
												<element  name="server">
													<complexType>
														<sequence>
															<element fixed="{location/server/host}" name="host" />
															<element fixed="{location/server/port}" name="port" />
														</sequence>
													</complexType>
												</element>
											</xsl:if>
												<element name="{name(location/*[last()])}"/>
											</sequence>
										</complexType>
									</element>
									<element minOccurs="0" name="configuration">
										<complexType>
											<all>
												<xsl:copy-of select="simmit:getElementsFromSchema(module, 'configuration', $includedDocs)"/>
											</all>
										</complexType>
									</element>
								</sequence>
							</complexType>
						</element>
					</xsl:for-each>
				</all>
			</complexType>
			<complexType name="edges">
				<sequence>
					<xsl:for-each select="graph/edges/edge">
						<element minOccurs="0" name="edge">
							<complexType>
								<sequence>
									<element fixed="{name}" name="name"/>
									<element fixed="{description}" name="description" />
									<element name="minSize" type="nonNegativeInteger" default="0" minOccurs="0"/>
									<xsl:for-each select="node">
										<element name="node">
											<complexType>
												<sequence>
													<xsl:variable as="xs:string" name="nodeName" select="name"/>
													<element fixed="{$nodeName}" name="name"/>
													<element fixed="{port}" name="port">
														<complexType>
															<simpleContent>
																<extension base="string">
																	<attribute fixed="{simmit:getPortDirection(port, /*/graph/nodes/node[name = $nodeName]/module, $includedDocs)}" name="direction"/>
																</extension>
															</simpleContent>
														</complexType>
													</element>
												</sequence>
											</complexType>
										</element>
									</xsl:for-each>
								</sequence>
							</complexType>
						</element>
					</xsl:for-each>
				</sequence>
			</complexType>
			<complexType name="phases">
				<sequence>
					<element maxOccurs="unbounded" name="phase">
						<complexType>
							<sequence>
								<element maxOccurs="unbounded" name="node">
									<complexType>
										<sequence>
									
											<element name="name">
												<simpleType>
													<restriction base="string">
														<xsl:for-each select="graph/nodes/node/name">
															<enumeration value="{.}"/>
														</xsl:for-each>
													</restriction>
												</simpleType>
											</element>
											<element minOccurs="0" name="observed">
												<complexType/>
											</element>
											<sequence>
												<element maxOccurs="unbounded" minOccurs="0" name="port">
													<complexType>
														<sequence>
															<element name="name" type="string"/>
															<element minOccurs="0" name="reset">
																<complexType/>
															</element>
														</sequence>
													</complexType>
												</element>
											</sequence>
										</sequence>
									</complexType>
								</element>
							</sequence>
							<attribute name="nr" type="integer"/>
						</complexType>
					</element>
				</sequence>
			</complexType>
			<complexType name="job_settings">
				<sequence>
					<element name="default">
						<complexType>
							<sequence>
								<element maxOccurs="unbounded" minOccurs="0" name="node" type="simmit:availableNodes"/>
							</sequence>
						</complexType>
					</element>
					<element name="rounds">
						<complexType>
							<sequence>
								<element minOccurs="0" name="description" type="string"/>
								<element maxOccurs="{settings/rounds/@anz}" minOccurs="0" name="round">
									<complexType>
										<sequence>
											<element minOccurs="0" name="description" type="string"/>
											<element maxOccurs="unbounded" minOccurs="0" name="node" type="simmit:availableNodes"/>
										</sequence>
										<attribute name="nr">
											<simpleType>
												<restriction base="integer">
													<minInclusive value="1"/>
													<maxInclusive value="{settings/rounds/@anz}"/>
												</restriction>
											</simpleType>
										</attribute>
									</complexType>
								</element>
							</sequence>
							<attribute fixed="{settings/rounds/@anz}" name="anz"/>
						</complexType>
					</element>
				</sequence>
			</complexType>
			<complexType name="availableNodes">
				<choice>
					<xsl:for-each-group group-by="." select="graph/nodes/node/module">
						<xsl:variable name="moduleName" select="."/>
						<sequence>
							<element name="name">
								<simpleType>
									<restriction base="string">
										<xsl:for-each select="/*/graph/nodes/node/name[../module = $moduleName]">
											<enumeration value="{.}"/>
										</xsl:for-each>
									</restriction>
								</simpleType>
							</element>
							<xsl:copy-of select="simmit:getElementsFromSchema(., 'settings', $includedDocs)"/>
						</sequence>
					</xsl:for-each-group>
				</choice>
			</complexType>
		</schema>
	</xsl:template>
	<xsl:function name="simmit:getElementsFromSchema">
		<xsl:param as="xs:string" name="moduleName"/>
		<xsl:param as="xs:string" name="type"/>
		<xsl:param name="includedDocs"/>

		<xsl:for-each select="simmit:getComplexFromSchema($type, $moduleName, $includedDocs)/*[name() = 'all']/*">
			<element minOccurs="0" name="{@name}" type="{replace(@type, 'self:', concat($moduleName, ':'))}"/>
		</xsl:for-each>
	</xsl:function>
	<xsl:function name="simmit:getPortDirection">
		<xsl:param as="xs:string" name="portName"/>
		<xsl:param as="xs:string" name="moduleName"/>
		<xsl:param name="includedDocs"/>
	
		<xsl:variable name="port" select="simmit:getComplexFromSchema('portList', $moduleName, $includedDocs)/*/*[@name = $portName]"/>
		<xsl:if test="not($port)">
			<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createJob', 'err:symbols'), concat('Error: port not found in Module ', $moduleName, ': ', $portName))"/>
		</xsl:if>
		<xsl:value-of select="if(matches($port/@type, 'output')) then 'output' else 'input'"/>
	</xsl:function>
	<xsl:function as="node()" name="simmit:getComplexFromSchema">
		<xsl:param as="xs:string" name="complexName"/>
		<xsl:param as="xs:string" name="moduleName"/>
		<xsl:param name="includedDocs"/>
		<xsl:copy-of select="$includedDocs/*/*[name() = 'complexType' and @name = $complexName and ../*[name() = 'element' and  @name = $moduleName]]"/>
	</xsl:function>
</xsl:stylesheet>
