package xml.dialog;

import java.awt.BorderLayout;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import xml.TypedElement;

public class SimpleDialog extends javax.swing.JDialog implements ActionListener {
	private DialogHandling handler;
	private int actionCommand;
	private static final int UNDEFINED=-1, OK=0, CANCELED=1;
	
	public SimpleDialog(int type, DialogHandling handler, JFrame parent) {
		super(parent);
		
		actionCommand = UNDEFINED;
		this.handler = handler;
		createDialog(type);
	
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(actionCommand == UNDEFINED) 
					actionCommand = CANCELED;
				lock();
			}
		});
	}
	
	public TypedElement showDialog() {
		pack();
		setVisible(true);
		
		lock();
		dispose();
		return (actionCommand==OK)?handler.getElement():null;
	}
	
	private void createDialog(int type) {
		setTitle(handler.getTitle());
		
		JPanel mainPanel = new JPanel();
		mainPanel = handler.getPanel();
		mainPanel.setBorder(BorderFactory.createEtchedBorder());
			
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton cancelButton = new JButton("abbrechen");
		cancelButton.setActionCommand(Integer.toString(CANCELED));
		
		JButton okButton = new JButton("ok");
		okButton.setActionCommand(Integer.toString(OK));
	
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		JPanel borderLeft = new JPanel();
		borderLeft.setBounds(0, 0, 0, 5);
		getContentPane().add(borderLeft, BorderLayout.WEST);
	
		JPanel borderRight = new JPanel();
		borderRight.setBounds(0, 0, 0, 5);
		getContentPane().add(borderRight, BorderLayout.EAST);
		
		JPanel borderTop = new JPanel();
		borderLeft.setBounds(0, 0, 5, 0);
		getContentPane().add(borderTop, BorderLayout.NORTH);	
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private synchronized void lock() {
		if(actionCommand == UNDEFINED) {
			try {
				wait();
			} catch(Exception e) {}
		} else notify();
	}
	
	public void actionPerformed(ActionEvent event) {
		actionCommand = Integer.parseInt(event.getActionCommand());
		setVisible(false);
		lock();
	}
	
	public static void main(String [] args) {
		(new SimpleDialog(xml.TypedElement.INT, new StringElementHandler(), null)).showDialog();
		System.exit(0);
	}
}
