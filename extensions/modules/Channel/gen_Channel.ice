
// generated at 2006-04-17T19:04:18.053+02:00
#ifndef __CHANNEL_ICE__
#define __CHANNEL_ICE__


module Extensions {
	module Modules {
		module Channel {
			module Lethe {			
				
				struct Noise { 
					int period;
					int amplifier; 
				};
				
				
			
				struct Configuration { 
				
					bool icegenDUMMY; 
				};
			
				struct Settings { 
					Noise noiseReal;
					Noise noiseImag;
				};
			
				struct Result { 
				
					bool icegenDUMMY; 
				};
				
				struct State { 
					int counterReal;
					int counterImag; 
				};
			};	
		};
	};
};				
	
#endif
