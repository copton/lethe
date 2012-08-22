
#ifndef __EXTENSIONS__MODULES__SOURCE_H
#define __EXTENSIONS__MODULES__SOURCE_H

#include "gen_Source.h"
#include <Ice/Ice.h>

#include <core/logger.h>
#include "gen_adapter.h"
#include "gen_Source.h"
	
namespace Extensions {
    namespace Modules {
        namespace Source {
            template <class OutputStream1>
            class Module : public Core::Module {
            public:
                Module(int number, const std::string vertexName, unsigned int numberofInputStreams, unsigned int numberofOutputStreams, Core::Controller& controller, Ice::InputStreamPtr is) :
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
                typedef Lethe::Adapter<OutputStream1> Ports;
                Ports ports;
                
                typedef typename Ports::OutputVector OutputVector;

            protected:
                void user_init(const Lethe::Configuration& config)
                {
                    Core::Logger::info(getVertexName() + ": user_init called");
                    // TODO: initialize module
                    _config = config;
                }

                void user_finish()
                {
                    // TODO: cleanup and free ressources
                }

                void user_reset(const Lethe::Settings& settings)
                {
                    _settings = settings;
	                _state.counter = 0;    
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
                   
					OutputVector out = ports.output(1);
					out[0].value = _state.counter++; 
                    return CONTINUE;
                }

                Lethe::State user_serialize()
                {
                    return _state;
                }

                void user_deserialize(const Lethe::State& state)
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
