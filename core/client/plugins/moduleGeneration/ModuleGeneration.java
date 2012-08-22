package plugins.moduleGeneration;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

import javax.swing.*;

import Ice.Properties;

import event.DefaultEventHandler;
import gui.LetheController;
import gui.ToolbarUsable;

import plugins.treeTable.*;
import plugins.treeTable.dialog.*;
import util.IceNetworkConnection;
import util.XmlFileFilter;
import xml.TypedElement;
import client.PackageGenerator;

public class ModuleGeneration implements gui.Pluggable, ToolbarUsable, 
		java.awt.event.ActionListener {
	private XmlTreePanel mainPanel;
	private List buttonList;
	XmlTreeModel treeModel;

	boolean hasLocalModifications = false;
	String fileName;
	DefaultEventHandler exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	String path2module, path2type;
	
	public void init() {
		System.out.println("init ModuleGeneration");
		gui.LetheController controller = LetheController.getController();
		
		mainPanel = new XmlTreePanel();
		buttonList = createButtonList();

		controller.registerForToolbar(this);
		controller.addMenuListener(this);
		
		Properties properties = IceNetworkConnection.getProperties();
		path2module = properties.getProperty("xml.path2modules");
		path2type = properties.getProperty("xml.path2types");
	}

	public void release() {}

	public JPanel show() {
		return mainPanel;
	}

	public void hide() {}

	JButton mainButton;
	public JButton getShowButton() {
		String buttonDir = IceNetworkConnection.getProperties().getProperty("gui.iconDir");
		mainButton = new JButton(new ImageIcon(buttonDir+"/Wheel2.png"));
		mainButton.setToolTipText("ModuleGenerator");
		mainButton.setVisible(false);
		return mainButton;
	}

	public List addToToolbar() {
		return buttonList;
	}

	private static final int NEW_MODULE=0, NEW_TYPE=1, SAVE = 2, SAVE_AS = 3, 
	CREATE_PACKAGE = 4, CLOSE = 5;
	private List createButtonList() {
		List l = new java.util.ArrayList();
		
		String [][] buttonDesc = {
				{"New_Mod", "Erstelle neues Modul", Integer.toString(NEW_MODULE)},
				{"New_Typ", "Erstelle neuen Typ", Integer.toString(NEW_TYPE)},
				{"Save", "Speichern", Integer.toString(SAVE)},
				{"Save As", "Seichern unter", Integer.toString(SAVE_AS)},
				{"Create_Pack", "Package generieren", Integer.toString(CREATE_PACKAGE)},
				{"Close", "Fenster schliessen", Integer.toString(CLOSE)},
		};
		
		for(int i=0; i<buttonDesc.length; i++) {
			JButton b = new JButton(buttonDesc[i][0]);
			b.setToolTipText(buttonDesc[i][1]);
			b.setActionCommand(buttonDesc[i][2]);
			b.addActionListener(this);
			l.add(b);
		}
		return l;
	}
	
	private void createNewNode(int type) {
		if(hasLocalModifications) save();
		
		hasLocalModifications = false;
		fileName = null;
		
		TypedElement moduleNode = TypedElement.get(type);
		treeModel = new XmlTreeModel(new ModuleNode(moduleNode));
		
		((EditorDialog)mainPanel.getEditor()).initDialogs(usedDefinitionDialogs);
		mainPanel.setTree(treeModel);
		aktElement = moduleNode;
	}
	
	private org.jdom.Element aktElement;
	private void loadNewNode(boolean loadModule) {
		if(hasLocalModifications) save();
		
		java.io.File startDir = new java.io.File(loadModule?path2module:path2type); 
		JFileChooser fileChooser = new JFileChooser(startDir);
		fileChooser.setFileFilter(new XmlFileFilter());
		int resVal = fileChooser.showOpenDialog(null);
		
		if(resVal != JFileChooser.APPROVE_OPTION) return;
		fileName = fileChooser.getSelectedFile().getAbsolutePath();
		
		try {
			org.jdom.Element e = xml.XmlHelper.getRootElement(fileName, true);
			TypedElement root = new TypedElement(e);
			
			switch(root.getType()) {
			case TypedElement.MODULE_DEF: 
			case TypedElement.TYPE_DEF:
				((EditorDialog)mainPanel.getEditor()).initDialogs(usedDefinitionDialogs);
				treeModel = new XmlTreeModel(new ModuleNode(root, null)); break;
			case TypedElement.MODULE_INSTANCE: 
			case TypedElement.TYPE_INSTANCE:
				((EditorDialog)mainPanel.getEditor()).initDialogs(usedInstanceDialogs);
				treeModel = new XmlTreeModel(new InstanceNode(root, null)); break;		
			default: return;
			}
			
			mainPanel.setTree(treeModel);
			aktElement = e;
		} catch(Exception e) {
			exceptionHandler.fireEvent(new event.UnexpectedExceptionEvent(e));
		}
	}
	
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if(source instanceof JButton) buttonAction(event);
		else if(source instanceof JMenuItem) menuAction(event);
	}
	
	private void menuAction(ActionEvent event) {
		if(event.getActionCommand().equals("new:module")) createNewNode(TypedElement.MODULE_DEF);
		else if (event.getActionCommand().equals("new:type")) createNewNode(TypedElement.TYPE_DEF);
		else if (event.getActionCommand().equals("load:module")) loadNewNode(true);
		else if (event.getActionCommand().equals("load:type")) loadNewNode(false);
		else return;
			
		mainButton.setVisible(true);
		LetheController.getController().showPluggable(this);
	}
	
	private boolean save() {
		int type = ((XmlTreeNode)treeModel.getRoot()).getElement().getType();
		File startDir;
		switch(type) {
		case TypedElement.TYPE_INSTANCE:
		case TypedElement.MODULE_INSTANCE:
			startDir = new File(fileName+"/instances"); break;
		case TypedElement.TYPE_DEF: startDir = new File(path2type); break;
		default: startDir = new File(path2module);
		}
		
		JFileChooser fileChooser = new JFileChooser(startDir);
		fileChooser.setFileFilter(new XmlFileFilter());
		int resVal = fileChooser.showSaveDialog(null);
		
		if(resVal != JFileChooser.APPROVE_OPTION) return false;
		String fileName = fileChooser.getSelectedFile().getAbsolutePath();
	
		if(!fileName.toLowerCase().endsWith(".xml"))
			fileName += ".xml";
		
		if(this.fileName == null) this.fileName = fileName;
		return save(fileName);
	}
	
	private 	boolean save(String fileName) {
		try {
			treeModel.saveToFile(fileName);
		} catch(java.io.IOException e) {
			return false;
		}
		
		hasLocalModifications = false;
		return true;
	}
	
	private void createPackage() {
		PackageGenerationDialog dialog = 
			new PackageGenerationDialog(LetheController.getMainWindow());
		dialog.show();
		int code = dialog.getGenerationCode();
		if(code <= 0) return;
		
		System.out.println("code="+code);
		PackageGenerator generator = new PackageGenerator(code);
		
		aktElement = ((XmlTreeNode)treeModel.getRoot()).prepareForSaving(null);
		generator.generatePackage(aktElement, null);
		System.out.println("created package");// TODO: muss als Dialog angezeigt werden!
	}
	
	private void buttonAction(ActionEvent e) {
		int actionCommand = Integer.parseInt(e.getActionCommand());
		
		switch(actionCommand) {
		case NEW_MODULE: createNewNode(TypedElement.MODULE_DEF); break;
		case NEW_TYPE: createNewNode(TypedElement.TYPE_DEF); break;
		case SAVE: 
			if(fileName != null) {
				save(fileName); 
				break;
			}
		case SAVE_AS: save(); break;
		case CREATE_PACKAGE: 
			createPackage();
			break;
		case CLOSE: mainButton.setVisible(false); break;
		}	
	}
	
	private static Map usedDefinitionDialogs = getDefinitionDialogs();
	private static Map usedInstanceDialogs = getInstanceDialogs();
	private static Map getDefinitionDialogs() {
		Map knownDialogs = new HashMap();
		addSupportedTypes(new ModuleDefinitionDialog(), knownDialogs);
		addSupportedTypes(new TypeDefinitionDialog(), knownDialogs);
		addSupportedTypes(new InstanceDefinitionDialog(), knownDialogs);
		addSupportedTypes(new DirectoryContentDialog(), knownDialogs);
		
		addSupportedTypes(new StringDefinitionDialog(), knownDialogs);
		addSupportedTypes(new NumberDefinitionDialog(), knownDialogs);
		addSupportedTypes(new BooleanDefinitionDialog(), knownDialogs);
		addSupportedTypes(new ClassDefinitionDialog(), knownDialogs);
		addSupportedTypes(new ArrayDefinitionDialog(), knownDialogs);
		addSupportedTypes(new TableDefinitionDialog(), knownDialogs);
		addSupportedTypes(new DictionaryDefinitionDialog(), knownDialogs);
		addSupportedTypes(new TableDefinitionDialog(), knownDialogs);
		addSupportedTypes(new EnumDefinitionDialog(), knownDialogs);
		addSupportedTypes(new PortDefinitionDialog(), knownDialogs);	
		
		return knownDialogs;
	}
	
	private static Map getInstanceDialogs() {
		Map knownDialogs = new HashMap();
		addSupportedTypes(new ModuleDefinitionDialog(), knownDialogs);
		addSupportedTypes(new TypeDefinitionDialog(), knownDialogs);
		addSupportedTypes(new EditInstanceDialog(), knownDialogs);
		
		return knownDialogs;
	}
	
	private static void addSupportedTypes(DialogPluggable p, Map types) {
		int [] values = p.handledTypes();
		for(int i=0; i<values.length; i++) 
			types.put(new Integer(values[i]), p);
	}
}
