
// generated at 2006-04-17T19:04:24.97+02:00
#ifndef __SINK_ICE__
#define __SINK_ICE__


module Extensions {
	module Modules {
		module Sink {
			module Lethe {			
				
				
			
				struct Configuration { 
					int abort;
					int counter; 
				};
			
				struct Settings { 
				
					bool icegenDUMMY;
				};
			
				struct Result { 
					float errorProb; 
				};
				
				struct State { 
					int counter;
					int errorCounter; 
				};
			};	
		};
	};
};				
	
#endif
