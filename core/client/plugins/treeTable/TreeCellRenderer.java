package plugins.treeTable;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;
import xml.TypedElement;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeCellRenderer extends DefaultTreeCellRenderer implements xml.Types {
	 
	private static ImageIcon [] icons = xml.XmlHelper.elementIcons;
	
	public TreeCellRenderer() {
		super();
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			 boolean sel, boolean expanded, boolean leaf,
               int row, boolean hasFocus) {
	 
		 super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		 TypedElement element = (TypedElement)((DefaultMutableTreeNode)value).getUserObject();
		 
		 if(element.getType() < 0) {
			 System.out.println("Unknown type: "+element.getName());
		 }
		 else setIcon(icons[element.getType()]);
		 return this;
	 }
}
