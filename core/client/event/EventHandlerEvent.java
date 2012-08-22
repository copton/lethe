package event;

public class EventHandlerEvent {
	public EventHandler eventHandler;
	public Class eventType; 
	
	public EventHandlerEvent(EventHandler newHandler, Class eventType) {
		eventHandler = newHandler;
		this.eventType = eventType;
	}
}
