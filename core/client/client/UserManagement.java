package client;

import event.DefaultConsoleEventListener;
import event.DefaultEventHandler;
import event.UnexpectedExceptionEvent;
import util.IceNetworkConnection;
import Comm.AuthenticationService.*;
import Ice.ObjectPrx;

public class UserManagement {

	DefaultEventHandler exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	private UserManagementPrx proxy = null;
	public User getUser(String user) {
		if(!getProxy()) return null;
		try {
			return proxy.getUser(user, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return null;
		}	
	}
	
	public Group getGroup(String group) {
		if(!getProxy()) return null;
		try {
			return proxy.getGroup(group, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return null;
		}	
	}
	
	public User [] getAllUsers() {
		if(!getProxy()) return new User[0];
		try {
			return proxy.getAllUsers(proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return new User[0];
		}
	}
	
	public Group [] getAllGroups() {
		if(!getProxy()) return new Group[0];
		try {
			return proxy.getAllGroups(proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return new Group[0];
		}
	}
	
	public Permission [] getAllPermissions() {
		if(!getProxy()) return new Permission[0];
		try {
			return proxy.getAllPermissions(proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return new Permission[0];
		}
	}
	
	public boolean addUser(User u, String password) {
		if(!getProxy()) return false;
		try {
			proxy.addUser(u, password, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}
		return true;
	}
	
	public boolean addGroup(Group g) {
		if(!getProxy()) return false;
		try {
			proxy.addGroup(g, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}
		return true;
	}
	
	public boolean deleteUser(String user) {
		if(!getProxy()) return false;
		java.util.Map ctx = proxy.ice_communicator().getDefaultContext();
		java.util.Iterator it = ctx.keySet().iterator();
		System.out.println("list keys");
		while(it.hasNext()) {
			String key = (String)it.next();
			System.out.println(key+"="+ctx.get(key));
		}
		System.out.println("keys listed");
		
		try {
			proxy.deleteUser(user, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}
		return true;
	}
	
	public boolean deleteGroup(String group) {
		if(!getProxy()) return false;
		try {
			proxy.deleteGroup(group, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}
		return true;
	}
	
	public boolean setUser(User u) {
		if(!getProxy()) return false;
		try {
			proxy.setUser(u.name, u, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}	
		return true;
	}
	
	public boolean setGroup(Group g) {
		if(!getProxy()) return false;
		try {
			proxy.setGroup(g.name, g, proxy.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}	
		return true;
	}
	
	private boolean getProxy() {
		if(proxy == null) proxy = initProxy();
		return proxy != null;
	}
	
	private UserManagementPrx initProxy() {
		String proxy = "Management@Authentication.AuthenticationAdapter";
		ObjectPrx iceObject = IceNetworkConnection.get().stringToProxy(proxy);
		return UserManagementPrxHelper.checkedCast(iceObject);
	}
	
	private static String getUsage() {
		return "\tjava UserManagement [-u <user> -p <password>] -lu [<user>]\n"+
			"\tjava UserManagement [-u <user> -p <password>] -lg [<group>]\n"+
			"\tjava UserManagement [-u <user> -p <password>] -lp\n"+
			"\tjava UserManagement [-u <user> -p <password>] -au [-x <priority>] -g <group> <user> <password>\n"+
			"\tjava UserManagement [-u <user> -p <password>] -ag [-x <priority>] [-g <supergroup>] <group>\n"+
			"\tjava UserManagement [-u <user> -p <password>] -du <user>\n"+
			"\tjava UserManagement [-u <user> -p <password>] -dg <group>\n"+
			"\tjava UserManagement [-u <user> -p <password>] -su [-x <priority>] -g <group> <user>\n"+
			"\tjava UserManagement [-u <user> -p <password>] -sg [-x <priority>] [-g <supergroup>] <group>";	
	}
	
	public static void main(String [] args) {
		ParameterWrapper wrapper = new ParameterWrapper();
		
		String [] argv = {"-du", "test"};
		args = argv;
		
		if(!wrapper.init(args)) {
			System.out.println("usage: "+getUsage());
			System.exit(1);
		}
		
		UserManagement management = new UserManagement();
		
		DefaultConsoleEventListener debugOut = new DefaultConsoleEventListener();
		management.exceptionHandler.addEventListener(debugOut);
		
		Authentication auth = new Authentication();
		auth.exceptionHandler.addEventListener(debugOut);
		
		boolean authenticate = auth.authenticate(wrapper.loginUser, wrapper.loginPassword);
		if(authenticate == false) {
			System.out.println("authentication failed");
			System.exit(2);
		}
		
		boolean state = true;
		switch(wrapper.command) {
		case LIST_USER: listUser(management.getUser(wrapper.user)); break;
		case LIST_GROUP: listGroup(management.getGroup(wrapper.group)); break;
		case LIST_ALL_USERS: listAllUsers(management.getAllUsers()); break;
		case LIST_ALL_GROUPS: listAllGroups(management.getAllGroups()); break;
		case LIST_ALL_PERMISSIONS: listAllPermissions(management.getAllPermissions()); break;
		case ADD_USER: state = management.addUser(wrapper.u, wrapper.password); break;
		case ADD_GROUP: state = management.addGroup(wrapper.g); break;
		case DELETE_USER: state = management.deleteUser(wrapper.user); break;
		case DELETE_GROUP: state = management.deleteGroup(wrapper.group); break;
		case SET_USER: state = management.setUser(wrapper.u); break;
		case SET_GROUP: state = management.setGroup(wrapper.g); break;
		default: break;
		}
		
		System.exit(state?0:1);
	}
	
	private static void listUser(User u) {
		if(u == null) {
			System.out.println("unknown user");
			return;
		}
		
		System.out.println("user "+u.name+" in group "+u.group);
	
		for(int i=0; i<u.allowed.length; i++) 
			System.out.println("\t+ "+getPermissionString(u.allowed[i], 2));
		
		for(int i=0; i<u.denied.length; i++) 
			System.out.println("\t- "+getPermissionString(u.denied[i], 2));
	}
	
	private static void listGroup(Group g) {
		if( g == null) {
			System.out.println("unknown group");
			return;
		}
		
		System.out.println("group "+g.name+
				(g.superGroup.length() > 0?" extends "+g.superGroup:""));
	
		for(int i=0; i<g.allowed.length; i++) 
			System.out.println("\t+ "+getPermissionString(g.allowed[i], 2));
		
		for(int i=0; i<g.denied.length; i++) 
			System.out.println("\t- "+getPermissionString(g.denied[i], 2));
	}
	
	private static void listAllUsers(User [] users) {
		System.out.println("users:");
		for(int i=0; i<users.length; i++) 
			System.out.println("\t"+users[i].name+" in group "+users[i].group);
	}
	
	private static void listAllGroups(Group [] groups) {
		System.out.println("groups:");
		for(int i=0; i<groups.length; i++) 
			System.out.println("\t"+groups[i].name+
					(groups[i].superGroup.length() > 0?" extends "+groups[i].superGroup:""));
	}
	
	private static void listAllPermissions(Permission [] permissions) {
		System.out.println("permissions:");
		for(int i=0; i<permissions.length; i++)
			System.out.println(getPermissionString(permissions[i], 1));
	}
	
	private static String getPermissionString(Permission p, int indent) {
		StringBuffer str = new StringBuffer(p.name);
		
		if(p.actions.length == 0) System.err.println("0 actions: "+p.name);
		
		for(int i=0; i<p.actions.length; i++) {
			str.append("\n"); for(int l=0; l<indent; l++) str.append("\t");
			str.append(p.actions[i].name);
			
			if(p.actions[i].onGroups.length > 0) {
				str.append(" on groups:");
				for(int j=0; j<p.actions[i].onGroups.length; j++)
					str.append(" "+p.actions[i].onGroups[j]);
			}
		}
		
		return str.toString();
	}
	
	private static final int LIST_USER=0, LIST_GROUP=1, LIST_ALL_USERS=2, 
		LIST_ALL_GROUPS=3, LIST_ALL_PERMISSIONS=4, 
		ADD_USER=5, ADD_GROUP=6,
		DELETE_USER=7, DELETE_GROUP=8,
		SET_USER=9, SET_GROUP=10;
	
	private static class ParameterWrapper {
		int command = -1;
		Group g;
		User u;
		String user, group, permission, password;
		String loginUser, loginPassword;
		int priority = -1;
		
		public ParameterWrapper() {
	 		Ice.Properties properties = IceNetworkConnection.getProperties();
	 		loginUser = properties.getProperty("config.Authentication.user");
	 		loginPassword = properties.getProperty("config.Authentication.passwd");
		}
		
		public boolean init(String [] args) {
			int i=0;
			while(i < args.length) {
				if(args[i].equalsIgnoreCase("-u")) loginUser = args[++i];
				else if(args[i].equalsIgnoreCase("-p")) loginPassword = args[++i];
				else if(args[i].equalsIgnoreCase("-lu")) {
					if(args.length > i+1) {
						command = LIST_USER;
						user = args[++i];
					} else command = LIST_ALL_USERS;
				}else if(args[i].equalsIgnoreCase("-lg")) {
					if(args.length > i+1) {
						command = LIST_GROUP;
						group = args[++i];
					} else command = LIST_ALL_GROUPS;
				}else if(args[i].equalsIgnoreCase("-lp")) {
					command = LIST_ALL_PERMISSIONS;
				}else if(args[i].equalsIgnoreCase("-au")) {
					if(args.length == i+1) return false;
					command = ADD_USER;
				}else if(args[i].equalsIgnoreCase("-ag")) {
					if(args.length == i+1) return false;
					command = ADD_GROUP;
				}else if(args[i].equalsIgnoreCase("-du")) {
					if(args.length == i+1) return false;
					command = DELETE_USER;
					user = args[++i];
				}else if(args[i].equalsIgnoreCase("-dg")) {
					if(args.length == i+1) return false;
					command = DELETE_GROUP;
					group = args[++i];
				}else if(args[i].equalsIgnoreCase("-su")) {
					if(args.length == i+1) return false;
					command = SET_USER;
				}else if(args[i].equalsIgnoreCase("-sg")) {
					if(args.length == i+1) return false;
					command = SET_GROUP;
				}else if(args[i].equalsIgnoreCase("-g")) {
					if(args.length == i+1) return false;
					group = args[++i];
				}else if(args[i].equalsIgnoreCase("-x")) {
					if(args.length == i+1) return false;
					priority = Integer.parseInt(args[++i]);
				}
				else {
					switch(command) {
					case ADD_GROUP:
					case SET_GROUP: {
						g = new Group();
						g.name = args[i];
						g.superGroup = group!=null?group:"";
						g.priority = priority;
						g.allowed = new Permission[0];
						g.denied = new Permission[0];
						break;
					}
					case ADD_USER: {
						if(args.length == i+1) return false;
						u = new User();
						u.name = args[i];
						u.group = group;
						u.priority = priority;
						u.allowed = new Permission[0];
						u.denied = new Permission[0];
						password = args[++i];
						break;
					}
					case SET_USER: {
						u = new User();
						u.name = args[i];
						u.group = group;
						u.priority = priority;
						u.allowed = new Permission[0];
						u.denied = new Permission[0];
						break;
					}
					
					default: return false;
					}
				} 
				
				i++;
			}
			
			if(loginUser == null || loginPassword == null) return false;
			
			switch(command) {
			case LIST_USER: case LIST_GROUP:
			case LIST_ALL_USERS:
			case LIST_ALL_GROUPS:
			case LIST_ALL_PERMISSIONS: return true;
			case ADD_USER: 
			case SET_USER: return u != null && u.group != null;
			case ADD_GROUP:
			case SET_GROUP: return g != null;
			case DELETE_USER: 
			case DELETE_GROUP: return true;
			default: return false;
			}
		}
	}
}
