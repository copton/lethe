package client;

import Ice.ObjectPrx;
import Comm.Job.*;
import Comm.Manager.*;
import Comm.SourceService.ProtectedInterfacePrx;

import util.IceNetworkConnection;
import xml.JobSpecification;

import event.*;

public class SimulationManagement {
	
	DefaultEventHandler exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	private PublicInterfacePrx manager;
	public  PublicInterfacePrx getManager() {
		if(manager == null) manager = createNewManager();
		return manager;
	}
	
	public void connect() {
		manager = createNewManager();
	}
	
	private PublicInterfacePrx createNewManager() {
		String proxy = "Manager@Manager.ManagerAdapter";
		ObjectPrx iceObject = IceNetworkConnection.get().stringToProxy(proxy);
		PublicInterfacePrx prx = PublicInterfacePrxHelper.checkedCast(iceObject);
		return prx;
	}
	
	public Specification createJobSpecification(String xmlFile) {
		if(manager == null) return null;
		
		try {
			JobSpecification jobSpec = new JobSpecification(xmlFile);
			Specification job = jobSpec.getJob();
			
			job.owner = (String)manager.ice_communicator().getDefaultContext().get("user");
			System.out.println("OWNER: "+job.owner);
			if(job.owner == null)
				job.owner = "phia";
			job.jobId = manager.getNextId(manager.ice_communicator().getDefaultContext());
			System.out.println("owner="+job.owner+", jobId="+job.jobId);
		//	job.sourceServers = jobSpec.announceSources(job.jobId);
			job.sourceServers = new ProtectedInterfacePrx[0];
			return job;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("createJobSpecification failed");
			return null;
		}
	}
	
	public boolean startSimulation(Specification job) {
		try {
			return manager.startSimulation(job, manager.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			return false;
		}
	}
	
	public void restartSimulation(String jobId) {
		try {
			manager.restartSimulation(jobId, manager.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}
	
	public void moveSimulation(String jobId, String host) {
		try {
			manager.moveSimulationToHost(jobId, host, manager.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}
	
	public JobInformation getSimulation(String jobId) {
		return manager.getInformation(jobId, manager.ice_communicator().getDefaultContext());
	}
	
	public JobInformation [] getAllSimulations() {
		if(manager == null) return new JobInformation[0];
		JobInformation [] jobs = manager.getInformationForAll(manager.ice_communicator().getDefaultContext());
		return jobs;
	}
	
	public String[] getAllUsers() {
		if(manager == null) return new String[0];
		else return manager.getAllUsers(manager.ice_communicator().getDefaultContext());
	}
	
	public void stopSimulation(String jobId) {
		try {
			manager.pauseSimulation(jobId, manager.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}	
	
	public void abortSimulation(String jobId) {
		try {
			manager.abortSimulation(jobId, manager.ice_communicator().getDefaultContext());
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}
	
	public void getResults(String jobId) {
		Freeze.Connection connection =          
			 Freeze.Util.createConnection(IceNetworkConnection.get(), "db");
		PersistantJobs results = new PersistantJobs(connection, "results", true);
		
		try {
			Persistence simulation = manager.getResults(jobId);
			
			if(simulation != null) results.put(jobId, simulation);
		} catch(Exception e) {}
			
		results.close();
		connection.close();	
	}
	
	private static String getUsage() {
		return "\tjava SimulationManagement [-u <user> -p <passwd>] -l\n"+
			"\tjava SimulationManagement [-u <user> -p <passwd>] -s <job.xml>\n"+
			"\tjava SimulationManagement [-u <user> -p <passwd>] -a <jobId>\n"+
			"\tjava SimulationManagement [-u <user> -p <passwd>] -p <jobId>\n"+
			"\tjava SimulationManagement [-u <user> -p <passwd>] -g <jobId>\n"+
			"\tjava SimulationManagement [-u <user> -p <passwd>] -r <jobId>\n"+
			"\tjava SimulationManagement [-u <user> -p <passwd>] -m <jobId> <server>";				
	}
	
	public static void main(String [] args) {
		
		ParameterWrapper wrapper = new ParameterWrapper();
		if(!wrapper.init(args)) {
			System.out.println("usage: "+getUsage());
			System.exit(1);
		}
		
		SimulationManagement s = new SimulationManagement();
		
		DefaultConsoleEventListener debugOut = new DefaultConsoleEventListener();
		s.exceptionHandler.addEventListener(debugOut);
		s.connect();
		
		Authentication auth = new Authentication();
		auth.authenticate(wrapper.user, wrapper.password);
		
		switch(wrapper.command) {
		case LIST: listSimulations(s); break;
		case START: s.startSimulation(s.createJobSpecification(wrapper.fileName)); break;
		case PAUSE: s.stopSimulation(wrapper.jobId); break;
		case ABORT: s.abortSimulation(wrapper.jobId); break;
		case RESUME: s.restartSimulation(wrapper.jobId); break;
		case RESULTS: s.getResults(wrapper.jobId); break;
		case MOVE: s.moveSimulation(wrapper.jobId, wrapper.server);
		}
	}
	
	private static void listSimulations(SimulationManagement management) {
		JobInformation [] jobs = management.getAllSimulations();
		for(int i=0; i<jobs.length; i++) {
			System.out.println(jobs[i].jobId + ": " + jobs[i].name +
					(jobs[i].description.length()>0?" - "+jobs[i].description:""));
			for(int j=0; j<jobs[i].roundIds.length; j++) 
				System.out.println("\t"+jobs[i].roundIds[j]+": ");
		}
	}
	
	private static final int LIST=0, START=1, PAUSE=2, RESUME=3, ABORT=4, MOVE=5, RESULTS=6;
	private static class ParameterWrapper {
		int command = -1;
		String user, password;
		String jobId, fileName, server;
		
		public ParameterWrapper() {
	 		Ice.Properties properties = IceNetworkConnection.getProperties();
	 		user = properties.getProperty("config.Authentication.user");
	 		password = properties.getProperty("config.Authentication.passwd");
		}
		
		public boolean init(String [] args) {
			int i=0;
			while(i < args.length) {
				if(args[i].equalsIgnoreCase("-u")) user = args[++i];
				else if(args[i].equalsIgnoreCase("-p")) password = args[++i];
				else if(args[i].equalsIgnoreCase("-l")) command = LIST;
				else if(args[i].equalsIgnoreCase("-s")) command = START;
				else if(args[i].equalsIgnoreCase("-a")) command = ABORT;
				else if(args[i].equalsIgnoreCase("-p")) command = PAUSE;
				else if(args[i].equalsIgnoreCase("-g")) command = RESULTS;
				else if(args[i].equalsIgnoreCase("-r")) command = RESUME;
				else if(args[i].equalsIgnoreCase("-m")) command = MOVE;
				else {
					switch(command) {
					case LIST: return false;
					case PAUSE: case RESUME: case ABORT: case RESULTS:
						jobId = args[i]; break;
					case START: 
						fileName = args[i]; break;
					case MOVE:
						if(jobId == null) jobId = args[i];
						else server = args[i]; 
						break;
					default: return false;
					}
				}	
				i++;
			}
			
			if(user == null || password == null) return false;
			
			switch(command) {
			case LIST: return true;
			case START: return fileName != null;
			case PAUSE: case RESUME: case ABORT: case RESULTS: return jobId != null;
			case MOVE: return jobId != null && server != null;
			default: return false;
			}
		}
	}
}
