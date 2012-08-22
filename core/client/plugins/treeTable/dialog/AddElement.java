package plugins.treeTable.dialog;

import xml.TypedElement;
import javax.swing.JPanel;

public abstract class AddElement extends JPanel {
	abstract public void reset();
	public void init(TypedElement e) {}
	abstract public TypedElement getElement();
}
