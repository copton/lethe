#include "logger.h"

#include <stdarg.h>
#include <iostream>

namespace Core {
    Logger* Logger::_instance = 0;

    Logger::Logger(Comm::Logger::PublishPrx publish, const std::string& name, bool debug) :
        _name(name),
        _publish(publish),
        _logfile("/tmp/simulation.log", std::ios_base::app),
        _debug(debug)
    {
        _log(Comm::Logger::INFO, "log opened");
    }

    void Logger::init(Comm::Logger::PublishPrx publish, const std::string& name, bool debug)
    {
        assert (_instance == 0);
        assert (publish || debug);
        _instance = new Logger(publish, name, debug);
    }

    void Logger::_log(Comm::Logger::TypeEnum type, const std::string& text)
    {
        Comm::Logger::Message msg;
        msg.origin = _name;
        msg.text = text;
        msg.type = type;
        { Monitor::Lock l(_monitor);
            if (! _debug) {
                _logfile << text << std::endl;
                _publish->add(msg);
            } else {
                std::cout << text << std::endl;
            }   
        }
    }

    void Logger::_log(Comm::Logger::TypeEnum type, const char* format, va_list ap)
    {
        va_list copy;
        va_copy(copy, ap);
        const int size = vsnprintf(0, 0, format, copy);
        va_end(copy);

        char* buffer = new char[size + 1];
        vsnprintf(buffer, size + 1, format, ap);
        
        std::string str(buffer);
        _log(type, str);
        delete [] buffer;
    }

    Logger::Log<Comm::Logger::EMERG> Logger::emergency;
    Logger::Log<Comm::Logger::ALERT> Logger::alert;
    Logger::Log<Comm::Logger::CRIT> Logger::critical;
    Logger::Log<Comm::Logger::ERR> Logger::error;
    Logger::Log<Comm::Logger::WARNING> Logger::warning;
    Logger::Log<Comm::Logger::NOTICE> Logger::notice;
    Logger::Log<Comm::Logger::INFO> Logger::info;
    Logger::Log<Comm::Logger::DEBUG> Logger::debug;

}
