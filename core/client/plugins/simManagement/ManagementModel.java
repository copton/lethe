package plugins.simManagement;

import plugins.treeTable.TreeTableModel;
import util.EnumerationWrapper;

import javax.swing.tree.*;

import Comm.Manager.ChangedId;

import java.util.*;

public class ManagementModel extends DefaultTreeModel implements TreeTableModel {
	
	private DefaultMutableTreeNode root;
	public ManagementModel() {
		super(new DefaultMutableTreeNode());
		root = (DefaultMutableTreeNode)getRoot();
	}
	
	public void setJobs(List jobData) {
		root.removeAllChildren();
		if(jobData.size() == 0) return;
		
		JobData [] children = (JobData[])jobData.toArray(new JobData[0]);
		int [] indices = new int[children.length];
		
		for(int i=0; i<children.length; i++) {
			root.add(children[i]);
			indices[i] = i;
			
			listedJobs.put(children[i].getId(), children[i]);
		}
		try {
			fireTreeStructureChanged(this, root.getPath(), indices, children);
		} catch(Exception e) {}
	}
	
	public void addJob(JobData d) {
		root.add(d);
		Object [] children = new Object[1];
		children[0] = d;
		int [] indices = new int[1];
		indices[0] = root.getChildCount()-1;
		
		listedJobs.put(d.getId(), d);
		fireTreeNodesInserted(root, getPathToRoot(root), indices, children);
	}
	
	public void removeAll() {
		int [] indices = new int[root.getChildCount()];
		for(int i=0; i<indices.length; i++) indices[i] = i;
		
		fireTreeNodesRemoved(root, root.getPath(), indices, root.getPath());
		root.removeAllChildren();
	}
	
	private Map listedJobs = new HashMap();
	protected void updateJob(ChangedId changes) {
		JobData job = (JobData)listedJobs.get(changes.jobId);
		if(job == null) return;
		
		job.update(changes);
		
		int [] index = new int[1];
		index[0] = EnumerationWrapper.createList(root.children()).indexOf(job);
		
		Object [] object = new Object[1];
		object[0] = job;
		
		this.fireTreeNodesChanged(root, root.getPath(), index, object);
	}

	public int getColumnCount() {
		return JobData.getItemCount();
	}
	
	public String getColumnName(int pos) {
		return JobData.getName(pos);
	}

	public Class getColumnClass(int pos) {
		return JobData.getClass(pos);
	}

	public Object getValueAt(Object node, int column) {
		JobData data = (JobData)node;
		return data.getItem(column);
	}

	public void setValueAt(Object value, Object node, int column) {}
	public boolean isCellEditable(Object node, int column) {
		return JobData.isEditable(column);
	}

}
