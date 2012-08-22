package xml.dialog;

import javax.swing.*;
import java.awt.Font;
import java.awt.GridLayout;

import xml.TypedElement;

public class StringElementHandler implements DialogHandling {
	private JTextField [] textField = new JTextField[5];
	private JTextArea description;
	private TypedElement element = TypedElement.get(TypedElement.STRING);
	private static final int NAME=0, LENGTH=1, MIN_LENGTH=2, MAX_LENGTH=3, DEFAULT=4;
	
	public String getTitle() {
		return "neues String-Element";
	}

	public TypedElement getElement() {
		return readElement(element);
	}

	public JPanel getPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(6, 2, 5, 5));
		
		JLabel nameLabel = new JLabel("Name");
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		textField[NAME] = new JTextField(element.getChildText("name"));
		mainPanel.add(nameLabel);
		mainPanel.add(textField[NAME]);
		
		textField[LENGTH] = new JTextField(element.getChildText("length"));
		mainPanel.add(new JLabel("Länge"));
		mainPanel.add(textField[LENGTH]);
		
		textField[MIN_LENGTH] = new JTextField(element.getChildText("minLength"));
		mainPanel.add(new JLabel("Mindestlänge"));
		mainPanel.add(textField[MIN_LENGTH]);
		
		textField[MAX_LENGTH] = new JTextField(element.getChildText("maxLength"));
		mainPanel.add(new JLabel("Maximallänge"));
		mainPanel.add(textField[MAX_LENGTH]);
		
		textField[DEFAULT] = new JTextField(element.getChildText("default"));
		mainPanel.add(new JLabel("Standardwert"));
		mainPanel.add(textField[DEFAULT]);
		
		description = new JTextArea(element.getChildText("description"), 3, 20);
		description.setBorder(BorderFactory.createLoweredBevelBorder());
		mainPanel.add(new JLabel("Beschreibung"));
		mainPanel.add(description);
		
		return mainPanel;
	}
	
	private TypedElement readElement(TypedElement base) {
		base.getChild("name").setText(textField[NAME].getText());
		base.getChild("length").setText(textField[LENGTH].getText());
		base.getChild("minLength").setText(textField[MIN_LENGTH].getText());
		base.getChild("maxLength").setText(textField[MAX_LENGTH].getText());
		base.getChild("default").setText(textField[DEFAULT].getText());
		base.getChild("description").setText(description.getText());
	
		return base;
	}
}
