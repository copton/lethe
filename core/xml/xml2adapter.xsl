<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:simmit="https://proj.5nord.org/simmit">

	<xsl:output method="text" indent="yes"/>
	
	<xsl:template match="simmit:module">
#ifndef __EXTENSIONS__MODULES__<xsl:value-of select="upper-case(name)"/>__LETHE__ADAPTER_H
#define __EXTENSIONS__MODULES__<xsl:value-of select="upper-case(name)"/>__LETHE__ADAPTER_H

#include &lt;utility&gt;
	
namespace Extensions {
	namespace Modules {
        namespace <xsl:value-of select="name"/> {
            namespace Lethe {
                template &lt;<xsl:value-of select="string-join((simmit:getStreams(ports/input/port, 'class Input'), simmit:getStreams(ports/output/port, 'class Output')), ', ')" />&gt;
                class Adapter {
                public:
                    <xsl:for-each select="ports/input/port">
                    typedef Core::Templates::ReadVector&lt;typename InputStream<xsl:value-of select="position()"/>::buffer_type, <xsl:value-of select="if(string-length(usedType) > 0) then usedType else concat('Extensions::Types::', type)"/>&gt; <xsl:value-of select="simmit:capitalize(name)"/>Vector;
    
                    <xsl:value-of select="simmit:capitalize(name)"/>Vector <xsl:value-of select="simmit:capitalize(name, 'low')"/>(size_t count)
                    {
                            assert (_inputStream<xsl:value-of select="position()"/>.first != 0);
                            assert (count &lt;= <xsl:value-of select="blocksize"/>);
                            typename InputStream<xsl:value-of select="position()"/>::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream<xsl:value-of select="position()"/>.first->read(count, count, _inputStream<xsl:value-of select="position()"/>.second, data, offset, size);

                            return <xsl:value-of select="simmit:capitalize(name)"/>Vector(data, count, offset, size, callback);
                    }

                    <xsl:value-of select="simmit:capitalize(name)"/>Vector <xsl:value-of select="simmit:capitalize(name, 'low')"/>(size_t count, size_t forward)
                    {
                            assert (_inputStream<xsl:value-of select="position()"/>.first != 0);
                            assert (count &lt;= <xsl:value-of select="blocksize"/>);

                            typename InputStream<xsl:value-of select="position()"/>::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream<xsl:value-of select="position()"/>.first->read(count, forward, _inputStream<xsl:value-of select="position()"/>.second, data, offset, size);

                            return <xsl:value-of select="simmit:capitalize(name)"/>Vector(data, count, offset, size, callback);
                    }
                    </xsl:for-each>
    
                    <xsl:for-each select="ports/output/port">
                    typedef Core::Templates::WriteVector&lt;<xsl:value-of select="if(string-length(usedType) > 0) then usedType else concat('Extensions::Types::', type)"/>, typename OutputStream<xsl:value-of select="position()"/>::buffer_type&gt; <xsl:value-of select="simmit:capitalize(name)"/>Vector;
     
                    <xsl:value-of select="simmit:capitalize(name)"/>Vector <xsl:value-of select="simmit:capitalize(name, 'low')"/>(size_t count, bool throwWhenStreamHasNoReaders=false)
                    {
                            assert (_outputStream<xsl:value-of select="position()"/>.first != 0);
                            assert (count &lt;= <xsl:value-of select="blocksize"/>);

                            typename OutputStream<xsl:value-of select="position()"/>::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _outputStream<xsl:value-of select="position()"/>.first->write(count, _outputStream<xsl:value-of select="position()"/>.second, throwWhenStreamHasNoReaders, data, offset, size);

                            return <xsl:value-of select="simmit:capitalize(name)"/>Vector(data, count, offset, size, callback);
                    }
                    </xsl:for-each>

                    void addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
               		 switch(streamId) {
            			<xsl:for-each select="ports/input/port">
            				<xsl:variable name="name" select="concat('InputStream', position())"/>
                    	case <xsl:value-of select="position()-1"/>: 
                            {
                    		<xsl:value-of select="concat(simmit:capitalize($name), '* ', simmit:capitalize($name, 'low'))" /> = dynamic_cast &lt;<xsl:value-of select="simmit:capitalize($name)"/>*&gt;(stream); 
                   			assert(<xsl:value-of select="simmit:capitalize($name, 'low')"/>);
                   			_<xsl:value-of select="simmit:capitalize($name, 'low')"/> = std::make_pair(<xsl:value-of select="simmit:capitalize($name, 'low')"/>, moduleId);
                            }
                   			break;
                    	</xsl:for-each>		
                    	default:
                    		assert(false);
                    	}    
                    }
                    
                    void addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
                    	switch(streamId) {
            			<xsl:for-each select="ports/output/port">
            				<xsl:variable name="name" select="concat('OutputStream', position())"/>
                    	case <xsl:value-of select="position()-1"/>: 
                            {
                    		<xsl:value-of select="concat(simmit:capitalize($name), '* ', simmit:capitalize($name, 'low'))" /> = dynamic_cast &lt;<xsl:value-of select="simmit:capitalize($name)"/>*&gt;(stream); 
                   			assert(<xsl:value-of select="simmit:capitalize($name, 'low')"/>);
                   			_<xsl:value-of select="simmit:capitalize($name, 'low')"/> = std::make_pair(<xsl:value-of select="simmit:capitalize($name, 'low')"/>, moduleId);
                            }
                   			break;
                    	</xsl:for-each>		
                    	default:
                    		assert(false);
                    	}
                    }

                private:
                    <xsl:for-each select="ports/input/port">std::pair&lt;InputStream<xsl:value-of select="position()"/>*, unsigned int&gt; _inputStream<xsl:value-of select="position()"/>;
                    </xsl:for-each>
                    <xsl:for-each select="ports/output/port">std::pair&lt;OutputStream<xsl:value-of select="position()"/>*, unsigned int&gt; _outputStream<xsl:value-of select="position()"/>;
                    </xsl:for-each>
                };

                enum Caller {
                	LETHE_SCHEDULER, <xsl:value-of select="upper-case(string-join(ports/*/port/name, ', '))" />
                };

                Caller inputPortMap[] = {<xsl:value-of select="upper-case(string-join(ports/input/port/name, ', '))" />};
                Caller outputPortMap[] = {<xsl:value-of select="upper-case(string-join(ports/output/port/name, ', '))" />};
            }
        }
    }
}

#endif
</xsl:template>
	<xsl:function name="simmit:getStreams">
		<xsl:param name="ports" />
		<xsl:param name="prefix" />
		
		<xsl:for-each select="$ports">
			<xsl:sequence select="concat($prefix, 'Stream', position())" />
			</xsl:for-each>
	</xsl:function>
	<xsl:function name="simmit:capitalize">
		<xsl:param name="name" />
		<xsl:value-of select="simmit:capitalize($name, 'up')" />
	</xsl:function>
	<xsl:function name="simmit:capitalize">
		<xsl:param name="name" />
		<xsl:param name="lowOrUp"/>
		
		<xsl:value-of select="concat(if($lowOrUp = 'low') then lower-case(substring($name, 1, 1)) else upper-case(substring($name, 1, 1)), substring($name, 2))"/>
	</xsl:function>
</xsl:stylesheet>
