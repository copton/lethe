import Ice
import IceGrid

Ice.loadSlice("-I../slices/ --all ../slices/proxy.ice ../slices/generator.ice ../slices/logger.ice")
import Comm

import generator
import dispatcher
import observer

class Application(Ice.Application):
    def getGenerator(self):
        return self.generator

    def getDispatcher(self):
        return self.dispatcher

    def getManager(self):
        return self.manager

    def getExternAdapter(self):
        return self.externAdapter

    def getInternAdapter(self):
        return self.internAdapter

    def getObserver(self):
        return self.observer

    def getProperty(self, name):
        return self.properties.getProperty(name)

    def run(self, argv):
        self.properties = self.communicator().getProperties()
        
        debug = len(argv) == 2 and argv[1] == "debug"

        if debug:
            print "debug mode"

        if debug:
            endpoint = "tcp -h localhost -p 9998"
            self.externAdapter = self.communicator().createObjectAdapterWithEndpoints("GeneratorAdapter",endpoint)
        else:
            self.externAdapter = self.communicator().createObjectAdapter("GeneratorAdapter")

        if not debug:
            self.dispatcher = dispatcher.Dispatcher()
            self.internAdapter = self.communicator().createObjectAdapterWithEndpoints("InternAdapter", "tcp -h localhost")
            self.observer = observer.Observer()

            managerBase = self.communicator().stringToProxy("ManagerCallback@Manager.ServiceAdapter")
            self.manager = Comm.Manager.CallbackPrx.checkedCast(managerBase)
            assert (self.manager)

        self.generator = generator.Generator(self, debug)

        self.externAdapter.activate()
        if not debug:
            self.internAdapter.activate()
            self.observer.start()

        print "Generator: up"
        self.communicator().waitForShutdown()

        print "Generator: shutdown generator"
        self.generator.shutdown()

        if not debug:
            print "Generator: shutdown observer"
            self.observer.shutdown()

            print "Generator: shutdown dispatcher"
            self.dispatcher.shutdown()

        print "Generator: shutdown complete"
