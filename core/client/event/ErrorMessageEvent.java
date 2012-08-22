package event;

public class ErrorMessageEvent {
	public String msg;
	public String className, fileName, methodName;
	public Object thrower;
	
	public ErrorMessageEvent(String msg, Object thrower) {
		this.msg = msg;
		this.thrower = thrower;
		
		StackTraceElement [] st = (new Exception()).getStackTrace();
		className = st[2].getClassName();
		fileName = st[2].getFileName();
		methodName = st[2].getMethodName();
	}
}
