package plugins.treeTable.dialog;

import xml.*;
public class XmlComboBox extends javax.swing.JComboBox implements Types {

	public XmlComboBox() {
		setRenderer(new LabelComboBoxRenderer());
		setModel(new LabelListModel());
	}
	
	public String getSelectedString() {
		TypedElement e = (TypedElement)getSelectedItem();
		if(e == null) return "";

		if(e.getChildText("name").length() == 0) return e.getName();
		return e.getChildText("name");
	}
	
	public LabelListModel getListModel() {
		return (LabelListModel)getModel();
	}
	
	public void setSelectedString(String s) {
		LabelListModel model = (LabelListModel)getModel();
		java.util.Iterator it = model.getAll().iterator();
		while(it.hasNext()) {
			TypedElement e = (TypedElement)it.next();
			if(e.getName().equals(s) || e.getChildText("name").equals(s)) {
				setSelectedItem(e);
				return;
			}
		}
		
		setSelectedIndex(0);
	}
}
