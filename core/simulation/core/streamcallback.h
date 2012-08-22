#ifndef __CORE__STREAMCALLBACK_H
#define __CORE__STREAMCALLBACK_H

#include "streamcache.h"

namespace Core {
    class Reference {
    public:
        Reference() : _counter(1) { }
        bool isZero() { return _counter == 0; }
        void inc() { _counter++; }
        void dec() { _counter--; assert(_counter >= 0); }
    private:
        int _counter;
    };

    class StreamCallback {
    public:
        StreamCallback(int id, size_t count, StreamCache* cache) :
            _id(id), _count(count), _cache(cache), _ref(new Reference())
        { }

        StreamCallback(const StreamCallback& callback) :
            _id(callback._id), _count(callback._count), _cache(callback._cache), _ref(callback._ref)
        { 
            _ref->inc();
        }

        ~StreamCallback()
        {
            _ref->dec();
            if (_ref->isZero()) {
                if (_id == -1) {
                    _cache->increaseWritePointer(_count);
                } else {
                    _cache->increaseReadPointer(_id, _count);
                }
                delete _ref;
            }
        }

    private:
        void operator=(const StreamCallback& callback) { }

        int _id;
        size_t _count;
        StreamCache* _cache;
        Reference* _ref;
    };
}

#endif
