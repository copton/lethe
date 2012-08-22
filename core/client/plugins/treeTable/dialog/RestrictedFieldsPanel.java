package plugins.treeTable.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import java.util.*;

import xml.*;

public class RestrictedFieldsPanel extends JPanel implements ItemListener, Types {

	public RestrictedFieldsPanel(JComboBox box) {
		super();
		initDialog();
		box.addItemListener(this);
	}

	public List getElements() {
		List l = new ArrayList();
		l.add(stringPanel);
		l.add(numberPanel);
		return l;
	}
	
	private static final int LENGTH=0, MIN_LENGTH=1, MAX_LENGTH=2;
	private static final int MIN_VALUE=0, MAX_VALUE=1;
	public void init(TypedElement e) {
		initField(length[LENGTH], e.getChildText("length"));
		initField(length[MIN_LENGTH], e.getChildText("minLength"));
		initField(length[MAX_LENGTH], e.getChildText("maxLength"));
		
		initField(value[MIN_VALUE], e.getChildText("minValue"));
		initField(value[MAX_VALUE], e.getChildText("maxValue"));
	}
	
	public List getElement() {
		List l = new ArrayList();
		switch(type) {
		case STRING_RESTRICTION: 
			if(!addElement(length[LENGTH], "length", l)) {
				addElement(length[MIN_LENGTH], "minLength", l);
				addElement(length[MAX_LENGTH], "maxLength", l);
			}
			break;
		case NUMBER_RESTRICTION: 
			addElement(value[MIN_VALUE], "minValue", l);
			addElement(value[MAX_VALUE], "maxValue", l);
		}
		return l;
	}
	
	private static final int NO_RESTRICTION=0, STRING_RESTRICTION=1, NUMBER_RESTRICTION=2;
	public void itemStateChanged(ItemEvent item) {
		TypedElement e = (TypedElement)item.getItem();
		showRestrictions(e);
	}
	
	public void showRestrictions(TypedElement e) {
		switch(e.getType()) {
		case STRING: showRestrictions(STRING_RESTRICTION); break;
		case INT: case LONG: case BYTE: case FLOAT: 
			case DOUBLE: showRestrictions(NUMBER_RESTRICTION); break;
		default: showRestrictions(NO_RESTRICTION);
		}	
	}
	
	private int type;
	private JPanel stringPanel=new JPanel(), numberPanel=new JPanel();
	private void showRestrictions(int type) {
		this.type = type;
		stringPanel.setVisible(type==STRING_RESTRICTION?true:false);
		numberPanel.setVisible(type==NUMBER_RESTRICTION?true:false);
	}
	
	
	private JTextField [] length = new JTextField[3];
	private JTextField [] value = new JTextField[2];
	private void initDialog() {
		String [] stringLabels = {"length:", "min. length:", "max. length:"};
		String [] numberLabels = {"min. value:", "max. value:"};
		
		setFields(stringLabels, length, stringPanel);
		setFields(numberLabels, value, numberPanel);
	}
	
	private void setFields(String [] labels, JTextField [] fields, JPanel panel) {
		GridBagLayout gl = new GridBagLayout();
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		
		panel.setLayout(gl);
		panel.setBorder(BorderFactory.createTitledBorder("type restrictions"));
		
		for(int i=0; i<labels.length; i++) {
			JLabel label = new JLabel(labels[i]);
			fields[i] = new JTextField();
			
			gl.setConstraints(label, cLabel);
			gl.setConstraints(fields[i], cText);
			
			panel.add(label);
			panel.add(fields[i]);
		}
	}
	
	private void initField(JTextField field, String text) {
		field.setText(text);
	}
	
	private boolean addElement(JTextField textField, String elementName, List l) {
		if(textField.getText().length() == 0) return false;
		
		TypedElement e = new TypedElement(elementName, SIZE);
		e.setText(textField.getText());
		l.add(e);
		return true;
	}
}
