#ifndef __LOGGER_ICE_
#define __LOGGER_ICE_

module Comm {
    module Logger {
        enum TypeEnum {
            EMERG,
            ALERT,
            CRIT,
            ERR,
            WARNING,
            NOTICE,
            INFO,
            DEBUG
        };


        sequence<TypeEnum> TypeSeq;

        struct Message {
            TypeEnum type;
            string origin;
            string text;
            long timestamp;
        };
        
        sequence<Message> MessageSeq;

        interface Publish {
            void add(Message theMessage);
        };

        interface Lastlog {
            MessageSeq get(TypeSeq include, TypeSeq exclude, string originRegex, string textRegex, long timeFrom, long timeTo);
        };
    };
};

#endif
