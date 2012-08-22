package plugins.treeTable.dialog;

import java.awt.Component;
import javax.swing.*;
import xml.*;

public class LabelCellRenderer extends JLabel implements ListCellRenderer {
	private ImageIcon [] imageIcons = XmlHelper.elementIcons;
	
	public LabelCellRenderer() {
		super();
		setOpaque(true);
		setHorizontalAlignment(LEFT);
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean arg4) {
		if(isSelected) 
			setBackground(list.getSelectionBackground());
		else setBackground(list.getBackground());
	
		TypedElement element = (TypedElement)value;
		if(element == null) return this;
		setIcon(imageIcons[element.getType()]);
		setText(element.getChildText("name"));
		
		return this;
	}

}
