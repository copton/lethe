package plugins.treeTable;

import java.awt.event.*;
import javax.swing.tree.TreePath;


import util.EnumerationWrapper;

import xml.*;


public class TreeCellEditor implements MouseListener, Types {
	XmlTreePanel mainPanel;
	XmlTreeNode lastNode = null;
	
	public TreeCellEditor(XmlTreePanel mainPanel) {
		this.mainPanel = mainPanel;
	}
	
	public void mouseClicked(MouseEvent e) {
		TreePath path = null;
		if(e.getSource() instanceof javax.swing.JTree) 
			path = mainPanel.getTree().getClosestPathForLocation(e.getX(), e.getY());
		else if(e.getSource() instanceof javax.swing.JList) {
			if(e.getClickCount() < 2) return;
			javax.swing.JList list = (javax.swing.JList)e.getSource();
			TypedElement clickedElement = (TypedElement)list.getSelectedValue();
			java.util.Iterator it = EnumerationWrapper.createList(lastNode.children()).iterator();
			while(it.hasNext()) {
				XmlTreeNode treeNode = (XmlTreeNode)it.next();
				if(treeNode.getElement().equals(clickedElement)) {
					path = new TreePath(treeNode.getPath());
					break;
				}
			}
		}
		
		if(path != null) {
			mainPanel.getTree().setSelectionPath(path);
			mainPanel.getTree().scrollPathToVisible(path);
			showNode(path);
		}
	}
	
	private void showNode(TreePath path) {
		XmlTreeNode aktNode = (XmlTreeNode)path.getLastPathComponent();
	
		
		if(aktNode == lastNode) return;
		
		if(lastNode != null) {// Auto-Apply
			mainPanel.editor.actionPerformed(new ActionEvent(this, 0, Integer.toString(EditorDialog.APPLY)));
			mainPanel.tree.updateUI();
		}
			
		lastNode = aktNode;
		
		TypedElement element = aktNode.getElement();
		
		if(!mainPanel.editor.setDialog(element, mainPanel.getTree())) return;
		mainPanel.setEditor(mainPanel.editor);
	}

	public void setElement(TypedElement e) {
		lastNode.update(e);
		((XmlTreeModel)mainPanel.getTree().getModel()).nodeChanged(lastNode);
		mainPanel.getTree().updateUI();
	}
	
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
}
