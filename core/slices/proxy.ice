#ifndef __PROXY_ICE__
#define __PROXY_ICE__

#include <simulation.ice>
#include <callbacks.ice>

module Comm {
    module Proxy {
        interface \Interface extends ::Comm::Manager::Callback {
            ["amd"] void onAlive(Comm::Simulation::\Interface* simulation);
        };
    };
};

#endif
