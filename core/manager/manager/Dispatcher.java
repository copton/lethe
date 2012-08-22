package manager;

import java.util.*;

import callbacks.*;

public class Dispatcher extends ArrayList implements Runnable {
	
	private Dispatcher() {
		super();
		(new Thread(this)).start();
	}
	
	private static Dispatcher handler = new Dispatcher();
	public static Dispatcher get() {
		return handler;
	}
	
	public synchronized boolean add(Object o) {
		if(!(o instanceof Action)) return false;
		
		super.add(o);
		notify();
		return true;
	}
	
	public synchronized void run() {
		while(true) {
			while(isEmpty()) 
				try {wait(60000);} catch(Exception e) {} // check every minute for new data
			
			Action a = (Action)remove(0);
			a.doAction();
		}
	}
}
