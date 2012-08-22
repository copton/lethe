import threading
import time

def now():
    return int(time.time())

class Dispatcher(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.lock = threading.Condition() 
        self.queue = []
        self.timeQueue = []
        self.waiting = False
        self.quit = False
        self.start()

    def addWithTimeout(self, timeout, function, *params):
        self.lock.acquire()
        self.timeQueue.append((now() + timeout, function, params))
        self.timeQueue.sort()
        if self.waiting:
            self.lock.notify()
        self.lock.release()

    def add(self, function, *params):
        self.lock.acquire()
        if self.waiting:
            self.lock.notify()
        self.queue.append((function, params))
        self.lock.release()

    def shutdown(self):
        self.lock.acquire()
        self.quit = True
        if self.waiting:
            self.lock.notify()
        self.lock.release()
        self.join()

    def run(self):
        def call((function, params)):
            self.lock.release()
            function(*params)
            self.lock.acquire()
            
        self.lock.acquire()

        while not self.quit:
            while len(self.queue) != 0:
                call(self.queue.pop(0))

            while True:
                wait = None
                if len(self.timeQueue) != 0:
                    timeout = self.timeQueue[0][0] - now()
                    if timeout <= 0:
                        time, function, params = self.timeQueue.pop(0)
                        call((function, params))
                        continue
                    else:
                        wait = timeout

                self.waiting = True
                self.lock.wait(wait)
                self.waiting = False
                break
