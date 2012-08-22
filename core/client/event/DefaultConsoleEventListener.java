package event;

public class DefaultConsoleEventListener implements DebugMessageListener, ErrorMessageListener,
StatusInformationListener, UnexpectedExceptionListener, WarningMessageListener {

	public void debugMessageOccured(DebugMessageEvent e) {
		System.out.println("[DEBUG: "+e.className+"."+e.methodName+"] "+e.msg);
	}

	public void errorMessageOccured(ErrorMessageEvent e) {
		System.out.println("[ERROR: "+e.className+"."+e.methodName+"] "+e.msg);
	}

	public void statusInformationOccured(StatusInformationEvent e) {
	//	System.out.println("[DEBUG: "+e.className+"."+e.methodName+"] "+e.msg);
	}

	public void unexpectedExceptionOccured(UnexpectedExceptionEvent e) {
		e.e.printStackTrace();
	}

	public void warningMessageOccured(WarningMessageEvent e) {
		System.out.println("[WARNING: "+e.className+"."+e.methodName+"] "+e.msg);
	}

}
