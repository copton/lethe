#ifndef __CORE__LOGGER_H
#define __CORE__LOGGER_H

#include <IceUtil/Thread.h>
#include <comm/comm.h>
#include <stdarg.h>

#include <fstream>

namespace Core {
    class Logger {
    public:
        static void init(Comm::Logger::PublishPrx publish, const std::string& name, bool debug);

        template <Comm::Logger::TypeEnum type>
        class Log {
        public:
            void operator()(const std::string& text)
            {
                assert(Logger::_instance != 0); 
                Logger::_instance->_log(type, text);
            }

            void operator()(const char* format, ...)
            {
                assert(Logger::_instance != 0); 
                va_list ap;
                va_start(ap, format);
                Logger::_instance->_log(type, format, ap); 
                va_end(ap);
            }
        };

        static Log<Comm::Logger::EMERG> emergency;
        static Log<Comm::Logger::ALERT> alert;
        static Log<Comm::Logger::CRIT> critical;
        static Log<Comm::Logger::ERR> error;
        static Log<Comm::Logger::WARNING> warning;
        static Log<Comm::Logger::NOTICE> notice;
        static Log<Comm::Logger::INFO> info;
        static Log<Comm::Logger::DEBUG> debug;

    private:
        static Logger* _instance;
        Logger(Comm::Logger::PublishPrx publish, const std::string& name, bool debug);

        void _log(Comm::Logger::TypeEnum type, const std::string& text);
        void _log(Comm::Logger::TypeEnum type, const char* format, va_list ap);

        const std::string& _name;

        Comm::Logger::PublishPrx _publish;
        std::ofstream _logfile;

        typedef IceUtil::Monitor<IceUtil::Mutex> Monitor;
        bool _debug;
        Monitor _monitor;
    };
}

#define LOG Core::Logger::getInstance()
#ifdef NDEBUG
inline void __do_nothing(const std::string& s) {}
inline void __do_nothing(const char* format, ...) { }
#define DEBUGOUT __do_nothing
#else
#define DEBUGOUT Core::Logger::debug
#endif


#endif
