#include <time.h>
#include <sys/times.h>

#include "controller.h"
#include "helper.h"
#include "types.h"
#include "logger.h"

namespace Core {
    Controller::Controller(Ice::CommunicatorPtr communicator, const std::string& jobId, const std::string& suffix, GraphLogic& graphLogic, Application& application) :
        _jobId(jobId),
        _suffix(suffix),
        _communicator(communicator),
        _application(application),
        _graphLogic(graphLogic)
    { 
        assert (_graphLogic.getJobId() == _jobId);
    }

    void Controller::setup(Simulation* simulation)
    {
        _simulation = simulation;
    }

    void Controller::init(const Comm::Job::Specification& job)
    {
        DEBUGOUT("Controller: init");
        assert (job.jobId == _jobId);

        _job = job;
        _numberofPhases = job.theGraph.thePhases.size();
        
        std::vector<Ice::InputStreamPtr> isVector;
        const Comm::Job::Graph::Vertices& vertices = _job.theGraph.theVertices;
        for (unsigned int i=0; i<vertices.size(); ++i) {
            Ice::InputStreamPtr is = Ice::createInputStream(_communicator, vertices[i].config);
            isVector.push_back(is);
        }

        _graphLogic.createGraph(_vertices, _edges, *this, isVector);
    }

    void Controller::_resetGraph()
    {
        // reset timers
        _timeofStart = time(0);
        _timeofStop = 0;
        _timeofContinue = time(0);
        _runtime = 0;
        _timeofEnd = 0;

        // reset all edges
        std::for_each(_edges.begin(), _edges.end(), std::mem_fun(&Stream::reset));

        // reset all vertices
        const Comm::Job::SettingsDict& dict = _job.settings[_state.round];
        const Comm::Job::Graph::Vertices& vertices = _job.theGraph.theVertices;
        for (unsigned int i=0; i<vertices.size(); ++i) {
            Comm::Job::SettingsDict::const_iterator value = dict.find(vertices[i].vertexName);
            assert (value != dict.end()); // TODO throw SpecException

            Ice::InputStreamPtr is = Ice::createInputStream(_communicator, value->second);
            _vertices[i]->reset(is);
        }
    }

    void Controller::start(int round)
    {
        DEBUGOUT("Controller: doStart");
        // reset status
        _status.livetime = 0;
        _status.runtime = 0;
        _status.cputime = 0;
        
        // reset _state
        _state.round = round;
        _state.phase = 0;
        _state.index = 0;
        _state.observed.clear();
        _state.scheduled.clear();
        
        _resetGraph();

        _newPhase();
    }

    void Controller::resume(const Comm::Simulation::Checkpoint& checkpoint)
    {
        DEBUGOUT("Controller: resume");
        // restore _status
        _status = checkpoint.theStatus;
        
        // restore _state
        {
            Ice::InputStreamPtr in = Ice::createInputStream(_communicator, checkpoint.theState[0]);
            State::ice_readController(in, _state);
        }

        _resetGraph();
        
        //restore vertices and user modules
        for (unsigned int index = 0; index < _vertices.size(); ++index) {
            Ice::InputStreamPtr in = Ice::createInputStream(_communicator, checkpoint.theState[1 + index]);
            _vertices[index]->deserialize(in);
        }
        
        //restore edges
        for (unsigned int index = 0; index < _edges.size(); ++index) {
            Ice::InputStreamPtr in = Ice::createInputStream(_communicator, checkpoint.theState[1 + _vertices.size() + index]);
            _edges[index]->deserialize(in);
        }
    }


    Comm::Simulation::StateSeq Controller::getState()
    {
        DEBUGOUT("Controller: getState");
        Comm::Simulation::StateSeq state;
        for (unsigned int index=0; index < 1 + _vertices.size() + _edges.size(); ++index) {
            std::vector< Ice::Byte > data;
            Ice::OutputStreamPtr out = Ice::createOutputStream(_communicator);
            if (index == 0) {
                State::ice_writeController(out, _state);
            } else if (index < 1 + _vertices.size()) {
                _vertices[index - 1]->serialize(out);
            } else {
                _edges[index - _vertices.size() - 1]->serialize(out);
            }
            out->finished(data);
            state.push_back(data);
        }
        return state;
    }

    Comm::Simulation::Status Controller::getStatus()
    {
        DEBUGOUT("Controller: getStatus");
        Comm::Simulation::Status status = _status;

        time_t livetime;
        if (_timeofEnd == 0) {
            livetime = time(0) - _timeofStart;
        } else {
            livetime = _timeofEnd - _timeofStart;
        }

        status.livetime += livetime;
        DEBUGOUT("livetime = %d", status.livetime);

        assert (_timeofStop != 0);
        status.runtime += _runtime + (_timeofStop - _timeofContinue);
        DEBUGOUT("runtime = %d", status.runtime);

        tms tmsBuf;
        times(&tmsBuf);
        status.cputime += tmsBuf.tms_utime + tmsBuf.tms_stime;
        DEBUGOUT("cputime = %d", status.cputime);

        if (_vertices.empty()) {
            status.progress = 0;
        } else {
            float min = _vertices[0]->getProgress();
            for (unsigned int i=1; i < _vertices.size(); ++i) {
                float progress = _vertices[i]->getProgress();
                if (progress < min) {
                    min = progress;
                }
            }
            status.progress = min;
        }
        status.phaseNumber = _state.phase;
        return status;
    }

    Comm::Simulation::Results Controller::getResults()
    {
        DEBUGOUT("Controller: getResults");
        Comm::Simulation::Results results;

        const Comm::Job::Graph::Vertices& vertices = _job.theGraph.theVertices;
        for (unsigned int index=0; index<vertices.size(); ++index) {
            Ice::OutputStreamPtr out = Ice::createOutputStream(_communicator);
            _vertices[index]->getResult(out); 
            Comm::Simulation::ByteSeq data;
            out->finished(data);
            results[vertices[index].vertexName] = data;
        }
        return results;
    }

    const std::string& Controller::getJobId() const
    {
        return _jobId;
    }

    const std::string& Controller::getSuffix() const 
    {
        return _suffix;
    }

    const Comm::Job::Specification& Controller::getJob() const
    {
        return _job;
    }

    void Controller::run()
    {
        DEBUGOUT("Controller: run");
        try {
            while(_simulation->checkpoint(_timeofStop)) {
                if (_timeofStop != 0) {
                    _runtime += _timeofStop - _timeofContinue;
                    _timeofStop = 0;
                    _timeofContinue = time(0);
                }
                // DEBUGOUT("Controller: loop"); 

    //            DEBUGOUT("Controller: calling vertex %d", _state.scheduled[_state.index]);
                _vertices[_state.scheduled[_state.index]]->call(Module::SCHEDULER);
                assert (_state.scheduled.size() >= _state.observed.size());
                if (! _state.observed.empty()) {
                    _state.index += 1;
                    if ((unsigned int)_state.index >= _state.scheduled.size()) {
                        _state.index = 0;
                    }   
                } else {
                    _state.phase += 1;
                    if (_state.phase < _numberofPhases) {
                        DEBUGOUT("Controller: new phase %d", _state.phase);
                        _newPhase(); 
                    } else {
                        _timeofEnd = time(0);
                        Logger::info("end of round");
                        _simulation->endOfRound();
                    }
                }
            }
        } catch (std::runtime_error e) {
            Logger::error("simulation quit due to runtime error: %s", e.what()); 
            _application.shutdown();
        } catch (std::exception e) {
            Logger::error("simulation quit due to an unknown exception");
            _application.shutdown();
        }
        
        _graphLogic.shutdown();
        DEBUGOUT("Controller: shutting down");
    }

    void Controller::onVertexQuit(int number)
    {
        DEBUGOUT("Controller: onVertexQuit %d", number);
        eraseFrom(number, _state.observed);
        eraseFrom(number, _state.scheduled);
        for (unsigned int i=0; i < _state.observed.size(); ++i) {
            DEBUGOUT("observed: %d", _state.observed[i]);
        }
        for (unsigned int i=0; i < _state.scheduled.size(); ++i) {
            DEBUGOUT("scheduled: %d", _state.scheduled[i]);
        }
    }

    class Expand {
    public:
        Expand(const std::string& vertexName) : _vertexName(vertexName) { }
        Comm::Job::Graph::PortDescriptor operator()(const std::string& portName) 
        {
            Comm::Job::Graph::PortDescriptor port;
            port.portName = portName;
            port.vertexName = _vertexName;
            return port;
        }
    private:
        const std::string& _vertexName;
    };

    Comm::Job::Graph::PortDescSeq expand(const Comm::Job::Graph::StringSeq& seq, const std::string& str)
    {
        Comm::Job::Graph::PortDescSeq res;
        std::transform(seq.begin(), seq.end(), std::back_inserter(res), Expand(str));
        return res;
    }

    void Controller::_newPhase()
    {
        DEBUGOUT("Controller: newPhase: %d", _state.phase);
        const Comm::Job::Graph::Phase& phase = _job.theGraph.thePhases[_state.phase];

        for (unsigned int i=0; i<phase.activePorts.size(); ++i) {
            Comm::Job::Graph::PortDescriptor desc = phase.activePorts[i];
            DEBUGOUT("active Port: %s %s", desc.vertexName.c_str(), desc.portName.c_str());
        }
        
        _state.observed.clear();
        _state.scheduled.clear();
        for (unsigned int index = 0; index < _job.theGraph.theVertices.size(); ++index) {
            Comm::Job::Graph::Vertex& vertex = _job.theGraph.theVertices[index];

            BoolVector activeInputStreams;
            BoolVector activeOutputStreams;

            createContainmentVector(expand(vertex.inputPorts, vertex.vertexName), activeInputStreams, phase.activePorts);
            createContainmentVector(expand(vertex.outputPorts, vertex.vertexName), activeOutputStreams, phase.activePorts);

            _vertices[index]->newPhase(_state.phase, _numberofPhases, activeInputStreams, activeOutputStreams);

            bool scheduled=false;
            if (isElementOf(vertex.vertexName, phase.observedVertices)) {
                DEBUGOUT("Controller: adding module %d for observation", index);
                _state.observed.push_back(index);
                _state.scheduled.push_back(index);
                scheduled = true;
            }

            if (! scheduled && isElementOf(vertex.vertexName, phase.resultVertices)) {
                DEBUGOUT("Controller: adding module %d for scheduling", index);
                _state.scheduled.push_back(index);
            }
        }

        for (unsigned int index = 0; index < _job.theGraph.theEdges.size(); ++index) {
            Comm::Job::Graph::Edge& edge = _job.theGraph.theEdges[index];
            BoolVector activeWriters;
            BoolVector activeReaders;
            BoolVector resetBuffer;

            createContainmentVector(edge.writerPorts, activeWriters, phase.activePorts);
            createContainmentVector(edge.readerPorts, activeReaders, phase.activePorts);
            createContainmentVector(edge.readerPorts, resetBuffer, phase.resetPorts);

            _edges[index]->newPhase(activeWriters, activeReaders, resetBuffer);
        }
    }
}
