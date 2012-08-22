package plugins.treeTable.dialog;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import java.awt.*;
import xml.TypedElement;

public class AddEdgePanel extends AddElement {
	public AddEdgePanel() {
		super();
		initPanel();
	}
	
	private JTextField name = new JTextField();
	private void initPanel() {
		GridBagLayout gl = new GridBagLayout();
		setLayout(gl);

		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);

		JLabel nameLabel = new JLabel("name:");
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		
		gl.setConstraints(nameLabel, cLabel);
		gl.setConstraints(name, cText);
			
		add(nameLabel); add(name);
	}

	public TypedElement getElement() {
		TypedElement e = new TypedElement("edge", TypedElement.EDGE);
		TypedElement nameChild = new TypedElement("name", TypedElement.NAME);
		nameChild.addContent(name.getText());
			
		e.addContent(nameChild);
		return e;
	}

	public void reset() {
		name.setText("");
	}
}

