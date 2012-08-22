
#ifndef __EXTENSIONS__PIPES__COMPLEX_H
#define __EXTENSIONS__PIPES__COMPLEX_H

#include <core/logger.h>
#include "gen_Complex.h"
#include <Ice/Ice.h>
#include <gsl/gsl_complex_math.h>

namespace Extensions {
	namespace Types {
		class Complex : public Lethe::Complex {
		public:
                static void serialize(const Ice::OutputStreamPtr& os, const Complex& value)
                {
                    Lethe::ice_writeComplex(os, value);
                }

           		static const Complex deserialize(const Ice::InputStreamPtr& is)
                {
                Complex that;
                    Lethe::ice_readComplex(is, that);
                    return that;
                }

                Complex()
                { }

                Complex(const gsl_complex& c)
                {
                    real = GSL_REAL(c);
                    img = GSL_IMAG(c);
                } 

                operator gsl_complex() const
                {
                    return gsl_complex_rect(real, img);
                }
        	};
    	}
}

#endif			
