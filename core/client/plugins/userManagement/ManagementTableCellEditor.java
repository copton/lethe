package plugins.userManagement;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.*;

public class ManagementTableCellEditor extends AbstractCellEditor 
implements TableCellEditor {
	private TreeModel currentModel;
	private JTree tree;
	
	public ManagementTableCellEditor(JTree tree) {
		this.tree = new JTree();	
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean arg2, int row, int col) {
		currentModel = (TreeModel)value;
		tree.setModel(currentModel);
		return tree;
	}

	public Object getCellEditorValue() {
		return currentModel;
	}
}
