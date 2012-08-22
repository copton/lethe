package gui;

import javax.swing.*;
import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.*;

public class LetheWindow extends JFrame implements Personalizable {
	JMenuBar menu;
	JToolBar toolBar;
	JLabel statusPanel;
	JPanel mainPanel;
	List toolbarExtras;
	Element settings, menuElement;
	
	private final static String mainWindowSettingsName = "mainWindow";
	
	public LetheWindow() {
		super("Lethe");
		createWindow();
	}
	
	void createWindow() {	
		LetheController.getController().registerForSettings(this, mainWindowSettingsName);
			
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				LetheController.getController().exit();
			}
		});
		
		toolBar = createToolbar();
		getContentPane().add(toolBar, BorderLayout.PAGE_START);
		
		statusPanel = new JLabel();
		statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
	
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		getContentPane().add(mainPanel);
	
		show();
	}
	
	private void setMenu(Element menuBar) {
		menu = new JMenuBar();
		Iterator it = menuBar.getChildren("menu").iterator();
	
		while(it.hasNext()) 
			menu.add(createMenu((Element)it.next()));
	
		setJMenuBar(menu);
	}
	
	private JMenu createMenu(Element menu) {
		JMenu menuItem = new JMenu(menu.getAttributeValue("name"));
		Iterator it = menu.getChildren().iterator();
		while(it.hasNext()) {
			Element child = (Element)it.next();
			if(child.getName().equals("menu"))
				menuItem.add(createMenu(child));
			else {
				JMenuItem item = new JMenuItem(child.getAttributeValue("name"));
				item.setActionCommand(child.getAttributeValue("id"));
				menuItem.add(item);
			}
		}
		return menuItem;
	}
	
	public void addMenuListener(ActionListener l) {
		for(int i=0; i<menu.getMenuCount(); i++)
			addMenuListener(menu.getMenu(i), l);
	}
	
	private void addMenuListener(JMenu m, ActionListener l) {
		for(int i=0; i<m.getItemCount(); i++) {
			JMenuItem item = m.getItem(i);
			if(item instanceof JMenu)
				addMenuListener((JMenu)item, l);
			else m.getItem(i).addActionListener(l);
		}
	}
	
	public void removeMenuListener(ActionListener l) {
		for(int i=0; i<menu.getMenuCount(); i++)
			removeMenuListener(menu.getMenu(i), l);
	}
	
	private void removeMenuListener(JMenu m, ActionListener l) {
		for(int i=0; i<m.getItemCount(); i++) {
			JMenuItem item = m.getItem(i);
			if(item instanceof JMenu)
				removeMenuListener((JMenu)item, l);
			else m.getItem(i).removeActionListener(l);
		}
	}
	
	JToolBar createToolbar() {
		toolbarExtras = new ArrayList();
		JToolBar toolbar = new JToolBar();
		JPanel dummyPanel = new JPanel();
		dummyPanel.setVisible(false);
		toolbar.add(dummyPanel);
		toolbar.add(new javax.swing.JSeparator());
		toolbar.add(dummyPanel);
	
		return toolbar;
	}
	
	void addToToolbar(JButton button) {
		toolBar.add(button, toolBar.getComponentCount()-1);
		toolBar.updateUI();
	}
	
	void removeFromToolbar(JButton button) {
		toolBar.remove(button);
		toolBar.updateUI();
	}
	void setExtraToolbarPart(List toolbarExtras) {
		Iterator elements = this.toolbarExtras.iterator();
		while(elements.hasNext()) toolBar.remove((Component)elements.next());
		
		if(toolbarExtras != null) {
			this.toolbarExtras = new ArrayList(toolbarExtras);
			elements = toolbarExtras.iterator();
			int pos = 0;
			while(elements.hasNext()) toolBar.add((Component)elements.next(), pos++);			
		} else this.toolbarExtras.clear();
	
		toolBar.updateUI();			
	}
	
	void showMainPanel(JPanel panel) {
		if(panel == null) return;
		mainPanel.removeAll();
		
		mainPanel.add(panel);
		mainPanel.updateUI();
	}
	
	public Element getDefaultSettings() {
		Element settings = new Element(mainWindowSettingsName);
		
		Element dimension = new Element("windowSize");
		Element width = new Element("width");
		Element height = new Element("height");
		width.setText("800");
		height.setText("600");
		dimension.addContent(width);
		dimension.addContent(height);
		
		Element position = new Element("windowPosition");
		Element xPos = new Element("xPos");
		Element yPos = new Element("yPos");
		xPos.setText("100");
		yPos.setText("100");
		position.addContent(xPos);
		position.addContent(yPos);
		
		settings.addContent(dimension);
		settings.addContent(position);
		
		return settings;
	}
	
	public Element getSettings() {
		Element dimension = settings.getChild("windowSize");
		dimension.getChild("width").setText(Integer.toString(getWidth()));
		dimension.getChild("height").setText(Integer.toString(getHeight()));
		
		Element position = settings.getChild("windowPosition");
		position.getChild("xPos").setText(Integer.toString(getX()));
		position.getChild("yPos").setText(Integer.toString(getY()));
		
		settings.removeContent();
		settings.addContent(dimension);
		settings.addContent(position);
		settings.addContent(menuElement);
		
		return settings;
	}
	
	public void setSettings(Element settings) {
		if(settings == null) settings = this.getDefaultSettings();
		
		int width, height, xPos, yPos;
		this.settings = settings;
		
		Element dimension = settings.getChild("windowSize");
		width = Integer.parseInt(dimension.getChildText("width"));
		height = Integer.parseInt(dimension.getChildText("height"));
		
		Element position = settings.getChild("windowPosition");
		xPos = Integer.parseInt(position.getChildText("xPos"));
		yPos = Integer.parseInt(position.getChildText("yPos"));
		
		setSize(new Dimension(width, height));
		this.setLocation(new java.awt.Point(xPos, yPos));
		
		menuElement = settings.getChild("menuBar");
		setMenu(menuElement);
	}
	
	public static void main(String [] args) {
		LetheController.getController();
	}
}
