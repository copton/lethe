
#ifndef __JOB_INFORMATION_ICE__
#define __JOB_INFORMATION_ICE__

#include <job.ice>
#include <simulation.ice>

module Comm {
    module Job {
 	    sequence<::Comm::Simulation::Results> ResultsSeq;  
        sequence<::Comm::Simulation::Status> StatusSeq;
        sequence<string> StringSeq;

        struct JobInformation {
            string jobId;
            string name;
            string owner;
            string description;
            long startTime;

            SettingsDictSeq settings;
            ::Comm::Simulation::Status collocatedStatus;

            StringSeq roundIds;
            StatusSeq status;
            ResultsSeq results;
        };
    };
};


#endif

