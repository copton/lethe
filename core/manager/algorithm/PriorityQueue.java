package algorithm;

public class PriorityQueue extends java.util.ArrayList {

	public boolean add(Object o) {
		if(o instanceof Comparable) return add((Comparable)o, 0, size());
		else return false;
	}
	
	private boolean add(Comparable c, int left, int right) {
		if(left == right) {
			add(right, c);
			return true;
		} else if(right - left == 1) {
			if(c.compareTo(get(left)) < 0) return add(c, left, left);
			else return add(c, right, right);
		} else {		
			int middle = (left+right) >> 1;
			if(c.compareTo(get(middle)) < 0) return add(c, left, middle);
			else return add(c, middle, right);
		}
	}
}
