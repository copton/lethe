
package authentication;

import Comm.AuthenticationService._ProtectedInterfaceDisp;
import Ice.Current;

public class PermissionService extends _ProtectedInterfaceDisp {

	private UserManagement management;
	private AuthenticationService service;
	public PermissionService(AuthenticationService service, UserManagement management) {
		this.service = service;
		this.management = management;
	}
	
	public int getPriority(String user, Current __current) {
		return management.getUserObj(user).getPriority();
	}

	public boolean userExists(String user, String sessionId, Current __current) {
		return service.activeUsers.containsKey(sessionId) && user.equals(service.activeUsers.get(sessionId));
	}

	public boolean hasPermission(String userName, String owner, String permission, Current ctx) {
		return management.hasPermission(userName, owner, permission);
	}

	public boolean hasPermissions(String userName, String owner, String[] permissions, Current ctx) {
		for(int i=0; i<permissions.length; i++)
			if(!hasPermission(userName,  owner, permissions[i], ctx)) return false;
		
		return true;
	}
}
