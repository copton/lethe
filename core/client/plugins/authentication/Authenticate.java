package plugins.authentication;

import javax.swing.JPanel;

import org.jdom.Element;

import util.IceNetworkConnection;

import client.Authentication;
import gui.LetheController;
import gui.Personalizable;
import gui.Pluggable;

public class Authenticate implements Pluggable, Personalizable {

	private boolean autoLogin;
	public void init() {
		LetheController.getController().registerForSettings(this, settingsName);
		login();
	}
	
	private void login() {
        String user=null, password=null;
        
        Ice.Properties properties = IceNetworkConnection.getProperties();
        if(autoLogin) {
     		user = properties.getProperty("config.Authentication.user");
     		password = properties.getProperty("config.Authentication.passwd");
        }
        
        	if(user == null || user.length() == 0 || password == null) {
        		LoginWindow login = new LoginWindow(LetheController.getMainWindow());
        		login.show();
        		if(!login.logIn()) return;
        		
        		autoLogin = login.autoLogin();
        		
        		user = login.getUsername();
        		password = login.getPassword();
        		
        		if(autoLogin) {
        			java.util.Map authenticationProperties = new java.util.HashMap();
        			authenticationProperties.put("config.Authentication.user", user);
        			authenticationProperties.put("config.Authentication.passwd", password);
            			
        			IceNetworkConnection.saveProperties(authenticationProperties);
        		}
        	}
        
        boolean ok = (new Authentication()).authenticate(user, password);
        System.out.println("logged in? "+(ok?"yes":"no"));
	}

	public void release() {
		(new Authentication()).logout();
	}

	public JPanel show() {
		return null;
	}

	public void hide() {}

	private static final String settingsName = "Authentication";
	public void setSettings(Element e) {
		if(e == null) e = getDefaultSettings();
		String text = e.getChildText("autologin");
		autoLogin = text.equals("true");
	}

	public Element getSettings() {
		Element root = new Element(settingsName);
		
		Element autologin = new Element("autologin");
		autologin.setText(Boolean.toString(autoLogin));
		root.addContent(autologin);
		
		return root;
	}

	public Element getDefaultSettings() {
		Element root = new Element(settingsName);
		
		Element autologin = new Element("autologin");
		autologin.setText(Boolean.toString(false));
		root.addContent(autologin);
		
		return root;
	}
}
