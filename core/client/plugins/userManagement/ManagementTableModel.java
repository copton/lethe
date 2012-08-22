package plugins.userManagement;

import javax.swing.table.*;
import java.util.*;

public class ManagementTableModel extends AbstractTableModel {
	private String [] columns = {"  ", "Benutzer", "Gruppe", "erlaubt", "verboten"};
	private List data;
	
	public String getColumnName(int row) {
		return columns[row];
	}
	
	public Class getColumnClass(int col) {
		if (col == SELECTION) return Boolean.class;
		switch(col) {
		case SELECTION: return Boolean.class;
		case ALLOW: return javax.swing.tree.TreeModel.class;
		default: return String.class;
		}
	}
	
	public boolean isCellEditable(int row, int col) {
		switch(col) {
		case SELECTION:
		case ALLOW: return true;
		default: return false;
		}
	}
	
	private List selections = new ArrayList();
	public void setValueAt(Object value, int row, int col) {
		if(col != SELECTION) return;
		selections.set(row, value);
		fireTableCellUpdated(row, col);
	}
	
	
	public void setData(List data) {
		this.data = data;
		selections = new ArrayList(data.size());
		for(int i=0; i<data.size(); i++) selections.add(new Boolean(false));
		fireTableDataChanged();
	}
	
	public int getRowCount() {
		return data==null?0:data.size();
	}
	
	public int getColumnCount() {
		return columns.length;
	}
	
	private static final int SELECTION=0, USER=1, GROUP=2, ALLOW=3, DENY=4;
	public Object getValueAt(int row, int col) {
		UserData userData = (UserData)data.get(row);
		switch(col) {
		case SELECTION: return selections.get(row);
		case USER: return userData.user;
		case GROUP: return userData.group;
		case ALLOW: return userData.allowed;
		case DENY: return userData.denied;
		}
		return null;
	}
}
