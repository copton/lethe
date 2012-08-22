package plugins.packageGeneration;

import client.PackageGenerator;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class PackageGeneration implements java.awt.event.ActionListener {
	PackageGenerator generator;
	
	public PackageGeneration() {
		generator = new PackageGenerator();
		gui.LetheController.getController().addMenuListener(this);
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("new:package")) {
			(new Thread(new GeneratorThread(generator))).start();
		}
	}
	
	class GeneratorThread implements Runnable {
		PackageGenerator generator;
		
		public GeneratorThread(PackageGenerator generator) {
			this.generator = generator;
		}
		
		public void run() {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					if(f.isDirectory()) return true;
					else return f.getName().endsWith(".xml");
				}

				public String getDescription() {
					return "*.xml - module/type files";
				}	
			});
			int returnVal = fileChooser.showOpenDialog(null);
			if(returnVal != JFileChooser.APPROVE_OPTION) return;
			
			String fileName = fileChooser.getSelectedFile().getAbsolutePath();
			System.out.println("run");
			try {
				generator.generatePackage(fileName);
				System.out.println("generated");
			} catch(Exception e) {
				System.err.println("exception: "+e.getMessage());
			}
		}
	}
}
