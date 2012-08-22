package plugins.moduleGeneration;

import xml.TypedElement;

public class InstanceNode extends ModuleNode {

	public InstanceNode(TypedElement e) {
		super(e);
	}
	
	public InstanceNode(TypedElement e, InstanceNode parent) {
		this(e);
	}

	public void createChildren(TypedElement e) {
		switch(e.getType()) {
		case MODULE_INSTANCE:
			addChild("configuration", (TypedElement)e.getChild("parameter"));
			addChild("settings", (TypedElement)e.getChild("parameter"));
			break;
			
		case TYPE_INSTANCE:
			addChild("export", e);
			break;
		}
	}

	public String toString() {
		TypedElement e = getElement();
		switch(e.getType()) {
		case MODULE_INSTANCE:
		case TYPE_INSTANCE:
			return e.getName();
		case CONFIGURATION_DIR: return "configuration";
		case SETTINGS_DIR: return "settings";
		}
		return "fnord";
	}
	
	public TypedElement prepareForSaving(String fileName) {
		return ((InstanceNode)getRoot()).getElement();
	}
	
	private void addChild(String name, TypedElement parent) {
		if(parent == null) return;
		
		TypedElement child = (TypedElement)parent.getChild(name);
		if(child != null && child.getChildren().size() > 0)
			add(new InstanceNode(child));
	}
}
