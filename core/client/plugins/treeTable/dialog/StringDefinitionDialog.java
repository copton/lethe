package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;


public class StringDefinitionDialog extends AbstractDialog  {
	private static final int [] handledTypes = {TypedElement.STRING};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "default value:", "description:", "length:", "min. length:", "max. length"};
	private static int NAME=0, DESCRIPTION=2, LENGTH=3, MIN_LENGTH=4, MAX_LENGTH=5, ANZ_ITEMS=6;
	private TypedElement e;
	
	public StringDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private static final String [] elementNames = {"name", "default", "description", "length", "minLength", "maxLength"};
	public void init(TypedElement e) {
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		this.e = e;
	}

	public TypedElement getElement() {
		int [] types = {TypedElement.NAME, TypedElement.DEFAULT_VALUE, TypedElement.DESCRIPTION,
				TypedElement.SIZE, TypedElement.SIZE, TypedElement.SIZE};
		
		for(int i=0; i<elementNames.length; i++) 
			setElementValue(elementNames[i], textFields[i].getText(), types[i], e);
		
		
		if(textFields[LENGTH].getText().length() > 0) {
			e.removeChild("minLength");
			e.removeChild("maxLength");
		} else {
			e.removeChild("length");
			if(textFields[MIN_LENGTH].getText().length() == 0)
				e.removeChild("minLength");
			if(textFields[MAX_LENGTH].getText().length() == 0)
				e.removeChild("maxLength");
		}
	
		return e;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("string"));
		
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
