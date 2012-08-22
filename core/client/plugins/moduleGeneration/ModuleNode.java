package plugins.moduleGeneration;

import java.util.*;

import xml.*;
import plugins.treeTable.*;

public class ModuleNode extends XmlTreeNode implements Types {

	public ModuleNode(TypedElement e) {
		super(e);
	}
	
	public ModuleNode(TypedElement e, ModuleNode parent) {
		this(e);
	}
	
	public void createChildren(TypedElement element) {
		switch(element.getType()) {
		case MODULE_DEF:
			addChild("define", element);
			addChild("configuration", (TypedElement)element.getChild("parameter"));
			addChild("settings", (TypedElement)element.getChild("parameter"));
			addChild("results", (TypedElement)element.getChild("parameter"));
			addChild("serialize", (TypedElement)element.getChild("parameter"));
			addChild("input", (TypedElement)element.getChild("ports"));
			addChild("output", (TypedElement)element.getChild("ports"));
			break;
			
		case TYPE_DEF:
			addChild("define", element);
			break;
			
		case DEFINE_DIR:	
		case PARAMETER_DIR:
		case CONFIGURATION_DIR:
		case SETTINGS_DIR:
		case RESULTS_DIR:
		case SERIALIZE_DIR:
			addAllChildren(element);
			break;	
			
		case PORT_DIR: 
			addChild("input", element);
			addChild("output", element);
			break;
			
		case INPUT_PORT_DIR: 
		case OUTPUT_PORT_DIR:
			addAllChildren(element);
			break;
		
		case CLASS:
			addAllChildren((TypedElement)element.getChild("members"));
			break;
		}	
	}
	
	public String toString() {
		TypedElement element = (TypedElement)getUserObject();
		int type = element.getType();
		switch(element.getType()) {
		case MODULE_DEF:
		case TYPE_DEF:
			String name = element.getChildText("name");
			if(name == null) return (type == MODULE_DEF)?"module":"type";
			else return name;
			
		case DEFINE_DIR:
			return "defined objects";
				
		case CONFIGURATION_DIR: return "configuration";
		case SETTINGS_DIR: return "settings";
		case RESULTS_DIR: return "results";
		case SERIALIZE_DIR: return "state";
	
		case INPUT_PORT_DIR: return "inputs";
		case OUTPUT_PORT_DIR: return "outputs";
	
		default: 
			return element.getChildText("name");
		}
	}

	public TypedElement prepareForSaving(String fileName) {
		TypedElement root = ((ModuleNode)getRoot()).getElement();
		Set h = getAllImpliedTypes(root);
		TypedElement includes = (TypedElement)root.getChild("include");
		if(includes != null)
			includes.removeContent();
		else {
			includes = new TypedElement("include", TypedElement.INCLUDE_DIR);
			root.addContent(root.getContentSize()-2, includes);
		}
		
		Iterator it = h.iterator();
		while(it.hasNext()) {
			TypedElement included = new TypedElement("type", TypedElement.NAME);
			included.setText((String)it.next());
			includes.addContent(included);
			System.out.println("added "+included.getText());
		}
		System.out.println("prepared for saving");
		return root;
	}
	
	private Set getAllImpliedTypes(TypedElement e) {
		Set s = new HashSet();
		if(e.getAttribute("imprints") != null)
			s.add(e.getAttributeValue("name"));
		
		Iterator it = e.getChildren().iterator();
		while(it.hasNext())
			s.addAll(getAllImpliedTypes((TypedElement)it.next()));

		return s;
	}
	
	private void addAllChildren(TypedElement parent) {
		if(parent == null) return;
		
		Iterator it = parent.getChildren().iterator();
		while(it.hasNext())
			add(new ModuleNode((TypedElement)it.next()));
	}
	
	private void addChild(String name, TypedElement parent) {
		if(parent == null) return;
		
		TypedElement child = (TypedElement)parent.getChild(name);
		if(child != null)
			add(new ModuleNode(child));
	}
}
