package util;
import java.util.Map;
import java.io.*;
import Ice.Communicator;
import Ice.Properties;

public class IceNetworkConnection {
	
	private static Communicator communicator = null;
	
	public static Communicator get() {
		return get("configuration");
	}
	
	private static String configurationFile;
	public static Communicator get(String configFile) {
		if(communicator == null) {
			String [] params = new String[1];
			params[0] = "--Ice.Config="+configFile;
			communicator = Ice.Util.initialize(params);
			Map ctx = new java.util.HashMap();
			communicator.setDefaultContext(ctx);
		}
		configurationFile = configFile;
		return communicator;
	}
	
	public static Communicator get(String [] args) {
		if(communicator == null)
			communicator = Ice.Util.initialize(args);
	
		return communicator;
	}
	
	public static Properties getProperties() {
		if(communicator == null) get();
		return communicator.getProperties();
	}
	
	public static boolean checkContext(String key) {
		return communicator.getDefaultContext().containsKey(key);
	}
	
	public static void addToContext(Map map) {
		Map context = communicator.getDefaultContext();
		context.putAll(map);
		communicator.setDefaultContext(context);
	}
	
	public static void addToContext(Object key, Object value) {
		Map context = communicator.getDefaultContext();
		context.put(key, value);
		communicator.setDefaultContext(context);
	}
	
	public static void saveProperties(Map properties) {
		try {
			File tempFile = File.createTempFile("properties", "xml");
			BufferedReader reader = new BufferedReader(new FileReader(configurationFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			
			Properties origProperties = getProperties();
			String line;
			while((line = reader.readLine()) != null) {
				String [] parts = line.split("=");
				if(parts.length == 2 && properties.containsKey(parts[0])) {
					origProperties.setProperty(parts[0], (String)properties.get(parts[0]));
					writer.write(parts[0]+"="+properties.get(parts[0]+"\n"));
					properties.remove(parts[0]);
				} else writer.write(line+"\n");
			}
		
			java.util.Iterator it = properties.keySet().iterator();
			if(it.hasNext()) writer.write("\n");
			
			while(it.hasNext()) {
				String key = (String)it.next();
				writer.write(key+"="+properties.get(key)+"\n");
			}
			reader.close();
			writer.close();
			tempFile.renameTo(new File(configurationFile));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
