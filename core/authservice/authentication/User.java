
package authentication;

import org.jdom.Element;
import Comm.AuthenticationService.Permission;

public class User extends ManageableUnit {

	private String passwordString;
	protected byte[] sessionId;
	
	public User(Element userElement, UserManagement management) {
		super(userElement, management);
		group = userElement.getAttributeValue("group");
		passwordString = userElement.getAttributeValue("password");
	}

	public User(Comm.AuthenticationService.User u, String password) {
		name = u.name;
		group = u.group;
		passwordString = bytes2string(hashPassword(password));
		
		userElement = new Element("user");
		setPriority(u.priority);
		
		userElement.setAttribute("name", name);
		userElement.setAttribute("group", group);
		userElement.setAttribute("password", passwordString);
		userElement.setAttribute("priority", Integer.toString(getPriority()));
	
		allowedActions = parseActions("allow", u.allowed);
		deniedActions = parseActions("deny", u.denied);
	}
	
	public boolean isPasswordCorrect(String password) {
		return passwordString.equalsIgnoreCase(bytes2string(hashPassword(password)));
	}
	
	protected void setPassword(String password) {
		this.passwordString = password;
	}
	
	public boolean checkPermission(String permission) {
		return hasPermission(permission);
	}
	
	public boolean checkPermission(User owner, String permission) {
		if(hasPermission("admin.ignorePermission")) return true;
		if(owner == null) System.out.println("owner is null");
		else if(name == null) System.out.println("name is null");
		if(owner.name.equals(name) || hasPermission("admin.ignoreUser")) 
			return hasPermission(permission);
		else return hasPermission(owner.group, permission);
	}
	
	public Comm.AuthenticationService.User get() {
		Comm.AuthenticationService.User user = new Comm.AuthenticationService.User();
		user.name = name;
		user.group = group;
		user.priority = getPriority();
		
		java.util.List [] permissions = getPermissions();
		user.allowed = (Permission[])permissions[ALLOWED].toArray(new Permission[0]);
		user.denied = (Permission[])permissions[DENIED].toArray(new Permission[0]);
	
		return user;
	}
	
	public void set(Comm.AuthenticationService.User u) {
		setPriority(u.priority);
		group = u.group;
		
		allowedActions = parseActions("allow", u.allowed);
		deniedActions = parseActions("deny", u.denied);
	}

	private byte[] hashPassword(String password) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            return md.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
       		return password.getBytes();
		 }
	}

	private String bytes2string(byte [] byteList) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<byteList.length; i++) {
			int aktByte = (byteList[i]+256)%256; 
			int high=aktByte/16;
			int low=aktByte%16;

			sb.append((char)(high<10?'0'+high:'A'+(high-10)));
			sb.append((char)(low<10?'0'+low:'A'+(low-10)));
		}
		return sb.toString();
	}
}
