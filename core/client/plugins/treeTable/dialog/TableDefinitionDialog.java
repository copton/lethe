package plugins.treeTable.dialog;

import java.awt.*;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;
import xml.TypedElement;


public class TableDefinitionDialog extends AbstractDialog {
	private static final int [] handledTypes = {TypedElement.TABLE};
		
	private TypedElement element;
	private XmlComboBox elementTypes = new XmlComboBox();
	public TableDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private static final String [] elementNames = {"name", "elementType", "description", "anzCols", "minCols", "maxCols", "anzRows", "minRows", "maxRows"};
	private RestrictedFieldsPanel restricted = new RestrictedFieldsPanel(elementTypes);
	public void init(TypedElement e) {
		for(int i=0; i<ANZ_ITEMS; i++) 
			textFields[i].setText(e.getChildText(elementNames[i]));
			
		element = e;
		
		java.util.List l = new java.util.ArrayList();
		l.addAll(TypedElement.getSimpleTypes());
		l.addAll(DialogHelper.getLocalTypes(e));
		l.addAll(DialogHelper.getGlobalTypes());
		listModel.set(l);
		
		String selectedItem = e.getChildText(elementNames[ELEMENT_TYPE]);
		elementTypes.setSelectedString(selectedItem);
		
		restricted.init(e);
		restricted.showRestrictions((TypedElement)elementTypes.getSelectedItem());
	}

	public TypedElement getElement() {
		element.removeContent();
		
		setElementValue("name",textFields[NAME].getText(), TypedElement.NAME, element);
		setElementValue("elementType", elementTypes.getSelectedString(), TypedElement.ELEMENT_TYPE, element);
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
		
		if(textFields[COLS].getText().length() > 0) 
			setElementValue(elementNames[COLS], textFields[COLS].getText(), TypedElement.SIZE, element);
		else {
			if(textFields[MIN_COLS].getText().length() > 0) 
				setElementValue(elementNames[MIN_COLS], textFields[MIN_COLS].getText(), TypedElement.SIZE, element);
					
			if(textFields[MAX_COLS].getText().length() > 0) 
				setElementValue(elementNames[MAX_COLS], textFields[MAX_COLS].getText(), TypedElement.SIZE, element);
		}
		
		if(textFields[ROWS].getText().length() > 0) 
			setElementValue(elementNames[ROWS], textFields[ROWS].getText(), TypedElement.SIZE, element);
		else {
			if(textFields[MIN_ROWS].getText().length() > 0) 
				setElementValue(elementNames[MIN_ROWS], textFields[MIN_ROWS].getText(), TypedElement.SIZE, element);
					
			if(textFields[MAX_ROWS].getText().length() > 0) 
				setElementValue(elementNames[MAX_ROWS], textFields[MAX_ROWS].getText(), TypedElement.SIZE, element);
		}
		element.addContent(restricted.getElement());
		
		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "element type:", "description:", "cols:", "min. cols:", "max. cols:", "rows:", "min. rows:", "max. rows:"};
		
	private static int NAME=0, ELEMENT_TYPE=1, DESCRIPTION=2, COLS=3, MIN_COLS=4, MAX_COLS=5, ROWS=6, MIN_ROWS=7, MAX_ROWS=8, ANZ_ITEMS=9;
		
	private LabelListModel listModel;
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("table"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);

		listModel = (LabelListModel)elementTypes.getModel();
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == ELEMENT_TYPE)
				addComponent(elementTypes, text, gl1, defaultsPanel);
			else addComponent(textFields[i], text, gl1, defaultsPanel);
						
		}
			
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[ELEMENT_TYPE].setFont(boldFont);
				
		add(defaultsPanel);
		
		java.util.Iterator it = restricted.getElements().iterator();
		while(it.hasNext()) add((JPanel)it.next());
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}
}
