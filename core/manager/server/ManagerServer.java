package server;

import manager.Manager;
import Ice.*;

public class ManagerServer extends Application {

	public int run(String[] args) {
		
		try {
			Ice.ObjectAdapter adapter = communicator().createObjectAdapter("ManagerAdapter");
	
			Manager manager = Manager.createManager(communicator());
			adapter.add(manager, Util.stringToIdentity("Manager"));
			adapter.activate();
			
			ObjectAdapter adapter2 = communicator().createObjectAdapter("ServiceAdapter");
			adapter2.add(new manager.ProtectedManager(manager), Util.stringToIdentity("ManagerCallback"));
			adapter2.activate();
			manager.init();
			
			
			System.out.println("started");
			
			communicator().waitForShutdown();
			System.out.println("shutdown");
			manager.shutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}
	


	public static void main(String [] args) {
		ManagerServer s = new ManagerServer();
		int status = s.main("Lethe", args);
		System.exit(status);
	}

}
