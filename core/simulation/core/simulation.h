#ifndef __CORE__SIMULATION_H
#define __CORE__SIMULATION_H

#include <time.h>

#include <Ice/Ice.h>
#include <comm/comm.h>
#include "controller.h"
#include "application.h"
namespace Core {
    class Controller;
    class Application;
}

namespace Core {
    class Simulation : public Comm::Simulation::Interface {
    public:
        Simulation(Controller& controller, Comm::Proxy::InterfacePrx& proxy, Application& application, bool debug);

        void init_async(const Comm::Simulation::AMD_Interface_initPtr& callback, const Comm::Job::Specification& job, const ::Ice::Current& ctx);
        void start_async(const Comm::Simulation::AMD_Interface_startPtr& callback, int round, const ::Ice::Current& ctx); 
        void resume_async(const Comm::Simulation::AMD_Interface_resumePtr& callback, const Comm::Simulation::Checkpoint& theCheckpoint, const ::Ice::Current& ctx); 

        void stop_async(const Comm::Simulation::AMD_Interface_stopPtr& callback, const ::Ice::Current& ctx);
        void suspend_async(const Comm::Simulation::AMD_Interface_suspendPtr& callback, const ::Ice::Current& ctx);
        void createCheckpoint_async(const Comm::Simulation::AMD_Interface_createCheckpointPtr& callback, const ::Ice::Current& ctx);
        void getStatusInformation_async(const Comm::Simulation::AMD_Interface_getStatusInformationPtr& callback, const ::Ice::Current& ctx);
        void getResults_async(const Comm::Simulation::AMD_Interface_getResultsPtr& callback, const ::Ice::Current& ctx);
        void continue_async(const Comm::Simulation::AMD_Interface_continuePtr& callback, const ::Ice::Current& ctx);
        void die_async(const Comm::Simulation::AMD_Interface_diePtr& callback, const ::Ice::Current& ctx);

        void shutdown();

        void abort();
        void endOfRound();
        bool checkpoint(time_t& sleeptime);
        void onCallback();
        
    private:
        void _freezeController();
        void _unfreezeController();

    private:
        class Callback : public Comm::Manager::AMI_Callback_onSimulationFinished {
        public:
            Callback(Simulation* simulation);
            void ice_response();
            void ice_exception(const Ice::Exception& ex);
        private:
            Simulation* _simulation;
        };

    private:
        Comm::Simulation::State _state;
        typedef IceUtil::Monitor<IceUtil::Mutex> Monitor;
        Monitor _eventMonitor;
        Monitor _syncMonitor;
        bool _freeze;
        bool _frozen;
        bool _shutdown;

    private:
        Controller& _controller;
        IceUtil::ThreadPtr _controllerT;
        Comm::Proxy::InterfacePrx& _proxy;
        Application& _application;
        bool _debug;
    };
}

#endif
