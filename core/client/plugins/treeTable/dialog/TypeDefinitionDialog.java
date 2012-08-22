package plugins.treeTable.dialog;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.swing.*;

import plugins.treeTable.EditorDialog;
import xml.TypedElement;


public class TypeDefinitionDialog extends AbstractDialog  {
	private static final int [] handledTypes = {TypedElement.TYPE_DEF};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "type:", "description:", "author:", "version:", "date:"};
	private static int NAME=0, TYPE=1, DESCRIPTION=2, AUTHOR=3, VERSION=4, DATE=5, ANZ_ITEMS=6;
	
	private TypedElement element;
	private XmlComboBox typeDef = new XmlComboBox();
	
	public TypeDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private static final	DateFormat localDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private static final DateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public void init(TypedElement e) {
		String [] elementNames = {"name", "type", "description", "author", "version", "date"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		Date date;
		if(textFields[DATE].getText().length() == 0) date = new Date();
		else try {
			date = xmlDateFormat.parse(textFields[DATE].getText());
		} catch (Exception ex) { date = new Date(); }
		textFields[DATE].setText(localDateFormat.format(date));

		element = e;
		typeDef.setSelectedString(getClassType(e.getChildText("export")));
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
		setElementValue("author", textFields[AUTHOR].getText(), TypedElement.META_DATA, element);
		setElementValue("version", textFields[VERSION].getText(), TypedElement.META_DATA, element);
		setElementValue("export", textFields[NAME].getText(), TypedElement.META_DATA, element);
			
		Date date;
		if(textFields[DATE].getText().length() == 0) date = new Date();
		else try {
			date = localDateFormat.parse(textFields[DATE].getText());
		} catch (Exception ex) { date = new Date(); }
		setElementValue("date", xmlDateFormat.format(date), TypedElement.META_DATA, element);

		TypedElement defineDir = (TypedElement)element.getChild("define");
		TypedElement defineElement = findElement(defineDir, textFields[NAME].getText());

		String selectedType = typeDef.getSelectedString();
		if(defineElement != null && defineElement.getType() != TypedElement.getTypeForString(selectedType)) {
			defineDir.removeContent(defineElement);
			defineElement = null;
		}
		
		if(defineElement == null) {
			defineElement = TypedElement.get(TypedElement.getTypeForString(selectedType));
			defineElement.getChild("name").setText(textFields[NAME].getText());
			defineDir.addContent(0, defineElement);
		} 
		
		return element;
	}

	private String getClassType(String className) {
		System.out.println("className: "+className);
		java.util.Iterator it = element.getChild("define").getChildren().iterator();
		while(it.hasNext()) {
			TypedElement child = (TypedElement)it.next();
			if(child.getChildText("name").equals(className)) return child.getName();
		}
		
		return "class";
	}
	
	public JPanel getPanel() {
		return this;
	}
	
	private TypedElement findElement(TypedElement dir, String name) {
		java.util.Iterator it = dir.getChildren().iterator();
		while(it.hasNext()) {
			TypedElement child = (TypedElement)it.next();
			if(name.equals(child.getChildText("name"))) return child;
		}
		
		return null;
	}
	
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("type"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);

		typeDef.getListModel().set(TypedElement.getComplexTypes());
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == TYPE)
				addComponent(typeDef, text, gl1, defaultsPanel);			
			else addComponent(textFields[i], text, gl1, defaultsPanel);			
						
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[TYPE].setFont(boldFont);
		
		add(defaultsPanel);
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}
	
	public void addValueTypes(java.util.List elements, JComboBox box, TypedElement element) {
		Iterator it = elements.iterator();
		while(it.hasNext()) {
			TypedElement type = (TypedElement)it.next();
			String typeName = type.getChildText("name");
			box.addItem(typeName);
		}
	}
}
