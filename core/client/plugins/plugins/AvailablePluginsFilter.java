package plugins.plugins;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import util.StringHelper;

import java.util.*;

public class AvailablePluginsFilter extends FileFilter {
	List loadedPlugins;
	
	public AvailablePluginsFilter(List loadedPlugins) {
		super();
		this.loadedPlugins = getLastPathComponent(loadedPlugins);
	}
	
	public boolean accept(File f) {
		if(f.isDirectory() && !loadedPlugins.contains(f.getName())) return true;
		else if(f.isFile() && f.getName().endsWith(".class")) return true;
		else return false;
	}

	public String getDescription() {
		return "*.class - Plugin";
	}
	
	private List getLastPathComponent(List plugins) {
		List paths = new ArrayList(plugins.size());
		Iterator it = plugins.iterator();
		while(it.hasNext()) {
			String [] components = it.next().toString().split("\\.");
			if(components.length >= 2)
				paths.add(components[components.length-2]);
			else System.out.println("Huch: "+ StringHelper.join(Arrays.asList(components), "-"));		
		}
		
		return paths;
	}
}
