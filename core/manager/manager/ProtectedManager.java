package manager;

import java.util.Iterator;
import java.util.Map;

import Comm.Job.Persistence;
import Comm.Job.SimulationWrapper;
import Comm.Job.Specification;
import Comm.Logger.Message;
import Comm.Logger.TypeEnum;
import Comm.Manager.AMD_Callback_onSimulationError;
import Comm.Manager.AMD_Callback_onSimulationFinished;
import Comm.Manager.SimulationObject;
import Comm.Manager._CallbackDisp;
import Comm.Simulation.AMI_Interface_continue;
import Comm.Simulation.AMI_Interface_start;
import Comm.Simulation.InterfacePrx;
import Comm.Simulation.State;
import Ice.Current;
import Ice.LocalException;
import Ice.UserException;

public class ProtectedManager extends _CallbackDisp {

	private Manager manager;
	public ProtectedManager(Manager m) {
		manager = m;
	}
	
	public synchronized void onSimulationFinished_async(AMD_Callback_onSimulationFinished __cb, String jobId, String suffix, Map results, Current __current) {
		__cb.ice_response();
		
		SimulationObject simulation = getSimulationObject(jobId, suffix);
		simulation.simulationSuffix = null;
		String roundId = jobId+"-"+(simulation.round+1);
		
		Persistence p = (Persistence)manager.allSimulations.get(jobId);
		p.results[simulation.round] = results;
		manager.allSimulations.put(p.job.jobId, p);
		simulation.theState = State.FINISHED;
	
		manager.runningIds.remove(roundId);
		manager.allSimulationObjects.remove(roundId);
		
		Publisher.reportStatusChange(simulation);
			
		if(isLastRound(p)) handleLastRound(p);
		else {
			boolean foundFollower = false;
			SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(suffix);
			
			for(int i=1; i<=p.results.length; i++) {
				String id = jobId+"-"+i;
				if(manager.scheduledIds.contains(id)) {
					System.out.println("found next round: "+id);
					manager.scheduledIds.remove(id);
					
					SimulationObject newSimulation = (SimulationObject)manager.allSimulationObjects.get(id);
					
					if(newSimulation.hasCheckpoint) wrapper.simulation.continue_async(new Continue_Callback(newSimulation, suffix));
					else wrapper.simulation.start_async(new Start_Callback(newSimulation, suffix), i-1);
				
					foundFollower = true;
					break;
				}
			}
			if(!foundFollower) wrapper.active = false;
			manager.allSimulations.put(p.job.jobId, p);
		}
	}
	
	private class Continue_Callback extends AMI_Interface_continue {
		SimulationObject obj;
		String suffix;
		
		public Continue_Callback(SimulationObject obj, String suffix) {
			this.obj = obj;
			this.suffix = suffix;
		}
		
		public void ice_response() {
			String id= obj.jobId+"-"+(obj.round+1);
			obj.lastChangeTime = (new java.util.Date()).getTime();
			obj.simulationSuffix = suffix;
			obj.theState = State.RUNNING;
			manager.allSimulationObjects.put(id, obj);
			Publisher.reportStatusChange(obj);
			
			manager.runningIds.add(id);
		}
		
		public void ice_exception(LocalException ex) {
			failed(ex);
		}
		
		public void ice_exception(UserException ex) {
			failed(ex);
		}
		
		private void failed(Exception e) {
			Persistence p = (Persistence)manager.allSimulations.get(obj.jobId);
			try {
				SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(suffix);
				wrapper.simulation.die();
			} catch(Exception ex) {} // shouldn't happen
			
			p.freeSimulationObjects.remove(suffix);
			manager.allSimulations.put(obj.jobId, p);
			
			manager.scheduledIds.add(obj.jobId+"-"+(obj.round+1));
			manager.logException(e);
		}
	}

	private class Start_Callback extends AMI_Interface_start {
		SimulationObject obj;
		String suffix;
		
		public Start_Callback(SimulationObject obj, String suffix) {
			this.obj = obj;
			this.suffix = suffix;
		}
		
		public void ice_response() {
			String id= obj.jobId+"-"+(obj.round+1);
			obj.lastChangeTime = (new java.util.Date()).getTime();
			obj.simulationSuffix = suffix;
			obj.theState = State.RUNNING;
			manager.allSimulationObjects.put(id, obj);
			Publisher.reportStatusChange(obj);
			
			manager.runningIds.add(id);
		}
		
		public void ice_exception(LocalException ex) {
			failed(ex);
		}
		
		public void ice_exception(UserException ex) {
			failed(ex);
		}
		
		private void failed(Exception e) {
			Persistence p = (Persistence)manager.allSimulations.get(obj.jobId);
			
			try {
				SimulationWrapper wrapper = (SimulationWrapper)p.freeSimulationObjects.get(suffix);
				wrapper.simulation.die();
			} catch(Exception ex) {} // shouldn't happen
			
			p.freeSimulationObjects.remove(suffix);
			manager.allSimulations.put(obj.jobId, p);
			
			manager.scheduledIds.add(obj.jobId+"-"+(obj.round+1));
			manager.logException(e);
			e.printStackTrace();
		}
	}
	public synchronized void onSimulationError_async(AMD_Callback_onSimulationError __cb, String jobId, String suffix, String error, boolean abort, Current __current) {
		System.out.println("Simulation errored");
        __cb.ice_response();
        
		SimulationObject simulation = getSimulationObject(jobId, suffix);
		if(simulation == null) {
			System.out.println("jobId="+jobId+", suffix="+suffix);
			System.out.println("error="+error);
		} else {
			simulation.theState = State.ERROR;
			
			String roundId = simulation.jobId+"-"+(simulation.round+1);
			manager.runningIds.remove(roundId);
			Publisher.reportStatusChange(simulation);
			manager.allSimulationObjects.remove(roundId);
		}
		Persistence p = (Persistence)manager.allSimulations.get(jobId);
		if(simulation != null)p.checkpoints[simulation.round].theStatus.error = error;
		InterfacePrx simulationObj = (InterfacePrx)p.freeSimulationObjects.get(suffix);
		
		try {
			simulationObj.die();
		} catch(Exception e) {}
		
		p.freeSimulationObjects.remove(suffix);
		manager.allSimulations.put(jobId, p);
		
		if(isLastRound(p)) handleLastRound(p);
	}

	private boolean isLastRound(Persistence p) {
		for(int i=1; i<=p.checkpoints.length; i++)
			if(manager.allSimulationObjects.containsKey(p.job.jobId+"-"+i)) return false;
				
		return true;
	}

	private void handleLastRound(Persistence p) {
		System.out.println("WAS LAST ROUND");
		stopAllSourceServices(p.job);
		Iterator it = p.freeSimulationObjects.values().iterator();
		while(it.hasNext())
			try {
				SimulationWrapper wrapper = (SimulationWrapper)it.next();
				wrapper.simulation.die();
				System.out.println("killed interface");
			} catch(Exception e) {}
	
		p.freeSimulationObjects.clear();
		for(int i=0; i<p.checkpoints.length; i++)
			p.checkpoints[i].theState = new byte[0][0]; // overwrite it to save space in database
		manager.allSimulations.put(p.job.jobId, p);
		Publisher.reportSimulationFinished(p);
	}
	
	private void stopAllSourceServices(Specification job) {
		for(int i=0; i<job.sourceServers.length; i++) 
			try {
				job.sourceServers[i].stopSourceService(job.jobId);
			} catch(Exception e) {
				// perhaps sourceServer down. log it on breeze
				Message msg = new Message();
				msg.origin = "Manager";
				msg.text = "stopSourceService failed for "+job.jobId+": "+e.getLocalizedMessage();
				msg.type = TypeEnum.WARNING;
				msg.timestamp = (new java.util.Date()).getTime()/1000;
				
				manager.sysLog.add(msg);
			}
	}
	
	private SimulationObject getSimulationObject(String jobId, String suffix) {
		Persistence p = (Persistence)manager.allSimulations.get(jobId);
		for(int i=0; i<p.checkpoints.length; i++) {
			String roundId = jobId+"-"+(i+1);
			SimulationObject obj = (SimulationObject)manager.allSimulationObjects.get(roundId);
				
			if(obj != null && suffix.equals(obj.simulationSuffix)) return obj;
		}
		System.out.println("ERROR: COULDN'T FIND SIM_OBJECT");
		return null; // shouldn't happen!
	}
}
