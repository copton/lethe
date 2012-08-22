
package authentication;

import Comm.AuthenticationService.*;
import Comm.Exceptions.ActionNotAllowed;
import Ice.Current;

import java.io.FileOutputStream;
import java.util.*;
import org.jdom.Element;
import org.jdom.input.*;
import org.jdom.output.*;

public class UserManagement extends _UserManagementDisp {
	
	private Map users = new HashMap();
	private Map groups = new HashMap();
	
	public UserManagement(String fileName) {
		parseFile(fileName);
	}
	
	protected Group getGroupObj(String name) {
		return (Group)groups.get(name);
	}
	
	protected User getUserObj(String name) {
		return (User)users.get(name);
	}
	
	public Comm.AuthenticationService.User getUser(String name, Current __current) {
		return getUserObj(name).get();
	}

	public Comm.AuthenticationService.Group getGroup(String name, Current __current) {
		return getGroupObj(name).get();
	}

	public Comm.AuthenticationService.User[] getAllUsers(Current __current) {
		List allUsers = new ArrayList();
		Iterator it = users.values().iterator();
		while(it.hasNext()) 
			allUsers.add(((User)it.next()).get());
		
		return (Comm.AuthenticationService.User[])allUsers.toArray(new Comm.AuthenticationService.User[0]);
	}

	public Comm.AuthenticationService.Group[] getAllGroups(Current __current) {
		List allGroups = new ArrayList();
		Iterator it = groups.values().iterator();
		while(it.hasNext()) 
			allGroups.add(((Group)it.next()).get());
		
		return (Comm.AuthenticationService.Group[])allGroups.toArray(new Comm.AuthenticationService.Group[0]);
	}

	private Permission [] permissions = null;
	public Comm.AuthenticationService.Permission[] getAllPermissions(Current __current) {
		if(permissions == null) initPermissions();
		return permissions;
	}

	public void addUser(Comm.AuthenticationService.User u, String password, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, null, "userlist.add");
		if(users.containsKey(u.name)) throw new ActionNotAllowed("user already exists");
		
		User user = new User(u, password);
		users.put(user.name, user);
		if(root != null)
			root.getChild("users").addContent(user.userElement);
		else System.out.println("root is null!");
		saveFile();
	}

	public void addGroup(Comm.AuthenticationService.Group g, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, null, "grouplist.add");
		if(groups.containsKey(g.name)) throw new ActionNotAllowed("group already exists");
		
		Group group = new Group(g);
		groups.put(group.name, group);
		root.getChild("groups").addContent(group.userElement);
		saveFile();
	}

	public void setPassword(String user, String password, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, user, "userlist.edit");
		getUserObj(user).setPassword(password);
		saveFile();
	}

	public void deleteUser(String name, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, name, "userlist.remove");
		users.remove(name);
		removeElement(name, root.getChild("users"));
		saveFile();
	}

	public void deleteGroup(String name, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, name, "grouplist.remove");
		if(!groups.containsKey(name)) return;

		removeGroupFromElements(name);
		removeElement(name, root.getChild("groups"));
		groups.remove(name);
		saveFile();
	}

	public void setUser(String name, Comm.AuthenticationService.User u, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, name, "userlist.edit");
		getUserObj(name).set(u);
		saveFile();
	}

	public void setGroup(String name, Comm.AuthenticationService.Group g, Current __current) throws ActionNotAllowed {
		checkPermission(__current.ctx, name, "grouplist.edit");
		getGroupObj(name).set(g);
		saveFile();
	}

	protected boolean hasPermission(String user, String owner, String permission) {
		if(!users.containsKey(owner) || !users.containsKey(user)) return false;
		return getUserObj(user).checkPermission(getUserObj(owner), permission);
	}
	
	private void checkPermission(Map ctx, String owner, String permission) 
	throws Comm.Exceptions.ActionNotAllowed {
		if(ctx == null) {
			System.out.println("ctx is null!");
			throw new ActionNotAllowed("no sessionId");
		}
		String user = (String)ctx.get("user");
		if(owner == null) owner = user;
		if(user == null || !hasPermission(user, owner, permission)) 
			throw new ActionNotAllowed(permission);
	}
	
	private Element root;
	private String fileName = null;
	private void parseFile(String fileName) {
		try {
		    SAXBuilder builder = new SAXBuilder(true);
			builder.setValidation(true);
			builder.setFeature("http://apache.org/xml/features/validation/schema", true);
			root = builder.build(fileName).getRootElement();
			if(root == null) 
				System.out.println("loaded file, but root is null!");
		} catch (Exception e) {
			System.out.println("Show Error: "+e.getMessage());
			return;
		}
		
		this.fileName = fileName;
		Iterator it = root.getChild("users").getChildren().iterator();
		while(it.hasNext()) {
			User u = new User((Element)it.next(), this);
			users.put(u.name, u);
		}
		
		it = root.getChild("groups").getChildren().iterator();
		while(it.hasNext()) {
			Group g = new Group((Element)it.next(), this);
			groups.put(g.name, g);
		}
	}
	
	private void removeElement(String name, Element element) {
		Iterator it = element.getChildren().iterator();
		Element childElement = null;
		while(it.hasNext()) {
			Element child = (Element)it.next();
			if(child.getAttributeValue("name").equals(name)) {
				childElement = child;
				break;
			}
		}
		if(childElement != null)
			element.removeContent(childElement);
		else System.out.println("didn't find element "+name+" in "+element.getName());
	}
	
	private void saveFile() {
		if(fileName != null) {
			XMLOutputter outputter = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
			try {
				outputter.output(root, new FileOutputStream(new java.io.File(fileName)));	
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void removeGroupFromElements(String groupName) {
		Group group = getGroupObj(groupName);

		Iterator it = root.getChild("users").getChildren().iterator();
		while(it.hasNext()) {
			Element userElement = (Element)it.next();
			if(userElement.getAttributeValue("group").equals(groupName)) {
				getUserObj(userElement.getAttributeValue("name")).group = group.group;
				userElement.setAttribute("group", group.group);
			}
		}

		it = root.getChild("groups").getChildren().iterator();
		while(it.hasNext()) {
			Element userElement = (Element)it.next();
			if(userElement.getAttributeValue("supergroup") == null) continue;
			else if(userElement.getAttributeValue("supergroup").equals(groupName)) {
				getGroupObj(userElement.getAttributeValue("name")).group = group.group;
				userElement.setAttribute("supergroup", group.group);
			}
		}
	}
	
	private void initPermissions() {
		List permissionList = root.getChild("permissions").getChildren();
		permissions = new Permission[permissionList.size()];
		
		int i=0;
		Iterator it = permissionList.iterator();
		while(it.hasNext()) {
			Element permission = (Element)it.next();
	
			List actionList = new ArrayList();
			Iterator it2 = permission.getChildren().iterator();
			while(it2.hasNext()) 
				actionList.add(new Comm.AuthenticationService.Action(((Element)it2.next()).getAttributeValue("name"), new String[0]));
		
				permissions[i++] = new Permission(permission.getAttributeValue("name"), 
					(Comm.AuthenticationService.Action [])actionList.toArray(new Comm.AuthenticationService.Action[0]));
		}
	}
}
