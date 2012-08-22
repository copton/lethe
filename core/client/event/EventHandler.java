package event;

public interface EventHandler {
	public void addEventListener(Object l);
	public void removeEventListener(Object l);
		
	public void fireEvent(Object e);
}
