
package authentication;

import java.util.*;
import org.jdom.Element;
import Comm.AuthenticationService.Permission;

abstract public class ManageableUnit {
	protected String name;
	protected String group;
	private int priorityValue = -1;
	
	Map allowedActions, deniedActions;
	
	protected Element userElement;
	private UserManagement management;
	
	protected static final int ALLOWED=0, DENIED=1;
	protected ManageableUnit() {}
	
	public ManageableUnit(Element element, UserManagement management) {
		userElement = element;
		name = element.getAttributeValue("name");
		if(element.getAttribute("priority") != null)
			priorityValue = Integer.parseInt(element.getAttributeValue("priority"));
		
		allowedActions = parseActions(userElement.getChild("allow"));
		deniedActions = parseActions(userElement.getChild("deny"));

		this.management = management;
	}
	
	public int getPriority() {
		if(priorityValue >= 0) return priorityValue;
		else if(this.group != null) {
			Group group = management.getGroupObj(this.group);
			if(group != null) return group.getPriority();
		}
		return 1;
	}
	
	protected void setPriority(int priority) {
		priorityValue = priority;
		
		if(priority < 0) userElement.removeAttribute("priority");
		else if(userElement == null) System.out.println("userElement is null!");
		else userElement.setAttribute("priority", Integer.toString(priority));
		System.out.println("set priority to "+priority);
	}
	
	protected Map parseActions(Element permissions) {
		Map actions = new HashMap();
		if(permissions == null) return actions;
		
		Iterator it = permissions.getChildren("permission").iterator();
		while(it.hasNext()) {
			Element permission = (Element)it.next();
			List actionList = permission.getChildren("action");
			if(actionList == null || actionList.size() == 0)
				actionList = findPermission(permission.getAttributeValue("name"), permission).getChildren("action");
	
			Iterator it2 = actionList.iterator();
			while(it2.hasNext()) {
				Element actionElement = (Element)it2.next();
				Action action = new Action(actionElement);
				actions.put(action.name, action);
			}
		}
		
		return actions;
	}
	
	protected Map parseActions(String baseName, Permission [] permissions) {
		Map actions = new HashMap();
		userElement.removeChild(baseName);
		if(permissions.length == 0) return actions;
	
		Element baseElement = new Element(baseName);
		for(int i=0; i<permissions.length; i++) {
			Element permissionElement = new Element("permission");
			permissionElement.setAttribute("name", permissions[i].name);
			
			for(int j=0; j<permissions[i].actions.length; j++) {
				Action a = new Action(permissions[i].name, permissions[i].actions[j]);
				actions.put(a.name, a);
				permissionElement.addContent(a.getElement());
			}
			baseElement.addContent(permissionElement);
		}
		
		userElement.addContent(baseElement);
		return actions;
	}
	
	protected List [] getPermissions() {
		List [] lists = new List[2];
		
		Map [] permissions = getPermissionMap();
		lists[ALLOWED] = actions2permissions(permissions[ALLOWED]);
		lists[DENIED] = actions2permissions(permissions[DENIED]);
		
		return lists;
	}
	
	protected Map[] getPermissionMap() {
		Map [] permissionList = new Map[2];
		
		permissionList[ALLOWED] = getPermissionMap(ALLOWED);
		permissionList[DENIED] = getPermissionMap(DENIED);
		
		
		Group parent = management.getGroupObj(group);
		if(parent != null) {
			Map [] parentPermissions = parent.getPermissionMap();
			Iterator it = permissionList[ALLOWED].keySet().iterator();
			while(it.hasNext()) 
				parentPermissions[DENIED].remove(it.next());
			
			it = permissionList[DENIED].keySet().iterator();
			while(it.hasNext())
				parentPermissions[ALLOWED].remove(it.next());
			
			permissionList[ALLOWED].putAll(parentPermissions[ALLOWED]);
			permissionList[DENIED].putAll(parentPermissions[DENIED]);
		}
		
		return permissionList;
	}
	
	private Map getPermissionMap(int type) {
		Map map = new HashMap();
		Map actions = null;
		switch(type) {
		case ALLOWED: actions = allowedActions; break;
		case DENIED: actions = deniedActions; break;
		default: return map;
		}
		
		Iterator it = actions.keySet().iterator();
		while(it.hasNext()) {
			String actionName = (String)it.next();
			map.put(actionName, actions.get(actionName));
		}
		return map;
	}
	
	private List actions2permissions(Map actions) {
		Iterator it = actions.keySet().iterator();	
		Map permissionMap = new HashMap();
		
		while(it.hasNext()) {
			String permissionName = (String)it.next();
			Action action = (Action)actions.get(permissionName);
			
			Permission p = (Permission)permissionMap.get(action.permission);
			if(p == null) {
				p = new Permission();
				p.name = action.permission;
				p.actions = new Comm.AuthenticationService.Action[1];
				p.actions[0] = action.getAction();
				permissionMap.put(p.name, p);
			} else {
				List actionList = new ArrayList(java.util.Arrays.asList(p.actions));
				actionList.add(action.getAction());
				p.actions = (Comm.AuthenticationService.Action[])actionList.toArray(p.actions);
			}
		}
		
		return new ArrayList(permissionMap.values());
	}
	
	private Element findPermission(String name, Element element) {
		Iterator it = element.getDocument().getRootElement().
				getChild("permissions").getChildren().iterator();
		while(it.hasNext()) {
			Element permission = (Element)it.next();
			if(permission.getAttributeValue("name").equals(name)) return permission;
		}
		return null; // shouldn't happen!
	}
	
	protected boolean hasPermission(String permission) {
		if(deniedActions.containsKey(permission)) return false;
		else if(allowedActions.containsKey(permission)) return true;
		
		Group group = management.getGroupObj(this.group);
		if(group != null) return group.hasPermission(permission);
		else return false;
	}
	
	protected boolean hasPermission(String groupName, String permission) {	
		ManageableUnit unit = this;
		do {
			Group group = management.getGroupObj(groupName);	
			while(group != null) {
				if(unit.deniedActions.containsKey(permission)) {
					Action action = (Action)unit.deniedActions.get(permission);
					if(action.onGroups.contains(group.name)) return false;
				}
				if(unit.allowedActions.containsKey(permission)) {
					Action action = (Action)unit.allowedActions.get(permission);
					if(action.onGroups.contains(group.name)) return true;
				}
				group = management.getGroupObj(group.group);
			}
		} while(unit != null);
		
		return false;
	}
}
