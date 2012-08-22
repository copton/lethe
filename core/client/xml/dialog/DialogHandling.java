package xml.dialog;

import javax.swing.JPanel;

import xml.TypedElement;

public interface DialogHandling {
	public String getTitle();
	public TypedElement getElement();
	public JPanel getPanel();
}
