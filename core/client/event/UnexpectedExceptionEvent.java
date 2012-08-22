package event;

public class UnexpectedExceptionEvent {
	public Exception e;
	public String fileName, className, methodName;
	public int lineNumber;
	public boolean recoverable;
	
	public UnexpectedExceptionEvent(Exception e) {
		this(e, true);
	}
	
	public UnexpectedExceptionEvent(Exception e, boolean recoverable) {
		this.e = e;
		this.recoverable = recoverable;
		
		StackTraceElement[] st = e.getStackTrace();
		className = st[0].getClassName();
		fileName = st[0].getFileName();
		methodName = st[0].getMethodName();
		lineNumber = st[0].getLineNumber();
	}
}
