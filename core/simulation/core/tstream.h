#ifndef __CORE__TEMPLATES__STREAM_H
#define __CORE__TEMPLATES__STREAM_H

#include "tvectors.h"
#include "exceptions.h"
#include "helper.h"
#include <sys/types.h>
#include "logger.h"

#include "streamcache.h"
#include "streamcallback.h"

namespace Core {
    namespace Templates {
        template <class vtBuffer, class vtBase>
        class Stream : public Core::Stream {
        public:
            typedef vtBuffer buffer_type;

            Stream(int number, size_t size, unsigned int numberofReaders, unsigned int numberofWriters) :
                ::Core::Stream(number, numberofReaders, numberofWriters),
                _size(size + 1), // writepointer - readpointer == 1 <=> usage == size
                _buffer(new vtBuffer[_size]),
                _numberofReaders(numberofReaders),
                _cache(_numberofReaders, _size)
            { 
            }

            ~Stream()
            {
                delete [] _buffer;
            }

        protected:
            void _reset()
            {
                _state.readPointers = State::LongSequence(_numberofReaders, 0);
                _state.writePointer = 0;
                _updateCache();
            }

            void _newPhase(const BoolVector& activeReaders, const BoolVector& emptyBuffer)
            {
                assert (_numberofReaders == activeReaders.size());
                assert (_numberofReaders == emptyBuffer.size());

                const size_t maxReadPointer = _cache.getPointerOfReaderWithMaxUsage();
                const size_t writePointer = _cache.getWritePointer();

                // for the modules which are still not active the usage does not matter 
                // as it is reset when they become active
                for (unsigned int id = 0; id < _numberofReaders; ++id) {
                    if (activeReaders[id]) {
                        if (_cache.readerActive(id)) {
                            if (emptyBuffer[id]) {
                                _cache.removeReader(id);
                                _cache.addReader(id, writePointer);
                            }
                        } else {
                            size_t readPointer = emptyBuffer[id] ? writePointer : maxReadPointer;
                            _cache.addReader(id, readPointer);
                        }
                    } else {
                        if (_cache.readerActive(id)) {
                            _cache.removeReader(id);
                        }
                    }
                }
            }

            void _serialize(Ice::OutputStreamPtr& os)
            {
                _syncCache();
                State::ice_writeTStream(os, _state);

                const size_t maxUsage = _getMaxUsage();
                if (maxUsage > _state.writePointer) {
                    for (unsigned int i = 0; i < _state.writePointer; ++i) {
                        vtBase::serialize(os, _buffer[i]);
                    }
                    for (unsigned int i = _size - (maxUsage - _state.writePointer); i < _size; ++i) {
                        vtBase::serialize(os, _buffer[i]);
                    }
                } else {
                    for (unsigned int i = _state.writePointer - maxUsage; i < _state.writePointer; ++i) {
                        vtBase::serialize(os, _buffer[i]);
                    }
                }
            }

            void _deserialize(Ice::InputStreamPtr& is)
            {
                State::ice_readTStream(is, _state);

                _updateCache();
                const size_t maxUsage = _getMaxUsage();

                if (maxUsage > _state.writePointer) {
                    for (unsigned int i = 0; i < _state.writePointer; ++i) {
                        _buffer[i] = vtBase::deserialize(is);
                    }
                    for (unsigned int i = _size - (maxUsage - _state.writePointer); i < _size; ++i) {
                        _buffer[i] = vtBase::deserialize(is);
                    }
                } else {
                    for (unsigned int i = _state.writePointer - maxUsage; i < _state.writePointer; ++i) {
                        _buffer[i] = vtBase::deserialize(is);
                    }
                }

            }

        public:
            StreamCallback write(size_t count, unsigned int moduleId, bool throwWhenStreamHasNoReaders, vtBuffer*& data, size_t& offset, size_t& size)
            {
                if (_onWrite(count, moduleId, throwWhenStreamHasNoReaders)) {
                    data = 0;
                    return StreamCallback(-1, 0, &_cache);
                }

                assert (count <= _size - _getMaxUsage());

                // set return values
                data = _buffer;
                offset = _cache.getWritePointer();
                size = _size;

                return StreamCallback(-1, count, &_cache);
            }

            StreamCallback read(size_t count, size_t forward, unsigned int moduleId, vtBuffer*& data, size_t& offset, size_t& size)
            {
                _onRead(count, moduleId);

                size_t readPointer = _cache.getReadPointer(moduleId);
                assert (count <= _pointerToUsage(readPointer));

                // set return values
                data = _buffer;
                offset = readPointer;
                size = _size;

                return StreamCallback(moduleId, forward, &_cache);
            }

        private:
            size_t _getMaxUsage() const
            {
                return _pointerToUsage(_cache.getPointerOfReaderWithMaxUsage());
            }

        protected:
            size_t _usageof(unsigned int moduleId) const
            {
                return _pointerToUsage(_cache.getReadPointer(moduleId));
            }

            size_t _sizeofFreeSpace() const
            {
                return _size - _getMaxUsage();
            }

            int _idofModuleWithMaxUsage() const
            {
                return _cache.getIdOfReaderWithMaxUsage();
            }

            void _onWriterQuit(unsigned int moduleId)
            { 

            }

            void _onReaderQuit(unsigned int moduleId) 
            {
                _cache.removeReader(moduleId);
            }

        private:
            size_t _pointerToUsage(size_t readPointer) const
            {
                size_t writePointer = _cache.getWritePointer();
                if (readPointer > writePointer) {
                    return _size - (readPointer - writePointer);
                } else {
                    return writePointer - readPointer;
                }
            }

            void _updateCache()
            {
                _cache.clear();

                _cache.setWriter(_state.writePointer);

                for (unsigned int id=0; id<_numberofReaders; ++id) {
                    if (_isActive(id)) {
                        _cache.addReader(id, _state.readPointers[id]);
                    }
                }

                _cache.sort();
            }

            void _syncCache()
            {
                _state.writePointer = _cache.getWritePointer();
                for (unsigned int id=0; id<_numberofReaders; ++id) {
                    _state.readPointers[id] = _cache.getReadPointer(id);
                }
            }
            

        private:
            const size_t _size;
            vtBuffer *const _buffer;

            const unsigned int _numberofReaders;

            State::TStream _state;
            StreamCache _cache;
        };
    }
}

#endif
