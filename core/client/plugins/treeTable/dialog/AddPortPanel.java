package plugins.treeTable.dialog;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;

import plugins.treeTable.EditorDialog;

import java.util.*;

import util.DialogHelper;
import xml.TypedElement;

public class AddPortPanel extends AddElement {
	public AddPortPanel() {
		super();
		initPanel();
	}
	
	private JTextField name = new JTextField();
	private XmlComboBox type = new XmlComboBox();
	private void initPanel() {
		GridBagLayout gl = new GridBagLayout();
		setLayout(gl);

		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);

		String [] labelName = {"name:", "type:"};
		JLabel [] labels = new JLabel[labelName.length];
		for(int i=0; i<labels.length; i++) {
			labels[i] = new JLabel(labelName[i]);
			labels[i].setFont(labels[i].getFont().deriveFont(Font.BOLD));
			gl.setConstraints(labels[i], cLabel);
		}
		
		gl.setConstraints(name, cText);
		gl.setConstraints(type, cText);
		
		List l = new ArrayList();
		l.addAll(DialogHelper.getGlobalTypes());
		type.getListModel().set(l);

		add(labels[1]); add(type);
		add(labels[0]); add(name);
	}

	public TypedElement getElement() {
		TypedElement e = new TypedElement("port", TypedElement.PORT);
		e.addContent(new TypedElement("name", name.getText(), TypedElement.NAME));
		e.addContent(new TypedElement("type", type.getSelectedString(), TypedElement.FIELD));
		e.addContent(new TypedElement("usedType", TypedElement.FIELD));
		e.addContent(new TypedElement("blocksize", "1", TypedElement.SIZE));
		
		return e;
	}

	public void reset() {
		name.setText("");
		
		List l = new ArrayList();
		l.addAll(DialogHelper.getGlobalTypes());
		type.getListModel().set(l);
		
		type.setSelectedIndex(0);
	}
}
