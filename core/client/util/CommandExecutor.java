package util;
import java.io.*;

public class CommandExecutor {
	ReadThread outStream, errorStream;
	Process p;
	
	public CommandExecutor(String command, boolean wait) {
		this(command, wait, null);
	}	
	
	public CommandExecutor(String command, boolean wait, File directory) {
		try {
			p = Runtime.getRuntime().exec(command, null, directory);
			outStream = new ReadThread(p.getInputStream());
			errorStream = new ReadThread(p.getErrorStream());
			if(wait) {
				p.waitFor();
				outStream.stopThread = true;
				errorStream.stopThread = true;
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public int getExitcode() {
		return p.exitValue();
	}
	
	class ReadThread extends Thread {
		boolean stopThread;
		InputStream is;
		public ReadThread(InputStream is) {
			this.is = is;
			start();
		}
		
		public void run() {
			try {
				while(!stopThread) {
					if(is.available() > 0)
						System.out.print((char)is.read());
					else sleep(10);
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
