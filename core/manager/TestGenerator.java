
import Comm.Exceptions.ActionNotAllowed;
import Comm.Exceptions.BuildException;
import Comm.Exceptions.SpecException;
import Comm.Exceptions.StartupException;
import Comm.Generator.AMD_Interface_createSimulation;
import Comm.Generator._InterfaceDisp;
import Comm.Job.Specification;
import Comm.Simulation.AMD_Interface_continue;
import Comm.Simulation.AMD_Interface_createCheckpoint;
import Comm.Simulation.AMD_Interface_die;
import Comm.Simulation.AMD_Interface_getResults;
import Comm.Simulation.AMD_Interface_getStatusInformation;
import Comm.Simulation.AMD_Interface_init;
import Comm.Simulation.AMD_Interface_resume;
import Comm.Simulation.AMD_Interface_start;
import Comm.Simulation.AMD_Interface_stop;
import Comm.Simulation.AMD_Interface_suspend;
import Comm.Simulation.Checkpoint;
import Comm.Simulation.InterfacePrx;
import Comm.Simulation.InterfacePrxHelper;
import Comm.Simulation.State;
import Comm.Simulation.Status;
import Ice.Application;
import Ice.Current;
import Ice.Identity;
import Ice.ObjectAdapter;
import Ice.Util;
import java.util.*;

public class TestGenerator extends Application {

	ObjectAdapter adapter;
	public int run(String[] args) {
		
		try {
			adapter = communicator().createObjectAdapter("GeneratorAdapter");
	
			Generator g = new Generator();
			adapter.add(g, Util.stringToIdentity("Generator"));
			adapter.activate();
			
			System.out.println("started");
			
			communicator().waitForShutdown();
			System.out.println("shutdown");
			
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}
	
	private int num=0;
	public class Generator extends _InterfaceDisp {
		public synchronized void createSimulation_async(AMD_Interface_createSimulation __cb, Specification job, String suffix, boolean buildOnly, Current __current) throws BuildException, SpecException, StartupException {
			System.out.println("createSimulation");
			
			Identity id = Util.stringToIdentity("simulation"+num++);
			adapter.add(new TestSimulation(), id);
			
			System.out.println("create prx");
			InterfacePrx simulation = InterfacePrxHelper.checkedCast(adapter.createDirectProxy(id));
			System.out.println("created "+simulation);
			
			__cb.ice_response(simulation);
		}
	}
	
	public class TestSimulation extends Comm.Simulation._InterfaceDisp {

		
		int callTimes = (int)(Math.random()*10)+3;
		int aktRound=0;
		public void init_async(AMD_Interface_init __cb, Specification job, Current __current) throws ActionNotAllowed, SpecException {
			System.out.println("init");
			this.job = job;
			state = State.READY;
			globalLivetime = (new Date()).getTime()/1000;
			__cb.ice_response();
		}

		State state;
		Status status = new Status();
		long globalLivetime;
		long startTime;
		int duration = 2*60*1000;
		public void start_async(AMD_Interface_start __cb, int round, Current __current) throws ActionNotAllowed {
			System.out.println("start");
			state = State.RUNNING;
			aktRound=0;
			startTime = (new Date()).getTime()/1000;
			__cb.ice_response();
		}

		public void resume_async(AMD_Interface_resume __cb, Checkpoint theCheckpoint, Current __current) throws ActionNotAllowed {
			System.out.println("resume");
			state = State.RUNNING;
			aktRound++;
			startTime = (new Date()).getTime()/1000;
			__cb.ice_response();
		}

		public void stop_async(AMD_Interface_stop __cb, Current __current) throws ActionNotAllowed {
			System.out.println("stop");
			state = State.STOPPED;
			long livetime = (new Date()).getTime()/1000-startTime;
			status.livetime = globalLivetime - (new Date()).getTime();
			status.runtime += (int)(livetime*Math.random());
			__cb.ice_response();
		}

		private Specification job;
		public void suspend_async(AMD_Interface_suspend __cb, Current __current) throws ActionNotAllowed {
			System.out.println("suspend");
			state = State.STOPPED;
			long livetime = (new Date()).getTime()-startTime;
			status.livetime = globalLivetime - (new Date()).getTime()/ 1000;
			status.runtime += livetime;
			status.theState = state;
			
			Checkpoint cp = new Checkpoint(new byte[1][0], status);			
			__cb.ice_response(cp, job);
		}

		public void createCheckpoint_async(AMD_Interface_createCheckpoint __cb, Current __current) throws ActionNotAllowed {
			System.out.println("createCheckpoint");
		
			Checkpoint cp = new Checkpoint();
			cp.theState = new byte[0][0];
			cp.theStatus = new Status();
			cp.theStatus.theState = state;
		
			__cb.ice_response(cp);
		}

		public void getStatusInformation_async(AMD_Interface_getStatusInformation __cb, Current __current) throws ActionNotAllowed {
			System.out.println("getStatusInformation");
			
			status.theState = state;
			
			__cb.ice_response(status);
		}

		public void getResults_async(AMD_Interface_getResults __cb, Current __current) throws ActionNotAllowed {
			System.out.println("getResults");
			__cb.ice_response(new HashMap());
		}

		public void continue_async(AMD_Interface_continue __cb, Current __current) throws ActionNotAllowed {
			System.out.println("continue");
			state = State.RUNNING;
			startTime = (new Date()).getTime()/1000;
			__cb.ice_response();
		}

		public void die_async(AMD_Interface_die __cb, Current __current) throws ActionNotAllowed {
			System.out.println("die");
			__cb.ice_response();
		}
	}
	
	public static void main(String [] args) {
		TestGenerator tg = new TestGenerator();
		System.exit(tg.main("Lethe", args));
	}
}
