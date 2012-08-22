package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;
import xml.TypedElement;


public class InstanceDefinitionDialog extends AbstractDialog  {
	private static final int [] handledTypes = {TypedElement.INSTANCE};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];

	private static String [] names = {"name:", "type:", "description:"};
	private static int NAME=0, CLASS=1, DESCRIPTION=2, ANZ_ITEMS=3;
	
	private TypedElement element;
	public InstanceDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private ClassSelectionPanel selectionPanel;
	public void init(TypedElement e) {
		textFields[NAME].setText(e.getChildText("name"));
		textFields[DESCRIPTION].setText(e.getChildText("description"));
		element = e;
		
		java.util.List types = new java.util.ArrayList();
		types.addAll(DialogHelper.getLocalTypes(e));
		types.addAll(DialogHelper.getGlobalTypes());
		
		selectionPanel.setTypes(types);
		selectionPanel.setSelectedItem(e.getAttributeValue("name"));
		selectionPanel.setImprint(e.getAttributeValue("imprints"));
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		element.setAttribute("name", selectionPanel.getItemText());
		String imprint = selectionPanel.getImprint();
		if(imprint != null) element.setAttribute("imprints", imprint);
		else element.removeAttribute("imprints");
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
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("instance"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);

		selectionPanel = new ClassSelectionPanel(gl1);
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			if(i == CLASS) {
				java.util.Iterator it = selectionPanel.getContent().iterator();
				while(it.hasNext()) {
					addComponent((JComponent)it.next(), label, gl1, defaultsPanel);	
					addComponent((JComponent)it.next(), text, gl1, defaultsPanel);	
				}
			} else {
				addComponent(labels[i], label, gl1, defaultsPanel);
				addComponent(textFields[i], text, gl1, defaultsPanel);
			}
						
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[CLASS].setFont(boldFont);
		
		add(defaultsPanel);
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}
}
