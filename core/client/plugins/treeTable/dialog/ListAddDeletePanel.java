package plugins.treeTable.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ListAddDeletePanel extends JPanel implements ActionListener {
	
	private LabelListModel model = new LabelListModel();
	public ListAddDeletePanel() {
		super();
		initDialog();
	}
	
	public void setRenderer(ListCellRenderer renderer) {
		list.setCellRenderer(renderer);
	}
	
	public LabelListModel getModel() {
		return model;
	} 
	
	public void setTitle(String title) {
		listPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(0, 5, 0, 5)));	
	}
	
	private AddElement activeView;
	public void setView(AddElement view) {
		if(activeView != null) addElementPanel.remove(activeView);
		if(view == null) System.out.println("view is null");
		addElementPanel.add(view);
		addElementPanel.setVisible(false);
	
		view.reset();
		activeView = view;
		callback = null;
	}
	
	private ActionListener callback;
	public void setCallback(ActionListener callback) {
		this.callback = callback;
		activeView.setVisible(false);
	}
	
	private static final int DELETE=0, ADD=1, CANCEL=2, ADD_ELEMENT=3;
	private JList list = new JList(model);
	JPanel addElementPanel = new JPanel(new BorderLayout());
	JPanel listPanel = new JPanel(new BorderLayout());
	private void initDialog() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setTitle("");
		
		list.setVisibleRowCount(4);
		list.setEnabled(true);
		listPanel.add(new JScrollPane(list));
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		String [] buttonNames = {"delete", "add", "cancel", "add"};
		for(int i=0; i<buttonNames.length; i++) {
			JButton b = new JButton(buttonNames[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(this);
			if(i<CANCEL) buttonPanel.add(b);
			else buttonPanel2.add(b);
		}
		listPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		addElementPanel.add(buttonPanel2, BorderLayout.SOUTH);
		addElementPanel.setBorder(BorderFactory.createTitledBorder("add"));
		addElementPanel.setVisible(false);
		
		add(listPanel);
		add(addElementPanel);
	}

	public void setActiveElement(xml.TypedElement e) {
		activeElement = e;
	}
	
	private xml.TypedElement activeElement;
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case DELETE: 
			model.removeAll(java.util.Arrays.asList(list.getSelectedValues()));
			break;
		case ADD: 
			if(callback != null) callback.actionPerformed(event);
			else {
				activeView.reset();
				activeView.init(activeElement);
				addElementPanel.setVisible(true); 
			}
			break;
		case CANCEL: addElementPanel.setVisible(false); break;
		case ADD_ELEMENT: 
			if(activeView != null) model.add(activeView.getElement()); 
			break;
		}
		
	}

	
}
