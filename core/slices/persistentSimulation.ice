
#ifndef __PERSISTENT_SIMULATION_ICE__
#define __PERSISTENT_SIMULATION_ICE__

#include <simulation.ice>

module Comm {
    module Manager {
        struct SimulationObject {
			string jobId;
			int round;
			long lastChangeTime;
			bool hasCheckpoint;
			bool removeFromScheduling;
			string simulationSuffix;
			::Comm::Simulation::State theState;
		};
	};
};

#endif
