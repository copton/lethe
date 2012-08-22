#ifndef __INFORMATION_ICE__
#define __INFORMATION_ICE__

module Comm {
	module Simulation {
		sequence<byte> ByteSeq;
        dictionary<string, ByteSeq> Results;

        enum State {
            NEW,
			READY,
            RUNNING,
            STOPPED,
            LOCKED,
            FINISHED,
            ERROR
        };

        struct Status {
            float progress;
            State theState;
            string error;
            int phaseNumber;
            long livetime;
            long runtime;
            long cputime;
        };	

        sequence<ByteSeq> StateSeq;
        struct Checkpoint {
            StateSeq theState;
            Status theStatus;
        };
   }; 
};

#endif
