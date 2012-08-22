
#ifndef __SCHEDULER_ICE__
#define __SCHEDULER_ICE__

module Comm {
    module Manager {
        struct SchedulerInformation {
			long startTime;
            int priority;
            long elapsedTime;
            float percentDone;
        };
	};
};

#endif
