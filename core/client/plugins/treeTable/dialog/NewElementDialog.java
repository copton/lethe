package plugins.treeTable.dialog;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.*;

import xml.TypedElement;

public class NewElementDialog extends JDialog implements java.awt.event.ActionListener {
	public NewElementDialog(List availableTypes) {
		super();
		setTitle("neues Element");
		elementType = new JComboBox(availableTypes.toArray());
		initDialog();
	}
	
	public TypedElement showDialog() {
		return null;
	}

	// Buttons
	private JButton [] buttons = new JButton[2];
	private String [] buttonNames = {"cancel", "apply"};
	private static final int CANCEL=0, OK=1;
	
	// Textfields
	private JLabel [] labels = new JLabel[2];
	private String [] labelNames = {"name:", "type:"};
	private JTextField textName = new JTextField();
	private JComboBox elementType;
	private void initDialog() {
		getContentPane().setLayout(new BorderLayout());
		
		GridBagLayout gl = new GridBagLayout();
		JPanel mainPanel = new JPanel(gl);
		
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		
		for(int i=0; i<2; i++) {
			labels[i] = new JLabel(labelNames[i]);
			gl.setConstraints(labels[i], cLabel);
		}
		
		gl.setConstraints(textName, cText);
		gl.setConstraints(elementType, cText);
		
		mainPanel.add(labels[0]);
		mainPanel.add(textName);
		mainPanel.add(labels[1]);
		mainPanel.add(elementType);
		
		getContentPane().add(mainPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		for(int i=0; i<buttons.length; i++) {
			buttons[i] = new JButton(buttonNames[i]);
			buttons[i].setActionCommand(Integer.toString(i));
			buttons[i].addActionListener(this);
			buttonPanel.add(buttons[i]);
		}
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case CANCEL: System.out.println("cancel"); break;
		case OK: System.out.println("ok"); break;
		}
	}
}
