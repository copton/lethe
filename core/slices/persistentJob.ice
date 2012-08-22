
#ifndef __PERSISTENT_JOB_ICE__
#define __PERSISTENT_JOB_ICE__

#include <job.ice>
#include <simulation.ice>

module Comm {
    module Job {
		sequence<::Comm::Simulation::Checkpoint> CheckpointSeq;
		sequence<::Comm::Simulation::Results> ResultSeq;

		struct SimulationWrapper {
			::Comm::Simulation::\Interface* simulation;
			string suffix;
			string host;
			bool active;
		};

		dictionary<string, SimulationWrapper> SimulationDict;
        
		struct Persistence {
			Specification job;
			CheckpointSeq checkpoints;        	
			SimulationDict freeSimulationObjects;
			ResultSeq results;			
			int nextSuffixId;
		};
	};
};

#endif
