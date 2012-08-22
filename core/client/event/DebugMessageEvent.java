package event;

public class DebugMessageEvent {
	public String msg;
	public int level;
	public String className, fileName, methodName;
	public Object thrower;
	
	public DebugMessageEvent(String msg, Object thrower) {
		this(msg, 1, thrower);
	}
	
	public DebugMessageEvent(String msg, int level, Object thrower) {
		this.msg = msg;
		this.level = level;
		this.thrower = thrower;
		
		StackTraceElement [] st = (new Exception()).getStackTrace();
		className = st[2].getClassName();
		fileName = st[2].getFileName();
		methodName = st[2].getMethodName();
	}
}
