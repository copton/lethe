package service;

import java.util.*;

import java.io.File;
import controller.SourceController;
import controller.filesystem.LocalService;
import controller.svn.SvnService;

import Comm.Exceptions.RessourceNotFoundException;
import Comm.SourceService.*;
import Ice.Communicator;
import Ice.Current;
import Ice.Identity;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import IcePatch2.FileServerPrxHelper;

public class SourceService extends _PublicInterfaceDisp {	
	protected HashMap sourceController;
	protected PersistentSources activeSources;
	private Freeze.Connection connection;
	private ObjectAdapter adapter;
	private Properties properties;
	public SourceService(Communicator communicator) {
	
		properties = communicator.getProperties();	
		connection =          
			 Freeze.Util.createConnection(communicator, "db");
		
		rootDir = properties.getProperty("config.tempdir");
		activeSources = new PersistentSources(connection, "sources", true);	
		sourceController = new HashMap();
		
		// add sourceController from configuration-file
		try {
			sourceController.put(SvnSource.class, new SvnService(properties.getPropertiesForPrefix("config.svn")));
		} catch(Exception e) {
			e.printStackTrace();
		}
		sourceController.put(LocalSource.class, new LocalService(properties.getPropertiesForPrefix("config.file")));
	}
	
	public void setAdapter(ObjectAdapter adapter) {
		this.adapter = adapter;
	}
	
	public void announce(String jobId, String [] files, Filter f, SourceDesc source, Current __current)
	throws RessourceNotFoundException {
		System.out.println("foo");
		String dirName = createTempDirectory(jobId);
		System.out.println("foo1");
		
		SourceController sc = (SourceController)sourceController.get(source.getClass());
		System.out.println("foo2");
		if(sc == null) {
			System.out.println("RessourceNotFound: "+source.getClass());
			throw new RessourceNotFoundException("unknown ressource-type: "+source.getClass());
		}
		System.out.println("foo3");
		
		sc.getSource(source, files, dirName);
		System.out.println("foo4");
		
		filterSource(dirName, files, f);
		System.out.println("foo5");
		
		addServer(jobId, dirName);
		System.out.println("added Server");
	}
	
	private void filterSource(String dirName, String [] files, Filter f) {
		String retainedFilesMatch;
		switch(f.value()) {
		case Filter._CODE:
			retainedFilesMatch = "(IcePatch2.sum)|(.*\\.bz2)|(.*\\.h)|(.*\\.ice)|(.*\\.cpp)|(config.mak)";
			break;
		case Filter._XML:
			retainedFilesMatch = "(IcePatch2.sum)|(.*\\.bz2)|(.*\\.xml)|(.*\\.xsd)|(.*\\.ice)|(instances)";
			break;
		default: return;
		}
	
		for(int i=0; i<files.length; i++) {
			File baseDir = new File(dirName, files[i]);
			File [] children = baseDir.listFiles();
			for(int j=0; j<children.length; j++) 
				if(!children[j].getName().matches(retainedFilesMatch))
					deleteFile(children[j]);
		}
	}
	
	private PatchServerWrapper addServer(String jobId, String dirName) {
		if(!activeSources.containsKey(jobId)) { // else source is active
			try {
				createChecksums(dirName);
				PatchServer server = new PatchServer(dirName);
	
				String fooName = jobId+"/server";
				Identity id = Util.stringToIdentity(fooName);
				
				PatchServerWrapper wrapper = new PatchServerWrapper();
				wrapper.server = FileServerPrxHelper.uncheckedCast(adapter.add(server, id));
				wrapper.counter = 0;
				wrapper.identity = id;
				wrapper.jobId = jobId;
				
				activeSources.put(jobId, wrapper);
				System.err.println("added "+wrapper.server);
				
				return wrapper;
			} catch(Exception e) {
				e.printStackTrace();
			}
 		}
		return null;
	}

	String rootDir;
	private String createTempDirectory(String jobId) {
		File baseDir = new File(rootDir, jobId);
		if(!baseDir.exists()) {
			baseDir.mkdirs();
			try {
				baseDir.createNewFile();
			} catch(Exception e) {
				e.printStackTrace(); // should be logged!
			}
		}
		
		return baseDir.getAbsolutePath();
	}

	private void removeTempDirectory(String jobId) {
		File tempDir = new File(rootDir, jobId);
		deleteFile(tempDir);
	}
	
	private void deleteFile(File f) {
		if(f.isDirectory()) {
			File [] children = f.listFiles();
			for(int i=0; i<children.length; i++)
				deleteFile(children[i]);
		}
		f.delete();
	}
	
	protected synchronized void removeServer(PatchServerWrapper wrapper) {
		removeTempDirectory(wrapper.jobId);
		adapter.remove(wrapper.identity);
	}	
	
	private void createChecksums(String dir) {
		String command = properties.getPropertyWithDefault("config.icepatch2calc",
				"icepatch2calc")+" "+dir;
		System.out.println("cmd: "+command);
		
		try {
			Process proc = Runtime.getRuntime().exec(command);
			java.io.InputStream is = proc.getInputStream();
			java.io.InputStream err = proc.getErrorStream();
			
			proc.waitFor();
			
			while(is.available() > 0) System.out.print((char)is.read());
			while(err.available() > 0) System.out.print((char)err.read());
			
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public String[] getAvailableModules(SourceDesc source, Current __current) {
		SourceController controller = (SourceController)sourceController.get(source.getClass());
		if(controller == null) return new String[0];
		
		try {
			File tempDir = File.createTempFile("temp", "dir", new File(rootDir));
			
			String [] files = controller.getSourceList(source, tempDir.getAbsolutePath());
		
			deleteFile(tempDir);
			return files;
		} catch(Exception e) {
			return new String[0];
		}
	}
	
	private Map userProxies = new HashMap();
	public String syncFiles(SourceDesc source, String[] files, Current __current) {
		try {
			File tempFile = File.createTempFile("user", "sync", new File(rootDir));
			tempFile.mkdirs();
			String dirName = tempFile.getAbsolutePath();
			
			SourceController sc = (SourceController)sourceController.get(source.getClass());
			if(sc == null) throw new RessourceNotFoundException("unknown ressource-type: "+source.getClass());
		
			sc.getSource(source, files, dirName);
			
			filterSource(dirName, files, Filter.XML);
			addServer(tempFile.getName(), dirName);
			
			PatchServerWrapper wrapper = new PatchServerWrapper();
			wrapper =  addServer(dirName, dirName);
			
			if(wrapper != null) userProxies.put(wrapper.server, wrapper);
			
			return wrapper.server.toString();
		} catch(Exception e) {
			return null;
		}
	}

	public void synched(String localProxy, Current __current) {
		if(userProxies.containsKey(localProxy)) {
			PatchServerWrapper wrapper = (PatchServerWrapper)userProxies.get(localProxy);
			
			removeServer(wrapper);
			userProxies.remove(localProxy);
		}
	}

	private ProtectedInterfacePrx interfacePrx = null;
	public void init(ProtectedInterfacePrx interfacePrx) {
		this.interfacePrx = interfacePrx;
	}
	
	public ProtectedInterfacePrx getInterface(Current __current) {
		return interfacePrx;
	}
}
