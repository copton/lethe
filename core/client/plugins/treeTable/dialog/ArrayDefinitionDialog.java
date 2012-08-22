package plugins.treeTable.dialog;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import java.util.*;

import util.DialogHelper;
import xml.TypedElement;
import xml.XmlHelper;


public class ArrayDefinitionDialog extends AbstractDialog {
	private static final int [] handledTypes = {TypedElement.ARRAY};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name", "element type:", "default:", "description:", "fields:", "min. fields:", "max. fields:"};

	private static int NAME=0, ELEMENT_TYPE=1, DEFAULT=2, DESCRIPTION=3, SIZE=4, MIN_SIZE=5, MAX_SIZE=6, ANZ_ITEMS=7;

	private TypedElement element;
	private XmlComboBox elementTypes = new XmlComboBox();
	private RestrictedFieldsPanel restricted = new RestrictedFieldsPanel(elementTypes);
	public ArrayDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private static final String [] elementNames = {"name", "elementType", "default", "description", "size", "minSize", "maxSize"};
	public void init(TypedElement e) {
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		element = e;
		
		List type = new ArrayList();
		type.addAll(TypedElement.getSimpleTypes());
		type.addAll(DialogHelper.getLocalTypes(e));
		type.addAll(DialogHelper.getGlobalTypes());
		type.remove(element);
		boxModel.set(type);
		
		String selectedItem = e.getChildText(elementNames[ELEMENT_TYPE]);
		elementTypes.setSelectedString(selectedItem);
		textFields[DEFAULT].setText(e.getChildText(elementNames[DEFAULT]));
		
		restricted.init(e);
		restricted.showRestrictions((TypedElement)elementTypes.getSelectedItem());
	}

	public TypedElement getElement() {
		element.removeContent();
		
		setElementValue("name", textFields[NAME].getText(), TypedElement.NAME, element);
		setElementValue("elementType", elementTypes.getSelectedString(), TypedElement.ELEMENT_TYPE, element);
		element.addContent(getDefaultElement());
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
		
		for(int i=SIZE; i<=MAX_SIZE; i++)
			element.removeChild(elementNames[i]);
			
		if(textFields[SIZE].getText().length() > 0) 
			setElementValue("size", textFields[SIZE].getText(), TypedElement.SIZE, element);
		else {
			if(textFields[MIN_SIZE].getText().length() > 0) 
				setElementValue("minSize", textFields[MIN_SIZE].getText(), TypedElement.SIZE, element);
					
			if(textFields[MAX_SIZE].getText().length() > 0) 
				setElementValue("maxSize", textFields[MAX_SIZE].getText(), TypedElement.SIZE, element);
		}
			
		element.addContent(restricted.getElement());
		return element;
	}

	public JPanel getPanel() {
		return this;
	}

	private XmlComboBox defaultEnum = new XmlComboBox();
	private ConfigurationPanel defaultConfig = new ConfigurationPanel();
	private TypedElement getDefaultElement() {
		TypedElement e = new TypedElement("default", TypedElement.DEFAULT_VALUE);
		
		int type = ((TypedElement)elementTypes.getSelectedItem()).getType();
		switch(type) {
		case TypedElement.STRING: case TypedElement.INT: case TypedElement.LONG: 
		case TypedElement.FLOAT: case TypedElement.DOUBLE: case TypedElement.BYTE: 
		case TypedElement.BOOLEAN: 
			e.setText(textFields[DEFAULT].getText()); break;
		case TypedElement.ENUM: 
			e.setText(((TypedElement)defaultEnum.getSelectedItem()).getText()); break;
		case TypedElement.ARRAY: case TypedElement.TABLE:
		case TypedElement.DICTIONARY: case TypedElement.CLASS:
			e = defaultConfig.getElement();
			e.setName("default");
		}
		return e;
	}
	
	private LabelListModel boxModel;
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		GridBagLayout gl1 = new GridBagLayout();
		JPanel mainPanel = new JPanel(gl1);
		mainPanel.setBorder(BorderFactory.createTitledBorder("array"));
	
		boxModel = (LabelListModel)elementTypes.getModel();
		defaultConfig.setBorder(BorderFactory.createEmptyBorder());
		elementTypes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				defaultChanged();
			}
		});
		
		defaultEnum.setRenderer(new ListCellRenderer() {
			private JLabel label = new JLabel();
			private final ImageIcon stringIcon = XmlHelper.elementIcons[TypedElement.STRING];
			public Component getListCellRendererComponent(JList list, Object value, int arg2, boolean isSelected, boolean arg4) {
				TypedElement e = (TypedElement)value;
				label.setOpaque(true);
				if(isSelected) label.setBackground(list.getSelectionBackground());
				else label.setBackground(list.getBackground());
				
				label.setText(e.getText());
				label.setIcon(stringIcon);
				return label;
			}
		});
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, mainPanel);
			if(i == ELEMENT_TYPE)
				addComponent(elementTypes, text, gl1, mainPanel);
			else addComponent(textFields[i], text, gl1, mainPanel);
			
			if(i == DEFAULT) {
				addComponent(defaultEnum, text, gl1, mainPanel);
				addComponent(defaultConfig, text, gl1, mainPanel);
			}
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[ELEMENT_TYPE].setFont(boldFont);
		
		add(mainPanel);
		Iterator it = restricted.getElements().iterator();
		while(it.hasNext()) add((JPanel)it.next());
	}
	
	private void defaultChanged() {
		TypedElement selected = (TypedElement)elementTypes.getSelectedItem();
		boolean showText, showEnum, showConfig;
		showText = showEnum = showConfig = false;
		
		switch(selected.getType()) {
		case TypedElement.ENUM:
			showEnum = true;
			defaultEnum.getListModel().set(selected.getChild("members").getChildren());
			break;
		case TypedElement.CLASS: 	
		case TypedElement.DICTIONARY: 
		case TypedElement.ARRAY: 
		case TypedElement.TABLE:
			showConfig = true;
			defaultConfig.init(TypedElement.createInstance(selected));
			break;
		default: 
			showText = true;
			textFields[DEFAULT].setText(selected.getChildText("default"));
			break;
		}
		
		defaultEnum.setVisible(showEnum);
		defaultConfig.setVisible(showConfig);
		textFields[DEFAULT].setVisible(showText);
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}
}
