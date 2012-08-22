package plugins.userManagement;

import javax.swing.JTable;
import javax.swing.tree.TreeModel;
import java.util.*;

public class ManagementTableHandler {
	private ManagementTableModel tableModel = new ManagementTableModel();
	private JTable table = new JTable(tableModel);
	private List dataList = new ArrayList();
	
	public ManagementTableHandler() {
		ManagementTableCellRenderer treeRenderer = new ManagementTableCellRenderer();
		table.setDefaultRenderer(TreeModel.class, 
				treeRenderer);
		table.setDefaultEditor(TreeModel.class,
				new ManagementTableCellEditor(treeRenderer));
	}
	
	public JTable getTable() {
		return table;
	}
	
	public void addData(UserData data) {
		dataList.add(data);
		tableModel.setData(dataList);
	}
	
	public List deleteSelected() {
		int [] rows = table.getSelectedRows();
		for(int i=rows.length-1; i>=0; i--)
			dataList.remove(i);
		setData(dataList);
		return dataList;
	}
	
	public void setData(java.util.List data) {
		tableModel.setData(data);
	}
}
