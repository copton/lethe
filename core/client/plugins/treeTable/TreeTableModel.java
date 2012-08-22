package plugins.treeTable;

import javax.swing.tree.TreeModel;

public interface TreeTableModel extends TreeModel {
	public int getColumnCount();
	public String getColumnName(int pos);
	public Class getColumnClass(int pos);
	
	public Object getValueAt(Object node, int column);
	public void setValueAt(Object value, Object node, int column);
	
	public boolean isCellEditable(Object node, int column);
}
