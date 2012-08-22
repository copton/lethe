package plugins.treeTable.dialog;

import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JComboBox;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseListener;
import xml.TypedElement;


public abstract class AbstractDialog extends JPanel implements DialogPluggable {

	public final String [] simpleTypes = {"string", "int", "long", "byte", "float", "double", "boolean"};
	public void setElementValue(String name, String value, int type, TypedElement parent) {
		if(value == null) value = "";
		
		TypedElement child = (TypedElement)parent.getChild(name);
		if(child == null) {
			child = new TypedElement(name, type);
			parent.addContent(child);
		}
		child.setText(value);
	}
	
	public JPanel getPanel() {
		return this;
	}
	
	public void addValueTypes(java.util.List elements, JComboBox box, TypedElement element) {
		Iterator it = elements.iterator();
		while(it.hasNext()) {
			TypedElement type = (TypedElement)it.next();
			String typeName = type.getChildText("name");
			if(!typeName.equals(element.getChildText("name"))) {
				box.removeItem(typeName);
				box.addItem(typeName);
			}
		}
	}
	
	public boolean hasChanges() {
		return true;
	}
	
	public void setListShortcut(MouseListener listener) {}
	
	public void addComponent(Component c, GridBagConstraints constr, GridBagLayout gl, JPanel panel) {
		gl.setConstraints(c, constr);
		panel.add(c);
	}
}
