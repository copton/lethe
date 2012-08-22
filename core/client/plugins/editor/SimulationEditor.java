package plugins.editor;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import event.UnexpectedExceptionEvent;

import gui.LetheController;
import gui.Pluggable;
import gui.ToolbarUsable;
import plugins.treeTable.*;
import plugins.treeTable.dialog.*;
import util.IceNetworkConnection;
import util.XmlFileFilter;
import xml.TypedElement;
import xml.XmlHelper;

public class SimulationEditor implements Pluggable, ToolbarUsable,
		java.awt.event.ActionListener {

	private XmlTreePanel mainPanel = new XmlTreePanel();
	private XmlTreeModel treeModel;
	
	public void init() {	
		loadNewModel();
		
		LetheController.getController().registerForToolbar(this);
		LetheController.getController().addMenuListener(this);
	}

	public void release() {
		// TODO: if changes: save
	}

	public JPanel show() {
		return mainPanel;
	}

	public void hide() {}

	public JButton getShowButton() {
		String buttonDir = IceNetworkConnection.getProperties().getProperty("gui.iconDir");
		JButton mainButton = new JButton(new ImageIcon(buttonDir+"/Wheel.png"));
		mainButton.setToolTipText("JobEditor");
		return mainButton;
	}

	private List toolbar = initToolbar();
	public List addToToolbar() {
		return toolbar;
	}

	private List initToolbar() {
		List l = new java.util.ArrayList();

		String [] buttonNames = {"new", "load", "save", "save as"};
		for(int i=0; i<buttonNames.length; i++) {
			JButton b = new JButton(buttonNames[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(this);
			l.add(b);
		}
		return l;
	}
	
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if(source instanceof JButton) buttonAction(event);
		else if(source instanceof JMenuItem) menuAction(event);
	}
	
	private void menuAction(ActionEvent event) {
		if(event.getActionCommand().equals("new:job")) {
			loadNewModel();
			LetheController.getController().showPluggable(this);
		} else if(event.getActionCommand().equals("load:job")) {
			loadModel();
			LetheController.getController().showPluggable(this);
		}
	}
	
	private String fileName;
	private static final int NEW=0, LOAD=1, SAVE=2, SAVE_AS=3;
	private void buttonAction(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		
		switch(action) {
		case NEW: 
			loadNewModel();
			break;
		case LOAD: 
			loadModel();
			break;
		case SAVE: 
			if(fileName != null) save(fileName);
			else save();
			break;
		case SAVE_AS: 
			save();
			break;
		}
	}
	
	private void loadNewModel() {
		treeModel = new XmlTreeModel(new EditorNode(TypedElement.get(TypedElement.JOB)));
		
		((EditorDialog)mainPanel.getEditor()).initDialogs(usedDialogs);
		mainPanel.setTree(treeModel);
		
		fileName = null;
	}
	
	private void loadModel() {
		File baseDir = new File(IceNetworkConnection.getProperties().getProperty("xml.path2jobs"));
		if(!baseDir.exists()) baseDir.mkdirs();
		
		JFileChooser fileChooser = new JFileChooser(baseDir);
		fileChooser.setFileFilter(new XmlFileFilter());
		
		int resVal = fileChooser.showOpenDialog(null);
		
		if(resVal != JFileChooser.APPROVE_OPTION) return;
		fileName = fileChooser.getSelectedFile().getAbsolutePath();

		TypedElement root;
		try {
			root = new TypedElement(XmlHelper.getRootElement(fileName, true));
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			fileName = null;
			return;
		}
		
		((EditorDialog)mainPanel.getEditor()).initDialogs(usedDialogs);
		treeModel = new XmlTreeModel(new EditorNode(root));
		mainPanel.setTree(treeModel);
	}
	
	  private void save() {
          File baseFile = new File(IceNetworkConnection.getProperties().
        		  getProperty("xml.path2jobs"));
          
          JFileChooser fileChooser = new JFileChooser(baseFile);
          int resVal = fileChooser.showSaveDialog(null);
          
          if(resVal != JFileChooser.APPROVE_OPTION) return;
          String fileName = fileChooser.getSelectedFile().getAbsolutePath();
         
          if(this.fileName == null) this.fileName = fileName;
          save(fileName);
	  }
  
	  private event.DefaultEventHandler exceptionHandler = new event.DefaultEventHandler(event.DefaultEventHandler.EXCEPTION_EVENT);
	  private void save(String fileName) {
		  try {
			  treeModel.saveToFile(fileName);
           } catch(java.io.IOException e) {
        	   	  exceptionHandler.fireEvent(new event.UnexpectedExceptionEvent(e));
           }
	  }
	
	
	private static Map usedDialogs = getUsedDialogs();
	private static Map getUsedDialogs() {
		Map knownDialogs = new java.util.HashMap();
		addSupportedTypes(new JobDefinitionDialog(), knownDialogs);
		addSupportedTypes(new DirectoryContentDialog(), knownDialogs);
		addSupportedTypes(new EdgeNodeListDialog(), knownDialogs);
		addSupportedTypes(new NodeCreationDialog(), knownDialogs);
		addSupportedTypes(new EdgeCreationDialog(), knownDialogs);
		addSupportedTypes(new RoundDefinitionDialog(), knownDialogs);
		addSupportedTypes(new PhaseDefinitionDialog(), knownDialogs);
		return knownDialogs;
	}

	private static void addSupportedTypes(DialogPluggable p, Map types) {
		int [] values = p.handledTypes();
		for(int i=0; i<values.length; i++) 
			types.put(new Integer(values[i]), p);
	}
}
