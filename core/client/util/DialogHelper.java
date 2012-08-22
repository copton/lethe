package util;

import Ice.Properties;
import xml.TypedElement;
import xml.Types;

import org.jdom.Element;

import java.util.*;
import java.io.File;

public class DialogHelper implements Types {
	
	private static DialogHelper helper = new DialogHelper();
	private DialogHelper() {
		Properties properties = IceNetworkConnection.getProperties();
		path2modules = properties.getProperty("xml.path2modules");
		path2types = properties.getProperty("xml.path2types");
	}
	
	public static List getLocalTypes(TypedElement node) {
		return helper.getLocals(node);
	}
	
	public static List getGlobalTypes() {
		return helper.getTypes();
	}
	
	public static Element getGlobalType(String name) {
		return helper.getType(helper.getFileName(name));
	}
	
	public static List getTypeImprints(String name) {
		return helper.getTypeImprintForName(helper.getFileName(name));
	}
	
	
	public static List getTypeImprintElements(String name) {
		return helper.getImprintsAsType(helper.getFileName(name));
	}
	
	
	public static List getModules() {
		return helper.getAllModules();
	}
	
	public static Element getModule(String name) {
		return helper.getModuleForName(helper.getFileName(name));
	}
	
	public static List getModuleImprints(String name) {
		return helper.getModuleImprintsForName(helper.getFileName(name));
	}
	
	public static List getModuleImprintNames(String name) {
		return helper.getModuleImprintForName(helper.getFileName(name));
	}
	
	public static TypedElement getModuleImprint(String name, String module) {
		return helper.getModuleImprintElement(helper.getFileName(name), helper.getFileName(module));
	}
	
	private List getLocals(TypedElement node) {
		List l = new ArrayList();
		
		Element rootElem = getRootElement(node);
		if(rootElem == null) return l;
		
		TypedElement root = new TypedElement(rootElem);
		switch(root.getType()) {
		case MODULE_DEF:
		case TYPE_DEF:
			if(root.getChild("define") == null) break;
			l.addAll(root.getChild("define").getChildren());
		}
		
		return l;
	}
	
	private Map typeList;
	private String path2types;
	private List getTypes() {	
		if(typeList == null) typeList = readXmlFiles(path2types);
		else testForChanges(typeList, path2types);
		
		return getExportedElementsInList(typeList.values());
	}
	
	private List getExportedElementsInList(Collection elements) {
		List l = new ArrayList();
		Iterator it = elements.iterator();
		while(it.hasNext()) {
			ElementWrapper wrapper = (ElementWrapper)it.next();
			TypedElement e = (TypedElement)wrapper.element;
			String exported = e.getChildText("export");
			
			Iterator it2 = e.getChild("define").getChildren().iterator();
			while(it2.hasNext()) {
				TypedElement child = (TypedElement)it2.next();
				if(child.getChildText("name").equals(exported)) {
					l.add(child);
					break;
				}
			}
		}
		return l;
	}
	
	private Element getType(String name) {
		if(typeList == null) typeList = readXmlFiles(path2types);
		else testForChanges(moduleList, path2modules);
		
		ElementWrapper wrapper = (ElementWrapper)typeList.get(name);
		if(wrapper != null) {
			String exported = wrapper.element.getChildText("export");
			Iterator it = wrapper.element.getChild("define").getChildren().iterator();
			while(it.hasNext()) {
				TypedElement child = (TypedElement)it.next();
				if(child.getChildText("name").equals(exported)) 
					return child;
			}
		}
		return null;
	}
	
	private Map typeImprints = new HashMap();
	private List getImprintsAsType(String name) {
		String basePath = path2types+File.separator+name+File.separator;
	
		Map imprints = (Map)typeImprints.get(basePath);
		if(imprints == null) {
			imprints = readXmlFiles(basePath+"instances/");
			typeImprints.put(getFileName(name), imprints);
		}
		else testForChanges(imprints, basePath+"instances/");
	
		return getExportedImprintsInList(imprints.values());
	}
	
	private List getTypeImprintForName(String name) {
		String basePath = path2types+File.separator+name+File.separator;
		
		Map imprints = (Map)typeImprints.get(basePath);
		if(imprints == null) {
			imprints = readXmlFiles(basePath+"instances/");
			typeImprints.put(getFileName(name), imprints);
		} else testForChanges(imprints, basePath+"instances/");
		
		Set keys = imprints.keySet();
		List l = new ArrayList();
		Iterator it = keys.iterator();
		while(it.hasNext()) 
			l.add((new File((String)it.next())).getName());
		
		return l;
	}
	
	private List getExportedImprintsInList(Collection c) {
		List l = new ArrayList();
		Iterator it = c.iterator();
		while(it.hasNext()) {
			ElementWrapper wrapper = (ElementWrapper)it.next();
			l.add(wrapper.element.getChild("export"));
		}
		
		return l;
	}
	
	
	private Map moduleList;
	private String path2modules;
	private List getAllModules() {
		if(moduleList == null) moduleList = readXmlFiles(path2modules);
		else testForChanges(moduleList, path2modules);
		
		return getElementsInList(moduleList.values());
	}
	
	private Element getModuleForName(String name) {
		if(moduleList == null) moduleList = readXmlFiles(path2modules);
		else testForChanges(moduleList, path2modules);
		
		ElementWrapper wrapper = (ElementWrapper)moduleList.get(name);
		if(wrapper != null) return wrapper.element;
		else return null;
	}
	
	private Map moduleImprints = new HashMap();
	private List getModuleImprintsForName(String name) {
		String basePath = path2modules+File.separator+name+File.separator;
	
		Map imprints = (Map)moduleImprints.get(name);
		if(imprints == null) {
			imprints = readXmlFiles(basePath+"instances/");
			moduleImprints.put(name, imprints);
		}
		else testForChanges(imprints, basePath+"instances/");
	
		return getElementsInList(imprints.values());
	}	

	private TypedElement getModuleImprintElement(String name, String module) {
		String basePath = path2modules+File.separator+name+File.separator;
	
		Map imprints = (Map)moduleImprints.get(module);
		if(imprints == null) {
			imprints = readXmlFiles(basePath+"instances/");
			moduleImprints.put(module, imprints);
		}
		else testForChanges(imprints, basePath+"instances/");
	
		return (TypedElement)((ElementWrapper)imprints.get(name)).element;
	}	
	
	private List getModuleImprintForName(String name) {
		String basePath = path2modules+File.separator+name+File.separator;
		
		Map imprints = (Map)moduleImprints.get(basePath);
		if(imprints == null) {
			imprints = readXmlFiles(basePath+"instances/");
			moduleImprints.put(getFileName(name), imprints);
		} else testForChanges(imprints, basePath+"instances/");
		
		Set keys = imprints.keySet();
		List l = new ArrayList();
		Iterator it = keys.iterator();
		while(it.hasNext()) 
			l.add((new File((String)it.next())).getName());
		
		return l;
	}
	
	private void addElement(File file, Map hash) {
		try {
			Element e = xml.XmlHelper.getRootElement(file.getAbsolutePath(), true);
			if(e == null) return;
			String fileName = getFileName(file.getName());
			hash.put(fileName, 
					new ElementWrapper(new TypedElement(e), 
							fileName, file.lastModified()));
		} catch(Exception e) {
		}
	}
	
	private String getFileName(String name) {
		if(name.indexOf(".") < 0) return name;
		return name.substring(0, name.lastIndexOf("."));
	}
	
	private List getElementsInList(Collection c) {
		List l = new ArrayList();
		Iterator it = c.iterator();
		while(it.hasNext()) 
			l.add(((ElementWrapper)it.next()).element);
		
		return l;
	}
	
	private void testForChanges(Map files, String baseDir) {
		List availableFiles = getAllXmlFiles(baseDir);
		Iterator it = availableFiles.iterator();
		
		Collection removedFiles = files.values();
		
		while(it.hasNext()) {
			// check for new/modified files
			File f = (File)it.next();
			ElementWrapper wrapper = (ElementWrapper)files.get(f.getAbsolutePath());
			if(wrapper == null || wrapper.modificationTime < f.lastModified())
				addElement(f, files);
		
			// check for removed files
			removedFiles.remove(f.getAbsolutePath());
		}

		it = removedFiles.iterator();
		while(it.hasNext()) files.remove(it.next());
	}
	
	private static List getAllXmlFiles(String name) {
		File file = new File(name);
		List l = new ArrayList();
			
		File [] children = file.listFiles();
		for(int i=0; i<children.length; i++) {
			if(children[i].isFile() && 
					children[i].getName().endsWith(".xml")) 
				l.add(children[i]);
			else if(children[i].isDirectory()) {
				File [] children2 = children[i].listFiles();
				for(int j=0; j<children2.length; j++)
					if(children2[j].isFile() &&
							children2[j].getName().endsWith(".xml")) 
						l.add(children2[j]);
			}
		}
		
		return l;
	}
	
	private Map readXmlFiles(String basePath) {
		Map m = new HashMap();
		List files = getAllXmlFiles(basePath);
		Iterator it = files.iterator();
		while(it.hasNext()) 
			addElement((File)it.next(), m);
		
		return m;
	}
	
	private class ElementWrapper {
		Element element;
		long modificationTime;
		String fileName;
		
		public ElementWrapper(Element e, String file, long time) {
			element = e;
			fileName = file;
			modificationTime = time;
		}
	}
	
	public static TypedElement getRootElement(TypedElement node) {
		TypedElement parent = (TypedElement)node.getParentElement();
		if(parent == null) return node;
		else return getRootElement(parent);
	}
}
