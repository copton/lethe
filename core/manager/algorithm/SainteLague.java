package algorithm;

import java.util.*;

import manager.Manager;

import Comm.AuthenticationService.ProtectedInterfacePrx;
import Comm.AuthenticationService.ProtectedInterfacePrxHelper;
import Comm.Job.Persistence;
import Comm.Simulation.State;
import Ice.ObjectPrx;

public class SainteLague implements SortingAlgorithm {
	private Manager manager;
	private int timeSlice;
	private ProtectedInterfacePrx priorityLookup;
	public void init(Manager manager) {
		this.manager = manager;
		timeSlice = manager.communicator.getProperties().getPropertyAsIntWithDefault("Scheduling.TimeSlice", 3600);
		priorityLookup = getPriorityLookup();
	}
	
	private ProtectedInterfacePrx getPriorityLookup() {
		ObjectPrx objProxy = manager.communicator.stringToProxy("Permission@Authentication.ServiceAdapter");
		return ProtectedInterfacePrxHelper.checkedCast(objProxy);
	}

	public List sort(Collection jobs, int seats) {
		List queue = new PriorityQueue();

		Iterator it = jobs.iterator();
		System.out.println("anz jobs="+jobs.size());
		if(jobs.size() == 0) return queue;
		
		while(it.hasNext()) {
			Persistence job = (Persistence)it.next();
			DecoratedSimulation base = new DecoratedSimulation(job);
			System.out.println("calculated for "+job.job.jobId);
			for(int i=0; i<seats; i++) {
				DecoratedSimulation newSimulation = (DecoratedSimulation)base.clone();
				newSimulation.totalPoints /= (i<<1)+1;
				queue.add(newSimulation);
			}
		}
		
		List seatList = new ArrayList();
		it = queue.iterator();
		while(it.hasNext()) {
			DecoratedSimulation ds = (DecoratedSimulation)it.next();
			seatList.add(ds.jobId);
		}
		
		return seatList;
	}
	
	private class DecoratedSimulation implements Comparable {
		int priorityPoints, agingPoints, pausedPoints, finishedPoints;
		int totalPoints;
		
		String jobId;
		
		public DecoratedSimulation(Persistence p) {
			jobId = p.job.jobId;
			
			try {
				priorityPoints = SainteLague.this.priorityLookup.getPriority(p.job.owner);
			} catch(Exception e) {
				priorityPoints = 1;
			}
			
			agingPoints = 0;
			long timeDiff = new Date().getTime() - p.job.schedulerInfo.startTime;
			
			for(int i=0; i<p.checkpoints.length; i++) {
				switch(p.checkpoints[i].theStatus.theState.value()) {
				case State._READY:
				case State._NEW:
					agingPoints += timeDiff; 
					break;
				case State._STOPPED:
					pausedPoints++;
					agingPoints += (timeDiff - p.checkpoints[i].theStatus.runtime); 
					break;
				case State._FINISHED: finishedPoints++; break;		
				}	
			} 
			
			// points per second
			agingPoints /= (1000*SainteLague.this.timeSlice);
			agingPoints /= p.checkpoints.length;
			totalPoints = calculatePoints();
	//		System.out.println("job "+jobId+": "+totalPoints+" = "+(agingPoints*priorityPoints)+" + "
	//				+pausedPoints+" + "+finishedPoints+" + "+priorityPoints);
		}
		
		private DecoratedSimulation() {}
		
		public Object clone() {
			DecoratedSimulation ds = new DecoratedSimulation();
			
			ds.jobId = jobId;
			ds.totalPoints = totalPoints;
			ds.priorityPoints = priorityPoints;
			ds.agingPoints = agingPoints;
			ds.pausedPoints = pausedPoints;
			ds.finishedPoints = finishedPoints;
			
			return ds;
		}
		
		private int calculatePoints() {
			return agingPoints*priorityPoints + pausedPoints + finishedPoints + priorityPoints;
		}
		
		public int compareTo(Object o) {
			DecoratedSimulation ds = (DecoratedSimulation)o;
			
			if(totalPoints > ds.totalPoints) return -1;
			if(totalPoints < ds.totalPoints) return 1;
			if(priorityPoints > ds.priorityPoints) return -1;
			if(priorityPoints < ds.priorityPoints) return 1;
			if(agingPoints > ds.agingPoints) return -1;
			if(agingPoints < ds.agingPoints) return 1;
			if(finishedPoints > ds.finishedPoints) return -1;
			if(finishedPoints < ds.finishedPoints) return 1;
			
			return (int)(Math.random()*2)*2-1; // return {-1, 1}
		}
	}	
}
