package event;

import java.util.*;
public class DefaultEventHandler implements EventHandler {
	public static final int DEBUG_EVENT=0, STATUS_EVENT=1, WARNING_EVENT=2,
			ERROR_EVENT=3, EXCEPTION_EVENT=4;
	
	private int type;
	private List listener;
	public DefaultEventHandler(int type) {
		listener = new ArrayList();
		this.type = type;
	
		switch(type) {
		case DEBUG_EVENT: SystemEventHandler.register(this, DebugMessageEvent.class); break;
		case WARNING_EVENT: SystemEventHandler.register(this, WarningMessageEvent.class); break;
		case ERROR_EVENT: SystemEventHandler.register(this, ErrorMessageEvent.class); break;
		case STATUS_EVENT: SystemEventHandler.register(this, StatusInformationEvent.class); break;
		case EXCEPTION_EVENT: SystemEventHandler.register(this, UnexpectedExceptionEvent.class); break;
		}
	}
	
	public void addEventListener(Object l) {
		switch(type) {
		case DEBUG_EVENT: if(l instanceof DebugMessageListener) break; else return;
		case STATUS_EVENT: if(l instanceof StatusInformationListener) break; else return;
		case WARNING_EVENT: if(l instanceof WarningMessageListener) break; else return;
		case ERROR_EVENT: if(l instanceof ErrorMessageListener) break; else return;
		case EXCEPTION_EVENT: if(l instanceof UnexpectedExceptionListener) break; else return;
		default: return;
		}
		
		if(!listener.contains(l)) listener.add(l);
	}

	public void removeEventListener(Object l) {
		listener.remove(l);
	}

	public void fireEvent(Object o) {
		Iterator it = listener.iterator();
		
		switch(type) {
		case DEBUG_EVENT: 
			if(o instanceof DebugMessageEvent) {
				while(it.hasNext())
					((DebugMessageListener)it.next()).debugMessageOccured((DebugMessageEvent)o);
			}; 
			return;
		case STATUS_EVENT: 
			if(o instanceof StatusInformationEvent) {
				while(it.hasNext())
					((StatusInformationListener)it.next()).statusInformationOccured((StatusInformationEvent)o);
			}; 
			return;
		case WARNING_EVENT: 
			if(o instanceof WarningMessageEvent) {
				while(it.hasNext())
					((WarningMessageListener)it.next()).warningMessageOccured((WarningMessageEvent)o);
			}; 
			return;
		case ERROR_EVENT: 
			if(o instanceof ErrorMessageEvent) {
				while(it.hasNext())
					((ErrorMessageListener)it.next()).errorMessageOccured((ErrorMessageEvent)o);
			}; 
			return;
		case EXCEPTION_EVENT: 
			if(o instanceof UnexpectedExceptionEvent) {
				while(it.hasNext())
					((UnexpectedExceptionListener)it.next()).unexpectedExceptionOccured((UnexpectedExceptionEvent)o);
			}; 
			return;
		default: return;
		}
	}
}
