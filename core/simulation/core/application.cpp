#include "application.h"

#include <sys/types.h>
#include <unistd.h>
#include <signal.h>

#include <comm/comm.h>
#include "logger.h"
#include "simulation.h"

namespace Core {
    Application::Application(GraphLogic& graphLogic) :
        _graphLogic(graphLogic)
    { }

    int Application::run(int argc, char* argv[])
    {
        assert (argc == 5 || argc == 4);
        const std::string jobId = argv[1];
        const std::string suffix = argv[2];
        const std::string proxyStr = argv[3];
        const bool debug = (proxyStr == "debug");
        assert (debug || argc == 5);
        const std::string locator = debug ? "" : argv[4];
        const std::string name = "Simulation-" + jobId + "-" + suffix;

        Comm::Logger::PublishPrx publish = 0;
        if (! debug) {
            Ice::ObjectPrx locatorBase = communicator()->stringToProxy(locator);
            assert (locatorBase);
            Ice::LocatorPrx locatorPrx = Ice::LocatorPrx::checkedCast(locatorBase);
            assert (locatorPrx);

            Ice::ObjectPrx publishBase = communicator()->stringToProxy("Publish@Breeze.BreezeAdapter");
            assert (publishBase);
            publish = Comm::Logger::PublishPrx::checkedCast(publishBase->ice_locator(locatorPrx));
            assert (publish);
        }

        Logger::init(publish, name, debug);

        Comm::Proxy::InterfacePrx proxyPrx = 0;
        if (! debug) {
            Ice::ObjectPrx proxyBase = communicator()->stringToProxy(proxyStr);
            assert(proxyBase);
            proxyPrx = Comm::Proxy::InterfacePrx::checkedCast(proxyBase);
            assert(proxyPrx);
        }

        Controller controller(communicator(), jobId, suffix, _graphLogic, *this); 
        Simulation* simulation = new Simulation(controller, proxyPrx, *this, debug);
        Ice::ObjectPtr simulationKeepAlive = simulation; // prevent ICE braindead reference counting from deleting my object when the communicator is shut down

        std::string endpoint = "tcp -h localhost";
        if (debug) {
            endpoint +=" -p 9999";
        }
        Ice::ObjectAdapterPtr adapter = communicator()->createObjectAdapterWithEndpoints("SimulationAdapter", endpoint);

        Ice::Identity id = Ice::stringToIdentity(name);
        Ice::ObjectPrx simulationBase = adapter->add(simulation, id);
        Comm::Simulation::InterfacePrx simulationPrx = Comm::Simulation::InterfacePrx::uncheckedCast(simulationBase);
        assert(simulationPrx);

        adapter->activate();
        if (! debug) {
            proxyPrx->onAlive(simulationPrx);
        } else {
            Logger::info(simulationPrx->ice_toString());
        }
        DEBUGOUT("Application: startup completed");

        communicator()->waitForShutdown();

        DEBUGOUT("Application: shutting down");
        simulation->shutdown();

        DEBUGOUT("Application: shutdown completed");
        return 0;
    }

    void Application::shutdown()
    {
        DEBUGOUT("Application: shutdown()");
        communicator()->shutdown();
    }
    
}
