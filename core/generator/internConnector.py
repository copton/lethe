import Ice
import Comm

class InternConnector(Comm.Proxy.Interface):
    def __init__(self, name, proxy, application):
        self.adapter = application.getInternAdapter()
        self.dispatcher = application.getDispatcher()
        self.locator = application.getProperty("Ice.Default.Locator")
        self.proxy = proxy

        self.id = Ice.stringToIdentity(name)
        self.adapter.add(self, self.id)

    def getOptions(self):
        options = []
        
        directProxy = self.adapter.createDirectProxy(self.id)
        options.append(directProxy.ice_toString())
        options.append(self.locator)

        return options

    def quit(self):
        self.adapter.remove(self.id)

    ## Incoming
    def onAlive_async(self, amdCallback, simulation, _ctx=None):
        self.simulationPrx = simulation
        self.dispatcher.add(self.proxy.onAlive, amdCallback)

    def onSimulationFinished_async(self, amdCallback, jobId, suffix, results, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (jobId, suffix, results), self.proxy.externConnector.onSimulationFinished)

    ## Outgoing
    def die(self):
        self.simulationPrx.die()

    def init(self, amiCallback, params):
        self.simulationPrx.init_async(amiCallback, *params)

    def start(self, amiCallback, params):
        self.simulationPrx.start_async(amiCallback, *params)

    def resume(self, amiCallback, params):
        self.simulationPrx.resume_async(amiCallback, *params)

    def stop(self, amiCallback, params):
        self.simulationPrx.stop_async(amiCallback, *params)

    def suspend(self, amiCallback, params):
        self.simulationPrx.suspend_async(amiCallback, *params)

    def _continue(self, amiCallback, params):
        self.simulationPrx.continue_async(amiCallback, *params)

    def createCheckpoint(self, amiCallback, params):
        self.simulationPrx.createCheckpoint_async(amiCallback, *params)

    def getStatusInformation(self, amiCallback, params):
        self.simulationPrx.getStatusInformation_async(amiCallback, *params)

    def getResults(self, amiCallback, params):
        self.simulationPrx.getResults_async(amiCallback, *params)

