#!/usr/bin/python

from getopt import getopt
import sys
import time

import Ice
Ice.loadSlice("-I../slices/ ../slices/logger.ice")
import Comm

def now():
    return int(time.time())

def mkday(days=0):
    localtime = [item for item in time.localtime()]
    for i in range(3,9):
        localtime[i] = 0
    localtime[2] -= days
    return int(time.mktime(localtime))

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


def getOptions(argv):
    def printHelp():
        text = """
usage: %(progname)s (-p <proxy> | -n <locator>) [-i <type>[,<type>[,...]]] [-e <type>[,<type>[,...]]] [-o <regex>]
[-m <regex>] [-b <days> | -c <timestamp>] [-s <days> | -d <timestamp>] [-f <seconds>] [-h]
-p <proxy> stringified proxy of the lastlog servant. Mutual exclusive to -n.
-n <locator> stringified proxy of the locator. Mutual exclusive to -p.
-i include messages of given type. Default is to include all.
-e exclude messages of given type. Default is to exclude none.
<type>: one of %(types)s.
-o include message if regex matches the origin. Default is match all origins.
-m include message if regex matches the content. Default is match all messages.
<regex>: regular expression in perl syntax.
-b include messages which arrived the latest <days> days back. Default is one day.
-s do not include messages within the last <days> days. Default is zero days.
<days> number of days relative to local system time.
-c include messages which arrived after the given timestamp. Mutual exclusive to -b.
-d include messages which arrived before the given timestamp. Mutual exclusive to -s.
<timestamp> number of seconds since the Epoch.
-f follow mode. Poll new messages every <seconds> seconds. Overwrites -s and -d.
-h: show this help and exit
""" 
        d = { 
            'progname' : argv[0],
            'types' : '\n\t\t'.join([""] + typeMap.keys()),
            }
        print text % d

    d = {
        'proxy'    : None,
        'locator'  : None,
        'include'  : [],
        'exclude'  : [],
        'origin'   : "",
        'text'     : "",
        'timeFrom' : mkday(),
        'timeTo'   : 0,
        'follow'   : 0,
        }

    options = getopt(argv[1:], "p:n:i:e:o:m:b:s:c:d:f:h")
    if options[1] != []:
        raise "unrecognized options: " + str(options[1])

    timeFrom, timeTo = False, False

    for option in options[0]:
        if option[0] == '-p':
            if d['locator'] != None:
                raise "mutual exclusive options -p and -n given"
            d['proxy'] = option[1]
        elif option[0] == '-n':
            if d['proxy'] != None:
                raise "mutual exclusive options -p and -n given"
            d['locator'] = option[1]
        elif option[0] == '-i':
            d['include'] = [typeMap[type] for type in option[1].split(',')]
        elif option[0] == '-e':
            d['exclude'] = [typeMap[type] for type in option[1].split(',')]
        elif option[0] == '-o':
            d['origin'] = option[1]
        elif option[0] == '-m':
            d['text'] = option[1]
        elif option[0] in ['-b', '-c']: 
            if timeFrom:
                raise "mutual exclusive options -b and -c given"
            else:
                timeFrom = True
            if option[0] == '-b':
                d['timeFrom'] = mkday(int(option[1]))
            else:
                d['timeFrom'] = int(option[1])
        elif option[0] in ['-s', '-d']:
            if timeTo:
                raise "mutual exclusive options -s and -d given"
            else:
                timeTo = True
            if option[0] == '-s':
                d['timeTo'] = mkday(int(option[1]) - 1)
            else:
                d['timeTo'] = int(option[1])
        elif option[0] == '-f':
            d['follow'] = int(option[1])
        elif option[0] == '-h':
            printHelp()
            return None

    if d['proxy'] == None and d['locator'] == None:
        raise "either -p or -n must be supplied"

    return d

def printMessages(messages):
    def printType(type):
        for k,v in typeMap.items():
            if v == type:
                return k
        return "unknown"

    for message in messages:
        print "Date:", time.ctime(message.timestamp)
        print "Type:", printType(message.type)
        print "Origin:", message.origin
        print "Text: \n" + message.text
        print "-------------------------"

class Application(Ice.Application):
    def run(self, argv):
        options = getOptions(argv) 
        if options == None:
            return 0
        
        self.options = options
        if options['locator'] != None:
            locatorBase = self.communicator().stringToProxy(options['locator'])
            locator = Ice.LocatorPrx.checkedCast(locatorBase)
            self.communicator().setDefaultLocator(locator)
            options['proxy'] = "Lastlog@Breeze.BreezeAdapter"

        proxy = self.communicator().stringToProxy(self.options['proxy'])
        lastlog = Comm.Logger.LastlogPrx.checkedCast(proxy)

        if options['follow'] == 0:
            messages = lastlog.get(options['include'], options['exclude'], options['origin'], options['text'], options['timeFrom'], options['timeTo'])
            printMessages(messages)
        else:
            timeTo = now()
            timeFrom = mkday()
            while True:
                messages = lastlog.get(options['include'], options['exclude'], options['origin'], options['text'], timeFrom, timeTo)
                printMessages(messages)
                time.sleep(options['follow'])
                timeFrom = timeTo
                timeTo = now()

            


        return 0

app = Application()
status = app.main(sys.argv)
sys.exit(status)

