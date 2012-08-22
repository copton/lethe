package plugins.userManagement;
 
import util.IceNetworkConnection;
import java.awt.BorderLayout;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import Ice.Properties;
import gui.*;
import java.awt.event.ActionEvent;

public class UserManagement implements Pluggable, ToolbarUsable, java.awt.event.ActionListener {
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private ManagementTableHandler userTable;
	private List buttonList = new ArrayList();
	
	private static final int ADD=0, EDIT=1, DELETE=2;
	public void init() {
		userTable = new ManagementTableHandler();
		
		JScrollPane sp = new JScrollPane(userTable.getTable());
		mainPanel.add(sp);
	
		String [] buttonHeader = {"hinzufügen", "editieren", "löschen"};
		for(int i=0; i<buttonHeader.length; i++) {
			JButton b = new JButton(buttonHeader[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(this);
			buttonList.add(b);
		}
		
		LetheController.getController().registerForToolbar(this);
	}

	public JPanel show() {
		return mainPanel;
	}

	public void hide() {}
	public void release() {}

	public JButton getShowButton() {
		Properties properties = IceNetworkConnection.getProperties();
		String iconName = properties.getProperty("gui.iconDir")+"User.png";
		JButton button = new JButton(new javax.swing.ImageIcon(iconName));
		return button;
	}

	public List addToToolbar() {
		return buttonList;
	}

	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case DELETE: 
			userTable.deleteSelected();
			break;
		case ADD: 
			userTable.addData(new UserData("phia", "test2", "read", "write"));
			break;
		case EDIT: break;
		}
	}
}
