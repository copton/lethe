#ifndef __CORE__STREAMCACHE_H
#define __CORE__STREAMCACHE_H

#include <list>
#include <utility>

#include "logger.h"

namespace Core {
    class StreamCache {
    public:
        StreamCache(unsigned int numberofReaders, size_t size) :
            _readers(new PointerIter[numberofReaders]),
            _numberofReaders(numberofReaders),
            _size(size),
            _numberofActiveReaders(0),
            _border(0)
        {
            clear(); 
        }

        ~StreamCache()
        {
            delete [] _readers;
        }
        
        size_t getReadPointer(unsigned int id) const
        {
            assert (readerActive(id));
            return _readers[id]->first;
        }

        size_t getWritePointer() const
        {
            assert (_writer != 0);
            return _writer->first;
        }

        unsigned int getIdOfReaderWithMaxUsage() const
        {
            assert (_numberofActiveReaders != 0);
            return _readerWithMaxUsage()->second;
        }

        size_t getPointerOfReaderWithMaxUsage() const
        {
            if (_numberofActiveReaders == 0) {
                return _border;
            }
            return _readerWithMaxUsage()->first;
        }

        void increaseWritePointer(size_t count)
        {
            _writer->first = (_writer->first + count) % _size;
            sort();
        }

        void increaseReadPointer(unsigned int id, size_t count)
        {
            _readers[id]->first = (_readers[id]->first + count) % _size;
            sort();
        }

        void clear()
        {
            _pointers.clear();
            for (unsigned int i=0; i<_numberofReaders; i++) {
                _readers[i] = 0;
            }
            _writer = 0;
            _numberofActiveReaders = 0;
        }

        void sort()
        {
            _pointers.sort();
        }

        void setWriter(size_t pointer)
        {
            assert (_writer == 0);
            // the writers gets the max id, so that it is sorted after all readers with the same pointer
            _pointers.push_front(std::make_pair(pointer, _numberofReaders));
            _writer = _pointers.begin();

            if (_numberofActiveReaders == 0) _border = _writer->first;
        }

        void addReader(unsigned int id, size_t pointer)
        {
            assert (_writer != 0);
            assert (_readers[id] == 0);

            _pointers.push_front(std::make_pair(pointer, id));
            _readers[id] = _pointers.begin();
            _numberofActiveReaders++;
        }

        void removeReader(unsigned int id)
        {
            assert (_readers[id] != 0);
            
            _numberofActiveReaders--;
            if (_numberofActiveReaders == 0) _border = _readers[id]->first;
            
            _pointers.erase(_readers[id]);
            _readers[id] = 0;
        }

        bool readerActive(unsigned int id) const
        {
            return _readers[id] != 0; 
        }


    private:
        typedef std::pair<size_t, unsigned int> Pointer;
        typedef std::list<Pointer> PointerList;
        typedef PointerList::iterator PointerIter;

    private:
        PointerList::const_iterator _readerWithMaxUsage() const
        {
            PointerIter reader = _writer;
            reader++;
            if (reader == _pointers.end()) {
                return _pointers.begin();
            } else {
                return reader;
            }
        }

    private:
        PointerList _pointers;
        PointerIter _writer;
        PointerIter* _readers;
        const unsigned int _numberofReaders;
        const size_t _size;
        int _numberofActiveReaders;
        size_t _border;
    };
}

#endif
