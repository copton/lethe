
package authentication;

import org.jdom.Element;

import Comm.AuthenticationService.Permission;

public class Group extends ManageableUnit {
	
	public Group(Element groupElement, UserManagement management) {
		super(groupElement, management);
		group = groupElement.getAttributeValue("supergroup");
	}

	public Group(Comm.AuthenticationService.Group g) {
		name = g.name;
		group = g.superGroup;
		setPriority(g.priority);
		
		userElement = new Element("group");
		userElement.setAttribute("name", name);
		userElement.setAttribute("priority", Integer.toString(g.priority));
		
		if(group.length() > 0)
			userElement.setAttribute("supergroup", group);
		
		allowedActions = parseActions("allow", g.allowed);
		deniedActions = parseActions("deny", g.denied);
	}
	
	public Comm.AuthenticationService.Group get() {
		Comm.AuthenticationService.Group g = new Comm.AuthenticationService.Group();
		g.name = name;
		g.superGroup = group;
		g.priority = getPriority();
	
		java.util.List [] permissions = getPermissions();
		g.allowed = (Permission[])permissions[ALLOWED].toArray(new Permission[0]);
		g.denied = (Permission[])permissions[DENIED].toArray(new Permission[0]);
	
		return g;
	}
	
	public void set(Comm.AuthenticationService.Group g) {
		group = g.superGroup;
		setPriority(g.priority);
		
		allowedActions = parseActions("allow", g.allowed);
		deniedActions = parseActions("deny", g.denied);		
	}
}
