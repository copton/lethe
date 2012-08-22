package plugins.treeTable.dialog;

import java.awt.*;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;


public class BooleanDefinitionDialog extends AbstractDialog  {
	private static final int [] handledTypes = {TypedElement.BOOLEAN};
	
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private JComboBox trueFalseBox;
	private static String [] names = {"name", "default value:", "description:"};
	private static int NAME=0, DEFAULT=1, DESCRIPTION=2, ANZ_ITEMS=3;
	private TypedElement element;
	
	public BooleanDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	public void init(TypedElement e) {
		String [] elementNames = {"name", "default", "description"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		trueFalseBox.setSelectedItem(e.getChildText(elementNames[DEFAULT]));
		element = e;
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		element.getChild("default").setText(trueFalseBox.getSelectedItem().toString());
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
			
		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("boolean"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);
		
		trueFalseBox = new JComboBox();
		trueFalseBox.addItem("true");
		trueFalseBox.addItem("false");
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == DEFAULT) 
				addComponent(trueFalseBox, text, gl1, defaultsPanel);
			else addComponent(textFields[i], text, gl1, defaultsPanel);
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
	
		add(defaultsPanel);
	}

	public int[] handledTypes() {
		return handledTypes;
	}
}
