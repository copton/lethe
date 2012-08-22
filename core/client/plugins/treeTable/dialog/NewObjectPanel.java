package plugins.treeTable.dialog;

import java.awt.event.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.util.*;
import xml.TypedElement;

public class NewObjectPanel extends JPanel implements java.awt.event.ActionListener {
	private LabelListModel model;
	public NewObjectPanel(LabelListModel model) {
		super();
		this.model = model;
		setBorder(BorderFactory.createTitledBorder("add element"));
		
		initDialog();
		setVisible(false);
	}
	
	private JButton defaultButton;
	public void showPanel() {
		typeName.setText("");
		defaultButton = getRootPane().getDefaultButton();
		getRootPane().setDefaultButton(button[ADD]);
		setVisible(true);
	}
	
	public void hidePanel() {
		if(this.isVisible()) {
			setVisible(false);
			getRootPane().setDefaultButton(defaultButton);
		}
	}
	
	public void setAvailableTypes(List l) {
		availableTypes.setTypes(l);
	}
	
	private final static int CANCEL=0, ADD=1;
	private ClassSelectionPanel availableTypes;
	
	private JTextField typeName = new JTextField();
	private JButton [] button = new JButton[2];
	private void initDialog() {
		GridBagLayout gl = new GridBagLayout();
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		
		setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel(gl);
		
		availableTypes = new ClassSelectionPanel(gl);
		gl.setConstraints(typeName, cText);
		
		JLabel nameLabel = new JLabel("name");
		gl.setConstraints(nameLabel, cLabel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		gl.setConstraints(buttonPanel, cText);
		
		String [] buttonName = {"cancel", "add"};
		for(int i=CANCEL; i<=ADD; i++) {
			button[i] = new JButton(buttonName[i]);
			button[i].setActionCommand(Integer.toString(i));
			button[i].addActionListener(this);
			buttonPanel.add(button[i]);
		}
		
		Iterator it = availableTypes.getContent().iterator();
		int i=0;
		while(it.hasNext()) {
			JComponent c = (JComponent)it.next();
			gl.setConstraints(c, (i++%2==0)?cLabel:cText);
			mainPanel.add(c);
		}
		
		mainPanel.add(nameLabel);
		mainPanel.add(typeName);
		
		add(mainPanel);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void addElement() {
		TypedElement aktElement = (TypedElement)availableTypes.getSelectedItem();
		TypedElement newElement = null;
		
		switch(aktElement.getType()) {
		case TypedElement.STRING: case TypedElement.INT: case TypedElement.LONG:
		case TypedElement.BYTE: case TypedElement.FLOAT: case TypedElement.DOUBLE:
		case TypedElement.BOOLEAN:
			newElement = (TypedElement)aktElement.clone(); break;
		default: 
			newElement = new TypedElement("instance", TypedElement.INSTANCE);
			newElement.addContent(new TypedElement("name", TypedElement.NAME));
		
			newElement.setAttribute("name", aktElement.getChildText("name"));
			String imprint = availableTypes.getImprint();
			if(imprint != null) newElement.setAttribute("imprints", imprint);
		}
	
		newElement.getChild("name").setText(typeName.getText());
		model.add(newElement);
	}
	
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case CANCEL: hidePanel(); break;
		case ADD: addElement(); break;
		}
	}
}
