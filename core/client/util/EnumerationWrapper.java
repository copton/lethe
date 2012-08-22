package util;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationWrapper implements Enumeration {
	Iterator it;
	
	public static java.util.List createList(Enumeration e) {
		java.util.List al = new java.util.ArrayList();
		while(e.hasMoreElements())
			al.add(e.nextElement());
		return al;
	}
	
	public EnumerationWrapper(Iterator it) {
		this.it = it;
	}
	
	public boolean hasMoreElements() {
		return it.hasNext();
	}

	public Object nextElement() {
		return it.next();
	}
}
