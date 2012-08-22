package plugins.treeTable.dialog;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;

import xml.TypedElement;

public class NodeCreationDialog extends AbstractDialog {

	public NodeCreationDialog() {
		super();
		initDialog();
	}
	
	private TypedElement e;
	public void init(TypedElement e) {
		this.e = e;
	
		name.setText(e.getChildText("name"));	
		description.setText(e.getChildText("description"));
		
		module.getListModel().set(DialogHelper.getModules());
		String selectedModule = e.getChildText("module");
		module.setSelectedString(selectedModule);
		
		StringComboBoxModel model = (StringComboBoxModel)instance.getModel();
		model.set(DialogHelper.getModuleImprintNames(selectedModule));
		instance.setSelectedItem(e.getChildText("instance"));
		
		setConfigurationPanel((TypedElement)e.getChild("configuration"));
	}

	public TypedElement getElement() {
		e.removeContent();
		
		setElementValue("name", name.getText(), TypedElement.NAME, e);
		setElementValue("description", description.getText(), TypedElement.DESCRIPTION, e);
		
		e.addContent(new TypedElement("module", module.getSelectedString(), TypedElement.FIELD));
		e.addContent(new TypedElement("instance", (String)instance.getSelectedItem(), TypedElement.FIELD));
		e.addContent(TypedElement.get(TypedElement.LOCATION));
		TypedElement config = getConfigurationPanel();
		if(config != null)
			e.addContent(config.detach());
		
		return e;
	}

	private JTextField name = new JTextField();
	private JTextField description = new JTextField();
	private XmlComboBox module = new XmlComboBox();
	private JComboBox instance = new JComboBox(new StringComboBoxModel());
	private ConfigurationPanel configurationPanel;
	private void initDialog() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		GridBagLayout gl = new GridBagLayout();
		
		JPanel mainPanel = new JPanel(gl);
		mainPanel.setBorder(BorderFactory.createTitledBorder("vertex"));
		
		String [] labelNames = {"name:", "module:", "imprint:", "description:"};
		JLabel [] labels = new JLabel[4];
		for(int i=0; i<labels.length; i++) {
			labels[i] = new JLabel(labelNames[i]);
			if(i<3) labels[i].setFont(labels[i].getFont().deriveFont(Font.BOLD));
			gl.setConstraints(labels[i], cLabel);
		}
		
		LabelListModel model = module.getListModel();
		model.set(DialogHelper.getModules());
		
		module.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				StringComboBoxModel model = (StringComboBoxModel)instance.getModel();
				model.set(DialogHelper.getModuleImprintNames(module.getSelectedString()));
				instance.setSelectedIndex(0);	
			}
		});
		
		instance.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				setConfigurationPanel(null);
			}
		});
		
		gl.setConstraints(name, cText);
		gl.setConstraints(module, cText);
		gl.setConstraints(instance, cText);
		gl.setConstraints(description, cText);
		
		mainPanel.add(labels[0]); mainPanel.add(name);
		mainPanel.add(labels[1]); mainPanel.add(module);
		mainPanel.add(labels[2]); mainPanel.add(instance);
		mainPanel.add(labels[3]); mainPanel.add(description);
			
		configurationPanel = new ConfigurationPanel();
		configurationPanel.setBorder(BorderFactory.createTitledBorder("configuration"));
		
		add(mainPanel);
		add(configurationPanel);
	}
	
	private TypedElement getConfigurationPanel() {
		if(!configurationPanel.isVisible()) return null;
		else return configurationPanel.getDiffElement();
	}
	
	private void setConfigurationPanel(TypedElement configuration) {
		String selectedModule = module.getSelectedString();
		TypedElement instanceElement = DialogHelper.getModuleImprint((String)instance.getSelectedItem(), selectedModule);
		TypedElement defaultSettings = (TypedElement)instanceElement.getChild("parameter").getChild("configuration");
		
		if(defaultSettings != null && defaultSettings.getChildren().size() > 0) {	
			configurationPanel.setVisible(true);
			
			configurationPanel.init(defaultSettings, configuration);
		} else configurationPanel.setVisible(false);
	}
	
	private final static int [] handledTypes = {TypedElement.NODE};
	public int[] handledTypes() {
		return handledTypes;
	}	
}
