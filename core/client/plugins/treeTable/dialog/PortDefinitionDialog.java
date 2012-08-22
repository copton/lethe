package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;
import xml.TypedElement;


public class PortDefinitionDialog extends AbstractDialog implements xml.Types {
	private static final int [] handledTypes = {TypedElement.PORT};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];

	private static String [] names = {"name:", "type:", "used type:", "max. blocksize:", "description:"};
	private static int NAME=0, STREAM_TYPE=1, BIB_TYPE=2, SIZE=3, DESCRIPTION=4, ANZ_ITEMS=5;
	
	private XmlComboBox streamTypes = new XmlComboBox();
	private TypedElement element;
	
	public PortDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	public void init(TypedElement e) {
		String [] elementNames = {"name", "type", "usedType", "blocksize", "description"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		element = e;
		
		java.util.List supportedTypes = new java.util.ArrayList();
		supportedTypes.addAll(DialogHelper.getGlobalTypes());
		streamTypes.getListModel().set(supportedTypes);
		
		String typeName = e.getChildText(elementNames[STREAM_TYPE]);
		streamTypes.setSelectedString(typeName);
	}
		
	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		element.getChild("blocksize").setText(textFields[SIZE].getText());
		setElementValue("type", streamTypes.getSelectedString(), TypedElement.TYPE_DEF, element);
		setElementValue("usedType", textFields[BIB_TYPE].getText(), TypedElement.TYPE_DEF, element);
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
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("port"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == STREAM_TYPE)
				addComponent(streamTypes, text, gl1, defaultsPanel);
			else addComponent(textFields[i], text, gl1, defaultsPanel);
						
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[STREAM_TYPE].setFont(boldFont);
		labels[BIB_TYPE].setFont(boldFont);
		labels[SIZE].setFont(boldFont);
		
		add(defaultsPanel);
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}
}
