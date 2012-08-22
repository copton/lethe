package util;

import java.util.List;
import java.util.Iterator;

public class StringHelper {
	public static String join(List l, String seq) {
		if(l.size() == 0) return "";
		else {
			Iterator it = l.iterator();
			StringBuffer str = new StringBuffer(it.next().toString());
			while(it.hasNext()) str.append(seq+it.next());
			return str.toString();
		}
	}
	
	public static String join(List l, String prefix, String suffix) {
		if(l.size() == 0) return "";
		else {
			Iterator it = l.iterator();
			StringBuffer str = new StringBuffer();
			while(it.hasNext()) str.append(prefix+it.next()+suffix);
			return str.toString();
		}
	}
}
