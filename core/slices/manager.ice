#ifndef __MANAGER_ICE__
#define __MANAGER_ICE__

#include <job.ice>
#include <jobInformation.ice>
#include <exceptions.ice>
#include <simulation.ice>
#include <persistentJob.ice>

module Comm {
	module Manager {
		sequence <string> HostSeq;
		sequence <string> UserSeq;		
		sequence <string> JobSeq;
		sequence <::Comm::Job::JobInformation> InformationSeq;
		sequence <::Comm::Simulation::Results> ResultSeq;

		struct ChangedId {
			string jobId;
			int round;
	
			::Comm::Simulation::Status collocatedStatus;
			::Comm::Simulation::Status newStatus;
		};

		interface Publisher {
			void report(ChangedId id);
			void simulationFinished(::Comm::Job::Persistence job);
		};

		interface PublicInterface {
			["amd"] bool startSimulation(::Comm::Job::Specification job) 
				 throws ::Comm::Exceptions::BuildException,
						::Comm::Exceptions::SpecException,
						::Comm::Exceptions::ActionNotAllowed;

			string getNextId();

			void pauseSimulation(string jobId)
				throws ::Comm::Exceptions::ActionNotAllowed;

			void restartSimulation(string jobId)
				throws ::Comm::Exceptions::ActionNotAllowed;

			["amd", "ami"] void moveSimulationToHost(string jobId, string host)
				throws ::Comm::Exceptions::ActionNotAllowed;

			void abortSimulation(string jobId)
				throws ::Comm::Exceptions::ActionNotAllowed;

			
			JobSeq getActiveSimulations();
			JobSeq getPausedSimulations();
			JobSeq getScheduledSimulations();
			JobSeq getAllSimulations();
	
			HostSeq getSimulationHosts();
			UserSeq getActiveUsers();
			UserSeq getAllUsers();

			idempotent ::Comm::Job::Persistence getResults(string jobId)
				throws ::Comm::Exceptions::ActionNotAllowed;

			::Comm::Job::JobInformation getInformation(string jobId);
			InformationSeq getInformationForAll();

			["amd", "ami"] void makeCheckpoint(string jobId)
				throws ::Comm::Exceptions::ActionNotAllowed;
		};
	};
};

#endif
