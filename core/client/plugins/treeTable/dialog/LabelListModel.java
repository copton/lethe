package plugins.treeTable.dialog;

import java.util.*;
import javax.swing.AbstractListModel;

import xml.TypedElement;

public class LabelListModel extends AbstractListModel implements javax.swing.ComboBoxModel {
	List items = new ArrayList();
	public void set(List items) {
		this.items = new ArrayList(items);
		if(items.size() > 0)
			fireContentsChanged(this, 0, items.size());
	}

	public List getAll() {
		return items;
	}
	
	public void add(TypedElement e) {
		if(!items.contains(e)) {
			items.add(e);
			fireIntervalAdded(this, items.size()-1, items.size());
		}
	}

	public void addAll(List l) {
		l.removeAll(items);
		int size = items.size();
		items.addAll(l);
		fireIntervalAdded(this, size, items.size());
	}
	
	public void remove(int pos) {
		items.remove(pos);
		fireIntervalRemoved(this, pos, pos+1);
	}
	
	public void remove(Object o) {
		int pos = items.indexOf(o);
		if(pos >= 0) {
			items.remove(pos);
			fireIntervalRemoved(this, pos, pos+1);
		}
	}
	
	public void removeAll(List elements) {
		int oldSize = items.size();
		items.removeAll(elements);
		fireIntervalRemoved(this, 0, oldSize);
	}
	
	public void clear() {
		int oldSize = items.size();
		items.clear();
		fireIntervalRemoved(this, 0, oldSize);
	}
	
	public int getSize() {
		return items.size();
	}

	public Object getElementAt(int pos) {
		if(pos >= items.size()) return null;
		return items.get(pos);
	}

	private Object selected;
	public void setSelectedItem(Object selected) {
		this.selected = selected;
	}

	public Object getSelectedItem() {
		if(selected != null) return selected;
		else if(items.size() > 0) return items.get(0);
		else return null;
	}
	
}
