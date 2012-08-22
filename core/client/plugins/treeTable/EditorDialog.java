package plugins.treeTable;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.*;

import javax.swing.*;
import xml.TypedElement;
import plugins.treeTable.dialog.DialogPluggable;

import java.util.*;

public class EditorDialog extends JPanel implements ActionListener {
	DialogPluggable dialog;
	
	public static final int LABEL=0, TEXT=1;
	private static GridBagConstraints labelConstraint, textConstraint;
	private Map knownDialogs;
	private TreeCellEditor callback;
	private TypedElement element;
	private JScrollPane sp;
	
	public EditorDialog(TreeCellEditor callback) {
		super(new BorderLayout());
		this.callback = callback;
		
		setBackground(Color.WHITE);
		initDialog();
	}
	
	private boolean listenerSet;
	public boolean setDialog(TypedElement element, JTree tree) {
		this.element = element;
		
		if(knownDialogs == null) return false;
		dialog = (DialogPluggable)knownDialogs.get(new Integer(element.getType()));
		if(dialog == null) {
			System.out.println("couldn't find dialog for: "+element.getName()+": "+element.getType());
			return false;
		}
		
		dialog.init(element);
		
		remove(sp);
		sp = new JScrollPane(dialog.getPanel());
		add(sp);
		
		if(!listenerSet) {
			listenerSet = true;
			tree.addMouseListener(new DefaultButtonSetter());
		}
		return true;
	}
	
	public static final int CANCEL=0, APPLY=1;
	private 	JButton [] button = new JButton[2];
	private void initDialog() {
		JPanel top = new JPanel();
		top.setSize(new Dimension(0, 10));
		add(top, BorderLayout.NORTH);
		
		JPanel left = new JPanel();
		left.setSize(new Dimension(10, 0));
		add(left, BorderLayout.WEST);
		
		JPanel right = new JPanel();
		right.setSize(new Dimension(10, 0));
		add(right, BorderLayout.EAST);
	
		sp = new JScrollPane();
		add(sp);
		
		String [] buttonName = {"reset", "apply"};
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		for(int i=0; i<=APPLY; i++) {
			button[i] = new JButton(buttonName[i]);
			button[i].addActionListener(this);
			button[i].setActionCommand(Integer.toString(i));
			buttonPanel.add(button[i]);
		}
		add(buttonPanel, BorderLayout.SOUTH);

		setBorder(BorderFactory.createLoweredBevelBorder());
	}

	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case APPLY: {
			if(dialog == null) break;
			
			TypedElement element = dialog.getElement();
			if(element != null) callback.setElement(element);
			break;
		}
		case CANCEL:	dialog.init(element); break;
		}
	}
	
	public static GridBagConstraints getConstraints(int type) {
		if(labelConstraint == null) initConstraints();
		
		switch(type) {
		case LABEL: return labelConstraint;
		case TEXT: return textConstraint;
		default: return new GridBagConstraints();
		}
	}
	
	private static void initConstraints() {
		labelConstraint = new GridBagConstraints();
		textConstraint = new GridBagConstraints();
		
		labelConstraint.fill = GridBagConstraints.HORIZONTAL;
		labelConstraint.anchor = GridBagConstraints.EAST;
		labelConstraint.insets = new Insets(0, 5, 5, 0);
		labelConstraint.weightx = 0;
		labelConstraint.gridwidth = GridBagConstraints.RELATIVE;
		labelConstraint.gridheight = 1;
		
		textConstraint.fill = GridBagConstraints.HORIZONTAL;
		textConstraint.insets = new Insets(0, 5, 5, 5);
		textConstraint.anchor = GridBagConstraints.WEST;
		textConstraint.weightx = 0.7;
		textConstraint.gridwidth = GridBagConstraints.REMAINDER;
		textConstraint.gridheight = 1;
	}
	
	public void initDialogs(Map knownDialogs) {
		this.knownDialogs = knownDialogs;
					
		List dialogList = new ArrayList();
		Iterator it = knownDialogs.values().iterator();
		while(it.hasNext()) {
			Object o = it.next();
			if(!dialogList.contains(o)) dialogList.add(o);
		}
		
		it = dialogList.iterator();
		while(it.hasNext()) {
			DialogPluggable dialog = (DialogPluggable)it.next();
			dialog.setListShortcut(callback);
			dialog.setListShortcut(new DefaultButtonSetter());
		}
	}

	private class DefaultButtonSetter extends MouseAdapter {
		public void mouseClicked(MouseEvent event) {
			if(getRootPane() != null)
				getRootPane().setDefaultButton(button[APPLY]);
		}
	}
}
