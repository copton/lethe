import threading
import os
from select import select

class Observer(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.lock = threading.Lock()
        self.quit = False
        self.pipes = { }

    def shutdown(self):
        self.lock.acquire()
        self.quit = True
        self.lock.release()
        self.join()
    
    def add(self, osConnector):
        self.lock.acquire()
        fd = osConnector.getFd()
        self.pipes[fd] = (os.fdopen(fd), osConnector.onMessage)
        self.lock.release()

    def remove(self, osConnector):
        self.lock.acquire()
        try:
            del self.pipes[osConnector.getFd()]
        except KeyError:
            pass
        self.lock.release()
    
    def run(self):
        while True:
            self.lock.acquire()
            pipes = self.pipes.copy()
            self.lock.release()

            ready = select(pipes.keys(), [], [], 1)[0]

            self.lock.acquire()
            if self.quit:
                return
            self.lock.release()

            for fd in ready:
                (pipe, callback) = pipes[fd]

                message = pipe.read()
               
                if message == "":
                    callback(eof=True)
                    self.lock.acquire()
                    try:
                        del self.pipes[fd]
                    except KeyError:
                        pass
                    self.lock.release()
                    pipe.close()
                else:
                    callback(message=message)
