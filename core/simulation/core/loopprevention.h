#ifndef __CORE__LOOPPREVENTION_H
#define __CORE__LOOPPREVENTION_H

#include "exceptions.h"

namespace Core {
    class LoopPrevention {
    public:
        class LoopDetected { };

        LoopPrevention(bool& flag) throw (LoopDetected)
            : _flag(flag)
        {
            if (_flag) {
                throw LoopDetected();
            }
            _flag = true;
        }

        ~LoopPrevention()
        {
            _flag = false;
        }

    private:
        bool& _flag;
    };
}

#endif
