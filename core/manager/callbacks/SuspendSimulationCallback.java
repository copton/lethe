package callbacks;

import Comm.Job.Specification;
import Comm.Simulation.AMI_Interface_suspend;
import Comm.Simulation.Checkpoint;
import Ice.LocalException;
import Ice.UserException;

public class SuspendSimulationCallback extends AMI_Interface_suspend {

	private Action action;
	public SuspendSimulationCallback(Action a) {
		super();
		action = a;
	}
	
	public void ice_response(Checkpoint theCheckpoint, Specification job) {
		System.out.println("TO_BE_IMPLEMENTED: SuspendedSimulationCallback.ice_responce");
	}


	public void ice_exception(LocalException ex) {
		action.exceptionOccured(action.obj, ex);
	}

	public void ice_exception(UserException ex) {
		action.exceptionOccured(action.obj, ex);
	}
}
