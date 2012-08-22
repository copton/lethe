#include "stream.h"
#include "helper.h"
#include "logger.h"

namespace Core {
    Stream::Stream(unsigned int number, unsigned int numberofReaders, unsigned int numberofWriters) :
        _number(number),
        _readers(numberofReaders),
        _writers(numberofWriters)
    { 
        DEBUGOUT("stream %d: created", _number);
    }

    Stream::~Stream()
    { }

    void Stream::addWriter(Core::Module* module, unsigned int streamId, unsigned int moduleId)
    {
        assert (module != 0);
        _writers[moduleId] = Module(module, streamId);
    }

    void Stream::addReader(Core::Module* module, unsigned int streamId, unsigned int moduleId)
    {
        assert (module != 0);
        _readers[moduleId] = Module(module, streamId);
    }

    void Stream::reset()
    {
        DEBUGOUT("stream %d: reset", _number);
        _state.writerId = -1;
        _state.activeReaders = State::BoolSequence(_readers.size(), false);
        _reset();
    }

    void Stream::newPhase(BoolVector activeWriters, BoolVector activeReaders, BoolVector emptyBuffer)
    {
        DEBUGOUT("stream %d: newPhase", _number);
        assert (activeWriters.size() == _writers.size());
        assert (activeReaders.size() == _readers.size());

        BoolVector::iterator writer = std::find(activeWriters.begin(), activeWriters.end(), true);
        if (writer == activeWriters.end()) {
            _state.writerId = -1;
        } else {
            _state.writerId = indexOf(writer, activeWriters);
            assert (std::count(activeWriters.begin(), activeWriters.end(), true) == 1);
        }

        _state.activeReaders = activeReaders;

        _updateCache();

        _newPhase(activeReaders, emptyBuffer);
    } 

    void Stream::_updateCache()
    {
        _cache.numberofActiveReaders = std::count(_state.activeReaders.begin(), _state.activeReaders.end(), true);
    }

    void Stream::serialize(Ice::OutputStreamPtr& os)
    {
        DEBUGOUT("stream %d: serialize", _number);
        State::ice_writeStream(os, _state);
        _serialize(os);
    }

    void Stream::deserialize(Ice::InputStreamPtr& is)
    {
        DEBUGOUT("stream %d: deserialize", _number);
        State::ice_readStream(is, _state);
        _updateCache();
        _deserialize(is);
    }

    void Stream::onWriterQuit(unsigned int moduleId)
    {
        DEBUGOUT("stream %d: onWriterQuit %d", _number, moduleId);
        assert ((unsigned int)_state.writerId == moduleId);
        
        for (unsigned int i = 0; i < _readers.size(); ++i) {
            if (_state.activeReaders[i]) {
                Module& module = _readers[i];
                module.module->onInputStreamClose(module.streamId);
            }
        }

        _state.writerId = -1;

        _onWriterQuit(moduleId);
    }

    void Stream::onReaderQuit(unsigned int moduleId)
    {
        DEBUGOUT("stream %d: onReaderQuit %d", _number, moduleId);
        assert (_state.activeReaders[moduleId] == true);

        _state.activeReaders[moduleId] = false;

        _cache.numberofActiveReaders -= 1;

        if ( ! hasReaders()) {
            if (_state.writerId != -1) {
                Module& module = _writers[_state.writerId];
                module.module->onOutputStreamClose(module.streamId);
            }
        }

        _onReaderQuit(moduleId);
    }

    bool Stream::hasReaders() const
    {
        return _cache.numberofActiveReaders != 0;
    }

    int Stream::getEdgeNumber() const
    {
        return _number;
    }

    void Stream::_onRead(size_t count, unsigned int moduleId)
    {
        // DEBUGOUT("stream %d: _onRead %d", _number, moduleId);
        assert (_state.activeReaders[moduleId] == true);
        while (1) {
            if (count > _usageof(moduleId)) {
                if (_state.writerId == -1) {
                    throw Exceptions::BufferEmpty();
                } else {
                    Module& module = _writers[_state.writerId];
                    module.module->call(Core::Module::OUTPUT_STREAM, module.streamId);
                }
            } else {
                return;
            }
        }
    }

    bool Stream::_onWrite(size_t count, unsigned int moduleId, bool throwWhenStreamHasNoReaders)
    {
        // DEBUGOUT("stream %d: _onWrite %d", _number, moduleId);
        assert (moduleId == (unsigned int)_state.writerId);

        while (1) {
            if (count > _sizeofFreeSpace()) {
                if (! hasReaders()) {
                    if (! _writers[moduleId].module->hasReaders()) {
                        throw Exceptions::ModuleHasNoReaders();
                    } else {
                        if (throwWhenStreamHasNoReaders) {
                            throw Exceptions::BufferFull();
                        } else {
                            return true;
                        }
                    }
                } else {
                    Module& module = _readers[_idofModuleWithMaxUsage()];
                    module.module->call(Core::Module::INPUT_STREAM, module.streamId);
                }
            } else {
                return false;
            }
        }
    }

    bool Stream::_isActive(unsigned int moduleId)
    {
        return _state.activeReaders[moduleId];
    }
}
