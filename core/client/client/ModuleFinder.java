package client;

import Comm.SourceService.*;
import Ice.ObjectPrx;
import Ice.Properties;
import util.IceNetworkConnection;
import xml.XmlHelper;

import java.util.*;

public class ModuleFinder {

	private PublicInterfacePrx proxy;
	private Properties properties;
	
	public void connect(String stringifiedProxy) {
		ObjectPrx prx = IceNetworkConnection.get().stringToProxy(stringifiedProxy);
		proxy = PublicInterfacePrxHelper.checkedCast(prx);
	}
	
	public Map getAvailables(SourceDesc source) {
		Map availables = new HashMap();
		
		String [] list = proxy.getAvailableModules(source);
		List modules = new ArrayList();
		List types = new ArrayList();
	
		for(int i=0; i<list.length; i++) {
			String [] splittings = list[i].split(java.io.File.separator);
			if(splittings[0].equals("modules")) modules.add(splittings[1]);
			else types.add(splittings[1]);
		}
		
		availables.put("modules", modules);
		availables.put("types", types);
		
		return availables;
		
	}
	
	public void loadModules(List modules, SourceDesc source) {
		if(modules.isEmpty()) return;
		
		String [] files = new String[modules.size()];
		for(int i=0; i<files.length; i++)
			files[i] = "modules"+java.io.File.separator+modules.get(i);
		
		// sync
		String localProxy = proxy.syncFiles(source, files);
		
		// start proxy
		startSync(localProxy, IceNetworkConnection.get().getProperties().getProperty("path2modules"));
		
		proxy.synched(localProxy);
		
		checkFiles(files);
	}
	
	public void loadTypes(List types, SourceDesc source) {
		if(types.isEmpty()) return;
		
		String [] files = new String[types.size()];
		for(int i=0; i<files.length; i++)
			files[i] = "types"+java.io.File.separator+types.get(i);
		
		// sync
		String localProxy = proxy.syncFiles(source, files);
		
		// start proxy
		startSync(localProxy, IceNetworkConnection.getProperties().getProperty("path2types"));
			
		proxy.synched(localProxy);
		
		checkFiles(files);
	}
	
	private void startSync(String localProxy, String dir) {
		properties = IceNetworkConnection.getProperties();
		String command = "icepath2client --IcePatch2.Endpoints=\""+localProxy+"\" "+dir;
		
		try {
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkFiles(String [] files) {
		String baseDir = (new java.io.File(properties.getProperty("path2modules"))).getParent();
		try {
			String schemaLocation = "https://proj.5nord.org/simmit "+properties.getProperty("path2xsd");
			for(int i=0; i<files.length; i++) {
				String baseName = baseDir+java.io.File.separator;
				try {
					String fileName = baseName+files[i]+".xml";
					org.jdom.Element root = XmlHelper.getRootElement(fileName, false);
					root.setAttribute("schemaLocation", schemaLocation, root.getNamespace("xsi"));
					XmlHelper.writeRootElement(root, fileName);
				} catch(Exception e) {}
				
				try {
					String fileName = baseName+files[i]+".xml";
					org.jdom.Element root = XmlHelper.getRootElement(baseName+"instances/"+files[i]+".xsd", false);
					Iterator it = root.getChildren("import").iterator();
					while(it.hasNext()) {
						org.jdom.Element child = (org.jdom.Element)it.next();
						if(child.getAttributeValue("namespace").equals("https://proj.5nord.org/simmit")) 
						child.setAttribute("schemaLocation", properties.getProperty("path2xsd"), root.getNamespace("xsi"));
					}
					XmlHelper.writeRootElement(root, fileName);
				} catch(Exception e) {}
				
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getUsage() {
		return "\tjava ModuleFinder -l [-p] <address>\n"+
			"\tjava ModuleFinder -m <module1> <module2> .. <moduleN> [-p] <address>\n"+
			"\tjava ModuleFinder -t <datatype1> <datatype2> .. <datatypeN> [-p] <address>\n";				
	}
	
	public static void main(String [] args) {
		ParameterWrapper wrapper = new ParameterWrapper();
		if(!wrapper.init(args)) {
			System.out.println("usage: "+getUsage());
			System.exit(1);
		}
		
		ModuleFinder finder = new ModuleFinder();
		finder.connect("CodeDistribution: tcp -h "+wrapper.address+" -p "+wrapper.port);
		
		LocalSource sourceDesc = new LocalSource();
		sourceDesc.path = IceNetworkConnection.getProperties().getPropertyWithDefault("SourceService.LocalSource.path", ".");
	
		switch(wrapper.command) {
		case LIST: listAvailables(finder, sourceDesc); break;
		case GET_MODULES: finder.loadModules(wrapper.modules, sourceDesc); break;
		case GET_TYPES: finder.loadTypes(wrapper.modules, sourceDesc); break;
		}
	}
	
	private static void listAvailables(ModuleFinder finder, SourceDesc sourceDesc) {
		Map availables = finder.getAvailables(sourceDesc); 
	
		if(availables.containsKey("modules")) {
			System.out.println("available modules:");
			Iterator it = ((List)availables.get("modules")).iterator();
			while(it.hasNext()) System.out.println("\t"+it.next());
		}
		
		if(availables.containsKey("types")) {
			System.out.println("available datatypes:");
			Iterator it = ((List)availables.get("types")).iterator();
			while(it.hasNext()) System.out.println("\t"+it.next());
		}
	}
	
	private static final int LIST=0, GET_MODULES=1, GET_TYPES=2;
	private static class ParameterWrapper {
		int command = -1;
		String address, port="10000";
		java.util.List modules = new java.util.ArrayList();
		
		public boolean init(String [] args) {
			int i=0;
			while(i < args.length) {
				if(args[i].equalsIgnoreCase("-l")) command = LIST;
				else if(args[i].equalsIgnoreCase("-m")) {
					command = GET_MODULES;
					while(!args[++i].startsWith("-") && i < args.length)
						modules.add(args[i]);
					if(i == args.length) modules.remove(modules.size()-1);
					i -= 1; // since the next element is the address, or -p
				}
				else if(args[i].equalsIgnoreCase("-t")) {
					command = GET_TYPES;
					while(!args[++i].startsWith("-") && i < args.length)
						modules.add(args[i]);
					if(i == args.length) modules.remove(modules.size()-1);
					i -= 1; // since the next element is the address, or -p
				}
				else if(args[i].equalsIgnoreCase("-p")) port = args[i++];
				
				else address = args[i];
				i++;
			}
			
			return (address != null);
		}
	}
}
