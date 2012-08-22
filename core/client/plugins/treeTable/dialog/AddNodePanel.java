package plugins.treeTable.dialog;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import xml.TypedElement;

public class AddNodePanel extends AddElement implements ItemListener {
	public AddNodePanel() {
		super();
		initPanel();
	}
	
	private JTextField name = new JTextField();
	private XmlComboBox module = new XmlComboBox();
	private JComboBox instance = new JComboBox(new StringComboBoxModel());
	private void initPanel() {
		GridBagLayout gl = new GridBagLayout();
		setLayout(gl);

		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);

		String [] labelName = {"name:", "module:", "imprint:"};
		JLabel [] labels = new JLabel[labelName.length];
		for(int i=0; i<labels.length; i++) {
			labels[i] = new JLabel(labelName[i]);
			labels[i].setFont(labels[i].getFont().deriveFont(Font.BOLD));
			gl.setConstraints(labels[i], cLabel);
		}
		
		gl.setConstraints(name, cText);
		gl.setConstraints(module, cText);
		gl.setConstraints(instance, cText);
		
		module.addItemListener(this);
		module.getListModel().set(DialogHelper.getModules());
			
		add(labels[0]); add(name);
		add(labels[1]); add(module);
		add(labels[2]); add(instance);
	}

	public TypedElement getElement() {
		TypedElement e = new TypedElement("node", TypedElement.NODE);
		TypedElement nameChild = new TypedElement("name", TypedElement.NAME);
		nameChild.addContent(name.getText());
		
		TypedElement moduleChild = new TypedElement("module", TypedElement.MEMBERS);
		TypedElement instanceChild = new TypedElement("instance", TypedElement.MEMBERS);
		TypedElement location = TypedElement.get(TypedElement.LOCATION);
		
		
		moduleChild.setText(module.getSelectedString());
		instanceChild.setText((String)instance.getSelectedItem());
		
		e.addContent(nameChild);
		e.addContent(moduleChild);
		e.addContent(instanceChild);
		e.addContent(location);
		e.addContent(new TypedElement("configuration", TypedElement.CONFIGURATION_DIR));
		return e;
	}

	public void itemStateChanged(ItemEvent event) {
		String selectedType = module.getSelectedString();
		StringComboBoxModel instanceModel = (StringComboBoxModel)instance.getModel();
		instanceModel.set(DialogHelper.getModuleImprintNames(selectedType));
		if(instance.getModel().getSize() > 0)
			instance.setSelectedIndex(0);
	}

	public void reset() {
		name.setText("");
		module.getListModel().set(DialogHelper.getModules());
		if(module.getModel().getSize() > 0)
			module.setSelectedIndex(0);
	}
}
