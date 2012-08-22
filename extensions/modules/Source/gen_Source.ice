
// generated at 2006-04-17T19:04:27.284+02:00
#ifndef __SOURCE_ICE__
#define __SOURCE_ICE__


module Extensions {
	module Modules {
		module Source {
			module Lethe {			
				
				sequence < byte > ByteSequence;
				
				
			
				struct Configuration { 
					bool useRandom; 
				};
			
				struct Settings { 
					bool newSeed;
					long seed;
				};
			
				struct Result { 
				
					bool icegenDUMMY; 
				};
				
				struct State { 
					ByteSequence rngState;
					int counter; 
				};
			};	
		};
	};
};				
	
#endif
