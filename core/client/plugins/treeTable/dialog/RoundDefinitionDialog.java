package plugins.treeTable.dialog;

import javax.swing.*;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;
import xml.TypedElement;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;

public class RoundDefinitionDialog extends AbstractDialog {

	public RoundDefinitionDialog() {
		super();
		initDialog();
	}
	
	private TypedElement e, vertices;
	private JTextField roundNumbers = new JTextField();
	private JPanel numberPanel = new JPanel();
	private JPanel settingsPanel = new JPanel();
	public void init(TypedElement e) {
		this.e = e;
		
		switch(e.getType()) {
		case TypedElement.ROUND_SETTINGS:
			settingsPanel.setBorder(BorderFactory.createTitledBorder("default settings"));
			roundNumbers.setText(e.getChild("rounds").getAttributeValue("anz"));
			break;
		case TypedElement.ROUND:
			settingsPanel.setBorder(BorderFactory.createTitledBorder(
					"settings for round "+e.getAttributeValue("nr")));
			break;
		}
		numberPanel.setVisible(e.getType() == TypedElement.ROUND_SETTINGS);
		
		vertices = (TypedElement)e.getDocument().getRootElement().getChild("graph").getChild("nodes");
	
		fields.clear();
		settingsPanel.removeAll();
		
		Iterator it = vertices.getChildren().iterator();
		while(it.hasNext()) 
			createField((TypedElement)it.next());
	}

	public TypedElement getElement() {
		switch(e.getType()) {
		case TypedElement.ROUND_SETTINGS: {
			TypedElement defaultSettings = (TypedElement)e.getChild("default");
			defaultSettings.removeContent();
			Iterator it = fields.keySet().iterator();	
			while(it.hasNext()) {
				String nodeName = (String)it.next();
				ConfigurationPanel p = (ConfigurationPanel)fields.get(nodeName);

				TypedElement settings = p.getDiffElement();
				if(settings != null) {
					settings.setName("node");
					settings.setAttribute("name", nodeName);
					defaultSettings.addContent(settings.detach());
				}
			}
			
			String roundsStr = roundNumbers.getText();
			if(roundsStr == null || roundsStr.length() == 0) roundsStr = "1";
			e.getChild("rounds").setAttribute("anz", roundsStr);
			
			int rounds = Integer.parseInt(roundsStr);
			int round_nodes = e.getChild("rounds").getChildren().size();
			
			// delete "überflüssige" rounds
			for(int i=rounds; i<round_nodes; i++)
				e.getChild("rounds").removeContent(rounds+1);
				
			// create missing rounds
			for(int i=rounds; i>round_nodes; i--) {
				TypedElement round = new TypedElement("round", TypedElement.ROUND);
				round.setAttribute("nr", Integer.toString(i));
				e.getChild("rounds").addContent(round_nodes, round);
			}
			break;
		}
		case TypedElement.ROUND:
			e.removeContent();
			Iterator it = fields.keySet().iterator();	
			while(it.hasNext()) {
				String nodeName = (String)it.next();
				ConfigurationPanel p = (ConfigurationPanel)fields.get(nodeName);

				TypedElement settings = p.getDiffElement();
				if(settings != null) {
					settings.setName("node");
					settings.setAttribute("name", nodeName);
					e.addContent(settings.detach());
				}
			}
		}
		return e;
	}

	private void initDialog() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		GridBagLayout gl = new GridBagLayout();
		numberPanel.setLayout(gl);
		
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		
		JLabel numberLabel = new JLabel("number of rounds: ");
		
		gl.setConstraints(numberLabel, cLabel);
		gl.setConstraints(roundNumbers, cText);
	
		numberPanel.add(numberLabel);
		numberPanel.add(roundNumbers);
		
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
		
		add(numberPanel);
		add(settingsPanel);
	}
	
	private Map fields = new HashMap();
	private void createField(TypedElement node) {
		JPanel p = new JPanel(new BorderLayout());
		String nodeName = node.getChildText("name");
		p.setBorder(BorderFactory.createTitledBorder(nodeName));
		
		TypedElement module = DialogHelper.getModuleImprint(node.getChildText("instance"), 
				node.getChildText("module"));
		
		TypedElement settings = (TypedElement)module.getChild("parameter").getChild("settings");
		if(settings.getChildren().size() == 0) return;
		
		ConfigurationPanel config = new ConfigurationPanel();
		TypedElement nodeSettings = findNodeSettings(nodeName, e);
		
		if(e.getType() == TypedElement.ROUND) {
			TypedElement defaults = (TypedElement)((TypedElement)e.getParent().getParent()).getChild("default");
			TypedElement defaultSettings = findNodeSettings(nodeName, defaults);
			
			config.init(shadowWithDefault(defaultSettings, settings), shadowWithDefault(nodeSettings, defaultSettings));
		} else config.init(settings, nodeSettings);
		
		p.add(config);
		fields.put(nodeName, config);
		
		settingsPanel.add(p);
	}
	
	private TypedElement shadowWithDefault(TypedElement settings, TypedElement defaultSettings) {
		if(defaultSettings == null) return settings;
		else if(settings == null) 
			return (TypedElement)defaultSettings.clone();
		else {
			int pos=1;
			Iterator it = defaultSettings.getChildren().iterator();
			TypedElement newSettings = (TypedElement)settings.clone();
			
			while(it.hasNext()) {
				TypedElement child = (TypedElement)it.next();
				if(newSettings.getChild(child.getName()) == null) 
					newSettings.addContent(pos, (TypedElement)child.clone());
				pos++;
			}
			return newSettings;
		}
	}
	
	private TypedElement findNodeSettings(String name, TypedElement node) {
		Iterator it = node.getChildren().iterator();
		while(it.hasNext()) {
			TypedElement child = (TypedElement)it.next();
			if(name.equals(child.getAttributeValue("name"))) return child;
		}
		
		return null;
	}
	
	private int [] handledTypes = {TypedElement.ROUND, TypedElement.ROUND_SETTINGS};
	public int[] handledTypes() {
		return handledTypes;
	}

}
