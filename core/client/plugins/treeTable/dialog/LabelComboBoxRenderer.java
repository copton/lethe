package plugins.treeTable.dialog;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

import xml.*;

public class LabelComboBoxRenderer implements ListCellRenderer, Types {
	protected LabelCellRenderer renderer = new LabelCellRenderer();
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
		JLabel label = (JLabel)renderer.getListCellRendererComponent(list, value, 
				index, isSelected, hasFocus);
		
		TypedElement e = (TypedElement)value;
		if(e == null) return label;
		if(label == null) {
			System.out.println("Label is null???!?");
			return new JLabel();
		}
		
		switch(e.getType()) {
		case STRING: case INT: case BYTE: case LONG:
		case FLOAT: case DOUBLE: case BOOLEAN:
			label.setText(e.getName()); break;
		case TYPE_INSTANCE:
			label.setText(e.getAttributeValue("imprints")); break;
		default:	
			if(e.getAttribute("classname") != null) 
				label.setText(e.getAttributeValue("classname"));
			else if(e.getChild("name") != null && e.getChildText("name").length() == 0) 
				label.setText(e.getName());
		}
			
		return label;
	}

}
