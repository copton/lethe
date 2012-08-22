#ifndef __CALLBACK_ICE__
#define __CALLBACK_ICE__

#include <information.ice>
#include <exceptions.ice>

module Comm {
    module SourceService {
		interface ProtectedInterface {
            string startSourceService(string jobId)
                throws ::Comm::Exceptions::UnknownUidException;
            
			void stopSourceService(string jobId);
        };
    };

    module Manager {
		interface Callback {	
			["amd", "ami"] void onSimulationFinished(string jobId, string suffix, ::Comm::Simulation::Results results);
			["amd", "ami"] void onSimulationError(string jobId, string suffix, string error, bool abort);
		};
    };
};

#endif
