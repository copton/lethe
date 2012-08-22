import Ice
import Comm

class ExternConnector(Comm.Simulation.Interface):
    def __init__(self, name, callback, proxy, application):
        self.proxy = proxy
        self.createCallback = callback
        self.dieCallback = None
        self.adapter = application.getExternAdapter()
        self.dispatcher = application.getDispatcher()
        self.manager = application.getManager()

        self.id = Ice.stringToIdentity(name)
        base = self.adapter.add(self, self.id)
        self.simulationPrx = Comm.Simulation.InterfacePrx.uncheckedCast(base)

    def quit(self):
        self.onDead()
        self.adapter.remove(self.id)

    ## Incoming
    def die_async(self, amdCallback, _ctx=None):
        self.dieCallback = amdCallback
        self.dispatcher.add(self.proxy.die, 0)
        
    def init_async(self, amdCallback, job, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (job,), self.proxy.internConnector.init)

    def start_async(self, amdCallback, round, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (round,), self.proxy.internConnector.start)

    def resume_async(self, amdCallback, state, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (state,), self.proxy.internConnector.resume)

    def stop_async(self, amdCallback, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (), self.proxy.internConnector.stop)

    def suspend_async(self, amdCallback, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (), self.proxy.internConnector.suspend)
    
    def continue_async(self, amdCallback, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (), self.proxy.internConnector._continue)

    def createCheckpoint_async(self, amdCallback, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (), self.proxy.internConnector.createCheckpoint)

    def getStatusInformation_async(self, amdCallback, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (), self.proxy.internConnector.getStatusInformation)

    def getResults_async(self, amdCallback, _ctx=None):
        self.dispatcher.add(self.proxy.relay, amdCallback, (), self.proxy.internConnector.getResults)
    
    ## Outgoing
    def onSimulationFinished(self, amiCallback, params):
        self.manager.onSimulationFinished_async(amiCallback, *params)

    def onSimulationError(self, amiCallback, jobId, suffix, error, abort):
        self.manager.onSimulationError_async(amiCallback, jobId, suffix, error, abort)

    def onAlive(self):
        self.createCallback.ice_response(self.simulationPrx)

    def onStartupError(self, error):
        self.createCallback.ice_exception(Comm.Exceptions.StartupException(error))

    def onDead(self):
        if self.dieCallback:
            self.dieCallback.ice_response()
        
