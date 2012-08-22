package callbacks;

import Comm.Generator.AMI_Interface_createSimulation;
import Comm.Simulation.InterfacePrx;
import Comm.Exceptions.*;
import Ice.LocalException;
import Ice.UserException;

public class BuildSimulationCallback extends AMI_Interface_createSimulation {

	private Action action;
	public BuildSimulationCallback(Action a) {
		super();
		action = a;
	}
	
	public void ice_response(InterfacePrx simulation) {
		System.out.println("BUILT ok");
		action.builtSimulation(action.obj, simulation);
	}

	public void ice_exception(LocalException ex) {
		System.out.println("BUILT failed1");
	//	action.exceptionOccured(action.obj, ex);
		ex.printStackTrace();
	}

	public void ice_exception(UserException ex) {
		System.out.println("BUILT failed2");
		
		if(ex instanceof BuildException) System.out.println("buildex");
		else if(ex instanceof CompileError) System.out.println("compileex");
		else System.out.println("exception of class "+ex.getClass());
		action.exceptionOccured(action.obj, ex);
	}
}
