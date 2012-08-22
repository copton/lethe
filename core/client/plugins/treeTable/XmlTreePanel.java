package plugins.treeTable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseListener;

import javax.swing.*;


public class XmlTreePanel extends JPanel {
	JPanel treePanel;
	JScrollPane treeScrollPane;
	JTree tree;
	JSplitPane sp;
	MouseListener editorListener;
	
	EditorDialog editor;
	public XmlTreePanel() {
		super(new BorderLayout());
		
		treePanel = new JPanel(new BorderLayout());
		treeScrollPane = new JScrollPane(treePanel);
	
		editorListener = new TreeCellEditor(this);
		editor = new EditorDialog((TreeCellEditor)editorListener);
	
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.setLeftComponent(treeScrollPane);
		sp.setRightComponent(editor);
	}
	
	public void setTree(XmlTreeModel model) {
		tree = new JTree(model);
		tree.setCellRenderer(new TreeCellRenderer());
		
		treePanel.removeAll();
		treePanel.add(tree);
		treeScrollPane = new JScrollPane(treePanel);
	
		sp.setLeftComponent(treeScrollPane);
		showEditor(true);
		
		// simulate click on root-element to show first page
		editorListener.mouseClicked(new java.awt.event.MouseEvent(tree, 0, 0, 0, 
				0, 0, 1, false));
	}
	
	public JTree getTree() {
		return tree;
	}
	
	public Component getEditor() {
		return sp.getRightComponent();
	}
	
	public void setEditor(Component editor) {
		sp.setRightComponent(editor);
	}
	
	public void showEditor(boolean show) {
		removeAll();
		if(show) {
			add(sp);
			tree.addMouseListener(editorListener);
		} else {
			add(treePanel);
			tree.removeMouseListener(editorListener);
		}
		
		super.updateUI();
	}
	
}
