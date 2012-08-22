package plugins.treeTable.dialog;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import xml.TypedElement;

public class ClassSelectionPanel implements ItemListener {
	private GridBagLayout gl;
	public ClassSelectionPanel(GridBagLayout gl) {
		this.gl = gl;
		initFields();
	}
	
	public void setTypes(List l) {
		listModel.set(l);
	}
	
	public void setSelectedItem(String name) {
		TypedElement selected = findElement(name, listModel.getAll());
		if(selected != null) 	types.setSelectedItem(selected);
	}
	
	public TypedElement getSelectedItem() {
		 TypedElement element = (TypedElement)types.getSelectedItem();
		 String imprint = getImprint();
		 if(imprint != null) element.setAttribute("imprints", imprint);
		 return element;
	}
	
	public String getItemText() {
		TypedElement e = (TypedElement)types.getSelectedItem();
		return e.getChildText("name");
	}
	
	public void setImprint(String s) {
		if(s != null && imprints.isVisible())
			imprints.setSelectedItem(s);
	}
	
	public String getImprint() {
		if(imprints.isVisible()) return (String)imprints.getSelectedItem();
		else return null;
	}
	
	public List getContent() {
		return contentList;
	}
	
	private LabelListModel listModel = new LabelListModel();
	private StringComboBoxModel imprintModel = new StringComboBoxModel();
	private JComboBox types = new JComboBox(), imprints = new JComboBox();
	private JLabel boxLabel, imprintLabel;
	private List contentList = new ArrayList();
	private void initFields() {
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		
		boxLabel = new JLabel("type:");
		imprintLabel = new JLabel("imprint:");
		
		gl.setConstraints(boxLabel, cLabel);
		gl.setConstraints(types, cText);
		gl.setConstraints(imprintLabel, cLabel);
		gl.setConstraints(imprints, cText);
		
		LabelComboBoxRenderer renderer = new LabelComboBoxRenderer();
		types.setModel(listModel);
		types.setRenderer(renderer);
		types.addItemListener(this);
		
		imprints.setModel(imprintModel);
		
		imprintLabel.setVisible(false);
		imprints.setVisible(false);
		
		contentList.add(boxLabel);
		contentList.add(types);
		contentList.add(imprintLabel);
		contentList.add(imprints);
	}

	public void itemStateChanged(ItemEvent event) {
		List globalItems = DialogHelper.getGlobalTypes();
		TypedElement e = (TypedElement)event.getItem();
			
		TypedElement selectedGlobalType = findElement(e.getChildText("name"), globalItems);
		if(selectedGlobalType != null) {
			List imprintList = DialogHelper.getTypeImprints(e.getChildText("name"));
			imprintModel.set(imprintList);
			imprintLabel.setVisible(true);
			imprints.setVisible(true);
			imprints.setSelectedIndex(0);
		} else {
			imprintLabel.setVisible(false);
			imprints.setVisible(false);
		}
	}
	
	private TypedElement findElement(String name, List list) {
		Iterator it = list.iterator();
		while(it.hasNext()) {
			TypedElement e = (TypedElement)it.next();
			if(e.getChildText("name").equals(name)) 
				return e;
		}
		return null;
	}
}
