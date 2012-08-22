package plugins.treeTable.dialog;

import java.awt.*;
import javax.swing.*;

import plugins.treeTable.EditorDialog;

import xml.TypedElement;

public class JobDefinitionDialog extends AbstractDialog {
	private static final int [] handledTypes = {TypedElement.JOB};
	public JobDefinitionDialog() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initDialog();
	}

	private TypedElement e;
	private static final int NAME=0, AUTHOR=1, DESCRIPTION=4, ANZ_TYPES=5;
	private JTextField [] textFields = new JTextField[ANZ_TYPES];
	private static final String [] elementNames = {"name", "author", "version", "date", "description"};
	public void init(TypedElement e) {
		this.e = e;
		for(int i=NAME; i<ANZ_TYPES; i++)
		textFields[i].setText(e.getChildText(elementNames[i]));
	}

	public TypedElement getElement() {
		TypedElement graph = (TypedElement)e.getChild("graph");
		TypedElement settings = (TypedElement)e.getChild("settings");
		
		e.removeContent();
		setElementValue(elementNames[NAME], textFields[NAME].getText(), TypedElement.NAME, e);
		for(int i=AUTHOR; i<DESCRIPTION; i++)
			setElementValue(elementNames[i], textFields[i].getText(), TypedElement.META_DATA, e);
		setElementValue(elementNames[DESCRIPTION], textFields[DESCRIPTION].getText(), TypedElement.DESCRIPTION, e);
	
		e.addContent(graph);
		e.addContent(settings);
		
		return e;
	}

	public JPanel getPanel() {
		return this;
	}
	
	private void initDialog() {
		GridBagConstraints label = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints text = EditorDialog.getConstraints(EditorDialog.TEXT);

		JPanel defaultsPanel = new JPanel();
		defaultsPanel.setBorder(BorderFactory.createTitledBorder("Job"));
		
		GridBagLayout gl1 = new GridBagLayout();
	
		defaultsPanel.setLayout(gl1);
		
		String [] labelNames = {"name:", "author:", "version:", "date:", "description:"};
		JLabel [] labels = new JLabel[ANZ_TYPES];
		
		for(int i=0; i<ANZ_TYPES; i++) {
			labels[i] = new JLabel(labelNames[i]);
			textFields[i] = new JTextField();
			
			addComponent(labels[i], label, gl1, defaultsPanel);
			addComponent(textFields[i], text, gl1, defaultsPanel);
		}

		add(defaultsPanel);
		
		Font boldFont = labels[NAME].getFont().deriveFont(Font.BOLD);
		labels[NAME].setFont(boldFont);
	}

	public int[] handledTypes() {
		return handledTypes;
	}
}
