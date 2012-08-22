module Core {
    module State {
        sequence<int> IntSequence;
        sequence<bool> BoolSequence;    
        sequence<long> LongSequence;

        struct Controller {
            int round;
            int phase;
            int index;
            IntSequence observed;
            IntSequence scheduled;
        };

        struct Stream {
            int writerId;
            BoolSequence activeReaders;    
        };

        struct TStream {
            LongSequence readPointers;
            long writePointer;
        };
        
        struct \Module {
            bool active;
            int numberofOutputStreams;
        };
    };
};
