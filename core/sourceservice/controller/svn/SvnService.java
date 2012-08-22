package controller.svn;

import controller.SourceController;
import Comm.Exceptions.RessourceNotFoundException;
import Comm.SourceService.*;

import java.io.File;
import java.util.*;

public class SvnService implements SourceController {
	
	String user, password;
	String pathToRepository, pathInRepository;
	String svnCommand;
	public SvnService(Map properties) throws Exception {
		svnCommand = (String)properties.get("config.svn.cmd");
		user =  (String)properties.get("config.svn.user");
		password =  (String)properties.get("config.svn.password");
		pathToRepository =  (String)properties.get("config.svn.pathToRepository");
		pathInRepository =  (String)properties.get("config.svn.pathInRepository");
	}
	
	public synchronized void getSource(SourceDesc source, String [] files, String directory) 
			throws RessourceNotFoundException {
		SvnSource svnSource = (SvnSource)source;
		
		
		Map sourceMap = getSourceList(files, svnSource);
		java.util.Iterator it = sourceMap.keySet().iterator();
		while(it.hasNext()) {
			String dir = (String)it.next();
			String command = svnCommand
				+" checkout "+(svnSource.revision>0?"-r "+svnSource.revision:"")
				+(user!=null&&password!=null?
						" --username "+user + " --password "+password+" ":"")
				+ sourceMap.get(dir) + " " + directory+"/"+dir;
			System.out.println("cmd: "+command);
			try {
				Process svnProc = Runtime.getRuntime().exec(command);
				svnProc.waitFor();		
			} catch(Exception e) {
				throw new RessourceNotFoundException(e.getMessage());
			}
		}
		
		removeAllSvnLinks(directory);
	}
	
	private void removeAllSvnLinks(String directory) {
		File dir = new File(directory);
		removeAllSvnLinks(dir);
	}
	
	private void removeAllSvnLinks(File dir) {
		if(dir.getName().equals(".svn"))
			deleteFile(dir);
		else if(dir.isDirectory()){
			File [] children = dir.listFiles();
			for(int i=0; i<children.length; i++)
				removeAllSvnLinks(children[i]);
		}
	}
	
	private void deleteFile(File f) {
		if(f.isDirectory()) {
			File [] children = f.listFiles();
			for(int i=0; i<children.length; i++)
				deleteFile(children[i]);
		}
		f.delete();
	}
	
	private Map getSourceList(String [] files, SvnSource svnSource) {
		Map sourceMap = new HashMap();
		String base = pathInRepository;
		if(!base.endsWith("/") && !base.endsWith("\\")) base += "/";
		for(int i=0; i<files.length; i++) {
			String dir = files[i].substring(0, files[i].lastIndexOf("/"));
			String source = (String)sourceMap.get(dir);
			
			if(source == null) source = base+files[i];
			else source += " "+base+files[i] ;
			sourceMap.put(dir, source);
		}
		
		return sourceMap;
	}

	public String[] getSourceList(SourceDesc source, String directory) {
	
		List fileList = new ArrayList();
		SvnSource svnSource = (SvnSource)source;
		
		String command = svnCommand
			+" checkout "+(svnSource.revision>0?"-r "+svnSource.revision:"")
			+(user!=null&&password!=null?
					" --username "+user + " --password "+password+" ":"")
			+ pathInRepository+" .";
		System.out.println("cmd: "+command);
		try {
			Process svnProc = Runtime.getRuntime().exec(command);
			svnProc.waitFor();
				
			System.out.println("\nstatuscode: "+svnProc.exitValue());
			
			String [] types = {"modules", "types"};
			
			for(int i=0; i<types.length; i++) {
				File file = new File(types[i], directory);
				if(!file.exists()) continue;
				
				File [] children = file.listFiles();
				for(int j=0; j<children.length; j++) 
					if(children[j].isDirectory())
						fileList.add(types[i]+File.separator+children[j].getName());
			}	
		} catch(Exception e) {
			return new String[0];
		}
	
		return (String[])fileList.toArray(new String[0]);
	}
}
