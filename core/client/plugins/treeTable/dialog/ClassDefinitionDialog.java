package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;

public class ClassDefinitionDialog extends AbstractDialog {
	private static final int [] handledTypes = {TypedElement.CLASS};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "description:"};
	
	private static int NAME=0, DESCRIPTION=1, ANZ_ITEMS=2;

	private TypedElement element;
	
	public ClassDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));	
		initDialog();
	}

	public void setListShortcut(java.awt.event.MouseListener listener) {
		memberList.addMouseListener(listener);
	}
	
	private static AddMemberPanel memberView = new AddMemberPanel();
	private static final String [] elementNames = {"name", "description"};
	public void init(TypedElement e) {
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
	
		element = e;
		memberList.setActiveElement(e);
		memberList.setView(memberView);
		memberList.getModel().set(e.getChild("members").getChildren());
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
				
		element.getChild("members").removeContent();
		element.getChild("members").addContent(memberList.getModel().getAll());
		
		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private ListAddDeletePanel memberList = new ListAddDeletePanel();
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("class"));
			
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);
	
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			addComponent(textFields[i], text, gl1, defaultsPanel);
		}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
			
		memberList.setTitle("members");
		memberList.setRenderer(new LabelCellRenderer());
		
		add(defaultsPanel);
		add(memberList);
	}

	public int[] handledTypes() {
		return handledTypes;
	}
}
