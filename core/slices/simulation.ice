#ifndef __SIMULATION_ICE__
#define __SIMULATION_ICE__

#include <exceptions.ice>
#include <job.ice>
#include <information.ice>

module Comm {
	module Simulation {
		interface \Interface {
			["amd", "ami"] void init(::Comm::Job::Specification job) 
                throws ::Comm::Exceptions::SpecException,
                       ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void start(int round)
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void resume(Checkpoint theCheckpoint)
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void stop() 
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void suspend(out Checkpoint theCheckpoint, out ::Comm::Job::Specification job)
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] Checkpoint createCheckpoint() 
                throws ::Comm::Exceptions::ActionNotAllowed;
			
			["amd", "ami"] Status getStatusInformation() 
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] Results getResults() 
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void continue()
                throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void die()
                throws ::Comm::Exceptions::ActionNotAllowed;
		};
	};
};

#endif
