package plugins.userManagement;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.table.TableCellRenderer;

public class ManagementTableCellRenderer extends JTree implements TableCellRenderer {
	public ManagementTableCellRenderer() {
		super();
	}
	
	public Component getTableCellRendererComponent(JTable table, Object object, boolean arg2, boolean arg3, int row, int col) {
		// TODO Auto-generated method stub
		if(!(object instanceof TreeModel)) {
			System.out.println("object is: "+object.getClass());
			return null;
		}
		table.setRowHeight(row, getRowHeight()*getRowCount());
		setModel((TreeModel)object);
		return this;
	}

}
