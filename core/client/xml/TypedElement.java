package xml;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Attribute;
import util.IceNetworkConnection;

import Ice.Properties;

import java.util.*;

public class TypedElement extends Element implements Types {
	int type;
	
	public TypedElement(String name, int type) {
		super(name);
		this.type = type;
		switch(type) {
		case MODULE_DEF: case TYPE_DEF: case JOB:
		case MODULE_INSTANCE: case TYPE_INSTANCE:
			new org.jdom.Document(this);
		}
	}
	
	public TypedElement(String name, String value, int type) {
		this(name, type);
		setText(value);
	}
	
	public TypedElement(Element e) {
		this(e, true);
	}

	public TypedElement(Element e, boolean isRoot) {
		super(e.getName(), e.getNamespace());
	
		java.util.Iterator it = e.getAdditionalNamespaces().iterator();
		while(it.hasNext()) 
			addNamespaceDeclaration((org.jdom.Namespace)it.next());
	
		it = e.getAttributes().iterator();
		while(it.hasNext()) {
			org.jdom.Attribute a = (org.jdom.Attribute)it.next();
			setAttribute(new org.jdom.Attribute(a.getName(), a.getValue(), a.getNamespace()));	
		}
		
		org.jdom.Namespace ns = e.getNamespace();
		if(ns.getPrefix().equals("module")) type = MODULE_INSTANCE;
		else if(ns.getPrefix().equals("type")) type = TYPE_INSTANCE;
		else type = getTypeForElement(e);
		
		setText(e.getText());
		
		it = e.getChildren().iterator();
		while(it.hasNext()) 
			addContent(new TypedElement((Element)it.next(), false));
	
		if(isRoot)
			new org.jdom.Document(this, e.getDocument().getDocType());
	}
	
	public static List getSimpleTypes() {
		List l = new ArrayList();
		l.add(get(STRING));
		l.add(get(INT));
		l.add(get(BYTE));
		l.add(get(LONG));
		l.add(get(FLOAT));
		l.add(get(DOUBLE));
		l.add(get(BOOLEAN));
		return l;
	}
	
	public static List getComplexTypes() {
		List l = new ArrayList();
		l.add(get(CLASS));
		l.add(get(ENUM));
		l.add(get(ARRAY));
		l.add(get(TABLE));
		l.add(get(DICTIONARY));
		return l;
	}
	public Object clone() {
		// TODO: test, if this really works as it should...
		TypedElement e = (TypedElement)super.clone();
		e.type = type;
		return e;
	}
	
	public boolean isEqual(Object o) {
		if(!(o instanceof TypedElement)) return false;
		
		TypedElement e = (TypedElement)o;
		if(!getName().equals(e.getName())) return false;
		if(!getText().equals(e.getText())) return false;
		
		List myAttributes = getAttributes();
		List otherAttributes = e.getAttributes();
		if(myAttributes.size() != otherAttributes.size()) return false;
		for(int i=0; i<myAttributes.size(); i++) {
			Attribute a1 = (Attribute)myAttributes.get(i);
			Attribute a2 = (Attribute)otherAttributes.get(i);
			if(!a1.getName().equals(a2.getName())) return false;
			if(!a1.getValue().equals(a2.getValue())) return false;
		}
		
		List myChildren = getChildren();
		List otherChildren = e.getChildren();
		if(myChildren.size() != otherChildren.size()) return false;
		for(int i=0; i<myChildren.size(); i++) 
			if(!((TypedElement)myChildren.get(i)).isEqual(otherChildren.get(i))) return false;
		
		return true;
	}
	
	public static int getTypeForString(String s) {
		if(!name2num.containsKey(s)) {
			System.out.println("didn't find : "+s);
			return -1;
		}
		else return ((Integer)name2num.get(s)).intValue();
	}
	
	private static java.util.Map type2num, name2num;
	private int getTypeForElement(Element e) {
		if(TypedElement.type2num == null) TypedElement.initMaps();
		
		if(TypedElement.type2num.containsKey(e.getAttributeValue("type")))
			return ((Integer)type2num.get(e.getAttributeValue("type"))).intValue();
		else if(TypedElement.name2num.containsKey(e.getName())) {
			int type = ((Integer)TypedElement.name2num.get(e.getName())).intValue();
			switch(type) {
			case PARAMETER_DIR: if(e.getParentElement() == null) return TYPE_DEF; break;
			case SETTINGS_DIR: if(!e.getParentElement().getName().equals("parameter")) return ROUND_SETTINGS; break;
			}
			return type;
		} else return -1; // UNDEF
	}
	
	public int getType() {
		return type;
	}
	
	public static TypedElement get(int type) {
		switch(type) {
		case JOB: return getJobElement();
		case MODULE_DEF: return getModuleDefinitionElement();
		case TYPE_DEF: return getTypeDefinitionElement();
		case STRING: return getStringElement();
		case INT: return getNumberElement("int", INT);
		case LONG: return getNumberElement("long", LONG);
		case BYTE: return getNumberElement("byte", BYTE);
		case FLOAT: return getNumberElement("float", FLOAT);
		case DOUBLE: return getNumberElement("double", DOUBLE);
		case BOOLEAN: return getBooleanElement();
		case ENUM: return getEnumElement();
		case CLASS: return getClassElement();
		case ARRAY: return getArrayElement("byte", BYTE);
		case TABLE: return getTableElement("byte", BYTE);
		case DICTIONARY: return getDictionaryElement("byte", BYTE);
		case PORT: return getPortElement();
		case SVN_SOURCE: return getSvnSource(0);
		case LOCAL_SOURCE: return getLocalSource("extensions");
		case LOCATION: return getLocation();
		}
		return null;
	}
	
	public static TypedElement getJobElement() {
		TypedElement e = new TypedElement("job", JOB);
		e.addContent(new TypedElement("name", "job", NAME));
		e.addContent(new TypedElement("author", META_DATA));
		e.addContent(new TypedElement("version", META_DATA));
		e.addContent(new TypedElement("date", META_DATA));
		e.addContent(new TypedElement("description", DESCRIPTION));
		
		TypedElement coreLocation = get(LOCATION);
		coreLocation.setName("core");
		e.addContent(coreLocation);
		System.out.println("added core");
		
		TypedElement graph = new TypedElement("graph", GRAPH);
		graph.addContent(new TypedElement("nodes", NODES));
		graph.addContent(new TypedElement("edges", EDGES));
		TypedElement phases = new TypedElement("phases", PHASES);
		TypedElement phase = new TypedElement("phase", PHASE);
		phase.setAttribute("nr", "1");
		phases.addContent(phase);
		graph.addContent(phases);
		e.addContent(graph);
		
		TypedElement settings = new TypedElement("settings", ROUND_SETTINGS);
		settings.addContent(new TypedElement("default", DEFAULT_VALUE));
		TypedElement rounds = new TypedElement("rounds", ROUNDS);
		rounds.setAttribute("anz", "1");
		
		TypedElement round = new TypedElement("round", ROUND);
		round.setAttribute("nr", "1");
		rounds.addContent(round);
		settings.addContent(rounds);
		
		e.addContent(settings);
		
		Namespace simmit = Namespace.getNamespace("simmit", "https://proj.5nord.org/simmit");
		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		e.setNamespace(simmit);
		e.addNamespaceDeclaration(simmit);
		e.addNamespaceDeclaration(xsi);
		
		Properties properties = IceNetworkConnection.getProperties();
		String schemaLocationString = "https://proj.5nord.org/simmit "
			+properties.getProperty("xml.path2xsd")
			+"/jobDescription.xsd";
		Attribute schemaLocation = new Attribute("schemaLocation", schemaLocationString, xsi);
		e.setAttribute(schemaLocation);
		
	
		return e;
	}
	
	public static TypedElement getModuleDefinitionElement() {
		TypedElement e = new TypedElement("module", MODULE_DEF);
		e.addContent(new TypedElement("name", "modul", NAME));
		e.addContent(new TypedElement("author", META_DATA));
		e.addContent(new TypedElement("version", META_DATA));
		e.addContent(new TypedElement("date", META_DATA));
		e.addContent(new TypedElement("type", "Quelle", META_DATA));
		e.addContent(new TypedElement("description", DESCRIPTION));
		e.addContent(new TypedElement("include", INCLUDE_DIR));
		e.addContent(new TypedElement("define", DEFINE_DIR));
		
		TypedElement parameter = new TypedElement("parameter", PARAMETER_DIR);
		parameter.addContent(new TypedElement("configuration", CONFIGURATION_DIR));
		parameter.addContent(new TypedElement("settings", SETTINGS_DIR));
		parameter.addContent(new TypedElement("results", RESULTS_DIR));
		parameter.addContent(new TypedElement("serialize", SERIALIZE_DIR));
		e.addContent(parameter);
		
		TypedElement ports = new TypedElement("ports", PORT_DIR);
		ports.addContent(new TypedElement("input", INPUT_PORT_DIR));
		ports.addContent(new TypedElement("output", OUTPUT_PORT_DIR));
		e.addContent(ports);
		
		Namespace simmit = Namespace.getNamespace("simmit", "https://proj.5nord.org/simmit");
		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		e.setNamespace(simmit);
		e.addNamespaceDeclaration(simmit);
		e.addNamespaceDeclaration(xsi);
		
		Properties properties = IceNetworkConnection.getProperties();
		String schemaLocationString = "https://proj.5nord.org/simmit "
			+properties.getProperty("xml.path2xsd")
			+"moduleDescription.xsd";
		Attribute schemaLocation = new Attribute("schemaLocation", schemaLocationString, xsi);
		e.setAttribute(schemaLocation);
		
		return e;
	}
	
	public static TypedElement getTypeDefinitionElement() {
		TypedElement e = new TypedElement("parameter", TYPE_DEF);
		e.addContent(new TypedElement("name", "type", NAME));
		e.addContent(new TypedElement("author", META_DATA));
		e.addContent(new TypedElement("version", META_DATA));
		e.addContent(new TypedElement("date", META_DATA));
		e.addContent(new TypedElement("description", DESCRIPTION));
		e.addContent(new TypedElement("export", EXPORT));
		e.addContent(new TypedElement("define", DEFINE_DIR));
		
		Namespace simmit = Namespace.getNamespace("simmit", "https://proj.5nord.org/simmit");
		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		e.setNamespace(simmit);
		e.addNamespaceDeclaration(simmit);
		e.addNamespaceDeclaration(xsi);
		
		Properties properties = IceNetworkConnection.getProperties();
		String schemaLocationString = "https://proj.5nord.org/simmit "
			+properties.getProperty("xml.path2xsd")
			+"typeDescription.xsd";
		Attribute schemaLocation = new Attribute("schemaLocation", schemaLocationString, xsi);
		e.setAttribute(schemaLocation);
		
		return e;
	}
	
	public static TypedElement getPortElement() {
		TypedElement e = new TypedElement("port", PORT);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("type", "byte", PORT_TYPE));
		e.addContent(new TypedElement("blocksize", "1", SIZE));
		return e;
	}
	
	public static TypedElement getStringElement() {
		TypedElement e = new TypedElement("string", STRING);
		e.addContent(new TypedElement("name", "string", NAME));
		e.addContent(new TypedElement("default", DEFAULT_VALUE));
		return e;
	}
	
	public static TypedElement getNumberElement(String name, int type) {
		TypedElement e = new TypedElement(name, type);
		e.addContent(new TypedElement("name", name, NAME));
		e.addContent(new TypedElement("default", "0", DEFAULT_VALUE));
		return e;		
	}
	
	public static TypedElement getBooleanElement() {
		TypedElement e = new TypedElement("boolean", BOOLEAN);
		e.addContent(new TypedElement("name", "boolean", NAME));
		e.addContent(new TypedElement("default", "false", DEFAULT_VALUE));
		return e;		
	}
	
	public static TypedElement getEnumElement() {
		TypedElement e = new TypedElement("enum", ENUM);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("default", DEFAULT_VALUE));
		e.addContent(new TypedElement("members", FIELD));
		return e;		
	}
	
	public static TypedElement getEnumElement(java.util.List members) {
		return getEnumElement(members.get(0).toString(), members);
	}
	
	public static TypedElement getEnumElement(String defaultValue, java.util.List members) {
		TypedElement e = new TypedElement("enum", ENUM);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("default", defaultValue, DEFAULT_VALUE));
	
		TypedElement memberElement = new TypedElement("members", FIELD);
		memberElement.addContent(members);
		e.addContent(memberElement);
		return e;		
	}
	
	public static TypedElement getClassElement() {
		TypedElement e = new TypedElement("class", CLASS);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("members", MEMBERS));
		
		return e;
	}
	
	public static TypedElement getClassElement(java.util.List members) {
		TypedElement e = new TypedElement("class", CLASS);
		e.addContent(new TypedElement("name", NAME));
		
		TypedElement memberElement = new TypedElement("members", MEMBERS);
		memberElement.addContent(members);
		e.addContent(memberElement);
		return e;
	}
	
	public static TypedElement getArrayElement(String typeName, int type) {
		TypedElement e = new TypedElement("array", ARRAY);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("elementType", typeName, ELEMENT_TYPE));
		e.addContent(new TypedElement("default", type));
		
		return e;
	}
	
	public static TypedElement getTableElement(String typeName, int type) {
		TypedElement e = new TypedElement("table", TABLE);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("elementType", typeName, ELEMENT_TYPE));
		e.addContent(new TypedElement("default", type));
		
		return e;
	}
	
	public static TypedElement getDictionaryElement(String typeName, int valueType) {
		TypedElement e = new TypedElement("dictionary", DICTIONARY);
		e.addContent(new TypedElement("name", NAME));
		e.addContent(new TypedElement("valueType", typeName, valueType));
	
		return e;
	}	
	
	public static TypedElement getLocation() {
		TypedElement e = new TypedElement("location", LOCATION);
		e.addContent(get(LOCAL_SOURCE));
		
		return e;
	}
	
	public static TypedElement getSvnSource(int revision) {
		TypedElement e = new TypedElement("SvnSource", SVN_SOURCE);
		e.addContent(new TypedElement("revision", Integer.toString(revision), META_DATA));
		
		return e;
	}
	
	public static TypedElement getLocalSource(String path) {
		TypedElement e = new TypedElement("LocalSource", LOCAL_SOURCE);
		e.addContent(new TypedElement("pathToExtensions", path, META_DATA));
		
		return e;
	}
	
	public static TypedElement createInstance(TypedElement def) {
		TypedElement e = new TypedElement(def.getChildText("name"), ANZ_TYPES);
		e.setAttribute("description", def.getChildText("description"));
		
		switch(def.getType()) {
		case STRING: e.type = STRING_INSTANCE; e.setAttribute("type", "string"); break;
		case INT: e.type = INT_INSTANCE; e.setAttribute("type", "int"); break;
		case LONG: e.type = LONG_INSTANCE; e.setAttribute("type", "long"); break;
		case BYTE: e.type = BYTE_INSTANCE; e.setAttribute("type", "byte"); break;
		case FLOAT: e.type = FLOAT_INSTANCE; e.setAttribute("type", "float"); break;
		case DOUBLE: e.type = DOUBLE_INSTANCE; e.setAttribute("type", "double"); break;
		case BOOLEAN: e.type = BOOLEAN_INSTANCE; e.setAttribute("type", "boolean"); break;
		case ENUM: e.type = ENUM_INSTANCE; e.setAttribute("type", "enum"); break;
		case CLASS: e.type = CLASS_INSTANCE; 
			Iterator it = def.getChild("members").getChildren().iterator();
			while(it.hasNext()) e.addContent(createInstance((TypedElement)it.next()));
			e.setAttribute("classname", def.getChildText("name"));
			e.setAttribute("type", "class");
			break;
		case DICTIONARY: 
			e.type = DICTIONARY_INSTANCE; 
			e.setAttribute("valueType", def.getChildText("valueType"));
			break;
		case ARRAY: e.type = ARRAY_INSTANCE;
		case TABLE: e.type = TABLE_INSTANCE; 
			e.setAttribute("elementType", def.getChildText("elementType"));
			break;
		}
		
		switch(def.getType()) {
		case STRING: case INT: case LONG:	case BYTE:
		case FLOAT: case DOUBLE: case BOOLEAN: case ENUM:
			e.setText(def.getChildText("default"));
			break;
		}
		return e;
	}

	public org.jdom.Element getChild(String s) {
		if(getChildren().size() == 0) return null;
		
		java.util.Iterator it = getChildren().iterator();
		while(it.hasNext()) {
			Element e = (Element)it.next();
			if(e.getName().equals(s)) return e;
		}
		return null;
	}

	private static void initMaps() {
		type2num = new java.util.HashMap();
		String [] types = {"string", "int", "long", "byte", "float", "double",
				"boolean", "class", "enum", "array", "table", "dicitionary"};
		int [] typeNums = {STRING_INSTANCE, INT_INSTANCE, LONG_INSTANCE, 
				BYTE_INSTANCE, FLOAT_INSTANCE, DOUBLE_INSTANCE,
				BOOLEAN_INSTANCE, CLASS_INSTANCE, ENUM_INSTANCE, 
				ARRAY_INSTANCE, TABLE_INSTANCE, DICTIONARY_INSTANCE};
		
		for(int i=0; i<types.length; i++)
			type2num.put(types[i], new Integer(typeNums[i]));
		
		
		name2num = new java.util.HashMap();
		String [] names = {"string", "int", "long", "byte", "float", "double",
				"boolean", "class", "enum", "array", "table", "dictionary",
				"module", "author", "version", "date", "type", "description",
				"include", "parameter", "location", "define", "members", "row",
				"default", "elementType", "valueType", "anzRows", "anzCols", 
				"minRows", "minCols", "maxRows", "maxCols", "size", "minSize",
				"maxSize", "minValue", "maxValue", "minLength", "maxLength",
				"length", "export", "job", "type", "name", "configuration",
				"settings", "results", "ports", "type", "instance",
				"output", "input", "port", "blocksize", "serialize",
				"graph", "nodes", "edges", "phases", "rounds", "phase", 
				"edge", "node", "round", "SvnSource", "LocalSource"};
		int [] nameNums = {STRING, INT, LONG, BYTE, FLOAT, DOUBLE,
				BOOLEAN, CLASS, ENUM, ARRAY, TABLE, DICTIONARY,
				MODULE_DEF, META_DATA, META_DATA, META_DATA, META_DATA, DESCRIPTION,
				INCLUDE_DIR, PARAMETER_DIR, LOCATION, DEFINE_DIR, MEMBERS, ROW,
				DEFAULT_VALUE, ELEMENT_TYPE, ELEMENT_TYPE, SIZE, SIZE,
				SIZE, SIZE, SIZE, SIZE, SIZE, SIZE,
				SIZE, SIZE, SIZE, SIZE, SIZE,
				SIZE, EXPORT, JOB, TYPE_DEF, NAME, CONFIGURATION_DIR,
				SETTINGS_DIR, RESULTS_DIR, PORT_DIR, META_DATA, INSTANCE,
				OUTPUT_PORT_DIR, INPUT_PORT_DIR, PORT, SIZE, SERIALIZE_DIR,
				GRAPH, NODES, EDGES, PHASES, ROUNDS, PHASE, EDGE, NODE,
				ROUND, SVN_SOURCE, LOCAL_SOURCE};
		
		for(int i=0; i<names.length; i++)
			name2num.put(names[i], new Integer(nameNums[i]));
	}
}
