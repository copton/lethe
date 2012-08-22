package plugins.treeTable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import plugins.simManagement.JobData;

public class TreeTable extends JTable {
	private TreeTableCellRenderer tree;
	public TreeTable(TreeTableModel model) {
		super();
		tree = new TreeTableCellRenderer(model);
		setModel(new TreeTableModelAdapter(model, tree));
		
		setDefaultRenderer(TreeTableModel.class, tree);
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
		
		ListToTreeSelectionWrapper selectionWrapper = new ListToTreeSelectionWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());
		setRowHeight(18);
		setShowGrid(false);
		setIntercellSpacing(new java.awt.Dimension(0, 0));
	} 
	
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableModel.class)? -1:editingRow;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	public void updateUI() {
		super.updateUI();
		if(tree != null) tree.updateUI();
	}
	
	public void setTreeRenderer(javax.swing.tree.TreeCellRenderer renderer) {
		tree.setCellRenderer(renderer);
	}
	
	public void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		if(tree != null && tree.getRowHeight() != rowHeight)
			tree.setRowHeight(getRowHeight());
	}
	
	private class TreeTableModelAdapter extends AbstractTableModel 
	implements TreeExpansionListener, TreeModelListener {
		private JTree tree;
		private TreeTableModel model;
	
		public TreeTableModelAdapter(TreeTableModel model, JTree tree) {
			super();
			this.model = model;
			this.tree = tree;
			tree.addTreeExpansionListener(this);
			model.addTreeModelListener(this);
		}
		
		public int getRowCount() {
			return tree.getRowCount()-1;
		}

		public int getColumnCount() {
			return model.getColumnCount();
		}
		
		public String getColumnName(int pos) {
			return model.getColumnName(pos);
		}
		
		public Class getColumnClass(int pos) {
			return model.getColumnClass(pos);
		}

		private Object nodeForRow(int row) {
			TreePath path = tree.getPathForRow(row+1);
			return path.getLastPathComponent();
		}
		
		public Object getValueAt(int row, int col) {
			return model.getValueAt(nodeForRow(row), col);
		}

		public void setValueAt(Object value, int row, int col) {
			model.setValueAt(value, nodeForRow(row), col);
		}
		
		public boolean isCellEditable(int row, int col) {
			return model.isCellEditable(nodeForRow(row), col);
		}
		
		public void treeExpanded(TreeExpansionEvent e) {
			fireTableDataChanged();
		}

		public void treeCollapsed(TreeExpansionEvent e) {
			fireTableDataChanged();
		}

		public void treeNodesChanged(TreeModelEvent e) {
			delayedFireTableDataChanged();
		}

		public void treeNodesInserted(TreeModelEvent e) {
			delayedFireTableDataChanged();	
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			delayedFireTableDataChanged();
		}

		public void treeStructureChanged(TreeModelEvent e) {
			delayedFireTableDataChanged();
		}
	
		private void delayedFireTableDataChanged() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableDataChanged();
					updateUI();
				}
			});
		}
	}
	
	private class TreeTableCellRenderer extends javax.swing.JTree 
	implements TableCellRenderer {
		public TreeTableCellRenderer(TreeTableModel model) {
			super();
			setCellRenderer(new TreeRenderer());
			this.setModel(model);
			setOpaque(true);
		}
		
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, TreeTable.this.getHeight()+getRowHeight());
		}
		
		public void paint(Graphics g) {
			g.translate(0, -visibleRow*getRowHeight());
			super.paint(g);
		}
		
		public void setRowHeight(int rowHeight) {
			if(rowHeight > 0) {
				super.setRowHeight(rowHeight);
				if(TreeTable.this != null &&
					TreeTable.this.getRowHeight() != rowHeight)
					TreeTable.this.setRowHeight(getRowHeight());
			}
		}
	
		public String getToolTipText(MouseEvent event) {
			event.translatePoint(0, visibleRow * getRowHeight());
			return super.getToolTipText(event);
	    }
		  
		private int visibleRow;
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int col) {
			
			if(isSelected)
				setBackground(table.getSelectionBackground());
			else setBackground(table.getBackground());
			visibleRow = row+1;
			return this;
		}

	}
	
	private class TreeRenderer extends JLabel implements javax.swing.tree.TreeCellRenderer {
		public TreeRenderer() {
			super();
			setOpaque(false);
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			setText(value.toString());
			if(value instanceof JobData)
				setIcon(((JobData)value).getImageIcon());
			else setIcon(null);
			return this;
		}
	}
	
	private class TreeTableCellEditor extends javax.swing.AbstractCellEditor 
		implements TableCellEditor {

		public TreeTableCellEditor() {
			super();
		}
		
		public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4) {
			return tree;
		}

		public boolean isCellEditable(java.util.EventObject e) {
			if(e instanceof MouseEvent) {
				for(int i=0; i<getColumnCount(); i++) {
					if(getColumnClass(i) == TreeTableModel.class) {
						MouseEvent me = (MouseEvent)e;
						MouseEvent newMe = new MouseEvent(tree, me.getID(),
								me.getWhen(), me.getModifiers(), me.getX()-getCellRect(0, i, false).x,
								me.getY()+getRowHeight(), me.getClickCount(), me.isPopupTrigger());
						tree.dispatchEvent(newMe);
						break;
					}
				}
			}
			return false;
		}
		
		public Object getCellEditorValue() {
			return null;
		}
	}
	
	private class ListToTreeSelectionWrapper extends DefaultTreeSelectionModel {
		
		protected boolean updatingListSelectionModel;
		public ListToTreeSelectionWrapper() {
			super();
			getListSelectionModel().addListSelectionListener(createListSelectionListener());
		}
		
		public ListSelectionModel getListSelectionModel() {
			return listSelectionModel;
		}
		
		public void resetRowSelection() {
			if(!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					super.resetRowSelection();
				}
				finally {
					updatingListSelectionModel = false;
				}
			}
		}
		
		public ListSelectionListener createListSelectionListener() {
			return new ListSelectionHandler();
		}
		
		protected void updateSelectedPathsFromSelectedRows() {
			if(!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					int min = listSelectionModel.getMinSelectionIndex();
					int max = listSelectionModel.getMaxSelectionIndex();
					
					clearSelection();
					if(min != -1 && max != -1) {
						for(int i=min; i<=max; i++) {
							if(listSelectionModel.isSelectedIndex(i)) {
								TreePath selPath = tree.getPathForRow(i+1);
								if(selPath != null)
									addSelectionPath(selPath);
							}
						}
					}
				}
				finally {
					updatingListSelectionModel = false;
				}
			}
		}
		
		private class ListSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedPathsFromSelectedRows();
			}
		}
	}
}
