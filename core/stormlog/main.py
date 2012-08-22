#!/usr/bin/python

import sys

import Ice
Ice.loadSlice("-I../slices/ ../slices/logger.ice")

import logger

class Application(Ice.Application):
    def run(self, argv):
        properties = self.communicator().getProperties()
        path = properties.getProperty("Breeze.Path")
        filename = properties.getProperty("Breeze.FileName")
        extension = properties.getProperty("Breeze.FileExtension")
        theLogger = logger.Logger(path, filename, extension)

        publish = logger.Publish(theLogger)
        lastlog = logger.Lastlog(theLogger)

        adapter = self.communicator().createObjectAdapter("BreezeAdapter")
        
        publishId = Ice.stringToIdentity("Publish")
        lastlogId = Ice.stringToIdentity("Lastlog")
        
        adapter.add(publish, publishId)
        adapter.add(lastlog, lastlogId)

        publishPrx = adapter.createDirectProxy(publishId)
        lastlogPrx = adapter.createDirectProxy(lastlogId)
        
        adapter.activate()

        text = "server started\n%s\n%s" % (publishPrx.ice_toString(), lastlogPrx.ice_toString())
        publish.log(text)
        self.communicator().waitForShutdown()
        publish.log("server shutting down")

application = Application()
status = application.main(sys.argv)
sys.exit(status)
