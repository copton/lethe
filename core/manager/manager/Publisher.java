package manager;

import java.util.*;
import Comm.Job.Persistence;
import Comm.Manager.ChangedId;
import Comm.Manager.PublisherPrx;
import Comm.Manager.PublisherPrxHelper;
import Comm.Manager.SimulationObject;
import Comm.Simulation.Checkpoint;
import Comm.Simulation.Status;
import IceStorm.NoSuchTopic;
import IceStorm.TopicManagerPrx;
import IceStorm.TopicManagerPrxHelper;
import IceStorm.TopicPrx;
import Comm.Logger.*;

public class Publisher implements Runnable {
	
	
	private Manager manager;
	private Publisher(Manager manager) {
		System.out.println("start publisher");
		 Ice.ObjectPrx obj = manager.communicator.stringToProxy("IceStorm/TopicManager");
		 if(obj == null) System.out.println("obj is null");
	
		 simulationStatusManager = TopicManagerPrxHelper.checkedCast(obj);	
		  
		 this.manager = manager;
		 if(manager == null) System.out.println("manager is null!");
//		 manager.sysLog.add(new Message(TypeEnum.NOTICE, "Manager.Publisher", "Publisher started", (new java.util.Date()).getTime()));
	}
	
	private static Publisher self;
	public static void init(Manager manager) {
		self = new Publisher(manager);
		System.out.println("initialized publisher");
		
	}
	
	private TopicManagerPrx simulationStatusManager;	
	private static List changedSimulations = new ArrayList();
	private static List finishedSimulations = new ArrayList();
	public static void reportStatusChange(SimulationObject simulation) {
		changedSimulations.add(simulation);
		(new Thread(self)).start();
	}
	
	public static void reportSimulationFinished(Persistence p) {
		finishedSimulations.add(p);
		(new Thread(self)).start();
	}
	
	protected HashMap userTopics = new HashMap();
	public static void addTopic(String name) {
		if(self.userTopics.containsKey(name)) return;
	
		try {
			self.userTopics.put(name, self.simulationStatusManager.create(name));
		} 
		catch(IceStorm.TopicExists ex) {
			try {
				self.userTopics.put(name, self.simulationStatusManager.retrieve(name));
			} catch(NoSuchTopic e) {} // kann ausgeschlossen werden, da ansonsten angelegt
		}
		catch(Exception ex) {}
	}
	
	protected void publishChanges(SimulationObject simulation) {
		String roundId =  simulation.jobId+"-"+(simulation.round+1);
		if(manager.allSimulationObjects.containsKey(roundId))
			manager.allSimulationObjects.put(roundId, simulation);
		
		ChangedId changed = new ChangedId();
		changed.jobId = simulation.jobId;
		changed.round = simulation.round;
		
		Persistence p = (Persistence)manager.allSimulations.get(simulation.jobId);
		Comm.Simulation.State oldState = p.checkpoints[simulation.round].theStatus.theState;
		
		p.checkpoints[simulation.round].theStatus.theState = simulation.theState;
		manager.allSimulations.put(simulation.jobId, p);
		
		changed.newStatus = p.checkpoints[simulation.round].theStatus;
		changed.collocatedStatus = manager.getCollocatedStatus(getStatusListFromCheckpoints(p.checkpoints));
		
		TopicPrx topic;
		try {
			topic = self.simulationStatusManager.retrieve(p.job.owner);
		} catch(NoSuchTopic e) {
			try {
				topic = self.simulationStatusManager.create(p.job.owner);
			} catch(Exception ex) {return;}
		}
		
		Ice.ObjectPrx  pub = topic.getPublisher();
		if(!pub.ice_isDatagram())
			pub = pub.ice_oneway();
		PublisherPrx publisher = PublisherPrxHelper.uncheckedCast(pub);
		publisher.report(changed);
		
		String msg = "job "+p.job.jobId+" round "+(simulation.round+1)
		+" changed status from "+oldState+" to "+simulation.theState;

		manager.sysLog.add(new Message(TypeEnum.INFO, "Manager", msg, (new Date()).getTime()/1000));
		System.out.println("reported "+msg);
	}
	
	private void publishFinished(Persistence p) {
		TopicPrx topic;
		try {
			topic = self.simulationStatusManager.retrieve(p.job.owner);
		} catch(NoSuchTopic e) {
			try {
				topic = self.simulationStatusManager.create(p.job.owner);
			} catch(Exception ex) {return;}
		}
		
		Ice.ObjectPrx  pub = topic.getPublisher();
		if(!pub.ice_isDatagram())
			pub = pub.ice_oneway();
		PublisherPrx publisher = PublisherPrxHelper.uncheckedCast(pub);
		publisher.simulationFinished(p);

		String msg = "SIMULATION "+p.job.jobId+" HAS FINISHED.";
		manager.sysLog.add(new Message(TypeEnum.INFO, "Manager", msg, (new Date()).getTime()/1000));
		System.out.println("reported "+msg);
	}
	
	
	private Status[] getStatusListFromCheckpoints(Checkpoint [] cp) {
		Status [] st = new Status[cp.length];
		for(int i=0; i<st.length; i++)
			st[i] = cp[i].theStatus;
		
		return st;
	}

	public synchronized void run() {
		while(!changedSimulations.isEmpty()) 
			publishChanges((SimulationObject)changedSimulations.remove(0));	

		while(!finishedSimulations.isEmpty()) 
			publishFinished((Persistence)finishedSimulations.remove(0));
	}
}
