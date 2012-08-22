import service.PatchServer;
import Ice.Application;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Util;

public class SourceServer extends Application {

	public int run(String[] arg0) {
		ObjectAdapter adapter = communicator().createObjectAdapterWithEndpoints("IcePatch2", "tcp");
		try {
			ObjectPrx proxy = adapter.add(new PatchServer("/Users/phia/akt_source/simmit/trunk/code/core/sourceservice"),
					Util.stringToIdentity("phia1/server")); // identity = jobId
			System.out.println(proxy);
			adapter.activate();
			System.out.println("started");
			communicator().waitForShutdown();
		} catch(Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	public static void main(String [] args) {
		SourceServer s = new SourceServer();
		System.exit(s.main("Test", args));
	}
}
