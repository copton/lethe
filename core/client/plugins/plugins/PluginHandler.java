package plugins.plugins;

import util.IceNetworkConnection;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import java.util.List;
import java.util.Iterator;

import gui.*;

public class PluginHandler implements Pluggable, java.awt.event.ActionListener {
	gui.PluginHandler pluginHandler;
	
	public void init() {
		pluginHandler = gui.PluginHandler.getPluginHandler();
		LetheController.getController().addMenuListener(this);
	}

	public void release() {
		LetheController.getController().removeMenuListener(this);
	}

	public JPanel show() {
		return null;
	}

	public void hide() {}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("plugin:add")) 
			addPlugin();
		else if(event.getActionCommand().equals("plugin:remove"))
			removePlugin();
	}
	
	private void addPlugin() {
		List loadedPlugins = pluginHandler.listPlugins();
		AvailablePluginsFilter filter = new AvailablePluginsFilter(loadedPlugins);
		
		Ice.Properties properties = IceNetworkConnection.getProperties();
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(filter);
		fileChooser.setCurrentDirectory(new java.io.File(properties.getProperty("gui.pluginDir")));
		
		int resVal = fileChooser.showOpenDialog(null); 
		
		if(resVal == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().getAbsolutePath();
			fileName = fileName.replace(java.io.File.separatorChar, '.');
			fileName = fileName.substring(fileName.indexOf("plugins"), fileName.lastIndexOf("."));
		
			LetheController.getController().addPlugin(fileName);
		}
	}
	
	private void removePlugin() {
		List loadedPlugins = pluginHandler.listPlugins();
		RemoveDialog dialog = new RemoveDialog(loadedPlugins);
		
		LetheController controller = LetheController.getController();
		List selectedPlugins = dialog.showDialog();
		Iterator it = selectedPlugins.iterator();
		while(it.hasNext()) {
			String plugin = it.next().toString();
			controller.deletePlugin(plugin);
		}
	}
}
