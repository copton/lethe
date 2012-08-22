#include "simulation.h"
#include "logger.h"

namespace Core {
    Simulation::Callback::Callback(Simulation* simulation) :
        _simulation(simulation)
    { }

    void Simulation::Callback::ice_response()
    {
        _simulation->onCallback();
    }

    void Simulation::Callback::ice_exception(const Ice::Exception& ex)
    {
        std::cerr << ex; 
        assert (false);
    }

    Simulation::Simulation(Controller& controller, Comm::Proxy::InterfacePrx& proxy, Application& application, bool debug) :
        _state(Comm::Simulation::NEW),
        _controller(controller),
        _controllerT(new ControllerThread(controller)),
        _proxy(proxy),
        _application(application),
        _debug(debug)
    { 
        _controller.setup(this);
    }

    void Simulation::init_async(const Comm::Simulation::AMD_Interface_initPtr& callback, const Comm::Job::Specification& job, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::init");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::NEW) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("init"));
            return;
        } else { 
            _controller.init(job);
            { Monitor::Lock l(_syncMonitor);
                _shutdown = false;
                _frozen = false;
                _freeze = true;
                _controllerT->start();
                while (! _frozen) {
                    _syncMonitor.wait();
                }
            }
            _state = Comm::Simulation::READY;
            callback->ice_response();
        }
    }

    void Simulation::start_async(const Comm::Simulation::AMD_Interface_startPtr& callback, int round, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::start");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::READY && _state != Comm::Simulation::FINISHED && _state != Comm::Simulation::STOPPED) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("start"));
            return;
        } else {
            _controller.start(round);
            _unfreezeController();
            _state = Comm::Simulation::RUNNING;
            callback->ice_response();
        }
    }

    void Simulation::resume_async(const Comm::Simulation::AMD_Interface_resumePtr& callback, const Comm::Simulation::Checkpoint& theCheckpoint, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::resume");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::READY && _state != Comm::Simulation::FINISHED && _state != Comm::Simulation::STOPPED) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("resume"));
            return;
        } else {
            _controller.resume(theCheckpoint);
            _unfreezeController();
            _state = Comm::Simulation::RUNNING;
            callback->ice_response();
        }
    }


    void Simulation::stop_async(const Comm::Simulation::AMD_Interface_stopPtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::stop");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::RUNNING) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("stop"));
            return;
        } else {
            _freezeController();
            _state = Comm::Simulation::STOPPED;
            callback->ice_response();
        }
    }   

    void Simulation::suspend_async(const Comm::Simulation::AMD_Interface_suspendPtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::suspend");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::RUNNING) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("suspend"));
            return;
        } else {
            _freezeController();
            _state = Comm::Simulation::STOPPED;
            Comm::Simulation::Checkpoint checkpoint;
            checkpoint.theState = _controller.getState();
            checkpoint.theStatus = _controller.getStatus();
            checkpoint.theStatus.theState = _state;
            checkpoint.theStatus.error = "";
            callback->ice_response(checkpoint, _controller.getJob());
        }
    }

    void Simulation::createCheckpoint_async(const Comm::Simulation::AMD_Interface_createCheckpointPtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::createCheckpoint");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::RUNNING and _state != Comm::Simulation::STOPPED) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("createCheckpoint"));
            return;
        } else {
            if (_state == Comm::Simulation::RUNNING)  {
                _freezeController();
            }

            Comm::Simulation::Checkpoint checkpoint;
            checkpoint.theState = _controller.getState();
            checkpoint.theStatus = _controller.getStatus();

            if (_state == Comm::Simulation::RUNNING) {
                _unfreezeController();
            }

            callback->ice_response(checkpoint);
        }
    }

    void Simulation::getStatusInformation_async(const Comm::Simulation::AMD_Interface_getStatusInformationPtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::getStatusInformation");
        Monitor::Lock l(_eventMonitor);
        Comm::Simulation::Status status;
        if (_state == Comm::Simulation::LOCKED) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("getStatusInformation"));
            return;
        } else if (_state == Comm::Simulation::RUNNING or _state == Comm::Simulation::STOPPED or _state == Comm::Simulation::FINISHED) {
            if (_state == Comm::Simulation::RUNNING)  {
                DEBUGOUT("getStatusInformation: freeze");
                _freezeController();
            }

            status = _controller.getStatus();

            if (_state == Comm::Simulation::RUNNING) {
                DEBUGOUT("getStatusInformation: unfreeze");
                _unfreezeController();
            }
        } else {
            status.progress = 0;
            status.runtime = 0;
            status.cputime = 0;
            status.livetime = 0;
        }
        status.theState = _state;
        status.error = "";
        callback->ice_response(status);
    }

    void Simulation::getResults_async(const Comm::Simulation::AMD_Interface_getResultsPtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::getResults");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::FINISHED) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("getResults"));
            return;
        } else {
            Comm::Simulation::Results results = _controller.getResults();
            callback->ice_response(results);
        }
    }

    void Simulation::continue_async(const Comm::Simulation::AMD_Interface_continuePtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::continue");
        Monitor::Lock l(_eventMonitor);
        if (_state != Comm::Simulation::STOPPED) {
            callback->ice_exception(Comm::Exceptions::ActionNotAllowed("continue"));
            return;
        } else {
            _unfreezeController();
            _state = Comm::Simulation::RUNNING;
            callback->ice_response();
        }
    }

    void Simulation::die_async(const Comm::Simulation::AMD_Interface_diePtr& callback, const ::Ice::Current& ctx)
    {
        DEBUGOUT("Simulation::die");
        callback->ice_response();
        _application.shutdown();
    }

    void Simulation::shutdown()
    {
        DEBUGOUT("Simulation::shutdown");
        { Monitor::Lock l(_eventMonitor);
            { Monitor::Lock l(_syncMonitor);
                _shutdown = true;
                if (_frozen) {
                    _freeze = false;
                    _syncMonitor.notify();        
                }
            }
            try {
                IceUtil::ThreadControl tc = _controllerT->getThreadControl(); 
                tc.join();
            } catch (IceUtil::ThreadNotStartedException) { }
        }
    }

    void Simulation::endOfRound()
    {
        DEBUGOUT("Simulation::endOfRound");
        Monitor::Lock l(_eventMonitor);     
        assert (_state == Comm::Simulation::RUNNING);
        const std::string& jobid = _controller.getJobId();
        const std::string& suffix = _controller.getSuffix();
        Comm::Simulation::Results results = _controller.getResults();
        if (! _debug) {
            _proxy->onSimulationFinished_async(new Callback(this), jobid, suffix, results);
            _state = Comm::Simulation::LOCKED;
        } else {
            _state = Comm::Simulation::FINISHED;
        }
        { Monitor::Lock l(_syncMonitor);
            _freeze = true;
        }
    }

    void Simulation::onCallback()
    {
        Monitor::Lock l(_eventMonitor);
        assert (_state == Comm::Simulation::LOCKED);
        { Monitor::Lock l(_syncMonitor);
            while (! _frozen) {
                _syncMonitor.wait();
            }
        }
        _state = Comm::Simulation::FINISHED;
    }

    bool Simulation::checkpoint(time_t& timeofStop)
    {
        // DEBUGOUT("Simulation checkpoint"); // XXX remove this if logging to breeze
        Monitor::Lock l(_syncMonitor);
        if (_shutdown) {
            return false;
        }
        if (_freeze) {
            _frozen = true;
            _syncMonitor.notify();
            timeofStop = time(0);
            DEBUGOUT("Simulation: checkpoint freeze");
            while (_freeze) {
                _syncMonitor.wait();
            }
            DEBUGOUT("Simulation: checkpoint awake");
            if (_shutdown) {
                return false;
            } 
            _frozen = false;
            _syncMonitor.notify();
        }
        return true;
    }

    void Simulation::_freezeController()
    {
        Monitor::Lock l(_syncMonitor);
        assert (! _frozen);
        _freeze = true;
        while (! _frozen) {
            _syncMonitor.wait(); 
        }
    }

    void Simulation::_unfreezeController()
    {
        Monitor::Lock l(_syncMonitor);
        assert (_frozen);
        _freeze = false;
        _syncMonitor.notify();
        while (_frozen) {
            _syncMonitor.wait();
        }
    }
}
