package server;

import service.SourceService;
import Comm.SourceService.LocalSource;
import Comm.SourceService.ProtectedInterfacePrxHelper;
import Comm.SourceService.SvnSource;
import Ice.LocalObjectImpl;
import Ice.Object;
import Ice.ObjectAdapter;
import Ice.Util;

public class SourceServer extends Ice.Application {

	public int run(String [] args) {
		ObjectAdapter adapter, adapter2;
		String host = communicator().getProperties().getPropertyWithDefault("config.host", "default");
		String port = communicator().getProperties().getPropertyWithDefault("config.port", "10000");
		
		IceObjectFactory factory = new IceObjectFactory();
		communicator().addObjectFactory(factory, "::Comm::SourceService::SvnSource");
		communicator().addObjectFactory(factory, "::Comm::SourceService::LocalSource");
		
		if(args.length > 0 && args[0].matches("-default")) {
			adapter = communicator().createObjectAdapter("SourceAdapter");
			adapter2 = communicator().createObjectAdapterWithEndpoints("ServiceAdapter", "tcp");
		} else {
			adapter = communicator().createObjectAdapterWithEndpoints("SourceAdapter", "tcp -h "+host+" -p "+port);
			adapter2 = communicator().createObjectAdapterWithEndpoints("ServiceAdapter", "tcp -h "+host+" -p default");
		}
	
		SourceService service = new SourceService(communicator());
		adapter.add(service, Util.stringToIdentity("Service"));
		adapter.activate();
		
		Ice.ObjectPrx prx = adapter2.add(new service.ProtectedSourceService(service, communicator()), Util.stringToIdentity("Sources"));
		service.init(ProtectedInterfacePrxHelper.checkedCast(prx));
		service.setAdapter(adapter2);
		adapter2.activate();
		
		System.out.println("SourceServer: Started");
		communicator().waitForShutdown();
		
		System.out.println("SourceServer: Shutdown");
		return 0;
	}
	
	private class IceObjectFactory extends LocalObjectImpl implements Ice.ObjectFactory {
		public Object create(String type) {
			if(type.equals("::Comm::SourceService::SvnSource")) return new SvnSource();
			else if(type.equals("::Comm::SourceService::LocalSource")) return new LocalSource();
			else return null;
		}

		public void destroy() {}
	}

	public static void main(String [] args) {
		SourceServer s = new SourceServer();
		System.exit(s.main("SourceServer", args));
	}
}
