package plugins.treeTable.dialog;

import javax.swing.JPanel;
import java.awt.event.MouseListener;
import xml.TypedElement;

public interface DialogPluggable {
	public void init(TypedElement e);
	public TypedElement getElement();
	public JPanel getPanel();
	public void setListShortcut(MouseListener listener);
	public int [] handledTypes();
	public boolean hasChanges();
}
