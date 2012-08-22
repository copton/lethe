#ifndef __JOB_ICE__
#define __JOB_ICE__

#include <graph.ice>
#include <scheduler.ice>
#include <callbacks.ice>

module Comm {
	module Job {
		sequence<byte> Settings;
        dictionary<string, Settings> SettingsDict;
        sequence<SettingsDict> SettingsDictSeq;

        sequence<::Comm::SourceService::ProtectedInterface*> SourceServerSeq;

		struct Specification {
			string jobId;
			string name;
			string owner;
			string description;

			Graph::SimulationGraph theGraph;
			::Comm::Manager::SchedulerInformation schedulerInfo;
			SettingsDictSeq settings;
            SourceServerSeq sourceServers;
		};
	};
};


#endif
