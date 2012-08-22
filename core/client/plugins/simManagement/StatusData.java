package plugins.simManagement;

import util.IceNetworkConnection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import Comm.Job.*;
import Comm.Simulation.State;
import Ice.Properties;

public class StatusData extends JobData {

	private int round;
	public StatusData(JobInformation job, int round) {
		super();
		userObject = job;
		this.round = round;
	}
	
	public boolean isLeaf() {
		return true;
	}
	
	public String toString() {
		return "round "+(round+1);
	}
	
	public ImageIcon getImageIcon() {
		return (ImageIcon)statusIcons.get(userObject.status[round].theState);
	}
	
	private static final Map statusIcons = initIcons();
	public Object getItem(int pos) {
		switch(pos) {
		case NAME: return "round "+round; 
		case ID: return userObject.roundIds[round];
		case DESCRIPTION:
		case USER:
		case START_TIME: return "";
		case ACTIVE_TIME:  return getDuration(userObject.status[round].livetime);
		case CPU_TIME: return getDuration(userObject.status[round].runtime);
		default: return ((JobData)parent).getItem(pos);
		}
	}
	
	private static final Map initIcons() {
		Map h = new HashMap();
		
		Properties properties = IceNetworkConnection.getProperties();
		String iconPath = properties.getProperty("gui.iconDir")+"/";
		
		h.put(State.NEW, new ImageIcon(iconPath+"GreySignal_small.png"));
		h.put(State.READY, new ImageIcon(iconPath+"WhiteSignal_small.png"));
		h.put(State.RUNNING, new ImageIcon(iconPath+"YellowSignal_small.png"));
		h.put(State.STOPPED, new ImageIcon(iconPath+"BlueSignal_small.png"));
		h.put(State.FINISHED, new ImageIcon(iconPath+"GreenSignal_small.png"));
		h.put(State.ERROR, new ImageIcon(iconPath+"RedSignal_small.png"));
		
		return h;
	}
}
