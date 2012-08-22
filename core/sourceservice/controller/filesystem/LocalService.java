package controller.filesystem;


import controller.SourceController;

import Comm.Exceptions.RessourceNotFoundException;
import Comm.SourceService.*;

import java.io.*;

public class LocalService implements SourceController {
	
	String path;
	public LocalService(java.util.Map properties) {
		System.out.println("properties="+properties);
		path =  (String)properties.get("config.file.path");
		System.out.println("LocalSource: path="+path);
	}
	
	public void getSource(SourceDesc source, String[] files, String directory) throws RessourceNotFoundException {
		LocalSource s = (LocalSource)source;
		System.out.println("getSource: "+source+ " files="+files+" to dir="+directory);	
		for(int i=0; i<files.length; i++) {
			System.out.println("copy "+files[i]+" from "+path+"/"+s.path);
			copyFile(files[i], path+"/"+s.path, directory);
			System.out.println("...copied");
		}
	}
	
	private void copyFile(String fileName, String path, String directory) throws RessourceNotFoundException {
		System.out.println("foo");
		File origin = new File(path, fileName);
		System.out.println("origin="+origin.getAbsolutePath());
		if(!origin.exists()) {
			System.err.println("file: "+origin.getAbsolutePath()+" doesn not exist");
			System.err.flush();
			throw new RessourceNotFoundException(origin.getAbsolutePath()+" not found");
		} else System.out.println("bar");
		
		System.out.println("fnord");
		File goal = new File(directory, fileName);	
		System.out.println("fnord1");
		copyDir(origin, goal);
		System.out.println("fnord2");
	}

	private void copyDir(File origin, File goal) throws RessourceNotFoundException {
		if(origin.isFile()) {
			try {
				FileReader reader = new FileReader(origin);
				FileWriter writer = new FileWriter(goal);
		
				char [] buffer = new char[1024];
				while(reader.read(buffer) > 0) 
					writer.write(buffer);
			
			} catch(Exception e) {
				throw new RessourceNotFoundException("could not copy "+origin.getAbsolutePath());
			}		
		} else {
			goal.mkdirs();
			File [] children = origin.listFiles();
			for(int i=0; i<children.length; i++)
				copyDir(children[i], new File(goal.getAbsolutePath(), children[i].getName()));
		}
	}
	
	public String[] getSourceList(SourceDesc source, String directory) {
		LocalSource s = (LocalSource)source;
		
		java.util.List fileList = new java.util.ArrayList();
		
		String [] types = {"modules", "types"};
		for(int i=0; i<types.length; i++) {
			File file = new File(types[i], path+"/"+s.path);
			File [] children = file.listFiles();
			
			for(int j=0; j<children.length; j++)
				if(children[j].isDirectory())
					fileList.add(types[i]+File.separator+children[j].getName());
		}
		
		return (String[])fileList.toArray(new String[0]);
	}
}
