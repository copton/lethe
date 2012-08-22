import helper
import externConnector
import internConnector
import osConnector

class AmiCallback:
    def __init__(self, proxy, amdCallback=None):
        self.amdCallback = amdCallback
        self.proxy = proxy

    def ice_response(self, *params):
        if self.amdCallback:
            self.amdCallback.ice_response(*params) 

    def ice_exception(self, ex):
        if self.amdCallback:
            self.amdCallback.ice_exception(ex)
        else:
            self.proxy._log("exception from ami callback: " + str(ex))

class Proxy:
    def __init__(self, jobId, suffix, path, callback, application):
        self.name = "Simulation-%s-%s" % (jobId, suffix)

        self._log("__init__")

        self.jobId = jobId
        self.suffix = suffix
        self.dispatcher = application.getDispatcher()
        self.application = application

        self.killTimeout = int(self.application.getProperty("Config.KillTimeout"))
        self.shutdownTimeout = int(self.application.getProperty("Config.ShutdownTimeout"))
        self.waitpidTimeout = int(self.application.getProperty("Config.WaitpidTimeout"))

        self.spawnCompleted = False
        self.childDidQuit = False
        self.childShallQuit = False
        self.didQuit = False

        self.externConnector = externConnector.ExternConnector(self.name, callback, self, application)
        self.internConnector = internConnector.InternConnector(self.name, self, application)
        
        options = []
        options += [self.jobId, self.suffix]
        options += self.internConnector.getOptions()
        self._log("forking %s %s" % (path, ' '.join(options)))
        (self.pid, fd) = helper.fork(path, options )
        
        self.osConnector = osConnector.OsConnector(fd, self, application)


    def onAlive(self, amdCallback):
        self._log("onAlive")
        if self.didQuit:
            return

        self.externConnector.onAlive()
        self.spawnCompleted = True
        amdCallback.ice_response()

    def die(self, stage):
        self._log("die: stage %d" % stage)
        if self.didQuit:
            return

        if stage == 0:
            self.childShallQuit = True
            self.internConnector.die()
            self.dispatcher.addWithTimeout(self.shutdownTimeout, self.die, 1)
        elif stage == 1:
            helper.kill(self.pid)
            self.dispatcher.addWithTimeout(self.killTimeout, self.die, 2)
        elif stage == 2:
            self._log("could not terminate simulation process (pid == %d)" % self.pid)
            self._quit() 

    def relay(self, amdCallback, params, target):
        self._log(target.__name__)
        if self.didQuit:
            amdCallback.ice_exception("Proxy has quit")
        else:
            target(AmiCallback(self, amdCallback), params) 

    def onPipeMessage(self, message, eof):
        self._log("onMessage")
        if self.didQuit:
            return

        if eof:
            if not self.childShallQuit or True:
                (status, err) = helper.waitpid(self.pid, self.waitpidTimeout)
                if err:
                    error = "child closed stdout and stderr but did not terminate within %d seconds (pid = %d)" % (timeout, self.pid)
                else:
                    error = helper.getReasonForExit(status)

                if not self.spawnCompleted:
                    self.externConnector.onStartupError(error)
                else:
                    self.externConnector.onSimulationError(AmiCallback(self), self.jobId, self.suffix, error, True)

            self.childDidQuit = True
            self._quit()
        else:
            if not self.childShallQuit or True:
                self.externConnector.onSimulationError(AmiCallback(self), self.jobId, self.suffix, message, False)

    def shutdown(self):
        if not self.childDidQuit:
            helper.kill(self.pid)

    def _quit(self):
        if self.didQuit:
            return

        self.didQuit = True

        self.externConnector.quit()
        self.internConnector.quit()
        self.osConnector.quit()
        self.application.getGenerator().onProxyQuit(self)

    def _log(self, text):
        print "Proxy: ", self.name, text
