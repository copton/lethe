package client;

import Comm.Job.Persistence;
import Comm.Job.Specification;
import Comm.Job.Graph.Vertex;
import Comm.Manager.PersistantJobs;

import org.jdom.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import util.IceNetworkConnection;
import util.SliceGenerator;
import xml.XmlHelper;

public class ResultExporter {

	public void export(Persistence job, String file) {
		Element rootElement = new Element("results");
		setDefaultAttributes(job.job, rootElement);
		
		try {
			// build files if necessary
			Vertex [] vertices = job.job.theGraph.theVertices;
			SliceGenerator generator = new SliceGenerator();
			for(int i=0; i<vertices.length; i++) 
				generator.generateJavaFilesFromSlices(vertices[i].moduleName, new ArrayList(), 
						"/Users/phia/akt_source/simmit/trunk/code/core/client/tmp"); //IceNetworkConnection.getProperties().getProperty("xml.tempdir"));
			
			// create rounds
			for(int i=0; i<job.results.length; i++) {
				Element round = new Element("round");
				round.setAttribute("nr", Integer.toString(i+1));
				writeResults(job.results[i], round, i, job.job);
				rootElement.addContent(round);
			}
		
			XmlHelper.writeRootElement(rootElement, file);
		} catch(Exception e) {
			// fire Exception
			e.printStackTrace();
		}
	}
	
	private void setDefaultAttributes(Specification job, Element root) {
		root.setAttribute("jobId", job.jobId);
		root.setAttribute("owner", job.owner);
		root.setAttribute("name", job.name);
		root.setAttribute("started", (new Date(job.schedulerInfo.startTime)).toString());
		
		if(job.description.length() > 0)
			root.setAttribute("description", job.description);
	}
	
	private void writeResults(Map results, Element round, int roundNum, Specification job) 
		throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		for(int i=0; i<job.theGraph.theVertices.length; i++) {
			Vertex v = job.theGraph.theVertices[i];
			
			String nodeName = v.vertexName;
			String moduleName = v.moduleName;
			
			Element node = new Element("node");
			node.setAttribute("name", nodeName);
			node.setAttribute("type", moduleName);
			
			node.addContent(getSettings(moduleName, (byte[])job.settings[roundNum].get(nodeName), SETTINGS, job));
			node.addContent(getSettings(moduleName, (byte[])results.get(nodeName), RESULTS, job));
			
			round.addContent(node);
		}
	}
	
	private Element getSettings(String moduleName, byte [] stream, int type, Specification job) 
		throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		Element node = new Element("will_be_renamed_");
		
		switch(type) {
		case SETTINGS: node.setName("settings"); break;
		case RESULTS: node.setName("results"); break;
		}
		
		if(stream != null) {
			Class resultClass = loadClass(moduleName, type);
			Object resultObj = this.readBytestream(stream, resultClass);
			getValuesFromStream(resultClass.getFields(), resultObj, node);
		}
		
		return node;
	}
	
	private void getValuesFromStream(java.lang.reflect.Field [] fields, Object resultObj, Element node)	
			throws IllegalAccessException, InstantiationException {
	
		if(fields[0].getName().equals("icegenDUMMY")) return; // is only dummy-result
		for(int i=0; i<fields.length; i++) {
			node.addContent(getValue(resultObj, fields[i]));
		}
	}
	
	private Element getValue(Object resultObj, java.lang.reflect.Field field) 
	throws IllegalAccessException, InstantiationException {
		Class type = field.getType();
		Element resValue = new Element("will_be_renamed_");
		resValue.setAttribute("name", field.getName());
	
		switch(getTypeNum(type)) {
		case BYTE: resValue.setName("byte"); resValue.setText(Byte.toString(field.getByte(resultObj))); break;
		case INT: resValue.setName("int"); resValue.setText(Integer.toString(field.getInt(resultObj))); break;
		case LONG: resValue.setName("long"); resValue.setText(Long.toString(field.getLong(resultObj))); break;
		case FLOAT: resValue.setName("float"); resValue.setText(Float.toString(field.getFloat(resultObj))); break;
		case DOUBLE: resValue.setName("double"); resValue.setText(Double.toString(field.getDouble(resultObj))); break;
		case BOOLEAN: resValue.setName("boolean"); resValue.setText(Boolean.toString(field.getBoolean(resultObj))); break;
		case STRING: resValue.setName("string"); resValue.setText((String)field.get(resultObj)); break;
		case ARRAY: resValue.setName("array"); addArray(resultObj, resValue); break;
		case DICTIONARY: resValue.setName("dictionary"); addDictionary(resultObj, resValue); break;
		case UNKNOWN: {
			String typeName = field.getType().getName();
			resValue.setName(typeName.substring(typeName.lastIndexOf(".")+1)); 
			Object typeObject = field.getType().newInstance();
			Field [] typeFields = field.getType().getFields();
			for(int i=0; i<typeFields.length; i++) 
				resValue.addContent(getValue(typeObject, typeFields[i]));
			break;
		}
		}
		return resValue;
	}
	
	private void addArray(Object obj, Element node) throws IllegalAccessException {
		int length = Array.getLength(obj);
		node.setAttribute("size", Integer.toString(length));
		
		int type = getTypeNum(obj.getClass());
		for(int i=0; i<length; i++) {
			Element resValue = new Element("will_be_renamed_");
			switch(type) {
			case BYTE: resValue.setName("byte"); resValue.setText(Byte.toString(Array.getByte(obj, i))); break;
			case INT: resValue.setName("int"); resValue.setText(Integer.toString(Array.getInt(obj, i))); break;
			case LONG: resValue.setName("long"); resValue.setText(Long.toString(Array.getLong(obj, i))); break;
			case FLOAT: resValue.setName("float"); resValue.setText(Float.toString(Array.getFloat(obj, i))); break;
			case DOUBLE: resValue.setName("double"); resValue.setText(Double.toString(Array.getDouble(obj, i))); break;
			case BOOLEAN: resValue.setName("boolean"); resValue.setText(Boolean.toString(Array.getBoolean(obj, i))); break;
			case STRING: resValue.setName("string"); resValue.setText((String)Array.get(obj, i)); break;
			case ARRAY: resValue.setName("array"); addArray(Array.get(obj, i), resValue); break;
			case DICTIONARY: resValue.setName("dictionary"); addDictionary(Array.get(obj, i), resValue); break;
			case UNKNOWN: // own types
			}
			node.addContent(resValue);
		}
	}
	
	private void addDictionary(Object obj, Element node) throws IllegalAccessException {
		Map m = (Map)obj;
		
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			String keyName = (String)it.next();
			Object keyValue = m.get(keyName);
			Element keyElement = getElementFromObject(keyValue);
			keyElement.setAttribute("key", keyName);
			node.addContent(keyElement);
		}
	}
	
	private Element getElementFromObject(Object  obj) throws IllegalAccessException {
		Element e = new Element("will_be_renamed_");
		switch(getTypeNum(obj.getClass())) {
		case _BYTE: e.setName("byte"); e.setText(((Byte)obj).toString()); break;
		case _INT: e.setName("int"); e.setText(((Integer)obj).toString()); break;
		case _LONG: e.setName("long"); e.setText(((Long)obj).toString()); break;
		case _FLOAT: e.setName("float"); e.setText(((Float)obj).toString()); break;
		case _DOUBLE: e.setName("double"); e.setText(((Double)obj).toString()); break;
		case _BOOLEAN: e.setName("boolean"); e.setText(((Boolean)obj).toString()); break;
		case STRING: e.setName("string"); e.setText((String)obj); break;
		case ARRAY: e.setName("array"); addArray(obj, e); break;
		case DICTIONARY: e.setName("dictionary"); addDictionary(obj, e); break;
		case UNKNOWN:
		}
		
		return e;
	}
	
	private static final int BYTE=0, INT=1, LONG=2, FLOAT=3, DOUBLE=4, BOOLEAN=5, STRING=6, ARRAY=7, DICTIONARY=8, 
	_BYTE=9, _INT=10, _LONG=11, _FLOAT=12, _DOUBLE=13, _BOOLEAN=14, UNKNOWN=15;
	private static final Map classMap = initClassMap();
	private int getTypeNum(Class c) {
		if(c.isArray()) return ARRAY;
		else if(!classMap.containsKey(c)) return UNKNOWN;
		else return ((Integer)classMap.get(c)).intValue();
	}
	
	private static Map initClassMap() {
		Map m = new HashMap();
		m.put(byte.class, new Integer(BYTE));
		m.put(int.class, new Integer(INT));
		m.put(long.class, new Integer(LONG));
		m.put(float.class, new Integer(FLOAT));
		m.put(double.class, new Integer(DOUBLE));
		m.put(boolean.class, new Integer(BOOLEAN));
		m.put(Byte.class, new Integer(_BYTE));
		m.put(Integer.class, new Integer(_INT));
		m.put(Long.class, new Integer(_LONG));
		m.put(Float.class, new Integer(_FLOAT));
		m.put(Double.class, new Integer(_DOUBLE));
		m.put(Boolean.class, new Integer(_BOOLEAN));
		
		m.put(String.class, new Integer(STRING));
		m.put(Map.class, new Integer(DICTIONARY));
		
		return m;
	}
	
	private final static int SETTINGS=0, RESULTS=1;
	private Class loadClass(String module, int type) throws ClassNotFoundException {
		
		String [] types = {"Settings", "Result"};
		if(type >= types.length) return null;
		
		String className = "Extensions.Modules."+module+".Lethe."+types[type];
		ClassLoader cl = ClassLoader.getSystemClassLoader();
	
		return cl.loadClass(className);
	}
	
	private Object readBytestream(byte [] stream, Class base) throws ClassNotFoundException {
		Ice.InputStream in = Ice.Util.createInputStream(IceNetworkConnection.get(), stream);
		Class [] classes = new Class[1];
		classes[0] = Ice.InputStream.class;
		
		Class helperClass = ClassLoader.getSystemClassLoader().loadClass(base.getName()+"Helper");
		
		try {
			Method read = helperClass.getMethod("read", classes);	
			Object [] objects = new Object[1];
			objects[0] = in;
			Object obj = read.invoke(null, objects);
			try {
			in.readPendingObjects();
			} catch(Exception ex) {}
			in.destroy();
			return obj;
		} catch(Exception e) {
			System.out.println(e+" failed: "+e.getMessage());
			return null;
		}	
	}
	
	private static String getUsage() {
		return "java ResultExporter -l\n"+
			"java ResultExporter -e <jobId> <fileName>";
	}
	
	private static boolean areParamsValid(String [] args) {
		if(args.length == 0 || args.length > 3) return false;
		if(args[0].equalsIgnoreCase("-l"))
			return args.length == 1;
		else if(args[0].equalsIgnoreCase("-e")) 
			return args.length == 3;
		else return false;
	}
	
	private static void listFiles(PersistantJobs jobs) {
		java.util.Iterator it = jobs.values().iterator();
		while(it.hasNext()) {
			Persistence p = (Persistence)it.next();
			System.out.println(p.job.jobId+ " - "+p.job.name + 
					(p.job.description.length()>0?": "+p.job.description:""));
		}
	}

	// java ResultExporter <jobId> <fileName>
	public static void main(String [] args) throws ClassNotFoundException, java.io.IOException {
		if(!areParamsValid(args)) {
			System.out.println(getUsage());
	//		System.exit(1);
		}
		
		Freeze.Connection connection =          
			 Freeze.Util.createConnection(IceNetworkConnection.get(), "/Users/phia/akt_source/simmit/trunk/code/core/client/db");
		PersistantJobs results = new PersistantJobs(connection, "results", true);
		
		String [] foo = {"-e", "phia1", "foo"};
		args = foo;
		if(args[0].equalsIgnoreCase("-l")) listFiles(results);
		else if(results.containsKey(args[1])) 
			(new ResultExporter()).export((Persistence)results.get(args[1]), args[2]);
		else System.out.println("no results found for "+args[1]);
		
		connection.close();
		System.exit(0);
	}
}
