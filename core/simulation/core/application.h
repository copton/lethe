#ifndef __CORE__APPLICATION_H
#define __CORE__APPLICATION_H

#include <Ice/Ice.h>
#include "graphLogic.h"
namespace Core {
    class GraphLogic;
}


namespace Core {
    class Application : public Ice::Application {
    public:
        Application::Application(GraphLogic& graphLogic);
        int run(int argc, char* argv[]);
        void shutdown();
    private:
        GraphLogic& _graphLogic;
    };
}

#endif
