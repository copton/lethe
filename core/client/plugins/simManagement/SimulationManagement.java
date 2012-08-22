package plugins.simManagement;

import java.awt.event.*;

import javax.swing.*;

import java.awt.BorderLayout;
import java.io.File;
import java.util.*;

import org.jdom.Element;
//import event.DefaultEventHandler;
//import event.UnexpectedExceptionEvent;
import gui.*;
import Comm.Job.Specification;
import plugins.treeTable.TreeTable;
import util.IceNetworkConnection;
import util.XmlFileFilter;

public class SimulationManagement implements Pluggable, ToolbarUsable, Personalizable, ActionListener {
	JPanel mainPanel;
	Element settings;
	int refreshTimeout;
	List filter;
	java.util.Timer timer = null;
	StatusListener statusListener;
	
	private client.SimulationManagement manager = new client.SimulationManagement();
/*	private DefaultEventHandler exceptionHandler = 
		new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
*/
	private final static String settingsName = "SimulationManagement";	
	
	private TreeTable table;
	private ManagementModel model = new ManagementModel();
	public void init() {
		table = new TreeTable(model);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(java.awt.Color.RED);
		mainPanel.add(new JScrollPane(table));
			
		createButtons();
		LetheController.getController().registerForToolbar(this);
		manager.connect();
	
		String [] users = manager.getAllUsers();
		System.out.println("registered users:");
		for(int i=0; i<users.length; i++)
			System.out.println(users[i]);
			
		statusListener = new StatusListener(model, users);
	}
	
	public void release() {
		statusListener.deregister();
	}

	public JPanel show() {
		// model.removeAll();
		model.removeAll();
		List jobs = JobData.createList(manager.getAllSimulations());
		if(jobs != null)
			model.setJobs(jobs);
		
		return mainPanel;
	}

	public void hide() {
	}

	public JButton getShowButton() {
		String buttonDir = IceNetworkConnection.getProperties().getProperty("gui.iconDir");
		JButton button = new JButton(new ImageIcon(buttonDir+"/Inform.png"));
		button.setToolTipText("Managing your simulations");		
		return button;
	}

	public List addToToolbar() {
		return buttonList;
	}
	
	private final static int NEW=0, ABORT=1,  PAUSE=2, RESTART=3;
	private List buttonList = new ArrayList();
	private List createButtons() {
		String [] buttonName = {"New", "Abort", "Pause", "Restart"};
		String [] toolTip = {"start new simulation", "abort simulations", "pause simulations", "restart simulations"};
	
		for(int i=0; i<buttonName.length; i++) {
			JButton b = new JButton(buttonName[i]);
			b.setActionCommand(Integer.toString(i));
			b.setToolTipText(toolTip[i]);
			b.addActionListener(this);
			buttonList.add(b);
		}
	
		
		/*
		((JButton)buttonList.get(7)).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.print.PrinterJob pJob = java.awt.print.PrinterJob.getPrinterJob();
					pJob.setCopies(1);
					pJob.setJobName("Simulations");
					pJob.setPrintable(new java.awt.print.Printable() {
						public int print(java.awt.Graphics pg, java.awt.print.PageFormat pf, int pageNum) {
							if(pageNum > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
							
							mainPanel.paint(pg);
							return java.awt.print.Printable.PAGE_EXISTS;
						}
					});
					if(pJob.printDialog() == false) return;
					pJob.print();
				} catch(Exception exec) {
					exec.printStackTrace();
				}
			}
		});
		*/
		
		return buttonList;
	}

	public void setSettings(Element e) {
		if(e == null) settings = getDefaultSettings();
		else settings = e;

		refreshTimeout = Integer.parseInt(settings.getChildText("refreshPeriod"));
		filter = new ArrayList();
		
		Iterator filters = settings.getChild("filter").getChildren().iterator();
		while(filters.hasNext()) {
			// add filter
			filters.next();
		}
	}

	public Element getSettings() {
		settings.removeContent();
		
		Element refreshPeriod = new Element("refreshPeriod");
		refreshPeriod.setText(String.valueOf(refreshTimeout));
		settings.addContent(refreshPeriod);
		
		Element filterElement = new Element("filter");
		
		Iterator it = filter.iterator();
		while(it.hasNext()) {
			it.next(); // TODO: generate Element of it!!
		}
		settings.addContent(filterElement);
		
		return settings;
	}

	public Element getDefaultSettings() {
		Element settings = new Element(settingsName);
		
		// lookup_period
		Element refreshPeriod = new Element("refreshPeriod");
		refreshPeriod.setText("300"); // 5min
		settings.addContent(refreshPeriod);
		
		// filter
		Element filterElement = new Element("filter");
		settings.addContent(filterElement);
		
		return settings;
	}

	class UpdateStatusTask extends TimerTask {
		SimulationManagement simObj;
		
		public UpdateStatusTask(SimulationManagement simObj) {
			this.simObj = simObj;
		}
		
		public void run() {
			//simObj.updateStatus();
		}
	}

	private String [] getSelectedIds() {
		Set ids = new HashSet();
		int [] selected = table.getSelectedRows();
		for(int i=0; i<selected.length; i++) 
			ids.add(table.getValueAt(selected[i], JobData.ID));
		
		return (String[])ids.toArray(new String[0]);
	}
	
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		String [] selected;
		switch(action) {
		case NEW: 
			// 1. show file open dialog
			String fileName; // = "/Users/phia/akt_source/simmit/trunk/jobs/testJob/testJob.xml";
		
			File baseDir = new File(IceNetworkConnection.getProperties().getProperty("xml.path2jobs"));
			JFileChooser fileChooser = new JFileChooser(baseDir);
			fileChooser.setFileFilter(new XmlFileFilter());
			
			int resVal = fileChooser.showOpenDialog(null);
			
			if(resVal != JFileChooser.APPROVE_OPTION) return;
			fileName = fileChooser.getSelectedFile().getAbsolutePath();

			Specification newJob = manager.createJobSpecification(fileName);
			System.out.println("created? "+((newJob!=null)?"yes":"no"));
			
			if(newJob != null) {
				manager.connect();
				
				if(manager.startSimulation(newJob)) {
					model.addJob(new JobData(manager.getSimulation(newJob.jobId)));
					System.out.println("added data");
				} else System.out.println("couldn't start job");
			}
			break;
		case ABORT: {
			selected = getSelectedIds();
			for(int i=0; i<selected.length; i++)
				manager.abortSimulation(selected[i]);
		
			List jobs = JobData.createList(manager.getAllSimulations());
			if(jobs != null)
				model.setJobs(jobs);
			break;
		}	
		case PAUSE: 
			selected = getSelectedIds();
			for(int i=0; i<selected.length; i++)
				manager.stopSimulation(selected[i]);
			break;
		case RESTART:
			selected = getSelectedIds();
			for(int i=0; i<selected.length; i++)
				manager.restartSimulation(selected[i]);
			break;
		default: break;
		}
	}
}
