#ifndef __CORE__GRAPHLOGIC_H
#define __CORE__GRAPHLOGIC_H

#include "module.h"
#include "stream.h"
#include "controller.h"
namespace Core {
    class Module;
    class Stream;
    class Controller;
}

namespace Core {
    class GraphLogic {
    public:
        virtual ~GraphLogic() { }
        virtual void createGraph(std::vector<Module*>& vertices, std::vector<Stream*>& edges, Controller& controller, const std::vector<Ice::InputStreamPtr>& isVector) =0;
        virtual void shutdown() =0;
        virtual const std::string& getJobId() =0;
    };
}

#endif
