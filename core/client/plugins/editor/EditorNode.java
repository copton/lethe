package plugins.editor;

import java.util.*;

import client.PackageGenerator;

import plugins.treeTable.XmlTreeNode;
import xml.TypedElement;
public class EditorNode extends XmlTreeNode {

	public EditorNode(TypedElement e) {
		super(e);
	}
	
	public EditorNode(TypedElement e, EditorNode parent) {
		this(e);
	}
	
	public void createChildren(TypedElement e) {
		switch(e.getType()) {
		case JOB:
			TypedElement graph = (TypedElement)e.getChild("graph");
			addChild("nodes", graph);
			addChild("edges", graph);
			addChild("phases", graph);
			addChild("settings", e);
			break;
		
		case NODES:
		case EDGES:
		case PHASES:
			addAllChildren(e);
			break;
	
		case ROUND_SETTINGS:
			addAllChildren((TypedElement)e.getChild("rounds"));
			break;
		}
	}
		
	public String toString() {
		TypedElement e = getElement();
		switch(e.getType()) {
		case JOB: return e.getChildText("name");
		case NODES: return "vertices";
		case EDGES: return "edges";
		case PHASES: return "phases";
		case EDGE:
		case NODE: return e.getChildText("name");
		case PHASE: return "phase "+e.getAttributeValue("nr");
		case ROUND_SETTINGS: return "settings";
		case ROUND: return "round "+e.getAttributeValue("nr");
		default: return "fnord"+e.getType();
		}
	}
	
	public void update(TypedElement e) {
		removeAllChildren();
		createChildren(e);
	}
	
	public TypedElement prepareForSaving(String name) {
		TypedElement root = ((EditorNode)getRoot()).getElement();
		if(root.getChild("core") == null) {
			TypedElement core = TypedElement.get(TypedElement.LOCATION);
			core.setName("core");
			root.addContent(5, core);
		}
		
		PackageGenerator generator = new PackageGenerator(PackageGenerator.JOB);
		generator.generatePackage(root, name);
		
		return null;
	}
	
	private void addAllChildren(TypedElement parent) {
		if(parent == null) return;
		
		Iterator it = parent.getChildren().iterator();
		while(it.hasNext())
			add(new EditorNode((TypedElement)it.next()));
	}
	
	private void addChild(String name, TypedElement parent) {
		if(parent == null) return;
		
		TypedElement child = (TypedElement)parent.getChild(name);
		if(child != null)
			add(new EditorNode(child));
	}
}
