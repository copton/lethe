import time
import sys
import os
import re

import Comm

def now():
    return int(time.time())

def mkday(timestamp=None):
    if timestamp == 0:
        return 0 # localtime(0) -> (1970, 1, 1, 1, 0, 0, 3, 1, 0) ???

    if timestamp == None:
        localtime = [item for item in time.localtime()]
    else:
        localtime = [item for item in time.localtime(timestamp)]
    for i in range(3,9):
        localtime[i] = 0
    return int(time.mktime(localtime))

def dayToString(day):
    date = time.localtime(day)
    return "%.4d-%.2d-%.2d" % (date[0], date[1], date[2])

def stringToDay(daystr):
    date = daystr.split('-')
    localtime = [int(date[0]), int(date[1]), int(date[2]), 0, 0, 0, 0, 0, 0]
    return int(time.mktime(localtime))

typeMap = {
    'ALERT' : Comm.Logger.TypeEnum.ALERT,
    'CRIT' : Comm.Logger.TypeEnum.CRIT,
    'ERR' : Comm.Logger.TypeEnum.ERR,
    'WARNING' : Comm.Logger.TypeEnum.WARNING,
    'NOTICE' : Comm.Logger.TypeEnum.NOTICE,
    'INFO' : Comm.Logger.TypeEnum.INFO,
    'DEBUG' : Comm.Logger.TypeEnum.DEBUG,
}


class Logger:
    def __init__(self, path, filename, extension):
        path = path.strip('"')
        if path[-1] != '/':
            path += '/'
        self.path = path

        filename = filename.strip('"')
        if filename[-1] != '-':
            filename += '-'
        self.filename = filename

        extension = extension.strip('"')
        if extension[0] != '.':
            extension = '.' + extension
        self.extension = extension

        self.today = mkday()

        files = os.listdir(path)
        files.sort() 

        cmp_filename = re.compile("%s([0-9]{4}-[0-9]{2}-[0-9]{2})%s" % (self.filename, self.extension))
        self.logfiles = []
        for filename in files:
            mo = cmp_filename.match(filename)
            if mo:
                day = stringToDay(mo.group(1))
                self.logfiles.append((day, self.path + filename))
        self.logfiles.reverse()

        if len(self.logfiles) != 0 and self.logfiles[0][0] == self.today:
            file = open(self.logfiles[0][1], "r")
            self.messages = self._load(file)
            file.close()
            self.logfile = open(self.logfiles[0][1], "a")
            del self.logfiles[0]
        else:
            filename = "%s/%s%s%s" % (self.path, self.filename, dayToString(self.today), self.extension)
            self.logfile = open(filename, "w")
            self.messages = []

    def _load(self, file):
        messages = []
        while True:
            line = file.readline()
            if line == "":
                break
            line.strip()
            messages.append(self._deserialize(line))
        return messages

    def _serialize(self, message):
        items = [str(message.timestamp), str(message.type), message.origin, message.text]
        return str(items)
        
    def _deserialize(self, line):
        items = eval(line)
        message = Comm.Logger.Message()
        message.timestamp = int(items[0])
        message.type = typeMap[items[1]]
        message.origin = items[2]
        message.text = items[3]
        return message

    def _logrotate(self):
        self.logfiles.insert(0, (self.today, self.logfile.name))
        self.logfile.close()

        self.messages = []         
        self.today = mkday()
        filename = "%s/%s%s%s" % (self.path, self.filename, dayToString(self.today), self.extension)
        self.logfile = open(filename, "w")

    def add(self, message):
        message.timestamp = now()

        if self.today != mkday():
            self._logrotate()

        self.messages.append(message)
        self.logfile.write(self._serialize(message) + '\n')
        self.logfile.flush()


    def get(self, include, exclude, originRegex, textRegex, time_from, time_to):
        def filter(messages):
            cmp_origin = re.compile(originRegex)
            cmp_text = re.compile(textRegex)
            result = []
            for message in messages:
                if not (time_to == 0 or message.timestamp < time_to):
                    break
                if not (time_from == 0 or message.timestamp >= time_from):
                    continue
                if not (include == [] or message.type in include):
                    continue
                if not (exclude == [] or message.type not in exclude):
                    continue
                if not (originRegex == "" or cmp_origin.match(message.origin)):
                    continue
                if not (textRegex == "" or cmp_text.match(message.text)):
                    continue
                result.append(message)
            return result

        if self.today != mkday():
            self._logrotate()

        day_from = mkday(time_from)
        day_to = mkday(time_to)

        if day_from > day_to and day_to != 0:
            return []

        result = []
        if day_to == 0 or day_to == self.today:
            result = filter(self.messages)
        for daystamp, filename in self.logfiles:
            if day_from != 0 and daystamp < day_from:
                break
            if day_to == 0 or daystamp <= day_to:
                file = open(filename, "r")
                messages = self._load(file)
                file.close()
                result = filter(messages) + result
        return result

class Publish(Comm.Logger.Publish):
    def __init__(self, logger):
        Comm.Logger.Publish.__init__(self)
        self.logger = logger

    def log(self, text):
        message = Comm.Logger.Message()
        message.text = text
        message.origin = "Breeze"
        message.type = Comm.Logger.TypeEnum.NOTICE
        self.logger.add(message)
    
    def add(self, message, _ctx=None):
        return self.logger.add(message)

class Lastlog(Comm.Logger.Lastlog):
    def __init__(self, logger):
        Comm.Logger.Lastlog.__init__(self)
        self.logger = logger

    def get(self, include, exclude, originRegex, textRegex, time_from, time_to, _ctx=None):
        return self.logger.get(include, exclude, originRegex, textRegex, time_from, time_to)

