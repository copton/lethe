package callbacks;

import Comm.Simulation.AMI_Interface_init;
import Ice.LocalException;
import Ice.UserException;

public class InitSimulationCallback extends AMI_Interface_init {

	private Action action;
	public InitSimulationCallback(Action a) {
		super();
		action = a;
	}
	
	public void ice_response() {
		action.initedSimulation(action.obj);
	}
	
	public void ice_exception(LocalException ex) {
		action.exceptionOccured(action.obj, ex);
	}

	public void ice_exception(UserException ex) {
		action.exceptionOccured(action.obj, ex);
	}
}
