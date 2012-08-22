package gui;

import java.util.*;
import event.*;

public class PluginHandler {
	List plugins;
	DefaultEventHandler exceptionHandler;
	
	private static PluginHandler handler = null;
	private static LetheController controller;
	
	private PluginHandler(List plugins) {
		exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	}	
	
	public static PluginHandler createPluginHandler(List plugins) {
		if(handler == null) {
			handler = new PluginHandler(plugins);
			handler.loadPlugins(plugins);
			handler.showPlugin(0);
		}
		return handler;
	}
	
	public static PluginHandler getPluginHandler() {
		return handler;
	}
	
	private void loadPlugins(List pluginNames) {
		plugins = new ArrayList();
		
		Iterator it = pluginNames.iterator();
		while(it.hasNext())
			addPlugin((String)it.next());
	}
	
	public void showPlugin(int num) {
		if(num >= plugins.size()) return;
		
		Object o = plugins.get(num);
		if(controller == null) 
			controller = LetheController.getController();
		
		if(o instanceof Pluggable)
			controller.showPluggable((Pluggable)o);
	}
	
	public void addPlugin(String name) {
		try {
			Class pluginClass = Class.forName(name);
			Object plugin = pluginClass.newInstance();
			if(plugin instanceof Pluggable) {
				((Pluggable)plugin).init();
				plugins.add(plugin);
			}
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		} 	
	}
	
	public List listPlugins() {
		List names = new ArrayList();
		
		Iterator it = plugins.iterator();
		while(it.hasNext()) names.add(it.next().getClass().getName());
		return names;
	}
	
	public void removePlugin(String name) {
		if(controller == null) 
			controller = LetheController.getController();
		
		Iterator it = plugins.iterator();
		while(it.hasNext()) {
			Pluggable p = (Pluggable)it.next();
			if(p.getClass().getName().equals(name)) {
				release(p);
				it.remove();
				return;
			}
		}
	}
	
	private void release(Pluggable p) {
		if(controller == null) 
			controller = LetheController.getController();
		
		controller.releasePluggable(p);
		p.release();
	}
	
	protected void exit() {
		Iterator it = plugins.iterator();
		while(it.hasNext()) 
			release((Pluggable)it.next());
	}
}
