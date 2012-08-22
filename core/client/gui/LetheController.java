package gui;

import util.IceNetworkConnection;
import java.util.*;

import javax.swing.JButton;
import java.awt.event.*;

import org.jdom.Element;

import event.DebugMessageEvent;
import event.DefaultEventHandler;

public class LetheController implements Personalizable {
	private LetheWindow mainWindow = null;
	
	SettingsManager settingsManager;
	PluginHandler pluginHandler;
	Pluggable activePlugin=null;
	List personalizables;
	List plugins;
	Element settings;
	ShowPluginAction buttonListener;
	
	private Map registeredButtons = new HashMap();
	private Map registeredMenuListener = new HashMap();
	
	private DefaultEventHandler eventHandler;
	
	private static LetheController controller = null;
	private final String propertyFile = IceNetworkConnection.getProperties().getProperty("gui.properties");
	private final static String settingsName = "mainController";
	
	private LetheController() {
		personalizables = new ArrayList();
		buttonListener = new ShowPluginAction();
		eventHandler = new DefaultEventHandler(DefaultEventHandler.DEBUG_EVENT);
	}
	
	private void init() {
		settingsManager = new SettingsManager(propertyFile);
		registerForSettings(this, settingsName);
		
		mainWindow = new LetheWindow();
		mainWindow.addMenuListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(event.getActionCommand().equals("exit"))
					exit();
			}
		});
		pluginHandler = PluginHandler.createPluginHandler(plugins); 	
		eventHandler.fireEvent(new DebugMessageEvent("initialisation completed", this));
		
		showNextPlugin();
	}
	
	public static LetheController getController() {
		if(controller == null) {
			controller = new LetheController();
			controller.init();
		}
		
		return controller;
	}
	
	public static javax.swing.JFrame getMainWindow() {
		return controller.mainWindow;
	}
	
	public void showPluggable(Pluggable plugin) {	
		if(activePlugin != null) activePlugin.hide();
		
		if(plugin instanceof ToolbarUsable) 
			mainWindow.setExtraToolbarPart(((ToolbarUsable)plugin).addToToolbar());
		else mainWindow.setExtraToolbarPart(null);
		mainWindow.showMainPanel(plugin.show());
		activePlugin = plugin;
	}
	
	private void addRegistration(Object o, Object p, Map settings) {
		List registered = (List)settings.get(p);
		if(registered == null) registered = new ArrayList(1);
		registered.add(o);
		settings.put(p, registered);	
	}
	
	public void registerForSettings(Personalizable p, String settingsName) {
		personalizables.add(p);
		p.setSettings(settingsManager.get(settingsName));
	}
	
	public void registerForToolbar(Object o) {
		if(!(o instanceof ToolbarUsable && o instanceof Pluggable)) return;
		
		JButton button = ((ToolbarUsable)o).getShowButton();
		if(button == null) return;
	
		addRegistration(button, o, registeredButtons);
		
		button.setActionCommand(Integer.toString(buttonListener.size()));
		button.addActionListener(buttonListener);
		buttonListener.addPlugin((Pluggable)o);
		mainWindow.addToToolbar(button);
	}
	
	
	public void addMenuListener(ActionListener listener) {
		if(listener instanceof Pluggable) 
			addMenuListener(listener, (Pluggable)listener);
	}
	
	public void addMenuListener(ActionListener listener, Pluggable p) {
		addRegistration(listener, p, registeredMenuListener);
		mainWindow.addMenuListener(listener);
	}
	
	public void removeMenuListener(ActionListener listener) {
		mainWindow.removeMenuListener(listener);
	}
	
	protected void releasePluggable(Pluggable p) {
		if(registeredMenuListener.containsKey(p)) {
			Iterator it = ((List)registeredMenuListener.get(p)).iterator();
			while(it.hasNext()) removeMenuListener((ActionListener)it.next());
			registeredMenuListener.remove(p);
		}
		
		if(registeredButtons.containsKey(p)) {
			Iterator it = ((List)registeredButtons.get(p)).iterator();
			while(it.hasNext()) mainWindow.removeFromToolbar((JButton)it.next());
			registeredButtons.remove(p);		
		}

		if(personalizables.contains(p)) {
			settingsManager.set(((Personalizable)p).getSettings());
			personalizables.remove(p);
		}

		if(p == activePlugin)
			showNextPlugin(p);
	}
	
	public void showNextPlugin() {
		showNextPlugin(activePlugin);
	}
	
	public void showNextPlugin(Pluggable p) {
		int num = pluginHandler.plugins.indexOf(p);
		if(num < 0) return;
		else pluginHandler.showPlugin((num+1)%pluginHandler.plugins.size());
	}
	
	public void deletePlugin(String p) {
		pluginHandler.removePlugin(p);
		plugins.remove(p);
	}
	
	public void addPlugin(String p) {
		if(plugins.contains(p)) return;
		
		pluginHandler.addPlugin(p);
		plugins.add(p);
	}
	
	public SettingsManager settingManager() {
		return settingsManager;
	}
	
	void exit() {			
		if(activePlugin != null)
			activePlugin.hide();
	
		pluginHandler.exit();
		
		Iterator it = personalizables.iterator();
		while(it.hasNext())
			settingsManager.set(((Personalizable)it.next()).getSettings());
			
		settingsManager.save();
		
		IceNetworkConnection.get().shutdown();
		System.exit(0);
	}

	public void setSettings(Element e) {
		if(e == null) settings = getDefaultSettings();
		else settings = e;
		
		plugins = new ArrayList();
		Iterator it = settings.getChild("plugins").getChildren().iterator();
		while(it.hasNext()) {
			Element child = (Element)it.next();
			plugins.add(child.getText());
		}	
	}

	public Element getSettings() {
		settings.removeContent();
	
		Element plugin = new Element("plugins");
		Iterator it = plugins.iterator();
		
		while(it.hasNext()) {
			String name = (String)it.next();
			Element child = new Element("plugin");
			child.setText(name);
			plugin.addContent(child);
		}
		settings.addContent(plugin);
	
		return settings;
	}

	public Element getDefaultSettings() {
		Element settings = new Element(settingsName);
		
		Element plugins = new Element("plugins");
		String [] names = {"SimulationEditor", "SimulationManagement", "UserManagement", "Debugger"};
		
		for(int i=0; i<names.length; i++) {
			Element child = new Element("plugin");
			child.setText(names[i]);
			plugins.addContent(child);
		}
		settings.addContent(plugins);
		
		return settings;
	}
	
	class ShowPluginAction implements ActionListener {
		List pluggables;
		
		public ShowPluginAction() {
			pluggables = new ArrayList();
		}
		
		public void addPlugin(Pluggable p) {
			pluggables.add(p);
		}
		
		public int size() {
			return pluggables.size();
		}
		
		public void actionPerformed(ActionEvent e) {
			int num = Integer.parseInt(e.getActionCommand());
			LetheController.getController().showPluggable((Pluggable)pluggables.get(num));
		}
	}
}
