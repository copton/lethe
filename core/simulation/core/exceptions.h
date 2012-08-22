#ifndef __CORE__EXCEPTIONS_H
#define __CORE__EXCEPTIONS_H

#include <stdexcept>

namespace Core {
    namespace Exceptions {
        class Base : public std::runtime_error { 
        public:
            Base(const std::string what) :
                std::runtime_error(what)
            { }
        };

        class LoopDetected : public Base { 
        public:
            LoopDetected(const std::string what) :
                Base(what)
            { }
        };

        class BufferEmpty : public Base { 
        public:
            BufferEmpty() :
                Base("Buffer Empty")
            { }
        };

        class BufferFull : public Base { 
        public:
            BufferFull() :
                Base("Stream has no readers")
            { }
        };

        class ModuleHasNoReaders : public Base { 
        public:
            ModuleHasNoReaders() :
                Base("Module has no readers")
            { }
        };
    }
}

#endif
