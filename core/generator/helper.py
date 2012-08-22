import os
import sys
import signal
import time
import traceback

def getTraceback():
    class Target:
        def __init__(self):
            self.error = ""
        def write(self, what):
            self.error += what
        def getError(self):
            return self.error
    target = Target()
    traceback.print_exc(None, target)
    return target.getError()

def fork(path, options):
    r,w = os.pipe()
    pid = os.fork()
    if pid == 0:
        os.dup2(w, sys.stderr.fileno())
        os.dup2(w, sys.stdout.fileno())
        os.close(w) 
        os.close(r)
        sys.stdin.close()
        applname = path[path.rfind('/') + 1 :]
        os.execv(path, [applname] + options)
    else:
        os.close(w)
        return pid, r

def kill(pid):
    os.kill(pid, signal.SIGKILL)

def waitpid(pid, timeout):
    waited = 0
    while 1:
        (pid, status) = os.waitpid(pid, os.WNOHANG)
        if not (pid == 0 and status == 0):
            return (status, False)
        if waited == timeout:
            return (0, True)
        time.sleep(1)
        waited += 1

def getReasonForExit(status):
    def getSignalName(signum):
        entries = dir(signal)
        for entry in entries:
            if entry.find("SIG") == 0:
                if entry.find("SIG_") == -1:
                    if signum == eval("signal.%s" % entry):
                        return entry
        return "unknown signal number %d" % signum

    reason = "child process terminated due to "
    if os.WIFEXITED(status):
        reason += "call to exit() with exit code %d" % os.WEXITSTATUS(status)
    elif os.WIFSIGNALED(status):
        reason += "a signal (%s)." % getSignalName(os.WTERMSIG(status))
        if os.WCOREDUMP(status):
            reason += " Core dumped"
    else:
        reason += "unknown reason"

    return reason
