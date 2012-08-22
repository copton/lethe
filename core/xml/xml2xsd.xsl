<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:simmit="https://proj.5nord.org/simmit" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="path2types" />
	<xsl:param name="path2specs" />
	
	<xsl:output indent="yes" method="xml"/>
	<xsl:template match="/">
		<xsl:value-of select="codepoints-to-string(10)"/>
		<xsl:comment>
			<xsl:value-of select="concat('generated at: ', current-dateTime())"/>
		</xsl:comment>
		<xsl:value-of select="codepoints-to-string(10)"/>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="simmit:module">
		<xsl:element name="schema">
			<xsl:variable name="simmit" select="'https://proj.5nord.org/simmit'"/>
			<xsl:attribute name="targetNamespace">
				<xsl:value-of select="concat($simmit, '/modules/', name)"/>
			</xsl:attribute>
			<xsl:namespace name="simmit">
				<xsl:value-of select="$simmit"/>
			</xsl:namespace>
			<xsl:namespace name="self">
				<xsl:value-of select="concat($simmit, '/modules/', name)"/>
			</xsl:namespace>
			<xsl:for-each select="distinct-values(include/type)">
				<xsl:namespace name="{.}">
					<xsl:value-of select="concat($simmit, '/types/', .)"/>
				</xsl:namespace>
			</xsl:for-each>
			<import namespace="https://proj.5nord.org/simmit" schemaLocation="{concat($path2specs, '/moduleDescription.xsd')}"/>
			<xsl:for-each select="distinct-values(include/type)">
				<import namespace="https://proj.5nord.org/simmit/types/{.}" schemaLocation="{concat($path2types, '/', ., '/instances/', ., '.xsd')}"/>
			</xsl:for-each>
			<element name="{name}">
				<complexType>
					<sequence>
						<group ref="self:headerSet"/>
						<element name="include" type="self:include" />
						<element name="parameter" type="self:parameter"/>
						<element name="ports" type="self:portList"/>
					</sequence>
				</complexType>
			</element>
			<group name="headerSet">
				<sequence>
					<element minOccurs="0" name="author" type="string"/>
					<element minOccurs="0" name="version" type="string"/>
					<element minOccurs="0" name="date" type="date"/>
					<element minOccurs="0" name="type" type="string"/>
					<element minOccurs="0" name="description" type="string"/>
				</sequence>
			</group>
			<complexType name="include">
				<all>
					<xsl:for-each select="include/type">
						<element name="type" fixed="{.}" />
					</xsl:for-each>
				</all>
			</complexType>
			<complexType name="parameter">
				<all>
					<xsl:for-each select="parameter/*[matches(name(), 'configuration|settings')]">
						<element name="{name()}" type="self:{name()}"/>
					</xsl:for-each>
				</all>
			</complexType>
			<complexType name="portList">
				<all>
					<xsl:for-each select="ports/*/port">
						<element name="{name}" type="simmit:{../name()}Port"/>
					</xsl:for-each>
				</all>
			</complexType>
			<xsl:for-each select="parameter/*[matches(name(), 'configuration|settings')]">
				<complexType name="{name()}">
					<xsl:copy-of select="simmit:createParameterDefinition(.)"/>
				</complexType>
			</xsl:for-each>
			<xsl:for-each select="define/*">
				<xsl:copy-of select="simmit:createParameterTypes(.)"/>
			</xsl:for-each>
			<xsl:for-each select="//*[minVale or maxValue or minLength or maxLength or length]">
				<xsl:copy-of select="simmit:createParameterRestrictionTypes(.)"/>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	<xsl:template match="simmit:parameter">
		<xsl:element name="schema">
			<xsl:variable name="simmit" select="'https://proj.5nord.org/simmit'"/>
			<xsl:attribute name="targetNamespace" select="concat($simmit, '/types/', name)"/>
			<xsl:namespace name="simmit">
				<xsl:value-of select="$simmit"/>
			</xsl:namespace>
			<xsl:namespace name="self">
				<xsl:value-of select="concat($simmit, '/types/', name)"/>
			</xsl:namespace>
			<xsl:for-each select="distinct-values(include/type)">
				<xsl:namespace name="{.}">
					<xsl:value-of select="concat($simmit, '/types/', .)"/>
				</xsl:namespace>
			</xsl:for-each>
			<xsl:for-each select="distinct-values(include/type)">
				<import namespace="https://proj.5nord.org/simmit/types/{.}" schemaLocation="{concat($path2types, '/', ., '/', ., '.xsd')}"/>
			</xsl:for-each>
			<import namespace="https://proj.5nord.org/simmit" schemaLocation="{concat($path2specs, '/moduleParameter.xsd')}"/>
			<element name="{name}">
				<complexType>
					<sequence>
						<group ref="self:headerSet"/>
						<xsl:if test="include/*">
							<element ref="self:include"/>
						</xsl:if>
						<element name="include" type="self:include" />
						<element name="export" type="self:{name}"/>
					</sequence>
					<attribute name="imprints" type="string"/>
				</complexType>
			</element>
			<group name="headerSet">
				<sequence>
					<element minOccurs="0" name="author" type="string"/>
					<element minOccurs="0" name="version" type="string"/>
					<element minOccurs="0" name="date" type="date"/>
					<element minOccurs="0" name="description" type="string"/>
				</sequence>
			</group>
			<complexType name="include">
				<all>
					<xsl:for-each select="include/type">
						<element name="type" fixed="{.}" />
					</xsl:for-each>
				</all>
			</complexType>
			<xsl:for-each select="define/*">
				<xsl:copy-of select="simmit:createParameterTypes(.)"/>
			</xsl:for-each>
			<xsl:for-each select="//*[minVale or maxValue or minLength or maxLength or length]">
				<xsl:copy-of select="simmit:createParameterRestrictionTypes(.)"/>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	<xsl:function name="simmit:createParameterDefinition">
		<xsl:param as="node() *" name="parameter"/>
		<xsl:variable name="defined" select="root($parameter[1])/*/define/*/name"/>
		<all>
			<xsl:for-each select="$parameter/*">
				<xsl:choose>
					<xsl:when test="name() = 'string'">
						<xsl:copy-of select="simmit:writeString(.)"/>
					</xsl:when>
					<xsl:when test="name() = 'boolean'">
						<element name="{name}" type="simmit:boolean"/>
					</xsl:when>
					<xsl:when test="matches(name(), 'int|long|byte|float|double')">
						<xsl:copy-of select="simmit:writeNumber(.)"/>
					</xsl:when>
					<xsl:when test="name() = 'instance'">
						<element name="{name}" type="{if(index-of($defined, @name) &gt; 0) then 'self' else @name}:{@name}"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXsd', 'err:object'), concat('Error: unknownObject: ', node-name(.) ))"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</all>
	</xsl:function>
	<xsl:function name="simmit:createParameterTypes">
		<xsl:param as="node() *" name="parameter"/>
		<xsl:choose>
			<xsl:when test="$parameter/name() = 'class'">
				<xsl:copy-of select="simmit:writeClassType($parameter)"/>
			</xsl:when>
			<xsl:when test="$parameter/name() = 'enum'">
				<xsl:copy-of select="simmit:writeEnumType($parameter)"/>
			</xsl:when>
			<xsl:when test="$parameter/name() = 'array'">
				<xsl:copy-of select="simmit:writeArrayType($parameter)"/>
			</xsl:when>
			<xsl:when test="$parameter/name() = 'table'">
				<xsl:copy-of select="simmit:writeTableType($parameter)"/>
			</xsl:when>
			<xsl:when test="$parameter/name() = 'dictionary'">
				<xsl:copy-of select="simmit:writeDictionaryType($parameter)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXsd', 'err:symbols'), concat('Error: unknownClasstype: ', node-name($parameter)))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:function name="simmit:writeOptionalParameter">
		<xsl:param name="obj"/>
		<xsl:param name="name"/>
		<xsl:copy-of select="simmit:writeOptionalParameter($obj, $name, $name)"/>
	</xsl:function>
	<xsl:function name="simmit:writeOptionalParameter">
		<xsl:param name="obj"/>
		<xsl:param name="attr"/>
		<xsl:param name="name"/>
		<xsl:variable name="value" select="$obj/*[name() = $attr]"/>
		<xsl:if test="$value">
			<attribute fixed="{$value}" name="{$name}"/>
		</xsl:if>
	</xsl:function>
	<xsl:function name="simmit:writeString">
		<xsl:param as="node()" name="string"/>
		<xsl:variable name="restriction" select="simmit:getRestrictions($string)"/>
		<xsl:choose>
			<xsl:when test="string-length($restriction)">
				<element name="{$string/name}">
					<complexType>
						<simpleContent>
							<extension base="{$restriction}">
								<attributeGroup ref="simmit:string_attributeGroup"/>
							</extension>
						</simpleContent>
					</complexType>
				</element>
			</xsl:when>
			<xsl:otherwise>
				<element name="{$string/name}" type="simmit:string"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:function name="simmit:writeNumber">
		<xsl:param as="node()" name="number"/>
		<xsl:variable name="restriction" select="simmit:getRestrictions($number)"/>
		<xsl:choose>
			<xsl:when test="string-length($restriction)">
				<element name="{$number/name}">
					<complexType>
						<simpleContent>
							<extension base="{$restriction}">
								<attributeGroup ref="simmit:{$number/name()}_attributeGroup"/>
							</extension>
						</simpleContent>
					</complexType>
				</element>
			</xsl:when>
			<xsl:otherwise>
				<element name="{$number/name}" type="simmit:{$number/name()}"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:function name="simmit:writeClassType">
		<xsl:param as="node()" name="obj"/>
		<complexType name="{$obj/name}">
			<xsl:copy-of select="simmit:createParameterDefinition($obj/members)"/>
			<attribute fixed="class" name="type"/>
			<attribute default="{$obj/description}" name="description"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'name', 'classname')"/>
		</complexType>
	</xsl:function>
	<xsl:function name="simmit:writeEnumType">
		<xsl:param as="node()" name="obj"/>
		<complexType name="{$obj/name}">
			<simpleContent>
				<extension base="self:{$obj/name}_RESTRICTION">
					<attribute fixed="{string-join($obj/members/name, ' ')}" name="elements"/>
					<attribute fixed="enum" name="type"/>
					<attribute default="{$obj/description}" name="description"/>
					<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'name', 'classname')"/>
				</extension>
			</simpleContent>
		</complexType>
		<simpleType name="{$obj/name}_RESTRICTION">
			<restriction base="string">
				<xsl:for-each select="$obj/members/name">
					<enumeration value="{.}"/>
				</xsl:for-each>
			</restriction>
		</simpleType>
	</xsl:function>
	<xsl:function name="simmit:writeArrayType">
		<xsl:param as="node()" name="obj"/>
		<complexType name="{$obj/name}">
			<sequence>
				<element maxOccurs="{if($obj/maxSize) then $obj/maxSize else if($obj/size) then $obj/size else 'unbounded'}" minOccurs="{if($obj/minSize) then $obj/minSize else if($obj/size) then $obj/size else '0'}" name="field">
					<complexType>
						<xsl:choose>
							<xsl:when test="matches($obj/elementType, 'string|int|long|byte|float|double')">
								<xsl:variable name="restriction" select="simmit:getRestrictions($obj)"/>
								<simpleContent>
									<extension base="{if(string-length($restriction)) then $restriction else if($obj/elementType = 'int') then 'integer' else $obj/elementType}">
										<attribute name="pos" type="integer"/>
										<attribute name="type" fixed="{$obj/elementType}"/>
									</extension>
								</simpleContent>
							</xsl:when>
							<xsl:otherwise>
								<complexContent>
									<extension base="{if(index-of(root($obj)/*/define/*/name, $obj/elementType) > 0) then 'self' else $obj/elementType}:{$obj/elementType}">
										<attribute name="pos" type="integer"/>
									</extension>
								</complexContent>
							</xsl:otherwise>
						</xsl:choose>
					</complexType>
				</element>
			</sequence>
			<attribute fixed="array" name="type"/>
			<attribute default="{$obj/description}" name="description"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'name', 'classname')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'elementType')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'size')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minSize')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxSize')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minValue')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxValue')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minLength')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxLength')"/>
		</complexType>
	</xsl:function>
	<xsl:function name="simmit:writeTableType">
		<xsl:param as="node()" name="obj"/>
		<complexType name="{$obj/name}">
			<sequence>
				<element maxOccurs="{if($obj/maxRows) then $obj/maxRows else if($obj/anzRows) then $obj/anzRows else 'unbounded'}" minOccurs="{if($obj/minRows) then $obj/minRows else if($obj/anzRows) then $obj/anzRows else '0'}" name="row">
					<complexType>
						<sequence>
							<element maxOccurs="{if($obj/maxCols) then $obj/maxCols else if($obj/anzCols) then $obj/anzCols else 'unbounded'}" minOccurs="{if($obj/minCols) then $obj/minCols else if($obj/anzCols) then $obj/anzCols else '0'}" name="field">
								<complexType>
									<xsl:choose>
										<xsl:when test="matches($obj/elementType, 'string|int|long|byte|float|double')">
											<xsl:variable name="restriction" select="simmit:getRestrictions($obj)"/>
											<simpleContent>
												<extension base="{if(string-length($restriction)) then $restriction else if($obj/elementType = 'int') then 'integer' else $obj/elementType}">
													<attribute name="pos" type="integer"/>
													<attribute name="type" fixed="{$obj/elementType}"/>
												</extension>
											</simpleContent>
										</xsl:when>
										<xsl:otherwise>
											<complexContent>
												<extension base="{if(index-of(root($obj)/*/define/*/name, $obj/elementType) &gt; 0) then 'self' else $obj/elementType}:{$obj/elementType}">
													<attribute name="pos" type="integer"/>
												</extension>
											</complexContent>
										</xsl:otherwise>
									</xsl:choose>
								</complexType>
							</element>
						</sequence>
						<attribute name="pos" type="integer"/>
					</complexType>
				</element>
			</sequence>
			<attribute fixed="table" name="type"/>
			<attribute default="{$obj/description}" name="description"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'name', 'classname')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'elementType')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'anzRows')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minRows')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxRows')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'anzCols')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minCols')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxCols')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minValue')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxValue')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'minLength')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'maxLength')"/>
		</complexType>
	</xsl:function>
	<xsl:function name="simmit:writeDictionaryType">
		<xsl:param as="node()" name="obj"/>
		<complexType name="{$obj/name}">
			<sequence>
				<element name="key" minOccurs="0" maxOccurs="unbounded">
					<complexType>
						<complexContent>
							<extension base="{if(index-of(root($obj)/*/define/*/name, $obj/valueType) &gt; 0) then 'self' else $obj/valueType}:{$obj/valueType}">
								<attribute name="name" type="string"/>
								<xsl:if test="matches($obj/valueType, 'string|int|byte|long|float|double|boolean')">
									<attribute name="type" fixed="{$obj/valueType}"/>
								</xsl:if>
							</extension>
						</complexContent>
					</complexType>
				</element>
			</sequence>
			<attribute fixed="dictionary" name="type"/>
			<attribute default="{$obj/description}" name="description"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'name', 'classname')"/>
			<xsl:copy-of select="simmit:writeOptionalParameter($obj, 'valueType')"/>
		</complexType>
	</xsl:function>
	<xsl:function name="simmit:createParameterRestrictionTypes">
		<xsl:param name="param"/>
		<simpleType name="{if($param/../../name) then $param/../../name else $param/../name()}_{$param/name}_RESTRICTION">
			<restriction base="{if($param/name() = 'int') then 'integer' else if($param/elementType) then $param/elementType else if ($param/valueType)then $param/valueType else $param/name()}">
				<xsl:if test="$param/minValue">
					<minInclusive value="{$param/minValue/text()}"/>
				</xsl:if>
				<xsl:if test="$param/maxValue">
					<maxInclusive value="{$param/maxValue/text()}"/>
				</xsl:if>
				<xsl:if test="$param/minLength">
					<minLength value="{$param/minLength/text()}"/>
				</xsl:if>
				<xsl:if test="$param/maxLength">
					<maxLength value="{$param/maxLength/text()}"/>
				</xsl:if>
				<xsl:if test="$param/length">
					<length value="{$param/length/text()}"/>
				</xsl:if>
			</restriction>
		</simpleType>
	</xsl:function>
	<xsl:function name="simmit:getRestrictions">
		<xsl:param name="obj"/>
		<xsl:choose>
			<xsl:when test="$obj[minValue or maxValue or minLength or maxLength or length]">
				<xsl:value-of select="concat('self:', string-join((if($obj/../../name) then $obj/../../name else $obj/../name(), $obj/name, 'RESTRICTION'), '_'))"/>
			</xsl:when>
			<xsl:when test="$obj/..">
				<xsl:value-of select="simmit:getRestrictions($obj/..)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
</xsl:stylesheet>
