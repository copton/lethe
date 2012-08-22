#include "module.h"
#include "logger.h"
#include "helper.h"

namespace Core {
    Module::Module(int number, const std::string vertexName, int numberofInputStreams, int numberofOutputStreams, Controller& controller) :
        _number(number),
        _vertexName(vertexName),
        _controller(controller),
        _outputStreams(numberofOutputStreams),
        _inputStreams(numberofInputStreams),
        _callRunning(false)
    { 
        DEBUGOUT(_vertexName + ": created");
    }

    Module::~Module()
    { }

    const std::string& Module::getVertexName() const
    {
        return _vertexName;
    }

    const int Module::getVertexNumber() const
    {
        return _number;
    }

    void Module::_addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
    {
        DEBUGOUT("%s: addInputStream: streamId=%d, moduleId=%d", _vertexName.c_str(), streamId, moduleId);
        assert (stream != 0);
        assert (streamId >= 0);
        assert ((unsigned int)streamId < _inputStreams.size());
        assert (moduleId >= 0);
        _inputStreams[streamId] = Stream(stream, moduleId);
    }

    void Module::_addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
    {
        DEBUGOUT("%s: addOutputStream: streamId=%d, moduleId=%d", _vertexName.c_str(), streamId, moduleId);
        assert (stream != 0);
        assert (streamId >= 0);
        assert ((unsigned int)streamId < _outputStreams.size());
        assert (moduleId >= 0);
        _outputStreams[streamId] = Stream(stream, moduleId);
    }

    void Module::reset(Ice::InputStreamPtr is)
    {
        DEBUGOUT(_vertexName + ": reset");
        lethe_reset(is);
    }

    void Module::getResult(Ice::OutputStreamPtr out)
    {
        DEBUGOUT(_vertexName + ": getResult");
        assert (! _callRunning);
        lethe_getResult(out);
    }

    void Module::newPhase(int number, int numberofPhases, const BoolVector& activeInputStreams, const BoolVector& activeOutputStreams)
    {
        DEBUGOUT(_vertexName + ": newPhase");
        _state.active = isElementOf(true, activeInputStreams) || isElementOf(true, activeOutputStreams);
        _state.numberofOutputStreams = std::count(activeOutputStreams.begin(), activeOutputStreams.end(), true);
        // if (_state.active) {
        //     DEBUGOUT(" active");
        // } else {
        //     DEBUGOUT(" not active");
        // }
        user_newPhase(number, numberofPhases);
    }

    float Module::getProgress()
    {
        DEBUGOUT(_vertexName + ": getProgress");
        assert (! _callRunning);
        if (! _state.active) {
            return 1;
        } else {
            return user_getProgress();
        }
    }

    void Module::serialize(Ice::OutputStreamPtr os)
    {
        DEBUGOUT(_vertexName + ": serialize");
        State::ice_writeModule(os, _state);
        lethe_serialize(os);
    }

    void Module::deserialize(Ice::InputStreamPtr is)
    {
        DEBUGOUT(_vertexName + ": deserialize");
        State::ice_readModule(is, _state);
        lethe_deserialize(is);
    }

    void Module::onInputStreamClose(unsigned int streamId)
    {
        DEBUGOUT("%s: onInputStreamClose %d", _vertexName.c_str(), streamId);
    }

    void Module::onOutputStreamClose(unsigned int streamId)
    {
        DEBUGOUT("%s: onOutputStreamClose %d", _vertexName.c_str(), streamId);
        _state.numberofOutputStreams -= 1;
    }

    bool Module::hasReaders()
    {
        return _state.numberofOutputStreams != 0;
    }

    void Module::call(Caller caller, unsigned int streamId)
    {
        DEBUGOUT("%s: call caller = %d streamId =%d", _vertexName.c_str(), caller, streamId);
        assert(_state.active);

        try {
            LoopPrevention lp(_callRunning);

            try {
                if (lethe_trigger(caller, streamId) == CONTINUE) {
                    return;
                }
            } catch (Exceptions::LoopDetected e) {
                throw Exceptions::LoopDetected(std::string(e.what()) + " " + _vertexName);
            } catch (Exceptions::Base e) {
                Logger::notice(_vertexName + " quit: " + e.what());
            } catch (std::runtime_error e) {
                Logger::critical(_vertexName + " quit due to runtime error: " + e.what());
            } catch (std::exception e) {
                Logger::critical(_vertexName + " quit due to exception: " + e.what());
            } catch (...) { 
                Logger::critical(_vertexName + " quit due to user exception of unknown type");
            }

            for (unsigned int i = 0; i < _outputStreams.size(); ++i) {
                _outputStreams[i].stream->onWriterQuit(_outputStreams[i].moduleId);
            }
            for (unsigned int i = 0; i < _inputStreams.size(); ++i) {
                _inputStreams[i].stream->onReaderQuit(_inputStreams[i].moduleId);
            }

            _controller.onVertexQuit(_number);

            _state.active = false;
        } catch (LoopPrevention::LoopDetected e) {
            throw Exceptions::LoopDetected("dead lock detected. backtrace: " + _vertexName);
        }
    }
}
