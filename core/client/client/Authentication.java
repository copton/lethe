package client;

import event.DefaultEventHandler;
import event.UnexpectedExceptionEvent;
import util.IceNetworkConnection;
import Comm.AuthenticationService.*;
import Ice.ObjectPrx;

public class Authentication {

	protected DefaultEventHandler exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	public boolean authenticate(String user, String password) {
	
		PublicInterfacePrx authenticationPrx = initProxy();
		System.out.println("get proxy: "+authenticationPrx);
		if(authenticationPrx == null) return false;
		try {
			String sessionId = authenticationPrx.authenticateUser(user, password);
			
			IceNetworkConnection.addToContext("user", user);
			IceNetworkConnection.addToContext("sessionId", sessionId);
	
			return true;
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}
	}
	
	public void logout() {
		String user = IceNetworkConnection.getProperties().getProperty("user");
		if(user == null || user.length() == 0) return;
		
		PublicInterfacePrx authenticationPrx = initProxy();
		if(authenticationPrx == null) return;
	
		authenticationPrx.logout(user);
	}
	
	private PublicInterfacePrx initProxy() {
		String proxy = "Authentication@Authentication.AuthenticationAdapter";
		ObjectPrx iceObject = IceNetworkConnection.get().stringToProxy(proxy);
		return PublicInterfacePrxHelper.checkedCast(iceObject);
	}
}
