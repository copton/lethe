
#ifndef __EXTENSIONS__PIPES__INTEGER_H
#define __EXTENSIONS__PIPES__INTEGER_H

#include "gen_Integer.h"
#include <Ice/Ice.h>

namespace Extensions {
	namespace Types {
		class Integer : public Lethe::Integer {
		public:
                static void serialize(const Ice::OutputStreamPtr& os, const Integer& value)
                {
                    Lethe::ice_writeInteger(os, value);
                }

           		static const Integer deserialize(const Ice::InputStreamPtr& is)
                {
                Integer that;
                    Lethe::ice_readInteger(is, that);
                    return that;
                }

                Integer()
                { }

                Integer(int i)
                {
                    value = i;
                }

                operator int() const
                {
                    return value;
                }
        	};
    	}
}

#endif			
