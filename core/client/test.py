#!/usr/bin/python

import Ice
Ice.loadSlice("-I../slices ../slices/simulation.ice --all")
import Comm

import time
import os
import sys

class Test:
    def __init__(self, base):
        self.base = base

    def connect(self):
        f = os.popen("./debug start", "r")
        lines = f.read()
        exitCode = f.close()
        if exitCode != None:
            print lines
            sys.exit(1)

        self.simulation = Comm.Simulation.InterfacePrx.checkedCast(self.base)
        assert (self.simulation)
        print "connected"

    def sleep(self, seconds):
        print "sleeping for", seconds, "seconds"
        time.sleep(seconds)

    def start(self, round):
        self.simulation.start(round)
        print "simulation started"

    def suspend(self):
        (checkpoint, job) = self.simulation.suspend()
        print "simulation suspended"
        return checkpoint

    def resume(self, checkpoint):
        self.simulation.resume(checkpoint)
        print "simulation resumed"
    
    def getStatusInformation(self):
        status = self.simulation.getStatusInformation()
        print status
        return status

    def stop(self):
        self.simulation.stop()
        print "simulation stopped"

    def _continue(self):
        self.simulation._continue()
        print "simulation continued"

    def die(self):
        self.simulation.die()
        print "killed simulation"

    def waitForCompletion(self):
        print "waiting for completion"
        while True:
            status =  self.simulation.getStatusInformation()
            if status.theState == Comm.Simulation.State.FINISHED:
                break
            print status.progress
            time.sleep(1)
        self.die()
            

    def testCase1(self):
        """poll status information until finished"""
        self.connect()
        self.start(0)
        while True:
            status = self.getStatusInformation()
            if status.theState == Comm.Simulation.State.FINISHED:
                break
            self.sleep(1)
        
        self.die()

    def testCase2(self):
        """stop and continue frequently"""
        self.connect()
        self.start(0)
        while True:
            try:
                self.stop()
            except Comm.Exceptions.ActionNotAllowed:
                break
            self.sleep(1)
            self._continue()
            self.sleep(1)
        status = self.getStatusInformation()
        assert status.theState == Comm.Simulation.State.FINISHED
        self.die()

    def testCase3(self):
        """suspend and continue"""
        self.connect()
        self.start(0)
        self.sleep(1)
        checkpoint = self.suspend()
        self.sleep(1)
        self._continue()
        self.waitForCompletion()

    def testCase4(self):
        """suspend and resume"""
        self.connect()
        self.start(0)
        self.sleep(1)
        checkpoint = self.suspend()
        self.sleep(1)
        self.resume(checkpoint)
        self.waitForCompletion()

    def testCase5(self):
        """suspend, restart and resume"""
        self.connect() 
        self.start(0)
        self.sleep(1)
        checkpoint = self.suspend()
        self.die()
        self.connect()
        self.resume(checkpoint)
        self.waitForCompletion()

    def testCase6(self):
        """suspend, restart, start, interrupt and resume"""
        self.connect() 
        self.start(0)
        self.sleep(1)
        checkpoint = self.suspend()
        self.die()
        self.connect()
        self.start(0)
        self.sleep(1)
        self.stop()
        self.resume(checkpoint)
        self.waitForCompletion()

class Application(Ice.Application):
    def run(self, argv):
        base = self.communicator().stringToProxy("Simulation-JOBID-SUFFIX -t:tcp -h localhost -p 9999")

        test = Test(base)
        testCase = getattr(test, "testCase%d" % int(argv[1]))
        print testCase.__doc__
        testCase()
        
        return 0

if len(sys.argv) == 1 or sys.argv[1] == "-h":
    print "usage: %s <testNumber>"
    import re
    pattern = re.compile("testCase([0-9]+)")
    for item in dir(Test):
        mo = pattern.match(item)
        if mo:
            function = getattr(Test, item)        
            print mo.group(1), function.__doc__
    sys.exit(0)
        
sys.exit(Application().main(sys.argv))
