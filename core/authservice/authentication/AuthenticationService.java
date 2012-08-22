
package authentication;

import Comm.AuthenticationService._PublicInterfaceDisp;
import Comm.Exceptions.AuthenticationFailedException;
import Ice.Current;
import java.util.*;

public class AuthenticationService extends _PublicInterfaceDisp {

	private UserManagement management;
	public AuthenticationService(UserManagement management) {
		this.management = management;
	}
	
	protected HashMap activeUsers = new HashMap();
	public String authenticateUser(String userName, String password, Current ctx) throws AuthenticationFailedException {
		User user = management.getUserObj(userName);
		if(!user.isPasswordCorrect(password)) {
			System.out.println("password is incorrect");
			AuthenticationFailedException e = new AuthenticationFailedException();
			e.user = userName;
			e.reason = "wrong password";
			throw e;
		}
			
		String sessionId = new String(generateToken());
		activeUsers.put(sessionId, userName);
		
		return sessionId;
	}

	public void logout(String user, Current current) {
		String sessionId = (String)current.ctx.get("sessionId");
		if(sessionId.equals(activeUsers.get(sessionId)))
			activeUsers.remove(sessionId);
	}
	
	private static final int tokenLength = 32;
	private byte[] generateToken() {
		byte[] token = new byte[tokenLength];
		for(int i=0; i<tokenLength; i++)
			token[i] = (byte)(Math.random()*256);
			
		return token;
	}
}
