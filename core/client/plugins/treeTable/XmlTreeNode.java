package plugins.treeTable;

import javax.swing.tree.*;
import xml.TypedElement;

abstract public class XmlTreeNode extends DefaultMutableTreeNode implements xml.Types {	
	public XmlTreeNode() {}
	
	public XmlTreeNode(TypedElement element) {
		super(element);
		createChildren(element);
	}
	
	abstract public void createChildren(TypedElement e);
	abstract public TypedElement prepareForSaving(String fileName); 
	
	public TypedElement getElement() {
		return (TypedElement)getUserObject();
	}
	
	public void update(TypedElement e) {
		removeAllChildren();
		createChildren(e);
	}
	
	public javax.swing.JPopupMenu getPopup() {
		return null;
	}
	
	public String getToolTip() {
		TypedElement e = getElement();
		String msg = null;
		String description = null;
		
		switch(e.getType()) {
		case STRING: case BOOLEAN:
		case INT: case LONG: case BYTE: case FLOAT: case DOUBLE:
			msg = "type: "+e.getName()+", default: "+e.getChildText("default");
			description = e.getChildText("description");	
			break;
	
		case STRING_INSTANCE: case BOOLEAN_INSTANCE: case INT_INSTANCE:
		case LONG_INSTANCE: case BYTE_INSTANCE: case FLOAT_INSTANCE: 
		case DOUBLE_INSTANCE: 
			msg = "type: "+e.getAttributeValue("type");
			description = e.getAttributeValue("description");
			break;
	
		case ENUM:
			msg = "type: "+e.getName() + ", elements: "+getEnumElements(e);
			description = e.getChildText("description");	
			break;
		
		case ENUM_INSTANCE:
			msg = "type: enum, elements: "+e.getAttributeValue("elements");
			description = e.getAttributeValue("description");
			break;		
			
		case CLASS_INSTANCE:
			msg = "type: "+e.getAttributeValue("classname");
			description = e.getAttributeValue("description");
			break;
			
			/*
		case CLASS: break;
		case ARRAY: break;
		case TABLE: break;
		case DICTIONARY: break;
		*/
		default: msg = "fnord";	
		}
		
		if(description != null && description.length() > 0)
			msg += ", description: "+description;
		
		return msg;
	}
	
	private String getEnumElements(TypedElement e) {
		StringBuffer st = new StringBuffer();
		java.util.Iterator it = e.getChild("members").getChildren().iterator();
		if(it.hasNext()) {
			st.append(((TypedElement)it.next()).getText());
				
			while(it.hasNext()) {
				st.append(", ");
				st.append(((TypedElement)it.next()).getText());
			}
		}
		return st.toString();
	}
}
