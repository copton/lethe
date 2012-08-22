package plugins.plugins;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.*;

import java.util.*;

public class RemoveDialog extends JDialog implements java.awt.event.ActionListener{
	JList pluginList;
	List selectedItems;
	
	public RemoveDialog(List loadedPlugins) {
		super((JFrame)null, true);
		selectedItems = new ArrayList();
		setResizable(false);
		initDialog(loadedPlugins);
	}
	
	public List showDialog() {
		pack();
		setVisible(true);
		return selectedItems;
	}
	
	private void initDialog(List loadedPlugins) {
		GridBagLayout gl = new GridBagLayout();
		getContentPane().setLayout(gl);
		GridBagConstraints cList = new GridBagConstraints();
		cList.gridwidth = GridBagConstraints.REMAINDER;
		cList.gridheight = GridBagConstraints.RELATIVE;
		cList.fill = GridBagConstraints.BOTH;
		cList.insets = new java.awt.Insets(10, 10, 0, 10);
		
		pluginList = new JList(loadedPlugins.toArray());
		pluginList.setBorder(BorderFactory.createLoweredBevelBorder());
		gl.setConstraints(pluginList, cList);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(pluginList);
		
		String [] buttonLabels = {"Abbrechen", "Entfernen"};
		for(int i=0; i<buttonLabels.length; i++) {
			JButton button = new JButton(buttonLabels[i]);
			button.setActionCommand(Integer.toString(i));
			button.addActionListener(this);
			buttonPanel.add(button);
		}
		GridBagConstraints cButton = new GridBagConstraints();
		cButton.gridwidth = GridBagConstraints.REMAINDER;
		cButton.gridheight = GridBagConstraints.REMAINDER;
		cButton.fill = GridBagConstraints.HORIZONTAL;
		
		gl.setConstraints(buttonPanel, cButton);
		getContentPane().add(buttonPanel);
	}

	protected static final int CANCEL=0, OK=1;
	public void actionPerformed(ActionEvent event) {
		int action  = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case OK: selectedItems = Arrays.asList(pluginList.getSelectedValues());
		}
		setVisible(false);
	}
}
