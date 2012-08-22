package plugins.treeTable.dialog;

import javax.swing.ComboBoxModel;
import javax.swing.AbstractListModel;

import java.util.*;

public class StringComboBoxModel extends AbstractListModel implements ComboBoxModel {

	private List items = new ArrayList();
	
	public void set(List l) {
		items = new ArrayList(l);
		fireContentsChanged(this, 0, items.size());
	}
	
	private Object selected = "";
	public void setSelectedItem(Object obj) {
		selected = obj;
	}

	public Object getSelectedItem() {
		return selected;
	}

	public int getSize() {
		return items.size();
	}

	public Object getElementAt(int pos) {
		return items.get(pos);
	}
}
