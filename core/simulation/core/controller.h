#ifndef __CORE__CONTROLLER_H
#define __CORE__CONTROLLER_H

#include <IceUtil/Thread.h>

#include <comm/comm.h>

#include "graphLogic.h"
#include "state.h"
#include "stream.h"
#include "module.h"
#include "simulation.h"
#include "application.h"
namespace Core {
    class Module;
    class Stream;
    class Simulation;
    class GraphLogic;
    class Application;
}

namespace Core {
    class Controller {
    public:
        Controller(Ice::CommunicatorPtr communicator, const std::string& jobId, const std::string& suffix, GraphLogic& graphLogic, Application& application);
        void setup(Simulation* simulation);

        void init(const ::Comm::Job::Specification& job);

        void start(int round);
        void resume(const Comm::Simulation::Checkpoint& checkpoint);

        Comm::Simulation::Status getStatus();
        Comm::Simulation::StateSeq getState();
        Comm::Simulation::Results getResults();

        const std::string& getJobId() const;
        const std::string& getSuffix() const;
        const Comm::Job::Specification& getJob() const;

        void run();

        void onVertexQuit(int number);

    private:
        std::vector<Module*> _vertices;
        std::vector<Stream*> _edges;

    private:
        void _newPhase();
        void _resetGraph();
        void _resetTimers();

    private:
        const std::string _jobId;
        const std::string _suffix;
        Comm::Job::Specification _job;
        int _numberofPhases;
        State::Controller _state;
        Comm::Simulation::Status _status;

        time_t _timeofStart;
        time_t _timeofStop;
        time_t _timeofContinue;
        time_t _runtime;
        time_t _timeofEnd;

    private:
        Simulation* _simulation;
        Ice::CommunicatorPtr _communicator;
        Application& _application;
        GraphLogic& _graphLogic;
    };

    class ControllerThread : public IceUtil::Thread {
    public:
        ControllerThread(Controller& controller) : _controller(controller) { }
        void run() { return _controller.run(); }
    private:
        Controller& _controller;
    };
}



#endif
