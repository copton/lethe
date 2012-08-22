package callbacks;

import Comm.Simulation.AMI_Interface_start;
import Ice.LocalException;
import Ice.UserException;

public class StartSimulationCallback extends AMI_Interface_start {

	private Action action;
	public StartSimulationCallback(Action a) {
		super();
		action = a;
	}
	
	public void ice_response() {
		action.startedSimulation(action.obj);
	}

	public void ice_exception(LocalException ex) {
		action.exceptionOccured(action.obj, ex);
	}

	public void ice_exception(UserException ex) {
		action.exceptionOccured(action.obj, ex);
	}
}
