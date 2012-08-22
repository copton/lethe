<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet exclude-result-prefixes="xs simmit" version="2.0" xmlns:simmit="https://proj.5nord.org/simmit" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="path2types" />
	<xsl:param name="imprinting" />
	
	<xsl:output indent="yes" method="xml"/>
	<xsl:template match="/">
		<xsl:value-of select="simmit:checkFile(.)"/>
		<xsl:value-of select="codepoints-to-string(10)"/>
		<xsl:comment>
			<xsl:value-of select="concat('generated at: ', current-dateTime())"/>
		</xsl:comment>
		<xsl:value-of select="codepoints-to-string(10)"/>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="simmit:parameter">
		<xsl:element name="type:{name}" namespace="https://proj.5nord.org/simmit/types/{name}">
			<xsl:namespace name="xsi">http://www.w3.org/2001/XMLSchema-instance</xsl:namespace>
			<xsl:attribute name="xsi:schemaLocation" select="concat('https://proj.5nord.org/simmit/types/', name, ' ', name, '.xsd')" />
			<xsl:attribute name="imprints" select="$imprinting" />
			
			<xsl:variable name="imprints" as="xs:string*">
				<xsl:for-each select="//*[@imprints]">
					<xsl:sequence select="concat($path2types, '/', @name, '/instances/', @imprints, '.xml')" />
				</xsl:for-each>
			</xsl:variable>
		
			<xsl:variable name="includedDocs" as="node() *">
				<xsl:for-each select="distinct-values($imprints)">
					<xsl:sequence select="doc(.)"/>
				</xsl:for-each>
			</xsl:variable>
			
			<xsl:variable name="obj">
				<class name="{name}">
					<name>export</name>
				</class>
			</xsl:variable>
		
			<xsl:copy-of copy-namespaces="no" select="author|version|date|description|include"/>
			<xsl:copy-of select="simmit:writeParameter($obj/class, define/*, $includedDocs)"/> 
		</xsl:element>
	</xsl:template>
	<xsl:template match="simmit:module">
		<xsl:element name="module:{name}" namespace="https://proj.5nord.org/simmit/modules/{name}">
			<xsl:namespace name="xsi">http://www.w3.org/2001/XMLSchema-instance</xsl:namespace>
			<xsl:attribute name="xsi:schemaLocation"><xsl:value-of select="concat('https://proj.5nord.org/simmit/modules/', name, ' ', name, '.xsd')"/></xsl:attribute>
			
			<xsl:variable name="imprints" as="xs:string*">
				<xsl:for-each select="//instance[@imprints]">
					<xsl:sequence select="concat($path2types, '/', @name, '/instances/', @imprints, '.xml')" />
				</xsl:for-each>
			</xsl:variable>
		
			<xsl:variable name="includedDocs" as="node() *">
				<xsl:for-each select="distinct-values($imprints)">
					<xsl:sequence select="doc(.)"/>
				</xsl:for-each>
			</xsl:variable>
			<xsl:copy-of copy-namespaces="no" select="author|version|date|type|description|include"/>
			<parameter>
				<xsl:for-each select="parameter/*[matches(name(), 'configuration|settings')]">
					<xsl:element name="{name()}">
						<xsl:copy-of copy-namespaces="no" select="simmit:writeParameter(./*, /simmit:module/define/*, $includedDocs)"/>
					</xsl:element>
				</xsl:for-each>
			</parameter>
			<ports>
				<xsl:apply-templates select="ports/*">
				<xsl:with-param name="root" select="."/>
				</xsl:apply-templates>
			</ports>
		</xsl:element>
	</xsl:template>
	<xsl:function name="simmit:writeParameter">
		<xsl:param as="node() *" name="parameter"/>
		<xsl:param as="node() *" name="definitions"/>
		<xsl:param as="node() *" name="includes"/>
		<xsl:for-each select="$parameter">
			<xsl:choose>
				<xsl:when test="matches(name(), 'string')">
					<xsl:copy-of select="simmit:writeString(.)"/>
				</xsl:when>
				<xsl:when test="matches(name(), 'int|long|float|double|byte')">
					<xsl:copy-of select="simmit:writeNumber(.)"/>
				</xsl:when>
				<xsl:when test="matches(name(), 'boolean')">
					<xsl:copy-of select="simmit:writeBoolean(.)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of copy-namespaces="no" select="simmit:writeClass(., $definitions, $includes)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:function>
	<xsl:function name="simmit:optionalAttribute">
		<xsl:param name="node"/>
		<xsl:param as="xs:string" name="name"/>
		<xsl:variable name="value" select="$node/*[name() = $name]"/>
		<xsl:if test="$value">
			<xsl:attribute name="{$name}">
				<xsl:value-of select="$value"/>
			</xsl:attribute>
		</xsl:if>
	</xsl:function>
	<xsl:function name="simmit:writeString">
		<xsl:param name="string"/>
		<xsl:element name="{$string/name}">
			<xsl:attribute name="type" select="'string'"/>
			<xsl:copy-of select="simmit:optionalAttribute($string, 'description')"/>
			<xsl:copy-of select="simmit:optionalAttribute($string, 'minLength')"/>
			<xsl:copy-of select="simmit:optionalAttribute($string, 'maxLength')"/>
			<xsl:copy-of select="simmit:optionalAttribute($string, 'length')"/>
			<xsl:value-of select="$string/default"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeNumber">
		<xsl:param name="number"/>
		<xsl:element name="{$number/name}">
			<xsl:attribute name="type">
				<xsl:value-of select="$number/name()"/>
			</xsl:attribute>
			<xsl:copy-of select="simmit:optionalAttribute($number, 'description')"/>
			<xsl:copy-of select="simmit:optionalAttribute($number, 'minValue')"/>
			<xsl:copy-of select="simmit:optionalAttribute($number, 'maxValue')"/>
			<xsl:value-of select="$number/default"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeBoolean">
		<xsl:param name="bool"/>
		<xsl:element name="{$bool/name}">
			<xsl:value-of select="simmit:optionalAttribute($bool, 'description')"/>
			<xsl:value-of select="$bool/default"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeClass">
		<xsl:param name="obj"/>
		<xsl:param name="definitions"/>
		<xsl:param as="node() *" name="includes"/>
		<xsl:choose>
			<xsl:when test="$obj/@name and $includes/*[matches(name(), concat('type:', $obj/@name))]">
				<xsl:copy-of select="simmit:writeIncludedObject($obj, $includes/*[name() = concat('type:', $obj/@name) and @imprints = $obj/@imprints])"/>
			</xsl:when>
			<xsl:when test="$obj/@name and $definitions[name =  $obj/@name]">
				<xsl:variable name="baseclass" select="$definitions[name = $obj/@name]"/>
				<xsl:choose>
					<xsl:when test="$baseclass/name() = 'class'">
						<xsl:copy-of select="simmit:writeClassObject($obj, $baseclass, $definitions, $includes)"/>
					</xsl:when>
					<xsl:when test="$baseclass/name() = 'enum'">
						<xsl:copy-of select="simmit:writeEnumObject($obj, $baseclass)"/>
					</xsl:when>
					<xsl:when test="$baseclass/name() = 'array'">
						<xsl:copy-of select="simmit:writeArrayObject($obj, $baseclass)"/>
					</xsl:when>
					<xsl:when test="$baseclass/name() = 'table'">
						<xsl:copy-of select="simmit:writeTableObject($obj, $baseclass)"/>
					</xsl:when>
					<xsl:when test="$baseclass/name() = 'dictionary'">
						<xsl:copy-of select="simmit:writeDictionaryObject($obj, $baseclass)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXml', 'err:classnames'), concat('Error: Unknown type: ', $baseclass/name))"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXml', 'err:classnames'), concat('Error: Unknown class: ', $obj/@name))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:function name="simmit:writeClassObject">
		<xsl:param name="obj"/>
		<xsl:param name="base"/>
		<xsl:param name="definitions"/>
		<xsl:param as="node() *" name="includes"/>
		<xsl:element name="{$obj/name}">
			<xsl:attribute name="type">class</xsl:attribute>
			<xsl:attribute name="classname">
				<xsl:value-of select="$base/name"/>
			</xsl:attribute>
			<xsl:copy-of select="simmit:optionalAttribute(if($obj/description) then $obj else $base, 'description')"/>
			<xsl:copy-of select="simmit:writeParameter($base/members/*, $definitions, $includes)"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeEnumObject">
		<xsl:param name="obj"/>
		<xsl:param name="base"/>
		<xsl:element name="{$obj/name}">
			<xsl:attribute name="type">enum</xsl:attribute>
			<xsl:attribute name="classname">
				<xsl:value-of select="$base/name"/>
			</xsl:attribute>
			<xsl:attribute name="elements">
				<xsl:value-of select="string-join($base/members/name, ' ')"/>
			</xsl:attribute>
			<xsl:copy-of select="simmit:optionalAttribute(if($obj/description) then $obj else $base, 'description')"/>
			<xsl:value-of select="$base/default"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeArrayObject">
		<xsl:param name="obj"/>
		<xsl:param name="base"/>
		<xsl:element name="{$obj/name}">
			<xsl:attribute name="type">array</xsl:attribute>
			<xsl:attribute name="classname">
				<xsl:value-of select="$base/name"/>
			</xsl:attribute>
			<xsl:attribute name="elementType">
				<xsl:value-of select="$base/elementType"/>
			</xsl:attribute>
			<xsl:copy-of select="simmit:optionalAttribute(if($obj/description) then $obj else $base, 'description')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'minSize')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'maxSize')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'size')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'minValue')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'maxValue')"/>
			<xsl:for-each select="1 to (if($base/size) then $base/size else  if($base/minSize) then $base/minSize else 1)">
				<field pos="{.}">
					<xsl:copy-of select="simmit:getValue($base/default)"/>
				</field>
			</xsl:for-each>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeTableObject">
		<xsl:param name="obj"/>
		<xsl:param name="base"/>
		<xsl:element name="{$obj/name}">
			<xsl:attribute name="type">table</xsl:attribute>
			<xsl:attribute name="classname">
				<xsl:value-of select="$base/name"/>
			</xsl:attribute>
			<xsl:attribute name="elementType">
				<xsl:value-of select="$base/elementType"/>
			</xsl:attribute>
			<xsl:copy-of select="simmit:optionalAttribute(if($obj/description) then $obj else $base, 'description')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'minRows')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'maxRows')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'anzRows')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'minCols')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'maxCols')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'anzCols')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'minValue')"/>
			<xsl:copy-of select="simmit:optionalAttribute($base, 'maxValue')"/>
			<xsl:for-each select="1 to (if($base/anzRows) then $base/anzRows else if($base/minRows) then $base/minRows else 1)">
				<row pos="{.}">
					<xsl:for-each select="1 to (if($base/anzCols) then $base/anzCols else if($base/minCols) then $base/minCols else 1)">
						<field pos="{.}">
							<xsl:copy-of select="simmit:getValue($base/default)"/>
						</field>
					</xsl:for-each>
				</row>
			</xsl:for-each>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeDictionaryObject">
		<xsl:param name="obj"/>
		<xsl:param name="base"/>
		<xsl:element name="{$obj/name}">
			<xsl:attribute name="type">dictionary</xsl:attribute>
			<xsl:attribute name="classname" select="$base/name"/>
			<xsl:attribute name="valueType" select="$base/valueType"/>
			<xsl:copy-of select="simmit:optionalAttribute(if($obj/description) then $obj else $base, 'description')"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:writeIncludedObject">
		<xsl:param name="obj"/>
		<xsl:param name="base"/>
		<xsl:element name="{$obj/name}">
			<xsl:attribute name="type" select="$base/export/@type" />
			<xsl:attribute name="classname" select="$obj/@name"/>
			<xsl:copy-of select="simmit:optionalAttribute(if($obj/description) then $obj else $base, 'description')"/>
			<xsl:copy-of select="$base/export/*"/>
		</xsl:element>
	</xsl:function>
	<xsl:function name="simmit:getValue">
		<xsl:param name="value"/>
		<xsl:choose>
			<xsl:when test="count($value/*) = 0">
				<xsl:value-of select="$value"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$value/*"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:template match="port">
		<xsl:element name="{name}">
			<xsl:attribute name="direction">
				<xsl:value-of select="../name()"/>
			</xsl:attribute>
			<xsl:attribute name="type">
				<xsl:value-of select="type"/>
			</xsl:attribute>
			<xsl:attribute name="blocksize">
				<xsl:value-of select="blocksize"/>
			</xsl:attribute>
			<xsl:attribute name="usedType">
				<xsl:value-of select="usedType"/>	
			</xsl:attribute>
			<xsl:attribute name="description">
				<xsl:value-of select="description"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>
	<xsl:function name="simmit:checkFile">
		<xsl:param name="root"/>
		
		<!-- check for: case-insensitiveness -->
		<xsl:variable name="invalidCases" select="simmit:getDuplicateNames(($root/*/define/class, $root/*/parameter/*), $root)"/>
		<xsl:if test="$invalidCases">
	 		<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXml', 'err:symbols'), concat('Error: invalid name since case-insensitive: ', string-join($invalidCases, ' ')))" /> 
		</xsl:if>

		<!-- check for: reserved names -->
		<xsl:variable name="reservedNames" select="$root//name[matches(lower-case(.), '^(results|compiletimeconfig|runtimeconfig)$')]" />
		<xsl:if test="$reservedNames">
			<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXml', 'err:symbols'), concat('Error: reserved name: ', string-join($reservedNames, ', ')))" /> 
		</xsl:if>
		
		<!-- check for: invalid nameparts -->
		<xsl:variable name="invalidNames" select="$root//name[matches(lower-case(.), '(^ice)|((ptr|helper|holder)$)|_')]" />
		<xsl:if test="$invalidNames">
			<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createXml', 'err:symbols'), concat('Error: invalid name: ', string-join($invalidNames, ', ')))" /> 
		</xsl:if>
		
	</xsl:function>
	<xsl:function name="simmit:getDuplicateNames" as="xs:string*">
		<xsl:param name="classes"/>
		<xsl:param name="root" />
		
		<xsl:variable name="classnames" select="tokenize(lower-case(string-join(($root/*/define/*/name, $root/*/include/type), ' ')), ' ')"/>
		<xsl:for-each select="$classes">
				<xsl:variable name="base" select="if(name() = 'class') then members/*/name else class/name" />
				<xsl:variable name="members" select="tokenize(lower-case(string-join($base, ' ')), ' ')" />
				<xsl:for-each-group select="$members" group-by=".">
					<xsl:variable name="aktMember" select="." />
					<xsl:if test="count(($classnames, $members)[. = $aktMember]) > 1">
						<xsl:sequence select="$aktMember " />
					</xsl:if>
				</xsl:for-each-group>
		</xsl:for-each>
	</xsl:function>
</xsl:stylesheet>
