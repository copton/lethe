package plugins.treeTable.dialog;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import java.util.*;

import util.DialogHelper;
import xml.TypedElement;

public class AddInstancePanel extends AddElement {
	public AddInstancePanel() {
		super();
		initPanel();
	}
	
	private JTextField name = new JTextField();
	private ClassSelectionPanel type;
	private void initPanel() {
		GridBagLayout gl = new GridBagLayout();
		setLayout(gl);

		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);

		type = new ClassSelectionPanel(gl);
		
		String [] labelName = {"name:"};
		JLabel [] labels = new JLabel[labelName.length];
		for(int i=0; i<labels.length; i++) {
			labels[i] = new JLabel(labelName[i]);
			labels[i].setFont(labels[i].getFont().deriveFont(Font.BOLD));
			gl.setConstraints(labels[i], cLabel);
		}
		
		gl.setConstraints(name, cText);
			
		Iterator it = type.getContent().iterator();
		while(it.hasNext()) add((JComponent)it.next());
		add(labels[0]); add(name);
	}

	public TypedElement getElement() {
		TypedElement e = (TypedElement)type.getSelectedItem();
		switch(e.getType()) {
		case TypedElement.STRING: case TypedElement.INT: case TypedElement.LONG:
		case TypedElement.BYTE: case TypedElement.FLOAT: case TypedElement.DOUBLE:
		case TypedElement.BOOLEAN:
			e.getChild("name").setText(name.getText());
			return (TypedElement)e.clone();
			
		default:
			TypedElement newInstance = new TypedElement("instance", TypedElement.INSTANCE);
			newInstance.addContent(new TypedElement("name", name.getText(), TypedElement.NAME));
			newInstance.setAttribute("name", e.getChildText("name"));
			String imprint = e.getAttributeValue("imprints");
			if(imprint != null) {
				System.out.println("imprint of element "+e.getChildText("name")+" is != null");
				newInstance.setAttribute("imprints", imprint);
			} else 	System.out.println("imprint of element "+e.getChildText("name")+" is null");
			return newInstance;
		}
	}

	public void reset() {
		name.setText("");
	}
	
	public void init(TypedElement e) {
		List l = new ArrayList();
		l.addAll(TypedElement.getSimpleTypes());
		l.addAll(DialogHelper.getLocalTypes(e));
		l.addAll(DialogHelper.getGlobalTypes());
		type.setTypes(l);
	}
}
