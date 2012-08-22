package plugins.treeTable.dialog;

import java.awt.*;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;

public class NumberDefinitionDialog extends AbstractDialog  {
	private static final int [] handledTypes = {TypedElement.INT, TypedElement.BYTE, 
		TypedElement.LONG, TypedElement.FLOAT, TypedElement.DOUBLE};
	
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "default value:", "description:", "min. value:", "max. value:"};
	private static int NAME=0, DEFAULT=1, DESCRIPTION=2, MIN_VALUE=3, MAX_VALUE=4, ANZ_ITEMS=5;
	private TypedElement element;
	
	public NumberDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	public void init(TypedElement e) {
		String [] elementNames = {"name", "default", "description", "minValue", "maxValue"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		switch(e.getType()) {
		case TypedElement.INT: defaultsPanel.setBorder(BorderFactory.createTitledBorder("integer")); break;
		case TypedElement.LONG: defaultsPanel.setBorder(BorderFactory.createTitledBorder("long")); break;
		case TypedElement.BYTE: defaultsPanel.setBorder(BorderFactory.createTitledBorder("byte")); break;
		case TypedElement.FLOAT: defaultsPanel.setBorder(BorderFactory.createTitledBorder("float")); break;
		case TypedElement.DOUBLE: defaultsPanel.setBorder(BorderFactory.createTitledBorder("double")); break;
		}
		element = e;
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		element.getChild("default").setText(textFields[DEFAULT].getText());
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
		
		element.removeChild("minValue");
		element.removeChild("maxValue");
		
		if(textFields[MIN_VALUE].getText().length() > 0) 
			setElementValue("minValue", textFields[MIN_VALUE].getText(), TypedElement.SIZE, element);
			
		if(textFields[MAX_VALUE].getText().length() > 0) 
			setElementValue("maxValue", textFields[MAX_VALUE].getText(), TypedElement.SIZE, element);
		
		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private JPanel defaultsPanel = new JPanel();
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);
	
		JPanel restrictionPanel = new JPanel();
		restrictionPanel.setBorder(BorderFactory.createTitledBorder("restrictions"));
		
		GridBagLayout gl1 = new GridBagLayout();
		GridBagLayout gl2 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);
		restrictionPanel.setLayout(gl2);
	
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, i<=DESCRIPTION?gl1:gl2, i<=DESCRIPTION?defaultsPanel:restrictionPanel);
			addComponent(textFields[i], text, i<=DESCRIPTION?gl1:gl2, i<=DESCRIPTION?defaultsPanel:restrictionPanel);
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
	
		add(defaultsPanel);
		add(restrictionPanel);
	}

	public int[] handledTypes() {
		return handledTypes;
	}
}
