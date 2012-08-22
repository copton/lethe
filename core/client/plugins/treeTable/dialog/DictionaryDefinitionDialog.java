package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;
import xml.TypedElement;

public class DictionaryDefinitionDialog extends AbstractDialog  {
	private static final int [] handledTypes = {TypedElement.DICTIONARY};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "value type:", "description:"};
	private static int NAME=0, VALUE_TYPE=1, DESCRIPTION=2, ANZ_ITEMS=3;
	
	private TypedElement element;
	private XmlComboBox valueTypes = new XmlComboBox();
	public DictionaryDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	public void init(TypedElement e) {
		element = e;
		String [] elementNames = {"name", "valueType", "description"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		java.util.List l = new java.util.ArrayList();
		l.addAll(TypedElement.getSimpleTypes());
	//	l.addAll(DialogHelper.getLocalTypes(e));
	//	l.addAll(DialogHelper.getGlobalTypes());
		
		LabelListModel listModel = (LabelListModel)valueTypes.getModel();
		listModel.set(l);
		
		valueTypes.setSelectedString(e.getChildText(elementNames[VALUE_TYPE]));
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		element.getChild("valueType").setText(valueTypes.getSelectedString());
		
		if(textFields[DESCRIPTION].getText().length() > 0) {
			if(element.getChild("description") == null)
				element.addContent(new TypedElement("description", TypedElement.DESCRIPTION));
			element.getChild("description").setText(textFields[DESCRIPTION].getText());
		}

		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("dictionary"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == VALUE_TYPE)
				addComponent(valueTypes, text, gl1, defaultsPanel);
			else addComponent(textFields[i], text, gl1, defaultsPanel);
			}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[VALUE_TYPE].setFont(boldFont);
	
		add(defaultsPanel);
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}
}
