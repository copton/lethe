package manager;

import java.util.*;

import Comm.AuthenticationService.ProtectedInterfacePrx;
import Comm.AuthenticationService.ProtectedInterfacePrxHelper;
import Comm.Exceptions.*;
import Comm.Generator.AMI_Interface_createSimulation;
import Comm.Job.JobInformation;
import Comm.Job.Persistence;
import Comm.Job.SimulationWrapper;
import Comm.Job.Specification;
import Comm.Logger.Message;
import Comm.Logger.PublishPrx;
import Comm.Logger.PublishPrxHelper;
import Comm.Logger.TypeEnum;
import Comm.Manager.*;
import Comm.Simulation.AMI_Interface_createCheckpoint;
import Comm.Simulation.State;
import Comm.Simulation.Checkpoint;
import Comm.Simulation.Status;
import Ice.Current;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.UserException;
import Ice.Util;
import IceGrid.AdminPrx;

public class Manager extends _PublicInterfaceDisp {	
	private Freeze.Connection connection;
	
	AdminPrx adminPrx;
	Comm.Manager.JobIds jobIdMap;
	PersistentSimulations allSimulationObjects;
	protected Scheduler scheduler;
	protected PublishPrx sysLog;
	public void init() {
		 Publisher.init(this);  
	
		 updateAvailableHosts();
		 
	     initIdLists();  
	     initListedUsers();
	     
	     ObjectPrx obj = communicator.stringToProxy("Publish@Breeze.BreezeAdapter");
		 sysLog = PublishPrxHelper.checkedCast(obj);
		 
		 scheduler = new Scheduler(this);
		 
	     String [] users = getAllUsers(null, true);
	     for(int i=0; i<users.length; i++) Publisher.addTopic(users[i]);
	     
	     scheduler.schedule();
	     System.out.println("Manager: initialized");
	}
	
	private void initListedUsers() {
		Iterator it = allSimulations.values().iterator();
		while(it.hasNext()) {
			Persistence p = (Persistence)it.next();
			listedUsers.add(p.job.owner);
		}
	}
	
	public void shutdown() {
		scheduler.stop();
		
		allSimulations.close();
		allSimulationObjects.close();
		jobIdMap.close();
		connection.close();
	}
	
	private Manager(Ice.Communicator communicator) {
		connection =          
			 Freeze.Util.createConnection(communicator, "db");
		allSimulations = new PersistantJobs(connection, "jobs", true);
		allSimulationObjects = new PersistentSimulations(connection, "rounds", true); 
		jobIdMap = new Comm.Manager.JobIds(connection, "ids", true);
		 
		 this.communicator = communicator;
	
	     adminPrx = IceGrid.AdminPrxHelper.checkedCast(communicator.stringToProxy("IceGrid/Admin"));
	}

	List scheduledIds = new ArrayList();
	List pausedIds = new ArrayList();
	List runningIds = new ArrayList();
	List userPausedIds = new ArrayList();
	private void initIdLists() {
		Iterator it = allSimulationObjects.keySet().iterator();
		while(it.hasNext()) {
			String roundId = (String)it.next();
			SimulationObject sim = (SimulationObject)allSimulationObjects.get(roundId);
			
			switch(sim.theState.value()) {
			case State._ERROR:
			case State._FINISHED: break;
			default:
				if(sim.simulationSuffix != null) {
					Persistence p = (Persistence)allSimulations.get(sim.jobId);
					try {
						SimulationWrapper simulation = (SimulationWrapper)p.freeSimulationObjects.get(sim.simulationSuffix);
						simulation.simulation.ice_ping();	
						
						if(sim.removeFromScheduling) pausedIds.add(roundId);
						else if(sim.theState == State.RUNNING) runningIds.add(roundId);
						else scheduledIds.add(roundId);
					} catch(Exception e) {
						p.freeSimulationObjects.remove(sim.simulationSuffix);
						allSimulations.put(sim.jobId, p);
						
						sim.simulationSuffix = null;
						sim.theState = State.STOPPED;
						allSimulationObjects.put(roundId, sim);
						scheduledIds.add(roundId);
					}
				} else scheduledIds.add(roundId);
			}
		}
	}
	
    public Ice.Communicator communicator = null;
    private	ProtectedInterfacePrx authPrx;
	private static Manager self;
	public static Manager createManager(Ice.Communicator communicator) {
		self = new Manager(communicator);
		return self;
	}
	
	private ProtectedInterfacePrx getAuthProxy() {
		String proxy = "Permission@Authentication.ServiceAdapter";
		ObjectPrx iceObject = communicator.stringToProxy(proxy);
		ProtectedInterfacePrx prx = ProtectedInterfacePrxHelper.checkedCast(iceObject);
		return prx;
	}
	
	private JobInformation getInformation(Persistence job) {
		JobInformation info = new JobInformation();

		info = new JobInformation();
		info.jobId = job.job.jobId;
		info.name = job.job.name;
		info.owner = job.job.owner;
		info.description = job.job.description;
		info.startTime = job.job.schedulerInfo.startTime;
	
		info.settings = job.job.settings;
		info.results = job.results;
		
 		int rounds = job.checkpoints.length;	
		info.collocatedStatus = new Status();
		info.roundIds = new String[rounds];
		info.status = new Status[rounds];
		
		for(int i=0; i<rounds; i++) {
			info.roundIds[i] = info.jobId+"-"+(i+1);
			info.status[i] = job.checkpoints[i].theStatus;
		}
		info.collocatedStatus = getCollocatedStatus(info.status);
		
		return info;
	}
	
	protected Status getCollocatedStatus(Status [] statusList) {
		
		Status collocatedStatus = new Status();
		
		int [] status = new int[7];
		for(int i=0; i<statusList.length; i++) {
			collocatedStatus.livetime += statusList[i].livetime;
			collocatedStatus.runtime  += statusList[i].runtime;
			collocatedStatus.cputime += statusList[i].cputime;
			
			status[statusList[i].theState.value()]++;
		}
		
		if(status[State._ERROR] > 0) collocatedStatus.theState = State.ERROR;
		else if(status[State._RUNNING] > 0) collocatedStatus.theState = State.RUNNING;
		else if(status[State._STOPPED] > 0) collocatedStatus.theState = State.STOPPED;
		else if(status[State._READY] > 0) collocatedStatus.theState = State.READY;
		else if(status[State._NEW] > 0) collocatedStatus.theState = State.NEW;
		else if(status[State._FINISHED] > 0) collocatedStatus.theState = State.FINISHED;
		else collocatedStatus.theState = State.NEW;	
	
		return collocatedStatus;
	}
	
	private void checkAuthentication(Map context, String owner, String action) 
	throws ActionNotAllowed {
		if(authPrx == null) authPrx = getAuthProxy();
		
		String user = (String)context.get("user");
		String sessionId = (String)context.get("sessionId");
		
		if(user == null || sessionId == null) throw new ActionNotAllowed("user not authenticated");
		if(owner == null) owner = user;
			
		if(authPrx == null || !authPrx.userExists(user, sessionId) 
				|| !authPrx.hasPermission(user, owner, action)) 
			throw new ActionNotAllowed(action);
	}
	
	PersistantJobs allSimulations;
	private List availableHosts = new ArrayList();
	private void updateAvailableHosts() {
		availableHosts = new ArrayList();
		String [] ids = adminPrx.getAllAdapterIds();
		for(int i=0; i<ids.length; i++) {
			if(ids[i].endsWith(".GeneratorAdapter")) {
				try {
					Comm.Generator.InterfacePrx generatorPrx = 
						Comm.Generator.InterfacePrxHelper.checkedCast(communicator.stringToProxy("Generator@"+ids[i]));
					availableHosts.add(generatorPrx);
				} catch(Exception e) {}
			}
		}
	}
	
	public void pauseSimulation(String jobId, Current __current) throws ActionNotAllowed {
		Specification job = getJob(getParentJobId(jobId));
		
		checkAuthentication(__current.ctx, job.owner, "simulation.stop");
		boolean callScheduler = false;
		
		if(allSimulations.containsKey(jobId)) { // pause all rounds, if jobId 
			for(int i=0; i<job.settings.length; i++) 
				callScheduler = pauseSimulationRound(job.jobId+"-"+(i+1)) || callScheduler;
			
		} else callScheduler = pauseSimulationRound(jobId);
	
		if(callScheduler) {
			scheduler.schedule();
		}
	}
		
	private boolean pauseSimulationRound(String jobId) throws ActionNotAllowed {
		System.out.print("pause simulation for "+jobId);
		SimulationObject simulation = (SimulationObject)allSimulationObjects.get(jobId);
		if(simulation == null) {
			System.out.println(", but job wasn't found");
			return false; // job not found
		}

		if(simulation.theState == State.ERROR || 
				simulation.theState == State.FINISHED) return false;
		
		boolean callScheduler = (simulation.theState == State.RUNNING);
		if(simulation.theState == State.RUNNING) {
			Persistence p = (Persistence)allSimulations.get(simulation.jobId);
			SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(simulation.simulationSuffix);
			wrapper.simulation.stop();
			wrapper.active = false;
			allSimulations.put(simulation.jobId, p);
			
			simulation.simulationSuffix = null;
		}
			
		userPausedIds.add(jobId);
		
		scheduledIds.remove(jobId);
		runningIds.remove(jobId);
		pausedIds.remove(jobId);
		
		simulation.theState = State.STOPPED;
		Publisher.reportStatusChange(simulation);
	
		return callScheduler;
	}

	Dispatcher handler = Dispatcher.get();
	public synchronized void startSimulation_async(AMD_PublicInterface_startSimulation __cb, Specification job, Current __current) throws ActionNotAllowed, BuildException, SpecException {
		if(job.owner == null) job.owner = (String)__current.ctx.get("user");
		
		checkAuthentication(__current.ctx, job.owner, "simulation.start");
		
		Publisher.addTopic(job.owner);
		
		String proxyString = scheduler.getGeneratorOnLeastLoadedNode();
		job.schedulerInfo.startTime = (new Date()).getTime();

		if(proxyString == null) { // can't test for compiling, thus add and hope
			addNewJob(job);
			scheduler.schedule();
			__cb.ice_response(true);
		} else {
			ObjectPrx proxy = communicator.stringToProxy(proxyString);
			System.out.println("proxyString: "+proxyString);
			Comm.Generator.InterfacePrx generator = Comm.Generator.InterfacePrxHelper.checkedCast(proxy);
			
			BuiltCallback callback = new BuiltCallback(job, __cb);
			generator.createSimulation_async(callback, job, "0", true);
		}
		
	}
	
	public void restartSimulation(String jobId, Current __current) throws ActionNotAllowed {
		Specification job = getJob(getParentJobId(jobId));
		
		checkAuthentication(__current.ctx, job.owner, "simulation.restart");
		
		if(jobId.indexOf("-") < 0) { // parent job -> restart all rounds
			for(int i=1; i<=job.settings.length; i++) 
				restartSimulation(job.jobId+"-"+i);
			return;
		}
		
		if(userPausedIds.contains(jobId)) {
			SimulationObject obj = 
				(SimulationObject)allSimulationObjects.get(jobId);
			
			obj.removeFromScheduling = false;
			allSimulationObjects.put(jobId, obj);
			
			move(jobId, userPausedIds, scheduledIds);
			scheduler.schedule();
		} 
	}

	public void abortSimulation(String jobId, Current __current) 
		throws ActionNotAllowed {
		jobId = getParentJobId(jobId);
		
		// get all active nodes and call die on them!
		Persistence p  = (Persistence)allSimulations.get(jobId);
		if(p == null) return;
	
		boolean exit = true;
		if(__current == null) System.out.println("current is null");
		else if(__current.ctx == null) System.out.println("ctx is null");
		else if(__current.ctx.get("user") == null) System.out.println("user is null");
		else if(p.job.owner == null) System.out.println("owner is null");
		else exit = false;
		
		if(exit) System.exit(0);
		
		checkAuthentication(__current.ctx, p.job.owner, "simulation.abort");
		
		SimulationObject simulation;
		for(int i=1; i<=p.job.settings.length; i++)	{
			String roundId = jobId+"-"+i;
			simulation = (SimulationObject)allSimulationObjects.get(roundId);
			if(simulation != null) {
				if(simulation.simulationSuffix == null) System.out.println("round "+(simulation.round+1)+" is null");
				else {
					SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(simulation.simulationSuffix);
					wrapper.simulation.die();
					p.freeSimulationObjects.remove(simulation.simulationSuffix);
				}
			}
			
			runningIds.remove(roundId);
			pausedIds.remove(roundId);
			scheduledIds.remove(roundId);
			userPausedIds.remove(roundId);
			allSimulationObjects.remove(roundId);
		}
		
		allSimulations.remove(jobId);
		System.out.println("aborted simulation: "+jobId);
	}
	
	private Specification getJob(String jobId) {
		Persistence p = (Persistence)allSimulations.get(jobId);
		if(p == null) return null;
		else return p.job;
	}

	protected String getParentJobId(String jobId) {
		int endOfParent = jobId.indexOf("-");
		if(endOfParent < 0) return jobId;
		else return jobId.substring(0, endOfParent);
	}

	private void addNewJob(Specification job) {
		Persistence p = createPersistence(job);
		p.job.schedulerInfo.startTime = (new Date()).getTime();
		allSimulations.put(job.jobId, p);
		
		
		for(int i=0; i<p.checkpoints.length; i++) {
			SimulationObject simObj = new SimulationObject();
			simObj.jobId = job.jobId;
			simObj.round = i;
			simObj.theState = State.NEW;
			simObj.simulationSuffix = null;
			simObj.lastChangeTime = (new Date()).getTime();
			simObj.hasCheckpoint = false;
			simObj.removeFromScheduling = false;
			
			String roundId = simObj.jobId+"-"+(simObj.round+1);
			allSimulationObjects.put(roundId, simObj);
			scheduledIds.add(roundId);
		}
	}
	
	public String[] getSimulationHosts(Current __current) {
		Set hostNames = new HashSet();
		String [] nodeNames = adminPrx.getAllNodeNames();
		for(int i=0; i<nodeNames.length; i++) {
			try {
				hostNames.add(adminPrx.getNodeHostname(nodeNames[i]));
			} catch(Exception e) {} // Host not reachable -> ignore it
		}
		
		return (String[])hostNames.toArray(new String[0]);
	}

	public String[] getActiveUsers(Current __current) {
		// TODO: not implemented yet
		return getAllUsers(__current);
	}

	public String[] getAllUsers(Current __current) {
		return getAllUsers(__current.ctx, false);
	}

	private Set listedUsers = new HashSet();
	private String [] getAllUsers(Map context, boolean isSystem) {
		if(isSystem) return (String[])listedUsers.toArray(new String[0]);
		else {
			List allowedUsers = new ArrayList();
			Iterator it = listedUsers.iterator();
			while(it.hasNext()) {
				String owner = (String)it.next();
				try {
					checkAuthentication(context, owner, "simulation.show");
					allowedUsers.add(owner);
				} catch(Exception e) {}
			}
			String user = (String)context.get("user");
			if(!allowedUsers.contains(user)) allowedUsers.add(user);
			System.out.println("allowed users="+allowedUsers);
			return (String [])allowedUsers.toArray(new String[0]);
		}
	}
	
	private Map simulationStatus = new HashMap();
	public Status[] getSimulationStatus(String jobId, Current __current) {
		List l = (List)simulationStatus.get(jobId);
		if(l == null) return new Status[0];
		else return (Status[])l.toArray(new Status[0]);
	}

	public Map getSimulationStatusList(String[] jobIds, Current __current) {
		Map m = new HashMap();
		
		for(int i=0; i<jobIds.length; i++)
			m.put(jobIds[i], getSimulationStatus(jobIds[i], __current));
		return m;
	}

	protected Persistence createPersistence(Specification job) {
		Persistence p = new Persistence();
		p.job = job;
		int rounds = job.settings == null?0:job.settings.length;
		p.checkpoints = new Checkpoint[rounds];
		p.results = new Map[rounds];
		p.freeSimulationObjects = new HashMap();
		for(int i=0; i<rounds; i++) {
			p.checkpoints[i] = new Checkpoint();
			p.checkpoints[i].theStatus = new Status(0, State.NEW, null, 0, 0, 0, 0);
			p.checkpoints[i].theState = new byte[job.settings[0].size()][0];
			
			p.results[i] = new HashMap();
		}
	
		return p;
	}
	
	
	public JobInformation getInformation(String jobId, Current __current) {
		if(jobId == null || !allSimulations.containsKey(jobId)) {
			System.out.println("job not found: "+jobId);
			throw new NullPointerException();

		}
		JobInformation job = getInformation((Persistence)allSimulations.get(jobId));
	
		return job;
	}

	public JobInformation[] getInformationForAll(Current __current) {
		JobInformation [] jobs = new JobInformation[allSimulations.size()];
		Iterator it = allSimulations.keySet().iterator();
		int i=0;
		while(it.hasNext()) 
			jobs[i++] = getInformation((Persistence)allSimulations.get(it.next()));
		return jobs;
	}

	public String[] getActiveSimulations(Current __current) {
		return (String[])getAllowedSimulations(runningIds, __current)
			.toArray(new String[0]);
	}

	public String[] getPausedSimulations(Current __current) {
		return (String[])getAllowedSimulations(userPausedIds, __current)
			.toArray(new String[0]);
	}

	public String[] getScheduledSimulations(Current __current) {
		return (String[])getAllowedSimulations(scheduledIds, __current)
			.toArray(new String[0]);
	}

	public String[] getAllSimulations(Current __current) {
		return (String[])getAllowedSimulations(allSimulations.keySet(), __current)
			.toArray(new String[0]);
	}

	private List getAllowedSimulations(Collection ids, Current __current) {
		String user = (String)__current.ctx.get("user");
		
		List allowedIds = new ArrayList();
		Iterator it = ids.iterator();
		
		while(it.hasNext()) {
			String jobId = (String)it.next();
			Persistence p = (Persistence)allSimulations.get(jobId);
			try {
				if(authPrx.hasPermission(user, p.job.owner, "simulation.show"))
					allowedIds.add(jobId);
			} catch(Exception e) {} // ignore, if an exception occurs -> id is not allowed
		}
		
		return allowedIds;
	}
	
	protected void move(String id, List from, List to) {
		if(from.remove(id))
			to.add(id);
	}
	
	protected void setError(SimulationObject simObj, Exception ex) {
		simObj.theState = State.ERROR;
		
		Persistence p = (Persistence)allSimulations.get(simObj.jobId+"-"+(simObj.round+1));
		if(p == null) {
			System.out.println("no p found!");
			return;
		}
		
		if(p.checkpoints[simObj.round] == null) 
			p.checkpoints[simObj.round] = new Checkpoint(new byte[0][0], new Status(0, State.ERROR, "", 0, 0, 0, 0));
		p.checkpoints[simObj.round].theStatus.error = ex+": "+(ex!=null?ex.getMessage():" (null)");
		
		Publisher.reportStatusChange(simObj);
		logException(ex);
	}
	
	protected void logException(Exception e) {
		Message msg = new Message();
		msg.origin = e.getStackTrace()[0].getClassName()+"."+e.getStackTrace()[0].getMethodName();
		msg.text = e+": "+e.getMessage();
		msg.timestamp = (new Date()).getTime()/1000;
		msg.type = TypeEnum.ERR;
		sysLog.add(msg);
		
		e.printStackTrace();
	}
	
	
	private class BuiltCallback extends AMI_Interface_createSimulation {
		
		private Specification job;
		private Object callback;
		public BuiltCallback(Specification job,  Object callback) {
			this.job = job;
			this.callback = callback;
		}	
		
		public void ice_response(Comm.Simulation.InterfacePrx simulation) {
			addNewJob(job);
			
			if(callback instanceof AMD_PublicInterface_startSimulation)
				((AMD_PublicInterface_startSimulation)callback).ice_response(true);
			else {
				try {
					callback.getClass().getMethod("ice_response", new Class[0])
					.invoke(callback, new Object[0]);
				}
				catch(Exception e) {
					Manager.this.logException(e);
				}
			}
			scheduler.schedule();
		}

		public void ice_exception(LocalException ex) {
			failed(ex);
		}
		
		public void ice_exception(UserException ex) {
			failed(ex);
		}
		
		private void failed(Exception ex) {
			logException(ex);
			
			if(callback instanceof AMD_PublicInterface_startSimulation)
				((AMD_PublicInterface_startSimulation)callback).ice_exception(ex);
			else {
				Class [] params = {Exception.class};
				Object [] paramObjs = new Object[1];
				paramObjs[0] = ex;
				
				try {
					callback.getClass().getMethod("ice_exception", params)
					.invoke(callback, paramObjs);
				}
				catch(Exception e) {
					Manager.this.logException(e);	
				}
			}
		}
	}
	
	private class CreateCheckpointCallback extends AMI_Interface_createCheckpoint {
	
		private SimulationObject simObj;
		AMD_PublicInterface_makeCheckpoint callback;
		public CreateCheckpointCallback(SimulationObject simObj, AMD_PublicInterface_makeCheckpoint __cb) {
			this.simObj = simObj;
			callback = __cb;
		}
		
		public synchronized void ice_response(Checkpoint cp) {
			Persistence p = (Persistence)allSimulations.get(simObj.jobId);
			p.checkpoints[simObj.round] = cp;
			allSimulations.put(simObj.jobId, p);
			callback.ice_response();
		}
		
		public void ice_exception(LocalException ex) {
			logException(ex);
			callback.ice_exception(ex);
		}
		public void ice_exception(UserException ex) {
			logException(ex);
			callback.ice_exception(ex);
		}
	}
	
	public static void main(String [] args) {
		Ice.Communicator c = Util.initialize(args);
		Manager m = Manager.createManager(c);
		
		m.getInformationForAll();
		String [] hosts = m.getSimulationHosts();
		for(int i=0; i<hosts.length; i++)
			System.out.println(hosts[i]);
		
		System.exit(0);
	}

	public String getNextId(Current __current) {
		String owner = __current.ctx.get("user").toString();
		if(owner == null) return null;
		
		Integer id = (Integer)jobIdMap.get(owner);
		
		int num = (id==null)?1:id.intValue();
		jobIdMap.put(owner, new Integer(num+1));
		
		return owner+num;
	}

	public void moveSimulationToHost_async(AMD_PublicInterface_moveSimulationToHost callback, String jobId, String host, Current __current) throws ActionNotAllowed {
		checkAuthentication(__current.ctx, null, "simulation.move");
		String proxyString = "Generator1@"+host+".GeneratorAdapter";
		
		Ice.ObjectPrx proxy = communicator.stringToProxy(proxyString);
		System.out.println("proxyString: "+proxyString);
		Comm.Generator.InterfacePrx generator = Comm.Generator.InterfacePrxHelper.checkedCast(proxy);
		
		Persistence pJob = (Persistence)allSimulations.get(this.getParentJobId(jobId));
		BuiltCallback cb = new BuiltCallback(pJob.job, callback);
		
		// 
		generator.createSimulation_async(cb, pJob.job, Integer.toString(pJob.nextSuffixId++), false);
		allSimulations.put(this.getParentJobId(jobId), pJob);
	}

	public Persistence getResults(String jobId, Current __current) throws ActionNotAllowed {
		checkAuthentication(__current.ctx, null, "simulation.show");
		return (Persistence)allSimulations.get(this.getParentJobId(jobId));
	}

	public void makeCheckpoint_async(AMD_PublicInterface_makeCheckpoint __cb, String jobId, Current __current) throws ActionNotAllowed {
		if(allSimulations.containsKey(jobId)) {
			Persistence p = (Persistence)allSimulations.get(jobId);
			for(int i=0; i<p.checkpoints.length; i++)
				makeCheckpointsForRound(jobId+"-"+(i+1), __cb);
		} else makeCheckpointsForRound(jobId, __cb);	
	}
	
	private void makeCheckpointsForRound(String roundId, AMD_PublicInterface_makeCheckpoint __cb) {
		SimulationObject obj = (SimulationObject)allSimulationObjects.get(roundId);
		if(obj == null) return;
		if(obj.theState != State.RUNNING) return; //checkpoint should be up-to-date
		
		Persistence p = (Persistence)allSimulations.get(obj.jobId);
		SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(obj.simulationSuffix);
		wrapper.simulation.createCheckpoint_async(new CreateCheckpointCallback(obj, __cb));
	}
}
