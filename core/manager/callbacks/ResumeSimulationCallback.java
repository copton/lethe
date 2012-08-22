package callbacks;

import Comm.Simulation.AMI_Interface_resume;
import Ice.LocalException;
import Ice.UserException;

public class ResumeSimulationCallback extends AMI_Interface_resume {

	private Action action;
	public ResumeSimulationCallback(Action a) {
		super();
		action = a;
	}
	
	public void ice_response() {
		action.resumedSimulation(action.obj);
	}

	public void ice_exception(LocalException ex) {
		action.exceptionOccured(action.obj, ex);
	}

	public void ice_exception(UserException ex) {
		action.exceptionOccured(action.obj, ex);
	}
}
