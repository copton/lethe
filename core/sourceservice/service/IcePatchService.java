package service;

public class IcePatchService implements Runnable {
	private Process proc;
	String cmd, initCmd;
	
	public IcePatchService(String directory) {
		String cmdRoot = "/usr/local/ice/bin/";
		initCmd = cmdRoot+"icepatch2calc "+directory;
		cmd = cmdRoot+"icepatch2server -v "+directory + " --IcePatch2.Endpoints=default"; 
		// TODO: setze Endpoints irgendwie auf irgendeinen Wert, bzw stelle sicher, das immer nur ein user ein directory synct
	}
	
	protected void start() throws java.io.IOException {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}
	
	public void run() {
		try {
			proc = Runtime.getRuntime().exec(initCmd);	
			proc.waitFor();
			proc = Runtime.getRuntime().exec(cmd);	
			proc.waitFor();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	protected void stop() {
		proc.destroy();
	}
	
	protected Process getProcess() {
		return proc;
	}
	
	protected java.io.InputStream getStream() {
		return proc.getInputStream();
	}
}
