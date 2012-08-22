package gui;

import org.jdom.Element;

public interface Personalizable {
	public void setSettings(Element e);
	public Element getSettings();
	Element getDefaultSettings();
}
