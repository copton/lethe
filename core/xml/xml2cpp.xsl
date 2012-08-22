<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:simmit="https://proj.5nord.org/simmit">

	<xsl:output method="text" indent="yes"/>
	
	<xsl:template match="/">
#ifndef __EXTENSIONS__<xsl:value-of select="if(simmit:module) then 'MODULES' else 'PIPES'" />__<xsl:value-of select="upper-case(*/name)"/>_H
#define __EXTENSIONS__<xsl:value-of select="if(simmit:module) then 'MODULES' else 'PIPES'" />__<xsl:value-of select="upper-case(*/name)"/>_H

#include "gen_<xsl:value-of select="*/name"/>.h"<xsl:apply-templates select="*" />
	</xsl:template>

	<xsl:template match="simmit:parameter">
#include &lt;Ice/Ice.h&gt;

namespace Extensions {
	namespace Types {
		class <xsl:value-of select="name"/> : public Lethe::<xsl:value-of select="name"/> {
		public:
                static void serialize(const Ice::OutputStreamPtr&amp; os, const <xsl:value-of select="name"/>&amp; value)
                {
                    Lethe::ice_write<xsl:value-of select="name"/>(os, value);
                }

           		static const <xsl:value-of select="name"/> deserialize(const Ice::InputStreamPtr&amp; is)
                {
                <xsl:value-of select="name"/> that;
                    Lethe::ice_read<xsl:value-of select="name"/>(is, that);
                    return that;
                }

                <xsl:value-of select="name"/>()
                { }
        	};
    	}
}

#endif			
</xsl:template>
<xsl:template match="simmit:module">
#include &lt;Ice/Ice.h&gt;

#include &lt;core/logger.h&gt;
#include "gen_adapter.h"
#include "gen_<xsl:value-of select="name"/>.h"
	
namespace Extensions {
    namespace Modules {
        namespace <xsl:value-of select="name" /> {
            template &lt;<xsl:value-of select="string-join((simmit:getStreams(ports/input/port, 'class Input'), simmit:getStreams(ports/output/port, 'class Output')), ', ')" />&gt;
            class Module : public Core::Module {
            public:
                Module(int number, const std::string vertexName, unsigned int numberofInputStreams, unsigned int numberofOutputStreams, Core::Controller&amp; controller, Ice::InputStreamPtr is) :
                    Core::Module(number, vertexName, numberofInputStreams, numberofOutputStreams, controller)
                { 
                    Lethe::Configuration config;
                    Lethe::ice_readConfiguration(is, config);
                    user_init(config);
                }

                ~Module()
                {
                    user_finish();
                }

                void addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                {
                    _addInputStream(stream, streamId, moduleId);
                    ports.addInputStream(stream, streamId, moduleId);
                }

                void addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                {
                    _addOutputStream(stream, streamId, moduleId);
                    ports.addOutputStream(stream, streamId, moduleId);
                }

            protected:
                void lethe_reset(Ice::InputStreamPtr is)
                {
                    Lethe::Settings settings;
                    Lethe::ice_readSettings(is, settings);
                    user_reset(settings);
                }

                Decision lethe_trigger(Core::Module::Caller caller, unsigned int streamId) 
                {
                    Lethe::Caller port;
                    if (caller == Core::Module::SCHEDULER) {
                        port = Lethe::LETHE_SCHEDULER;
                    } else if (caller == Core::Module::INPUT_STREAM) {
                        port = Lethe::inputPortMap[streamId];
                    } else {
                        port = Lethe::outputPortMap[streamId];
                    }

                    return user_trigger(port);
                }

                void lethe_serialize(Ice::OutputStreamPtr os)
                {
                    Lethe::ice_writeState(os, user_serialize());
                }

                void lethe_deserialize(Ice::InputStreamPtr is)
                {
                    Lethe::State state;
                    Lethe::ice_readState(is, state);
                    user_deserialize(state);
                }       

                void lethe_getResult(Ice::OutputStreamPtr os)
                {
                    Lethe::ice_writeResult(os, user_getResult());
                }

            private:
                typedef Lethe::Adapter&lt;<xsl:value-of select="string-join((simmit:getStreams(ports/input/port, 'Input'), simmit:getStreams(ports/output/port, 'Output')), ', ')" />&gt; Ports;
                Ports ports;
                <xsl:for-each select="ports/*/port/name">
                typedef typename Ports::<xsl:value-of select="simmit:capitalize(.)"/>Vector <xsl:value-of select="simmit:capitalize(.)"/>Vector;</xsl:for-each>

            protected:
                void user_init(const Lethe::Configuration&amp; config)
                {
                    Core::Logger::info(getVertexName() + ": user_init called");
                    // TODO: initialize module
                    _config = config;
                }

                void user_finish()
                {
                    // TODO: cleanup and free ressources
                }

                void user_reset(const Lethe::Settings&amp; settings)
                {
                    _settings = settings;
                    // TODO: reset module 
                }

                void user_newPhase(int phaseNumber, int numberofPhases)
                {
                    // TODO: prepare module for the next phase
                }

                Decision user_trigger(Lethe::Caller caller)
                {
                    // TODO: here goes the algorithm
                    // If you don't want to be called again during the current phase return QUIT
                    
                    return CONTINUE;
                }

                Lethe::State user_serialize()
                {
                    return _state;
                }

                void user_deserialize(const Lethe::State&amp; state)
                {
                    _state = state;
                }

                Lethe::Result user_getResult()
                {
                    // TODO: gather results of current round
                    return _result;
                }

                float user_getProgress()
                {
                    // TODO: return a value between 0 and 1
                    return 1;
                } 

            private:
                Lethe::Configuration _config;
                Lethe::Settings _settings;
                Lethe::State _state;
                Lethe::Result _result;
            };
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
		
		<xsl:value-of select="concat(upper-case(substring($name, 1, 1)), substring($name, 2))"/>
	</xsl:function>
</xsl:stylesheet>
