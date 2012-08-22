package plugins.simManagement;

import util.IceNetworkConnection;
import java.text.DateFormat;
import java.util.*;
import Comm.Simulation.*;
import Comm.Job.*;
import Comm.Manager.ChangedId;
import Ice.Properties;
import javax.swing.ImageIcon;

import javax.swing.tree.DefaultMutableTreeNode;

public class JobData extends DefaultMutableTreeNode {
	
	JobData() {
		super();
	}
	
	public static List createList(JobInformation [] jobs) {
		List l = new ArrayList();
		if(jobs == null) return l;
		
		for(int i=0; i<jobs.length; i++)
			l.add(new JobData(jobs[i]));
		
		return l;
	}
	
	public JobData(JobInformation job) {
		super();
		userObject = job;
		for(int i=0; i<job.roundIds.length; i++)
			add(new StatusData(job, i));
	}
	
	protected JobInformation userObject;
	public String toString() {
		if(userObject == null) return "fnord";
		return userObject.name;
	}
	
	public boolean isLeaf() {
		return (getChildCount() == 0);
	}
	
	public String getId() {
		return userObject.jobId;
	}
	
	public ImageIcon getImageIcon() {
		return (ImageIcon)statusIcons.get(userObject.collocatedStatus.theState);
	}
	
	// ok... STATUS = ImageIcon, NAME = DefaultMutableTreeNode, Rest: string
	static final int NAME=0, ID=1, USER=2, DESCRIPTION=3, START_TIME=4, ACTIVE_TIME=5, CPU_TIME=6, ANZ_ITEMS=7;
	public static int getItemCount() {
		return ANZ_ITEMS;
	}
	
	public static Class getClass(int pos) {
		switch(pos) {
		case NAME: return plugins.treeTable.TreeTableModel.class; // this allows treeRendering and access to status-children
		default:return String.class;
		}
	}
	
	private static final String [] names = {"name", "id", "user", "description", "start", "active", "cpu"};
	public static String getName(int pos) {
		return names[pos];
	}
	
	public Object getItem(int pos) {
		if(userObject == null) {
			System.out.println("obj is null!");
			return "foo";
		}
		switch(pos) {
		case NAME: return userObject.name; 
		case ID: return userObject.jobId;
		case USER: return userObject.owner;
		case DESCRIPTION: return userObject.description;
		case START_TIME: return getTime(new Date(userObject.startTime));
		case ACTIVE_TIME: return getDuration(userObject.collocatedStatus.runtime);
		case CPU_TIME: return getDuration(userObject.collocatedStatus.cputime);
		default: return null;
		}
	}
	
	protected void update(ChangedId changes) {
		userObject.collocatedStatus = changes.collocatedStatus;
		userObject.status[changes.round] = changes.newStatus;
		
		remove(changes.round);
		this.insert(new StatusData(userObject, changes.round), changes.round);
	}
	
	public static boolean isEditable(int column) {
		switch(column) {
		case NAME: return true;
		default: return false;
		}
	}
	
	private static final int DAY = 24*3600;
	protected String getDuration(long time) {
		long days = time/DAY;
		long hours = (time%DAY)/3600;
		long minutes = (time%3600)/60;
		long seconds = time%60;
			
		if(days > 0) return days+"d"+(hours > 0?hours+"h":"");
		else if(hours > 0) return hours+"h"+(minutes > 0?minutes+"m":"");
		else if(minutes > 0) return minutes+"m"+(seconds > 0?seconds+"s":"");
		else return seconds+"s";
	}
	
	private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	private static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	private String getTime(Date date) {
		long todayInMillisec = (new Date()).getTime();
		Date today = new Date(todayInMillisec - todayInMillisec%(DAY*1000));
		if(date.before(today))
			return dateFormat.format(date);
		else return timeFormat.format(date);
	}
	
	private static Map statusIcons = initIcons();
	private static Map initIcons() {
		Map h = new HashMap();
		
		Properties properties = IceNetworkConnection.getProperties();
		String iconPath = properties.getProperty("gui.iconDir")+"/";
		
		h.put(State.NEW, new ImageIcon(iconPath+"WhiteSignal.png"));
		h.put(State.READY, new ImageIcon(iconPath+"WhiteSignal.png"));
		h.put(State.RUNNING, new ImageIcon(iconPath+"YellowSignal.png"));
		h.put(State.STOPPED, new ImageIcon(iconPath+"BlueSignal.png"));
		h.put(State.FINISHED, new ImageIcon(iconPath+"GreenSignal.png"));
		h.put(State.ERROR, new ImageIcon(iconPath+"RedSignal.png"));
			
		return h;
	}
}
