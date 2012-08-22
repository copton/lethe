package plugins.treeTable;

import javax.swing.tree.*;

public class XmlTreeModel extends DefaultTreeModel {

	public XmlTreeModel(TreeNode root) {
		super(root);
	}

	public void saveToFile(String fileName) throws java.io.IOException {
		xml.XmlHelper.writeRootElement(((XmlTreeNode)getRoot()).prepareForSaving(fileName), fileName);
	}	
}
