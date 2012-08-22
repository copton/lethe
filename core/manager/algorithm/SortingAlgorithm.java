package algorithm;

public interface SortingAlgorithm {
	public void init(manager.Manager manager);
	public java.util.List sort(java.util.Collection jobs, int seats);
}
