#ifndef __CORE__TEMPLATES__VECTORS_H
#define __CORE__TEMPLATES__VECTORS_H

#include "streamcallback.h"
#include "logger.h"

namespace Core {
    namespace Templates {
        template <class vtFrom, class vtTo>
        class ReadVector {
        public:
            typedef vtTo value_type;

            ReadVector(const vtFrom* data, size_t count, size_t offset, size_t size, StreamCallback& callback) : 
                _data(data), _count(count), _offset(offset), _size(size), _callback(callback)
            { }

            const value_type operator[](size_t index) const
            {
                assert (index < _count);
                return _data[(_offset + index) % _size]; // calls vtFrom::operator vtTo()
            }
            
        private:
            void operator=(const ReadVector& v) { } // assignment is forbidden
            const vtFrom* _data;
            size_t _count;
            size_t _offset;
            size_t _size;
            StreamCallback _callback;
        };
        
        template <class vtFrom, class vtTo>
        class WriteVector {
        public:
            typedef vtFrom value_type;
            
            // the maximum size for the temporary buffer is known at compile
            // time. But I'm too lazy to add an additional template parameter
            // for that. So I use dynamic memory instead. In case it turnes out
            // that this hurts too much one will have to change it.
            WriteVector(vtTo* data, size_t count, size_t offset, size_t size, StreamCallback& callback) :
                _buffer(new vtFrom[count]), 
                _data(data),
                _count(count),
                _offset(offset),
                _size(size),
                _dummy(data == 0),
                _callback(callback)
            { }

            ~WriteVector()
            {
                if (! _dummy) {
                    for (unsigned int i = 0; i < _count; ++i) {
                        _data[(_offset + i) % _size] = _buffer[i]; // vtTo::operator=(vtFrom)
                    }
                }

                delete [] _buffer;
            }

            value_type& operator[](size_t index)
            {
                assert (index < _count);
                return _buffer[index];
            }

        private:
            void operator=(const WriteVector& v) { } // assignment is forbidden
            vtFrom* _buffer;
            vtTo* _data;
            
            size_t _count;
            size_t _offset;
            size_t _size;
            bool _dummy;
            StreamCallback _callback;
        };

        template <class T>
        class WriteVector<T, T> {
        public:
            typedef T value_type;

            WriteVector(T* data, size_t count, size_t offset, size_t size, StreamCallback& callback) :
                _dummy(data == 0),
                _count(count),
                _offset(_dummy ? 0 : offset),
                _size(_dummy ? count : size),
                _data(_dummy ? new T[count] : data),
                _callback(callback)
            { }

            ~WriteVector()
            {
                if (_dummy) {
                    delete [] _data;
                }
            }

            value_type& operator[](size_t index)
            {
                assert (index < _count);
                return _data[(_offset + index) % _size];
            }

        private:
            void operator=(const WriteVector& v) { } // assignment is forbidden
            bool _dummy;
            size_t _count;
            size_t _offset;
            size_t _size;
            T* _data;
            StreamCallback _callback;
        };
    }
}

#endif
