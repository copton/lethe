#!/usr/bin/python

from getopt import getopt
import sys

import Ice
Ice.loadSlice("-I../slices/ ../slices/logger.ice")
import Comm


def getOptions(argv):
    typeMap = {
        'EMERG' : Comm.Logger.TypeEnum.EMERG,
        'ALERT' : Comm.Logger.TypeEnum.ALERT,
        'CRIT' : Comm.Logger.TypeEnum.CRIT,
        'ERR' : Comm.Logger.TypeEnum.ERR,
        'WARNING' : Comm.Logger.TypeEnum.WARNING,
        'NOTICE' : Comm.Logger.TypeEnum.NOTICE,
        'INFO' : Comm.Logger.TypeEnum.INFO,
        'DEBUG' : Comm.Logger.TypeEnum.DEBUG,
    }

    def printHelp():
        text = """
usage: %(progname)s (-p <proxy> | -n <locator>) -m <message> [-t <type>] [-o <origin>] [-h]
-p <proxy> stringified proxy of the publisher servant. Mutual exclusive to -n.
-n <locator> stringified proxy of the locator. Mutual exclusive to -p.
-m <message>: the content of the message
-t: set the type of the message. 
<type> one of %(types)s
default is DEBUG
-o <origin> the origin of the message
-h: show this help and exit
""" 
        d = { 
            'progname' : argv[0],
            'types' : '\n\t\t'.join([""] + typeMap.keys()),
            }
        print text % d

    d = {
        'proxy' : None,
        'locator' : None,
        'text' : "",
        'type' : typeMap['DEBUG'],
        'origin' : "",
        }

    options = getopt(argv[1:], "t:o:m:p:n:h")
    if options[1] != []:
        raise "unrecognized options: " + str(options[1])

    for option in options[0]:
        if option[0] == '-p':
            if d['locator'] != None:
                raise "mutual exclusive options -p and -n given"
            d['proxy'] = option[1]
        elif option[0] == '-n':
            if d['proxy'] != None:
                raise "mutual exclusive options -p and -n given"
            d['locator'] = option[1]
        elif option[0] == '-m':
            d['text'] = option[1]
        elif option[0] == '-t':
            d['type'] = typeMap[option[1]]
        elif option[0] == '-o':
            d['origin'] = option[1]
        elif option[0] == '-h':
            printHelp()
            return None

    if d['text'] == "":
        raise "mandatory option -m not given"

    if d['proxy'] == None and d['locator'] == None:
        raise "either -p or -n must be supplied"

    return d

class Application(Ice.Application):
    def run(self, argv):
        options = getOptions(argv) 
        if options == None:
            return 0

        if options['locator'] != None:
            locatorBase = self.communicator().stringToProxy(options['locator'])
            locator = Ice.LocatorPrx.checkedCast(locatorBase)
            self.communicator().setDefaultLocator(locator)
            proxy = self.communicator().stringToProxy("Publish@Breeze.BreezeAdapter")
        else:
            proxy = self.communicator().stringToProxy(options['proxy'])
        publish = Comm.Logger.PublishPrx.checkedCast(proxy)
        
        message = Comm.Logger.Message()
        message.type = options['type']
        message.origin = options['origin']
        message.text = options['text']
        publish.add(message)

        return 0

app = Application()
status = app.main(sys.argv)
sys.exit(status)

