import exceptions
import threading
import os

import Ice
import Comm
import proxy
import codegen
import helper

class Generator(Comm.Generator.Interface):
    def __init__(self, application, debug):
        self.proxies = []
        self.debug = debug

        self.application = application
        self.id = Ice.stringToIdentity("Generator")
        self.adapter = application.getExternAdapter()
        self.adapter.add(self, self.id)
        if debug:
            prx = self.adapter.createDirectProxy(self.id)
            print prx.ice_toString()

    def createSimulation_async(self, callback, job, suffix, buildOnly, _ctx=None):
        try:
            print "Generator: createSimulation"
            (path, exception) = self._createSimulationExecutable(job)
            if exception:
                callback.ice_exception(exception)
                return

            if buildOnly or self.debug:
                callback.ice_response(None)
                return
            else:
                self.proxies.append(proxy.Proxy(job.jobId, suffix, path, callback, self.application))
        except exceptions.Exception, e:
            error =  helper.getTraceback() + str(e)
            print error
            callback.ice_exception(Comm.Exceptions.SpecException(error))

    def shutdown(self):
        for prx in self.proxies:
            prx.shutdown()

    def onProxyQuit(self, proxy):
        self.proxies.remove(proxy) 

    def _createSimulationExecutable(self, job):
        print "Generator: _createSimulationExecutable"
        if self.debug:
            cwd = os.getenv("PWD")
        else:
            cwd = os.getenv("PWD")+self.application.getProperty("Config.WorkingDirectory")
        buildPath = cwd + "/../simulation"

        # TODO get sources from Source Services

        preparedJob = codegen.prepareJob(job)
        configMak = open(buildPath + "/gen/config.mak", "w")
        codegen.generateConfigMak(preparedJob, configMak)
        configMak.close()

        main = open(buildPath + "/gen/main.cpp", "w")
        codegen.generateMain(preparedJob, main)
        main.close()

        if self.debug:
            return (None, None)

        makeCmd = self.application.getProperty("Config.MakeCmd")
        if makeCmd == "":
            makeCmd = "make"

        command = "cd %s; %s 2>&1" % (buildPath, makeCmd)
        print "Generator: make", command
        make = os.popen(command, "r")
        error = make.read()
        errorCode = make.close()
        if errorCode != None:
	    print error
            return (None, Comm.Exceptions.CompileError(error))

        command = "cd %s; %s link 2>&1" % (buildPath, makeCmd)
        print "Generator: link", command
        make = os.popen(command, "r")
        error = make.read()
        errorCode = make.close()
        if errorCode != None:
            return (None, Comm.Exceptions.LinkError(error))

        print "Generator: _createSimulationExecutable: return"
        return ("%s/link/simulation" % buildPath, None)

