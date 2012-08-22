package manager;

import java.util.*;

import algorithm.PriorityQueue;

import Comm.Exceptions.ActionNotAllowed;
import Comm.Generator.AMI_Interface_createSimulation;
import Comm.Generator.InterfacePrx;
import Comm.Generator.InterfacePrxHelper;
import Comm.Job.Persistence;
import Comm.Job.SimulationWrapper;
import Comm.Job.Specification;
import Comm.Manager.SimulationObject;
import Comm.Simulation.*;
import Comm.Job.SpecificationHolder;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.UserException;
import IceGrid.NodeInfo;
import IceGrid.ServerInfo;

public class Scheduler extends TimerTask {

	private Manager manager;
	private Timer timer;
	private algorithm.SortingAlgorithm sortingAlgorithm;
	
	public Scheduler(Manager m) {
		manager = m;
		
		int schedulePeriod = manager.communicator.getProperties().getPropertyAsIntWithDefault("Scheduling.TimeSlice", 3600);
		long aktTime = (new Date()).getTime()/1000; // time in seconds
		long delay = schedulePeriod - (aktTime%schedulePeriod);
		
		timer = new Timer();
		timer.scheduleAtFixedRate(this, delay*1000, schedulePeriod*1000);
		System.out.println("schedulePeriod: "+schedulePeriod+", delay="+delay);
	
		sortingAlgorithm = getSortingAlgorithm();
		sortingAlgorithm.init(manager);
	}
	
	private algorithm.SortingAlgorithm getSortingAlgorithm() {
		try {
			String className = manager.communicator.getProperties().getPropertyWithDefault("Scheduling.SortingAlgorithm", "SainteLague");
			Class algorithmClass = ClassLoader.getSystemClassLoader().loadClass("algorithm."+className);
			return (algorithm.SortingAlgorithm)algorithmClass.newInstance();
		} catch(Exception e) {
			manager.logException(e);
			return new algorithm.SainteLague();
		}
	}
	
	protected void stop() {
		timer.cancel();
	}
	
	private boolean isScheduling = false;
	public synchronized void schedule() {
		if(isScheduling) {
			System.out.println("is scheduling");
			return;
		}
		(new Thread(this)).start();
	}
	
	public void run() {
		if(isScheduling) {
			System.out.println("[run] is scheduling");
			return;
		}
		isScheduling = true;
			
		System.out.println("schedule");
		
		// pause old simulations
		pauseOldSimulations();
		System.out.println("paused simulations");
		
		// sort available slots
		List waitingSimulations = getWaitingSimulations();
		System.out.println("waiting simulations: "+waitingSimulations.size());
		if(waitingSimulations.size() == 0) {
			isScheduling = false;
			return; //nothing to schedule
		}
		
		List freeSlots = getFreeSlots();
		System.out.println("freeSlots: "+freeSlots.size());
		if(freeSlots.size() == 0) {
			isScheduling = false;
			return;
		}
		
		System.out.println(freeSlots.size()+"/"+waitingSimulations.size());
		List sortedSimulations = sortingAlgorithm.sort(waitingSimulations, freeSlots.size());
		
		// start next simulations
		while(freeSlots.size() > 0 && waitingSimulations.size() > 0) {
			String jobId = (String)sortedSimulations.remove(0);
			HostNode node = (HostNode)freeSlots.remove(0);
			startJob((Persistence)manager.allSimulations.get(jobId), node);
		}
	
		// remove old paused simulations
		removeOldSimulations();
		isScheduling = false;
		System.out.println("scheduling finished");
	}
	
	private void pauseOldSimulations() {	
		long aktTime = (new Date()).getTime();
		int  pos=0;
		while(pos < manager.runningIds.size()) {
			String roundId = (String)manager.runningIds.get(pos);
			SimulationObject simObj = (SimulationObject)manager.allSimulationObjects.get(roundId);
			Persistence p = (Persistence)manager.allSimulations.get(simObj.jobId);
			SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(simObj.simulationSuffix);
			if(isTooOld((aktTime - simObj.lastChangeTime)/1000)) { // time in java in millisec
				CheckpointHolder cpHolder = new CheckpointHolder();
				SpecificationHolder spHolder = new SpecificationHolder();
				
				try {
					wrapper.simulation.suspend(cpHolder, spHolder);
					wrapper.active = false;
					
					simObj.theState = State.STOPPED;
					simObj.lastChangeTime = (new Date()).getTime();
					
					p.checkpoints[simObj.round] = cpHolder.value;
					manager.allSimulations.put(simObj.jobId, p);
					
					Publisher.reportStatusChange(simObj);
				
					manager.move(roundId, manager.runningIds, manager.scheduledIds);
				} catch(ActionNotAllowed exception) {
					p.freeSimulationObjects.remove(simObj.simulationSuffix);
					manager.allSimulations.put(p, simObj.jobId);
					manager.logException(exception);
				}
			} else pos++;
		}
	}
	
	private void removeOldSimulations() {
		// get all paused simulations
		Map pausedOnNode = getPausedOnNodeSorted();
		
		// remove objects till |objects| <= maxPaused
		Iterator it = pausedOnNode.keySet().iterator();
		while(it.hasNext()) {
			String host = (String)it.next();
			List paused = (List)pausedOnNode.get(host);
				
			int maxPaused = manager.communicator.getProperties().
				getPropertyAsIntWithDefault("Config."+host+".maxPaused", 10);
			
			while(paused.size() > maxPaused) {
				NodeWrapper nodeWrapper = (NodeWrapper)paused.remove((int)(Math.random()*paused.size()));
				
				try {
					nodeWrapper.wrapper.simulation.die();
					for(int i=1; i<=nodeWrapper.p.checkpoints.length; i++) {
						String roundId = nodeWrapper.p.job.jobId+"-"+i;
						SimulationObject simObj = (SimulationObject)manager.allSimulationObjects.get(roundId);
						if(nodeWrapper.wrapper.suffix.equals(simObj.simulationSuffix)) {
							simObj.simulationSuffix = null;
							manager.allSimulationObjects.put(roundId, simObj);
							break;
						}
					}
					
				} catch(Exception e) {
					manager.logException(e);
				} finally {
					nodeWrapper.p.freeSimulationObjects.remove(nodeWrapper.wrapper.suffix);
					manager.allSimulations.put(nodeWrapper.p.job.jobId, nodeWrapper.p);			
				}
			}
		}
	}
	
	private Map getPausedOnNodeSorted() {
		Map m = new HashMap();
		
		Iterator it = manager.allSimulations.values().iterator();
		while(it.hasNext()) {
			Persistence p = (Persistence)it.next();
			Iterator it2 = p.freeSimulationObjects.values().iterator();
			while(it2.hasNext()) {
				SimulationWrapper wrapper = (SimulationWrapper)it2.next();
				if(!wrapper.active) {
					List wrapperList = (List)m.get(wrapper.host);
					if(wrapperList == null) wrapperList = new ArrayList();
					wrapperList.add(new NodeWrapper(p, wrapper));
					m.put(wrapper.host, wrapperList);
				}
			}
		}	
		
		return m;
	}
	
	private class NodeWrapper {
		Persistence p;
		SimulationWrapper wrapper;
		
		public NodeWrapper(Persistence p, SimulationWrapper wrapper) {
			this.p = p;
			this.wrapper = wrapper;
		} 	
	}
	
	private void startJob(Persistence p, HostNode node) {
		System.out.println("start job on node "+node.hostName);
		List simulations = new ArrayList();
		for(int i=0; i<p.checkpoints.length; i++) {
			String roundId = p.job.jobId+"-"+(i+1);
			if(manager.scheduledIds.contains(roundId))
				simulations.add(manager.allSimulationObjects.get(roundId));
		}
			
		SimulationWrapper wrapper = null;
		// search for paused simulations on node
		Iterator it = p.freeSimulationObjects.values().iterator();
		while(it.hasNext()) {
			wrapper = (SimulationWrapper)it.next();
			if(!wrapper.host.equals(node.hostName)) {
				wrapper = null;
				continue;
			}
				
			Iterator it2 = simulations.iterator();
			while(wrapper != null && it2.hasNext()) {
				SimulationObject simObj = (SimulationObject)it2.next();
				if(!wrapper.suffix.equals(simObj.simulationSuffix)) continue;
				
				if(simObj.theState == State.RUNNING) {
					wrapper = null;
					break;
				}
				else if(simObj.theState == State.STOPPED) {
					String roundId = simObj.jobId+"-"+simObj.round;	
					try{
						wrapper.simulation._continue();
						simObj.theState = State.RUNNING;
						System.out.println("found object and continued");
						manager.move(roundId, manager.scheduledIds, manager.runningIds);
						Publisher.reportStatusChange(simObj);
					
						return;
					} catch(Exception e) {
						try {
							wrapper.simulation.die();
						} catch(Exception ex) {}
				
						manager.logException(e);
					
						p.freeSimulationObjects.remove(simObj.simulationSuffix);
						simObj.simulationSuffix = null;
						manager.allSimulationObjects.put(roundId, simObj);
						manager.allSimulations.put(simObj.jobId, p);
					}
				}
			}
		}
			
		// else create new Object
		SimulationObject simObj = (SimulationObject)simulations.get(0);
		manager.scheduledIds.remove(simObj.jobId+"-"+(simObj.round+1));
		
		if(wrapper == null) {
			System.out.println("build simulation "+simObj.jobId);
			buildSimulation(simObj, node);
		} else {
			System.out.println("start simulation "+simObj.jobId+" on node "+wrapper.host);
			
			InitCallback init = new InitCallback(simObj, wrapper);
			(new Thread(init)).start();
		
			simObj.simulationSuffix = wrapper.suffix;
			manager.allSimulationObjects.put(simObj.jobId+"-"+(simObj.round+1), simObj);		
		}
		
	}
	
	private List getWaitingSimulations() {
		Set ids = new HashSet();
		Iterator it = manager.scheduledIds.iterator();
		while(it.hasNext()) 
			ids.add(manager.getParentJobId((String)it.next()));
		
		
		List jobs = new ArrayList();
		it = ids.iterator();
		while(it.hasNext()) 
			jobs.add(manager.allSimulations.get(it.next()));
		
		return jobs;
	}
	
	protected String getGeneratorOnLeastLoadedNode() {
		List slots = getSlots();
		if(slots.size() == 0) return null;
		
		HostNode node = (HostNode)slots.get(0);
		return "Generator@"+node.serverId+".GeneratorAdapter";
	}
	
	private List getFreeSlots() {
		List slots = new ArrayList();
		List nodes = getSlots();
		Iterator it = nodes.iterator();
		Map runningNodes = getNumberOfRunningNodes();
		
		while(it.hasNext()) {
			HostNode node = (HostNode)it.next();
			int maxRunningNodes = manager.communicator.getProperties().
					getPropertyAsIntWithDefault("Config."+node.hostName+".maxRunning", 1);
			
			int running = 0;
			if(runningNodes.containsKey(node.nodeName))
				running = ((Integer)runningNodes.get(node.nodeName)).intValue();
			int freeSlots = maxRunningNodes - running;
			for(int i=0; i<freeSlots; i++)
				slots.add(node);
		}
		return slots;
	}
	
	protected List getSlots() {
		List generatorIds = getGeneratorIds();
		
		PriorityQueue sorted = new PriorityQueue();
		Iterator it = generatorIds.iterator();
		while(it.hasNext()) {
			HostNode node = new HostNode((String)it.next());
			sorted.add(node);
		}	
		
		return sorted;
	}
	
	private Map getNumberOfRunningNodes() {
		Map nodes = new HashMap();
		
		Iterator it = manager.runningIds.iterator();
		while(it.hasNext()) {
			SimulationObject simObj = (SimulationObject)manager.allSimulationObjects.get(it.next());
			Persistence p = (Persistence)manager.allSimulations.get(simObj.jobId);
			SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(simObj.simulationSuffix);
			
			String node = wrapper.host;
			System.out.println("NODE NAME: "+node);
			
			if(nodes.containsKey(node)) {
				Integer i = (Integer)nodes.get(node);
				nodes.put(node, new Integer(i.intValue()+1));
			} else nodes.put(node, new Integer(1));
		}
		
		return nodes;
	}
	
	private List getGeneratorIds() {
		List ids = new ArrayList();
		String [] idList = manager.adminPrx.getAllServerIds();
		
		for(int i=0; i<idList.length; i++)
			if(idList[i].matches("Generator\\d+")) 
				ids.add(idList[i]);
		
		return ids;
	}
	
	private boolean isTooOld(long timeDiff) {
		int maxDiff = manager.communicator.getProperties().getPropertyAsInt("Scheduling.TimeSlice");
		return timeDiff >= maxDiff;
	}

	
	protected class HostNode implements Comparable {
		String serverId;
		String hostName;
		String nodeName;
		float load;
		
		public HostNode(String id) {
			serverId = id;
			
			try {
				ServerInfo info = manager.adminPrx.getServerInfo(id);
				load = manager.adminPrx.getNodeLoad(info.node).avg15;
				NodeInfo nodeInfo = manager.adminPrx.getNodeInfo(info.node);
				hostName = nodeInfo.hostname;
				nodeName = info.node;
			} catch(Exception e) {
				load = -1;
			}
		}

		public int compareTo(Object o) {
			HostNode node = (HostNode)o;
			
			if(load < node.load) return -1;
			if(load > node.load) return 1;
			return 0;
		}
	}
	
	// build, init && start/resume simulation
	private void buildSimulation(SimulationObject simObj, HostNode node) {
		ObjectPrx proxy = manager.communicator.stringToProxy("Generator@"+node.serverId+".GeneratorAdapter");
		InterfacePrx generator = InterfacePrxHelper.checkedCast(proxy);
		if(generator == null) return;
		
		Persistence p = (Persistence)manager.allSimulations.get(simObj.jobId);
		
		SimulationWrapper wrapper = new SimulationWrapper();
		wrapper.host = node.hostName;
		wrapper.active = true;
		wrapper.suffix = Integer.toString(p.nextSuffixId++);
		simObj.simulationSuffix = wrapper.suffix;
		
		BuiltCallback callback = new BuiltCallback(p.job, wrapper, simObj); 
		generator.createSimulation_async(callback, p.job, simObj.simulationSuffix, false);
		manager.allSimulations.put(simObj.jobId, p);
	}
	
	private class BuiltCallback extends AMI_Interface_createSimulation implements Runnable {
	
		private Specification job;
		private SimulationWrapper wrapper;
		private SimulationObject simObj;
		
		public BuiltCallback(Specification job, SimulationWrapper wrapper, SimulationObject simObj) {
			this.job = job;
			this.wrapper = wrapper;
			this.simObj = simObj;
		}
		
		public void ice_response(Comm.Simulation.InterfacePrx simulation) {
			Persistence p = (Persistence)manager.allSimulations.get(job.jobId);
			wrapper.simulation = simulation;
			p.freeSimulationObjects.put(wrapper.suffix, wrapper);
			manager.allSimulations.put(job.jobId, p);
			
			System.out.println("built object");
			(new Thread(this)).start(); // start new thread to init simulation. Bug (?) in Java-Implementation
		}

		public void ice_exception(LocalException ex) {
			ex.printStackTrace();
			manager.setError(simObj, ex);
		}

		public void ice_exception(UserException ex) {
			ex.printStackTrace();
			manager.setError(simObj, ex);
		}

		public void run() {
			System.out.println("init object");
			InitCallback callback = new InitCallback(simObj, wrapper);
			wrapper.simulation.init_async(callback, job);
		}
	}
	
	private class InitCallback extends AMI_Interface_init implements Runnable {

		private SimulationObject simObj;
		private SimulationWrapper wrapper;
		
		public InitCallback(SimulationObject simObj, SimulationWrapper wrapper) {
			this.simObj = simObj;
			this.wrapper = wrapper;
		}
		
		public void ice_response() {
			System.out.println("init object");
			(new Thread(this)).run();
		}

		public void ice_exception(LocalException ex) {
			ex.printStackTrace();
			manager.setError(simObj, ex);
		}

		public void ice_exception(UserException ex) {
			ex.printStackTrace();
			manager.setError(simObj, ex);
		}

		public void run() {
			if(!simObj.hasCheckpoint) {
				StartCallback callback = new StartCallback(simObj);
				wrapper.simulation.start_async(callback, simObj.round);
				System.out.println("started simulation");
			} else {
				Persistence p = (Persistence)manager.allSimulations.get(simObj.jobId);
				ResumeCallback callback = new ResumeCallback(simObj);
				wrapper.simulation.resume_async(callback, p.checkpoints[simObj.round]);
				System.out.println("continued simulation");
			}
		}
	}
	
	private class StartCallback extends AMI_Interface_start {

		private SimulationObject simObj;
		public StartCallback(SimulationObject simObj) {
			this.simObj = simObj;
		}
		
		public void ice_response() {
			simObj.theState = State.RUNNING;
			manager.runningIds.add(simObj.jobId+"-"+(simObj.round+1));
			Publisher.reportStatusChange(simObj);
		}

		public void ice_exception(LocalException ex) {
			manager.setError(simObj, ex);
		}

		public void ice_exception(UserException ex) {
			manager.setError(simObj, ex);
		}
		
	}
	
	private class ResumeCallback extends AMI_Interface_resume {

		private SimulationObject simObj;
		public ResumeCallback(SimulationObject simObj) {
			this.simObj = simObj;
		}
		
		public void ice_response() {
			simObj.theState = State.RUNNING;
			manager.runningIds.add(simObj.jobId+"-"+(simObj.round+1));
			Publisher.reportStatusChange(simObj);
		}

		public void ice_exception(LocalException ex) {
			manager.setError(simObj, ex);
		}

		public void ice_exception(UserException ex) {
			manager.setError(simObj, ex);
		}
	}
}
