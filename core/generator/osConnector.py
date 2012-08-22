class OsConnector:
    def __init__(self, fd, proxy, application):
        self.fd = fd
        self.proxy = proxy
        self.dispatcher = application.getDispatcher()
        self.observer = application.getObserver()

        self.observer.add(self)

    def getFd(self):
        return self.fd

    def quit(self):
        self.observer.remove(self)

    def onMessage(self, message=None, eof=False):
        self.dispatcher.add(self.proxy.onPipeMessage, message, eof)
