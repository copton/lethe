package plugins.authentication;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class LoginWindow extends JDialog implements ActionListener {
	
	public LoginWindow(JFrame mainFrame) {
		super(mainFrame, true); // modal dialog
		this.setTitle("authentication");
		initDialog();
	}
	
	private JTextField user = new JTextField(20);
	public String getUsername() {
		return user.getText();
	}
	
	private JTextField passwd =  new JTextField(20);
	public String getPassword() {
		return passwd.getText();
	}
	
	private boolean login;
	public boolean logIn() {
		return login;
	}
	
	private JCheckBox autoLogin = new JCheckBox("automatically log in");
	public boolean autoLogin() {
		return autoLogin.isSelected();
	}
	
	private void initDialog() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
		labelPanel.add(getLabelPanel("user:"));
		labelPanel.add(getLabelPanel("password:"));
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		textPanel.add(getTextPanel(user));
		textPanel.add(getTextPanel(passwd));
		
		fieldPanel.add(labelPanel);
		fieldPanel.add(textPanel);
		
		panel.add(fieldPanel);
		panel.add(autoLogin, BorderLayout.SOUTH);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.add(panel);
		mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		
		getContentPane().add(mainPanel);
		
		pack();
		setResizable(false);
	}
	
	private JPanel getLabelPanel(String label) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(new JLabel(label));
		return panel;
	}
	
	private JPanel getTextPanel(JTextField text) {
		JPanel panel = new JPanel();
		panel.add(text);
		
		return panel;
	}
	
	private final static int CANCEL=0, OK=1;
	private JPanel getButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		String [] labels = {"Cancel", "Ok"};
		for(int i=0; i<labels.length; i++) {
			JButton b = new JButton(labels[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(this);
			panel.add(b);
		}
		
		return panel;
	}

	public void actionPerformed(ActionEvent event) {
		int cmd = Integer.parseInt(event.getActionCommand());
		
		switch(cmd) {
		case CANCEL: login = false; break;
		case OK: login = true; break;
		}
		hide();
	}
}
