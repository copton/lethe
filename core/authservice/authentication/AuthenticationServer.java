package authentication;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Util;

public class AuthenticationServer extends Application {

	public int run(String[] args) {
		try {
		 	// add services
			String configFile = communicator().getProperties().getPropertyWithDefault("Configuration.File", "authentication.config");
             UserManagement management = new UserManagement(configFile);
             AuthenticationService auth = new AuthenticationService(management);
         
         	ObjectAdapter adapter = communicator().createObjectAdapter("AuthenticationAdapter");
             adapter.add(auth, Util.stringToIdentity("Authentication"));
             adapter.add(management, Util.stringToIdentity("Management"));
             adapter.activate();   

			ObjectAdapter adapter2 = communicator().createObjectAdapter("ServiceAdapter");
			adapter2.add(new PermissionService(auth, management), Util.stringToIdentity("Permission"));
			adapter2.activate();
            	
			System.out.println("Authentication: started");
			communicator().waitForShutdown();
			System.out.println("Authentication: shutdown");
			// shutdown manager -> close freezeDb
		} catch (Exception e) {
			//System.out.println(e);
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	public static void main(String [] args) {
		AuthenticationServer s = new AuthenticationServer();
		int status = s.main("Lethe", args);
		System.exit(status);
	}

}
