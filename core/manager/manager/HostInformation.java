package manager;

public class HostInformation {
	String name;
	int maxRunningSimulations;
	int maxPausedSimulations;
	
	public HostInformation(String name) {
		this(name, 1, 5);
	}
	
	public HostInformation(String name, int maxRunning, int maxPaused) {
		this.name = name;
		maxRunningSimulations = maxRunning;
		maxPausedSimulations = maxPaused;
	}
}
