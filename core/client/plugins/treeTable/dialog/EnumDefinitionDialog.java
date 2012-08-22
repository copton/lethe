package plugins.treeTable.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;
import java.util.Iterator;

public class EnumDefinitionDialog extends AbstractDialog implements java.awt.event.ActionListener {
	private static final int [] handledTypes = {TypedElement.ENUM};
	private JLabel [] labels = new JLabel[ANZ_ITEMS];
	private JTextField [] textFields = new JTextField[ANZ_ITEMS];
	private static String [] names = {"name:", "default:", "description:"};
	private static int NAME=0, DEFAULT=1, DESCRIPTION=2, ANZ_ITEMS=3;
	
	private JComboBox enumMembers;
	private TypedElement element;
	
	
	public EnumDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	public void init(TypedElement e) {
		String [] elementNames = {"name", "default", "description"};
		for(int i=0; i<ANZ_ITEMS; i++)
			textFields[i].setText(e.getChildText(elementNames[i]));
		
		java.util.List l = new java.util.ArrayList();
		l.add(TypedElement.get(TypedElement.STRING));
		addObjects.setAvailableTypes(l);
		
		listModel.clear();
		Iterator it = e.getChild("members").getChildren().iterator();
		while(it.hasNext()) {
			TypedElement name = (TypedElement)it.next();
			TypedElement newString = TypedElement.get(TypedElement.STRING);
			newString.getChild("name").setText(name.getText());
			listModel.add(newString);
		}
			
		enumMembers.setSelectedItem(e.getChildText(elementNames[DEFAULT]));
		element = e;
	}

	public TypedElement getElement() {
		element.getChild("name").setText(textFields[NAME].getText());
		TypedElement selectedItem = (TypedElement)enumMembers.getSelectedItem();
		element.getChild("default").setText(selectedItem!=null?selectedItem.getChildText("name"):"");
		
		setElementValue("description", textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, element);
		
		TypedElement members = (TypedElement)element.getChild("members");
		members.removeContent();
		for(int i=0; i<listModel.getSize(); i++) {
			TypedElement memberName = new TypedElement("name", TypedElement.NAME);
			memberName.setText(((TypedElement)listModel.getElementAt(i)).getChildText("name"));
			members.addContent(memberName);
		}
		return element;
	}

	public JPanel getPanel() {
		return this;
	}
	
	
	private LabelListModel listModel = new LabelListModel();
	private JList memberList = new JList(listModel);
	private NewObjectPanel addObjects = new NewObjectPanel(listModel);
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("enumeration"));
		
		GridBagLayout gl1 = new GridBagLayout();
		defaultsPanel.setLayout(gl1);
		
		enumMembers = new JComboBox(listModel);
		enumMembers.setRenderer(new LabelComboBoxRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
				JLabel label = (JLabel)renderer.getListCellRendererComponent(list, value, 
						index, isSelected, hasFocus);
				
				if(value == null) return label;
				else {
					TypedElement e = (TypedElement)value;
					label.setText(e.getChildText("name"));
				}
				return label;
			}
		});
		
		for(int i=0; i<ANZ_ITEMS; i++) {
			labels[i] = new JLabel(names[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			if(i == DEFAULT)
				addComponent(enumMembers, text, gl1, defaultsPanel);
			else addComponent(textFields[i], text, gl1, defaultsPanel);
			}
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
		labels[DEFAULT].setFont(boldFont);
	
		JPanel membersPanel = new JPanel(new BorderLayout());
		memberList.setCellRenderer(new LabelCellRenderer());
		memberList.setVisibleRowCount(5);
		membersPanel.add(new JScrollPane(memberList));
		membersPanel.setBorder(BorderFactory.createTitledBorder("members"));
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		String [] buttonNames = {"delete", "add"};
		for(int i=0; i<buttonNames.length; i++) {
			JButton b = new JButton(buttonNames[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(this);
			buttonPanel.add(b);
		}
		membersPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(defaultsPanel);
		add(membersPanel);
		add(addObjects);
	}
	
	public int[] handledTypes() {
		return handledTypes;
	}

	private final static int DELETE=0, ADD=1;
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case DELETE: 
			listModel.removeAll(java.util.Arrays.asList(memberList.getSelectedValues()));
			break;
		case ADD:
			addObjects.showPanel();
			break;
		}
	}
}
