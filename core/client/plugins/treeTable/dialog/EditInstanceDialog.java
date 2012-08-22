package plugins.treeTable.dialog;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

import xml.TypedElement;

public class EditInstanceDialog extends AbstractDialog {

	private ConfigurationPanel panel = new ConfigurationPanel();
	public EditInstanceDialog() {
		super();
		setLayout(new BorderLayout());
		add(panel);
	}
	
	public void init(TypedElement e) {
		panel.init(e);
		panel.setBorder(BorderFactory.createTitledBorder(e.getName()));
	}

	public TypedElement getElement() {
		return panel.getElement();
	}

	
	private static int [] handledTypes = {TypedElement.CONFIGURATION_DIR, TypedElement.SETTINGS_DIR};
	public int[] handledTypes() {
		return handledTypes;
	}
}
