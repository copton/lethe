<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:simmit="https://proj.5nord.org/simmit" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="text"/>
	
<xsl:template match="/">
// generated at <xsl:value-of select="current-dateTime()"/>
<xsl:variable name="filename" select="upper-case(*/name)"></xsl:variable>
#ifndef __<xsl:value-of select="$filename"/>_ICE__
#define __<xsl:value-of select="$filename"/>_ICE__
<xsl:for-each select="distinct-values(*/include/type)">	
#include &lt;gen_<xsl:value-of select="."/>.ice&gt;</xsl:for-each>
<xsl:value-of select="codepoints-to-string(10)" />
<xsl:apply-templates/>
#endif
</xsl:template>

<xsl:variable name="spaces" select="codepoints-to-string((9, 9, 9, 9, 9))" />
<xsl:variable name="prefix"/>

<xsl:template match="simmit:module">
module Extensions {
	module Modules {
		module <xsl:value-of select="name"/> {
			module Lethe {			
				<xsl:variable name="includes" select="include/type" />
				<xsl:value-of select="simmit:sortElements(('string','int','long','byte','float','double','boolean', $includes), define/*, $includes)"/>
				
			
				struct Configuration { 
				<xsl:if test="not(parameter/configuration/*)">
					bool icegenDUMMY;</xsl:if>
<xsl:value-of select="simmit:getMembers(parameter/configuration/*, $spaces, $prefix, $includes)"/> 
				};
			
				struct Settings { 
				<xsl:if test="not(parameter/settings/*)">
					bool icegenDUMMY;</xsl:if>
<xsl:value-of select="simmit:getMembers(parameter/settings/*, $spaces, $prefix, $includes)"/>
				};
			
				struct Result { 
				<xsl:if test="not(parameter/results/*)">
					bool icegenDUMMY;</xsl:if>
<xsl:value-of select="simmit:getMembers(parameter/results/*, $spaces, $prefix, $includes)"/> 
				};
				
				struct State { 
				<xsl:if test="not(parameter/serialize/*)">
					bool icegenDUMMY;</xsl:if>
<xsl:value-of select="simmit:getMembers(parameter/serialize/*, $spaces, $prefix, $includes)"/> 
				};
			};	
		};
	};
};				
	</xsl:template>

	<xsl:template match="simmit:parameter">
module Extensions {
	module Types {
		module Lethe {
			<xsl:variable name="includes" select="include/parameter/name" />
			<xsl:if test="some $child in export/* satisfies matches($child/name(), 'class|enum|array|table|dictionary')">
			module <xsl:value-of select="name"/>Def {
<xsl:value-of select="simmit:sortElements(('string','int','long','byte','float','double','boolean', $includes), define/*[name != /*/name], $includes)"/>
			};
			</xsl:if>
			<xsl:variable name="spaces" select="codepoints-to-string((9, 9, 9, 9))" />
			<xsl:variable name="prefix" select="concat(name, 'Def', '::')" />
<xsl:value-of select="simmit:sortElements(('string','int','long','byte','float','double','boolean', $includes), define/*[name = /*/name], $includes)"/>
		};
	};
};
</xsl:template>
	
	<xsl:function name="simmit:printDeclarations">
		<xsl:param name="nodes" as="node() *" />
		<xsl:param name="includes" as="xs:string *" />
		
				<xsl:for-each-group select="$nodes[name() = 'enum']" group-by="name">
				enum <xsl:value-of select="name"/> { <xsl:value-of select="string-join(members/name, ', ')"/> };
				</xsl:for-each-group>
				
				<xsl:for-each-group select="$nodes[name() = 'array']" group-by="name">
				sequence &lt; <xsl:value-of select="simmit:getParamName(elementType, $includes)"/> &gt; <xsl:value-of select="name"/>;
				</xsl:for-each-group>
				
				<xsl:for-each-group select="$nodes[name() = 'table']" group-by="name">
				sequence &lt; <xsl:value-of select="simmit:getParamName(elementType, $includes)"/> &gt; icegen<xsl:value-of select="upper-case(name)"/>array;
				sequence &lt; icegen<xsl:value-of select="upper-case(name)"/>array &gt; <xsl:value-of select="name"/>;
				</xsl:for-each-group>
				
				<xsl:for-each-group select="$nodes[name() = 'dictionary']" group-by="name">
				dictionary &lt; string, <xsl:value-of select="simmit:getParamName(valueType, $includes)"/> &gt; <xsl:value-of select="name"/>;
				</xsl:for-each-group>
				
				<xsl:for-each-group select="$nodes[name() = 'class']" group-by="name">
				struct <xsl:value-of select="name"/> { 
				<xsl:value-of select="simmit:getMembers(members/*, $spaces, $prefix, $includes)"/> 
				};
				</xsl:for-each-group>
	</xsl:function>
	
	<xsl:function name="simmit:getMembers">
		<xsl:param name="nodes" as="node() *" />
		<xsl:param name="spaces" />
		<xsl:param name="prefix" />
		<xsl:param name="includes" as="xs:string *" />
		
		<xsl:variable name="newline" select="codepoints-to-string(10)"/>

		<xsl:value-of select="string-join(simmit:getNames($nodes, $spaces, $prefix, $includes), $newline)"/>
	</xsl:function>
	
	<xsl:function name="simmit:getNames">
		<xsl:param name="node" as="node() *" />
		<xsl:param name="spaces" />
		<xsl:param name="prefix" />
		<xsl:param name="includes" as="xs:string *" />
		
		<xsl:for-each select="$node">
		<xsl:choose>
			<xsl:when test="matches(name(), 'string|int|long|byte|float|double')">
				<xsl:value-of select="concat(if(position() = 1) then codepoints-to-string(9) else $spaces, name(), ' ', name, ';')"/>
			</xsl:when>
			<xsl:when test="name() = 'boolean'">
				<xsl:value-of select="concat(if(position() = 1) then codepoints-to-string(9) else $spaces, 'bool ', name, ';')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat(if(position() = 1) then codepoints-to-string(9) else $spaces, if(index-of($includes, @name) > 0) then '::Extensions::Types::Slice::' else $prefix, @name, ' ', simmit:escape(name), ';')"/>
			</xsl:otherwise>
		</xsl:choose>
		</xsl:for-each>
	</xsl:function>
	
	<xsl:function name="simmit:sortElements">
		<xsl:param name="knownTypes" as="xs:string *"/>
		<xsl:param name="missingTypes" as="node() *" />
		<xsl:param name="includedTypes" as="xs:string *"/>
		
		<xsl:if test="count($missingTypes) > 0">
			<xsl:variable name="enums" select="$missingTypes[name() = 'enum']"/>
			<xsl:variable name="classes" select="$missingTypes[(name() = 'class') and (every $child in members/*/name() satisfies index-of($knownTypes, $child) >= 0)]" />
			<xsl:variable name="arrays" select="$missingTypes[matches(name(), 'array|table') and index-of($knownTypes, elementType/text()) >= 0 ]"/>
			<xsl:variable name="dicts" select="$missingTypes[name() = 'dictionary' and index-of($knownTypes, valueType/text()) >= 0 ]"/>
			<xsl:variable name="newTypes" select="$enums union $classes union $arrays union $dicts" />
	
			<xsl:if test="count($newTypes) = 0">
			 	<xsl:value-of select="error(QName('https://proj.5nord.org/simmit/createIce', 'err:symbols'), concat('Error: unresolvedSymbols: ', string-join($missingTypes/name, ', ') ))" /> 
			 </xsl:if>
	
			<xsl:value-of select="simmit:printDeclarations($newTypes, $includedTypes)" />
			<xsl:value-of select="simmit:sortElements(distinct-values(($knownTypes, $newTypes/name)), $missingTypes except $newTypes, $includedTypes)" />
		</xsl:if>
	</xsl:function>
	<xsl:function name="simmit:getParamName">
		<xsl:param name="name" as="xs:string" />
		<xsl:param name="included" as="xs:string *" />
		
		<xsl:choose>
			<xsl:when test="index-of($included, $name) > 0">
				<xsl:value-of select="concat('::Extensions::Types::Slice::', $name)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="if($name = 'boolean') then 'bool' else $name" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:function name="simmit:escape">
		<xsl:param name="name" />
		<xsl:value-of select="if(matches(lower-case($name), '^(module|interface|sequence|dictionary|enum|struct)$')) then concat('\', $name) else $name"/>
	</xsl:function>
</xsl:stylesheet>
