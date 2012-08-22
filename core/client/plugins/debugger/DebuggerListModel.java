package plugins.debugger;

import java.util.*;

public class DebuggerListModel extends javax.swing.AbstractListModel {

	private List dataLines = new ArrayList();
	
	public void add(String line, int type) {
		dataLines.add(new ListElement(line, type));
		this.fireIntervalAdded(this, dataLines.size()-1, dataLines.size());
	}
	
	public void removeFirstLines(int anz) {
		for(int i=0; i<anz; i++)
			dataLines.remove(0);
		
		this.fireIntervalRemoved(this, 0, anz);
	}
	
	public void clear() {
		int size = getSize();
		dataLines.clear();
		this.fireIntervalRemoved(this, 0, size);
	}
	
	public int getSize() {
		return dataLines.size();
	}

	public String getLine(int pos) {
		return ((ListElement)dataLines.get(pos)).text;
	}
	
	public Object getElementAt(int pos) {
		return dataLines.get(pos);
	}
}
