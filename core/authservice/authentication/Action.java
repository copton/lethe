
package authentication;

import java.util.*;
import org.jdom.Element;

public class Action {
	protected String name, permission, action;
	protected Set onGroups = new HashSet();
	private Element actionElement;
	
	public Action(Element actionElement) {
		permission = actionElement.getParentElement().getAttributeValue("name");
		action = actionElement.getAttributeValue("name");
		name = permission+"."+action;
		this.actionElement = actionElement;
		
		Iterator it = actionElement.getChildren("ongroup").iterator();
		while(it.hasNext()) {
			Element group = (Element)it.next();
			onGroups.add(group.getAttributeValue("name"));
		}
	}
	
	public Action(String permissionName, Comm.AuthenticationService.Action action) {
		permission = permissionName;
		this.action = action.name;
		name = permissionName+"."+action.name;
		actionElement = new Element("action");
		actionElement.setAttribute("name", name);
		
		for(int i=0; i<action.onGroups.length; i++) {
			onGroups.add(action.onGroups[i]);
			Element group = new Element("ongroup");
			group.setAttribute("name", action.onGroups[i]);
			actionElement.addContent(group);
		}
	}
	
	public Comm.AuthenticationService.Action getAction() {
		Comm.AuthenticationService.Action action = new Comm.AuthenticationService.Action();
		action.name = this.action;
		action.onGroups = (String[])onGroups.toArray(new String[0]);
		
		return action;
	}
	
	public Element getElement() {
		return actionElement;
	}
}
