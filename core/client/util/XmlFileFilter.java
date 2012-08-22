package util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class XmlFileFilter extends FileFilter {

	public boolean accept(File file) {
		if(file.isDirectory()) return true;
		else return file.getName().toLowerCase().endsWith(".xml");
	}

	public String getDescription() {
		return "XML files only";
	}
}
