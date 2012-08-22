package util;
import java.io.File;
import java.util.*;

public class SliceGenerator {
	
	public boolean generateJavaFilesFromSlices(String module, java.util.List includes, String to) {
		File tempDir = new File(to);
		
		if(tempDir.exists() && classStillValid(module, to, true) && classesStillValid(includes, to)) return false;
		else tempDir.mkdirs();

		String included = getIncludeFiles(module, includes);
		String iceFile = path2modules +"/"+module+"/gen_"+module+".ice ";
		
		String sliceCompiler = IceNetworkConnection.getProperties().getProperty("xml.sliceCompiler");
		String command = new String(sliceCompiler+" --ice "+included+"--stream "+iceFile);
		new CommandExecutor(command, true, tempDir);
		
		String javaFile = getJavaNames(to+"/Extensions/Modules/"+module+"/Lethe/");
		// TODO: lib-pfad aus config
		command = new String("javac -classpath .:../../../../libs/Ice.jar -source 1.4 " + javaFile);
		new CommandExecutor(command, true, tempDir);
	
		return true;
	}
	
	private static String path2types, path2modules;
	private String getIncludeFiles(String module, List iceFiles) {
		if(path2types == null) initPaths();
		
		StringBuffer str = new StringBuffer();
		
		
		boolean isModule = true;
		Iterator it = iceFiles.iterator();
		while(it.hasNext()) {
			String fileName = (isModule?path2modules:path2types) + "/"+it.next();
			File file = new File(fileName);
			str.append("-I"+file.getAbsolutePath()+" ");
			isModule = false;
		}
			
		return str.toString();
	}
	
	private String getJavaNames(String dir) {
		StringBuffer sb = new StringBuffer();
		
		File [] children = (new File(dir)).listFiles();
		for(int i=0; i<children.length; i++)
			if(children[i].getName().endsWith(".java"))
				sb.append(" "+children[i].getAbsolutePath());
			
		return sb.toString();
	}
	
	private static void initPaths() {
		Ice.Properties properties = IceNetworkConnection.getProperties();
		path2types = properties.getProperty("xml.path2types");
		path2modules = properties.getProperty("xml.path2modules");
	}
	
	
	private boolean classStillValid(String name, String to, boolean isModule) {
		String javaName;
		if(isModule) javaName = to+"Extensions/Modules/"+name+"/Lethe"; 
		else javaName = to+"Extensions/Types/Lethe/"+name+".java";
	
		String iceName;
		if(isModule) iceName = path2modules+name+"/gen_"+name+".ice"; 
		else iceName = path2types+name+"/gen_"+name+".ice";
		
		File javaFile = new File(javaName);
		File iceFile = new File(iceName);
		
		if(!javaFile.exists()) return false;
		else if(iceFile.lastModified() > javaFile.lastModified()) return false;
		else return true;
	}
	
	private boolean classesStillValid(List iceFiles, String to) {
		Iterator it = iceFiles.iterator();
		while(it.hasNext()) 
			if(!classStillValid((String)it.next(), to, false)) return false;
		
		return true;
	}
}
