package callbacks;

import Comm.Simulation.InterfacePrx;

public class Action {
	public static final int BUILD=1, START=2, INIT=3, RESUME=4;

	public int type;
	public Object obj;
	public Action(int type, Object obj) {
		this.type = type;
		this.obj = obj;
	}
	
	public void doAction() {
		switch(type) {
		case BUILD: buildSimulation(obj, new BuildSimulationCallback(this)); break;
		case START: startSimulation(obj, new StartSimulationCallback(this)); break;
		case INIT: initSimulation(obj, new InitSimulationCallback(this)); break;
		case RESUME: resumeSimulation(obj, new ResumeSimulationCallback(this)); break;
		}
	}
	
	public void buildSimulation(Object o, BuildSimulationCallback callback) {}
	public void builtSimulation(Object o, InterfacePrx simulation) {}
	
	public void startSimulation(Object o, StartSimulationCallback callback) {}
	public void startedSimulation(Object o) {}
	
	public void initSimulation(Object o, InitSimulationCallback callback) {}
	public void initedSimulation(Object o) {}
	
	public void resumeSimulation(Object o, ResumeSimulationCallback callback) {}
	public void resumedSimulation(Object o) {}
	
	public void exceptionOccured(Object o, Exception e) {
		System.out.println("BUILT failed");
		e.printStackTrace();
	}
}
