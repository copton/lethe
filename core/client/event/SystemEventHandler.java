package event;

import java.util.*;
public class SystemEventHandler implements EventHandler {
	List eventHandler;
	Map knownHandler;
	private static SystemEventHandler handler = new SystemEventHandler();
	
	private SystemEventHandler() {
		eventHandler = new ArrayList();
		knownHandler = new HashMap();
	}
	
	public static void register(EventHandler eventHandler, Class type) {
		List known = (List)handler.knownHandler.get(type);
		if(known == null) known = new ArrayList();
		if(!known.contains(eventHandler)) {
			known.add(eventHandler);
			handler.knownHandler.put(type, known);
			handler.fireEvent(new EventHandlerEvent(eventHandler, type));
		}
	}

	public static void addListener(EventHandlerListener l) {
		handler.addEventListener(l);
		Iterator it = handler.knownHandler.keySet().iterator();
		while(it.hasNext()) {
			Class eventType = (Class)it.next();
			List handlerList = (List)handler.knownHandler.get(eventType);
			Iterator it2 = handlerList.iterator();
			while(it2.hasNext()) 
				l.eventHandlerRegistered(new EventHandlerEvent((EventHandler)it2.next(), eventType));
		}
	}
	
	public void fireEvent(Object event) {
		Iterator it = handler.eventHandler.iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof EventHandlerListener)
				((EventHandlerListener)o).eventHandlerRegistered((EventHandlerEvent)event);
		}
	}

	public void addEventListener(Object l) {
		if(!(l instanceof EventHandlerListener)) return;
		if(!eventHandler.contains(l)) eventHandler.add(l);
	}

	public void removeEventListener(Object l) {
		if(!(l instanceof EventHandlerListener)) return;
		if(eventHandler.contains(l)) eventHandler.remove(l);		
	}
}
