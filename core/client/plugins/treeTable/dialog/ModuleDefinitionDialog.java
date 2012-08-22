package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModuleDefinitionDialog extends AbstractDialog  {
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "type:", "description:", "author:", "version:", "date:"};
	private static int NAME=0, TYPE=1, DESCRIPTION=2, AUTHOR=3, VERSION=4, DATE=5, ANZ_ITEMS=6;
	
	private static final String [] moduleTypes = {"Quelle", "Kodierer", "Kanal", "Dekodierer", "Senke"};
	private JComboBox moduleType = new JComboBox(moduleTypes);
	private TypedElement element;
	
	public ModuleDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private static final	DateFormat localDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private static final DateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private boolean isInstance;
	public void init(TypedElement e) {
		element = e;
		
		String [] elementNames = {"name", "type", "description", "author", "version", "date"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		isInstance = (element.getType() == TypedElement.MODULE_INSTANCE);
		
		if(isInstance)
			textFields[NAME].setText(element.getName());
		
		textFields[NAME].setEditable(!isInstance);
		moduleType.setEnabled(!isInstance);
		
		Date date;
		if(textFields[DATE].getText().length() == 0) date = new Date();
		else try {
			date = xmlDateFormat.parse(textFields[DATE].getText());
		} catch (Exception ex) { date = new Date(); }
		textFields[DATE].setText(localDateFormat.format(date));
			
		java.util.List l = new java.util.ArrayList();
		ComboBoxModel model = moduleType.getModel();
		for(int i=0;i<model.getSize(); i++)
			l.add(model.getElementAt(i));
		
		String moduleTypeName = e.getChildText(elementNames[TYPE]);
		if(!l.contains(moduleTypeName)) {
			l.add(moduleTypeName);
			moduleType = new JComboBox(l.toArray());
			moduleType.setEditable(true);
		}
		
		moduleType.setSelectedItem(moduleTypeName);
	}

	public TypedElement getElement() {
		if(!isInstance)
			setElementValue("name", textFields[NAME].getText(), TypedElement.NAME, element);
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
		setElementValue("author", textFields[AUTHOR].getText(), TypedElement.META_DATA, element);
		setElementValue("version", textFields[VERSION].getText(), TypedElement.META_DATA, element);
		String selectedItem = moduleType.getSelectedItem().toString();
		setElementValue("type", selectedItem, TypedElement.META_DATA, element);
		
		Date date;
		if(textFields[DATE].getText().length() == 0) date = new Date();
		else try {
			date = localDateFormat.parse(textFields[DATE].getText());
		} catch (Exception ex) { date = new Date(); }
		setElementValue("date", xmlDateFormat.format(date), TypedElement.META_DATA, element);
		
		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("module"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);

		moduleType.setEditable(true);
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == TYPE)
				addComponent(moduleType, text, gl1, defaultsPanel);
			else addComponent(textFields[i], text, gl1, defaultsPanel);			
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		
		add(defaultsPanel);
	}

	private static final int [] handledTypes = {TypedElement.MODULE_DEF, TypedElement.MODULE_INSTANCE};
	public int[] handledTypes() {
		return handledTypes;
	}
}
