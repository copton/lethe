package plugins.userManagement;

import javax.swing.tree.*;

public class UserData {
	protected String user;
	protected String group;
	protected TreeModel allowed;
	protected String denied;
	
	public UserData(String u, String g, String a, String d) {
		user = u;
		group = g;
	//	allowed = a;
		denied = d;
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("foo");
		rootNode.add(new DefaultMutableTreeNode("child"+Math.random()));
		allowed = new DefaultTreeModel(rootNode);
	}
}
