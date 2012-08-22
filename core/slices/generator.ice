
#ifndef __GENERATOR_ICE__
#define __GENERATOR_ICE__

#include <exceptions.ice>
#include <simulation.ice>
#include <job.ice>

module Comm {
	module Generator {
		interface \Interface {
			["amd", "ami"] ::Comm::Simulation::\Interface* createSimulation(::Comm::Job::Specification job, string suffix, bool buildOnly)
                throws ::Comm::Exceptions::SpecException,
				       ::Comm::Exceptions::BuildException, 
                       ::Comm::Exceptions::StartupException;
		};
	};
};



#endif
