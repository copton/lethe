package xml;

import org.jdom.*;

import util.IceNetworkConnection;

import Ice.OutputStream;
import Ice.Properties;

import util.SliceGenerator;

import java.util.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import Comm.Job.Graph.Port;
import Comm.Job.Graph.Vertex;

public class VertexWrapper {
	Vertex vertex;
	List includedClasses;
	List ports;
	byte [] settings;
	private static Map knownModules = new HashMap();
	
	public static VertexWrapper newWrapper(String name, Element root) {
		VertexWrapper originalWrapper = (VertexWrapper)knownModules.get(root.getName());
		
		VertexWrapper wrapper = new VertexWrapper(originalWrapper, name, root);
		return wrapper;
	}
	
	private VertexWrapper(VertexWrapper origWrapper, String name, Element root) {
		initValues();
		
		vertex = new Vertex();
		vertex.vertexName = name;
		vertex.moduleName = root.getName();
		
		if(buildCode(root) || origWrapper == null) {
			Object compiletimeObject = getInstance("Extensions.Modules."+vertex.moduleName+".Lethe.Configuration");
	
			includedClasses = getIncludedClasses(root);
			compiletimeObject = configure(root.getChild("parameter").getChild("configuration"), compiletimeObject);
			vertex.config = getBytestream(compiletimeObject);
		
			Object runtimeObject = getInstance("Extensions.Modules."+vertex.moduleName+".Lethe.Settings");
			runtimeObject = configure(root.getChild("parameter").getChild("settings"), runtimeObject);
			settings = getBytestream(runtimeObject);
		
			knownModules.put(vertex.moduleName, this);
		} else {
			vertex.config = copyBytestream(origWrapper.vertex.config);
			settings = copyBytestream(origWrapper.settings);
			ports = origWrapper.ports;
			includedClasses = origWrapper.includedClasses;
		}
		ports = getPorts(root.getChild("ports"));
	}
	
	public Comm.Job.Graph.Vertex get() {
		return vertex;
	}
	
	public List getPorts() {
		return ports;
	}
	
	public boolean buildCode(Element root) {
		SliceGenerator gen = new SliceGenerator();
		Properties properties = IceNetworkConnection.getProperties();
		
		String path2types = properties.getProperty("xml.path2types");
		
		List includedFiles = new ArrayList();
		XmlHelper.getIncludeFiles(root, includedFiles, path2types);
	
		boolean flag = gen.generateJavaFilesFromSlices(root.getName(), includedFiles, properties.getProperty("xml.tempdir"));
		System.out.println("generated: "+(flag?"yes":"no"));
		return flag;
	}
	
	private byte[] copyBytestream(byte [] input) {
		byte [] b = new byte[input.length];
		for(int i=0; i<b.length; i++) b[i] = input[i];
		
		return b;
	}
	
	private Object getInstance(String name) {
		try {
			return getClassObject(name).newInstance();
		} catch (Exception e) {
			System.out.println(e+" not found: "+e.getMessage());
			return null;
		} 
	}
	
	private Class getClassObject(String name) {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			return cl.loadClass(name);
		} catch (Exception e) {
			System.out.println(e+" not found: "+e.getMessage());
			return null;
		} 
	}
	
	private byte[] getBytestream(Object obj) {
		Class helperClass = getClassObject(obj.getClass().getName()+"Helper");
	
		Method writeMethod;
		try {
			Class [] classes = new Class[2];
			classes[0] = OutputStream.class;
			classes[1] = obj.getClass();
			writeMethod = helperClass.getMethod("write", classes);
			
			OutputStream stream = Ice.Util.createOutputStream(IceNetworkConnection.get());
			Object [] objList = new Object[2];
			objList[0] = stream;
			objList[1] = obj;
			writeMethod.invoke(null, objList);
			stream.writePendingObjects();
			
			byte [] data = stream.finished();
			stream.destroy();
			return data;
		} catch(Exception e) {
			System.out.println(e+" failed: "+e.getMessage());
			return null;
		}	
	}
	
	private Object readBytestream(byte [] stream, Class base) {
		Ice.InputStream in = Ice.Util.createInputStream(IceNetworkConnection.get(), stream);
		Class [] classes = new Class[1];
		classes[0] = Ice.InputStream.class;
		
		try {
			Method read = base.getMethod("read", classes);	
			Object [] objects = new Object[1];
			objects[0] = in;
			Object obj = read.invoke(null, objects);
			in.readPendingObjects();
			in.destroy();
			return obj;
		} catch(Exception e) {
			System.out.println(e+" failed: "+e.getMessage());
			return null;
		}	
	}
	
	// CompiletimeConfiguration
	public void configure(Element configuration) {
		if(configuration == null) return;
		
		Object configObj = readBytestream(vertex.config, getClassObject("Extensions.Modules."+vertex.moduleName+".Lethe.ConfigurationHelper"));
		vertex.config = getBytestream(configure(configuration, configObj));
	}
	
	// RuntimeConfiguration
	public void setSettings(Element settings) {
		this.settings = getSettings(settings);
	}
	
	public byte [] getSettings(Element settings) {
		if(settings == null) return this.settings;
		
		Object settingsObj = readBytestream(this.settings, getClassObject("Extensions.Modules."+vertex.moduleName+".Lethe.SettingsHelper"));
		return getBytestream(configure(settings, settingsObj));
	}
	
	static final int _string=0, _int=1, _byte=2, _long=3, _float=4, _double=5, 
		_boolean=6, _enum=7, _class=8, _dictionary=9, _table=10, _array=11;
	public Object configure(Element configuration, Object obj) {
		Iterator it = configuration.getChildren().iterator();
		try {
		while(it.hasNext()) {
			Element config = (Element)it.next();
			Field f = obj.getClass().getField(config.getName());
			String type = config.getAttributeValue("type");
			int typeNum = ((Integer)type2enum.get(type)).intValue();
			switch(typeNum) {
			case _string: f.set(obj, config.getText()); break;
			case _int: f.setInt(obj, Integer.parseInt(config.getText())); break;
			case _byte: f.setByte(obj, Byte.parseByte(config.getText())); break;
			case _long: f.setLong(obj, Long.parseLong(config.getText())); break;
			case _float: f.setFloat(obj, Float.parseFloat(config.getText())); break;
			case _double: f.setDouble(obj, Double.parseDouble(config.getText())); break;
			case _boolean: f.setBoolean(obj, getBoolean(config.getText())); break;
			default: {
				switch(typeNum) {
				case _enum: 
					Class newClass = getClassObject(getClassname(config.getAttributeValue("classname")));
					f.set(obj, newClass.getField(config.getText()).get(newClass.getName())); break;
				case _class: 
					Object instance = getInstance(getClassname(config.getAttributeValue("classname")));
					f.set(obj, configure(config, instance)); break;
				case _array: f.set(obj, getArray(config, config.getAttributeValue("elementType"))); break;
				case _table: f.set(obj, getTable(config, config.getAttributeValue("elementType"))); break;
				case _dictionary: f.set(obj, getDictionary(config, config.getAttributeValue("valueType"))); break;
				default: break; // throw unknownType-Exception
				}
			}
		}}
		} catch(Exception e) {
			System.out.println(e + ": "+e.getMessage());
		}
		
		return obj;
	}
	
	private boolean getBoolean(String s) {
		return s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equals("1");
	}
	
	private Object getTable(Element config, String fieldType) throws Exception {
		Class elementClass = (Class)type2class.get(fieldType);
		if(elementClass == null) elementClass = getClassObject(getClassname(fieldType));
		
		int [] dimensions = new int[2];
		dimensions[0] = 2;
		dimensions[1] = config.getChildren().size();
		
		Object array = Array.newInstance(elementClass, dimensions);
		Iterator it = config.getChildren().iterator();
		for(int pos=0; it.hasNext(); pos++)
			Array.set(array, pos, getArray((Element)it.next(), fieldType));
		
		return array;
	}
	
	private Object getArray(Element config, String fieldType) throws Exception {
		boolean simpleType = true;
		Class elementClass = (Class)type2class.get(fieldType);
		if(elementClass == null) {
			elementClass = getClassObject(getClassname(fieldType));
			simpleType = false;
		}
		Object array = Array.newInstance(elementClass, config.getChildren().size());
			
		Iterator it = config.getChildren().iterator();
		for(int pos=0; it.hasNext(); pos++) {
			Element node = (Element)it.next();
			if(simpleType)
				switch(((Integer)type2enum.get(fieldType)).intValue()) {
				case _string: Array.set(array, pos, node.getText()); break;
				case _int: Array.setInt(array, pos, Integer.parseInt(node.getText())); break;
				case _byte: Array.setByte(array, pos, Byte.parseByte(node.getText())); break;
				case _long: Array.setLong(array, pos, Long.parseLong(node.getText())); break;
				case _double: Array.setDouble(array, pos, Double.parseDouble(node.getText())); break;
				case _float: Array.setFloat(array, pos, Float.parseFloat(node.getText())); break;
				case _boolean: Array.setBoolean(array, pos, getBoolean(node.getText())); break;
				}
			else Array.set(array, pos, configure(node, elementClass.newInstance()));
		}
		return array;
	}
	
	private Object getDictionary(Element config, String fieldType) throws Exception {
		Map fieldObjects = new HashMap();
		String typeName = config.getAttributeValue("valueType");
		boolean simpleType = type2class.containsKey(typeName);
		
		Iterator it = config.getChildren().iterator();
		while(it.hasNext()) {
			Element keyField = (Element)it.next();
			String key = keyField.getAttributeValue("name");
			if(simpleType) {
				switch(((Integer)type2enum.get(typeName)).intValue()) {
				case _string: fieldObjects.put(key, keyField.getText()); break;
				case _int: fieldObjects.put(key, Integer.valueOf(keyField.getText())); break;
				case _byte: fieldObjects.put(key, Byte.valueOf(keyField.getText())); break;
				case _long: fieldObjects.put(key, Long.valueOf(keyField.getText())); break;
				case _double: fieldObjects.put(key, Double.valueOf(keyField.getText())); break;
				case _float: fieldObjects.put(key, Float.valueOf(keyField.getText())); break;
				case _boolean: fieldObjects.put(key, new Boolean(getBoolean(keyField.getText()))); break;	
				}
			}
			else {
				Object element = getInstance(config.getAttributeValue("elementType"));
				fieldObjects.put(key, configure(keyField, element));
			}
		}
		return fieldObjects;
	}
	
	private List getPorts(Element ports) {
		List portList = new ArrayList();
		List inputPortNames = new ArrayList();
		List outputPortNames = new ArrayList();
		
		if(ports != null) {
			Iterator it = ports.getChildren().iterator();
			while(it.hasNext()) {
				Element port = (Element)it.next();
				Port newPort = new Port();
				newPort.descriptor = new Comm.Job.Graph.PortDescriptor();
				newPort.descriptor.vertexName = vertex.vertexName;
				newPort.descriptor.portName = port.getName();
				newPort.basicType = port.getAttributeValue("type");
				newPort.blocksize = Integer.parseInt(port.getAttributeValue("blocksize"));
				newPort.usedType = port.getAttributeValue("usedType");
				portList.add(newPort);
				if(port.getAttributeValue("direction").equals("input"))
					inputPortNames.add(newPort.descriptor.portName);
				else outputPortNames.add(newPort.descriptor.portName);
			}
		}
		
		vertex.inputPorts = (String[])inputPortNames.toArray(new String[0]);
		vertex.outputPorts = (String[])outputPortNames.toArray(new String[0]);
		return portList;
	}
	
	private String getClassname(String classname) {
		if(includedClasses.contains(classname)) return "Extensions.Types.Slice."+classname;
		else return "Extensions.Modules."+vertex.moduleName+".Lethe."+classname;
	}
	
	private List getIncludedClasses(Element root) {
		List l = new ArrayList();
		Element node = root.getChild("include");
		
		if(node != null) {
			Iterator it = node.getChildren().iterator();
			while(it.hasNext())
				l.add(((Element)it.next()).getText());
		}
		return l;
	}
	
	private static HashMap type2class;
	private static HashMap type2enum;
	private static boolean initialized;
	private static void initValues() {
		if(!initialized) {
			type2class = new HashMap();
			type2class.put("string", String.class);
			type2class.put("int", int.class);
			type2class.put("byte", byte.class);
			type2class.put("long", long.class);
			type2class.put("float", float.class);
			type2class.put("double", double.class);
			type2class.put("boolean", boolean.class);
		
			type2enum = new HashMap();
			type2enum.put("string", new Integer(_string));
			type2enum.put("int", new Integer(_int));
			type2enum.put("byte", new Integer(_byte));
			type2enum.put("long", new Integer(_long));
			type2enum.put("float", new Integer(_float));
			type2enum.put("double", new Integer(_double));
			type2enum.put("boolean", new Integer(_boolean));
			type2enum.put("enum", new Integer(_enum));
			type2enum.put("class", new Integer(_class));
			type2enum.put("dictionary", new Integer(_dictionary));
			type2enum.put("table", new Integer(_table));
			type2enum.put("array", new Integer(_array));
				
			initialized = true;
		}
	}
}
