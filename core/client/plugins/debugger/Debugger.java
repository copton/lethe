package plugins.debugger;

import gui.LetheController;
import gui.ToolbarUsable;
import javax.swing.*;

import util.IceNetworkConnection;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;

import event.*;
import java.text.DateFormat;
import java.util.*;

public class Debugger implements gui.Pluggable, ToolbarUsable, ActionListener, StatusInformationListener,
		DebugMessageListener, WarningMessageListener, ErrorMessageListener, UnexpectedExceptionListener, EventHandlerListener {
	JPanel panel;
	JList debuggerList;
	DebuggerListModel listContent;
	LetheController controller;
	int debugLevel = 1;
	int logLevel = LOG_DEBUG;
	private final static int LOG_DEBUG=4, LOG_STATUS=3, LOG_WARNING=2, LOG_ERROR=1, LOG_EXCEPTION=0;
	
	// TODO: debug only
	DefaultConsoleEventListener consolePrinter = new DefaultConsoleEventListener();
	public void init() {		
		controller = LetheController.getController();
		
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
	
		listContent = new DebuggerListModel();
		debuggerList = new JList(listContent);
		debuggerList.setCellRenderer(new DebuggerListRenderer());
		
		JScrollPane scrollPane = new JScrollPane(debuggerList);
		
		debuggerList.setBorder(BorderFactory.createLoweredBevelBorder());
		
		panel.add(scrollPane);
		panel.setAutoscrolls(true);
		
		controller.registerForToolbar(this);
		SystemEventHandler.addListener(this);
	}

	public void release() {
		save();	
	}

	public JPanel show() {
		return panel;
	}

	public void hide() {}
	
	
	private int lineNum=0;
	private DefaultEventHandler exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	private void save() {
		int size = listContent.getSize();
		try {
			String fileName = "systemLog";
			FileOutputStream stream = new java.io.FileOutputStream(fileName, true);
			for(int i=lineNum; i<size; i++) {
				stream.write(listContent.getLine(i).getBytes());
				stream.write((new String("\n")).getBytes());
			}
			lineNum = size;
		} catch(java.io.IOException e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}
	
	private void resetList() {
		listContent.clear();
		lineNum = 0;
	}
	
	public JButton getShowButton() {
		String buttonDir = IceNetworkConnection.getProperties().getProperty("gui.iconDir");
		JButton mainButton = new JButton(new ImageIcon(buttonDir+"/Paste.png"));
		mainButton.setToolTipText("Debugger");
		return mainButton;
	}

	private final static String [] buttonNames = {"save", "clear"};
	private final static int SAVE=0, CLEAR=1;
	public java.util.List addToToolbar() {
		List l = new ArrayList();
		for(int i=0; i<buttonNames.length; i++) {
			JButton button = new JButton(buttonNames[i]);
			button.setActionCommand(Integer.toString(i));
			button.addActionListener(this);
			l.add(button);
		}
		
		return l;
	}

	public void statusInformationOccured(StatusInformationEvent e) {
		if(logLevel < LOG_STATUS) return;
	//	String debugString = getDebugString("status", e.className, e.fileName, e.methodName)+e.msg;
	//	listContent.add(debugString, DefaultEventHandler.STATUS_EVENT);
	//	checkSize();
	}

	public void debugMessageOccured(DebugMessageEvent e) {
		if(e.level < debugLevel || logLevel < LOG_DEBUG) return;
		String debugString = getDebugString("debug"+e.level, e.className, e.fileName, e.methodName)+e.msg;
		listContent.add(debugString, DefaultEventHandler.DEBUG_EVENT);
		checkSize();
	}

	public void warningMessageOccured(WarningMessageEvent e) {
		if(logLevel < LOG_WARNING) return;	
		String debugString = getDebugString("warning", e.className, e.fileName, e.methodName)+e.msg;
		listContent.add(debugString, DefaultEventHandler.WARNING_EVENT);
		checkSize();
	}
	
	public void errorMessageOccured(ErrorMessageEvent e) {
		if(logLevel < LOG_ERROR) return;
		String debugString = getDebugString("error", e.className, e.fileName, e.methodName)+e.msg;
		listContent.add(debugString, DefaultEventHandler.ERROR_EVENT);
		checkSize();
	}
	
	public void unexpectedExceptionOccured(UnexpectedExceptionEvent e) {
		if(logLevel < LOG_EXCEPTION) return;
		String debugString = getDebugString(e.e.getClass().getName(), e.className, e.fileName, e.methodName)+e.e.getLocalizedMessage();
		listContent.add(debugString, DefaultEventHandler.EXCEPTION_EVENT);
		checkSize();
	}

	public void eventHandlerRegistered(EventHandlerEvent e) {
		e.eventHandler.addEventListener(this);
		e.eventHandler.addEventListener(consolePrinter);
	}

	public String getDebugString(String type, String className, String fileName, String methodName) {
		DateFormat dateFormatter = new java.text.SimpleDateFormat("MMM dd yy HH:mm:ss");
		StringBuffer debugString = new StringBuffer("[");
		debugString.append(dateFormatter.format(new java.util.Date()));
		debugString.append(" ");
		debugString.append(className);
		debugString.append(":");
		debugString.append(methodName);
		debugString.append("] ");
		debugString.append("["+type+"] ");
		
		return debugString.toString();
	}

	private int MAX_SIZE = 10;
	private void checkSize() {
		int size = listContent.getSize();
		if(size > MAX_SIZE) {
			save();
			int anz2remove = MAX_SIZE/10;
			lineNum -= anz2remove;
			listContent.removeFirstLines(anz2remove);
		}
	}
	
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case SAVE: save(); break;
		case CLEAR: resetList(); break;
		}
	}

}
