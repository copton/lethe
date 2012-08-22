package callbacks;

import Comm.Simulation.AMI_Interface_stop;
import Ice.LocalException;
import Ice.UserException;

public class StopSimulationCallback extends AMI_Interface_stop {

	private Action action;
	public StopSimulationCallback(Action a) {
		super();
		action = a;
	}
	
	public void ice_response() {
		System.out.println("TO_BE_IMPLEMENTED: StopSimulationCallback.ice_responce");
	}


	public void ice_exception(LocalException ex) {
		action.exceptionOccured(action.obj, ex);
	}

	public void ice_exception(UserException ex) {
		action.exceptionOccured(action.obj, ex);
	}
}
