package plugins.treeTable.dialog;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import xml.TypedElement;

public class DirectoryContentDialog extends AbstractDialog {
	
	public DirectoryContentDialog() {
		initDialog();
	}
	
	public void setListShortcut(MouseListener listener) {
	//	usedObjectList.addMouseListener(listener);
	}
	
	private TypedElement e;
	public void init(TypedElement e) {
		this.e = e;
		LabelListModel model = list.getModel();
		model.set(e.getChildren());
		
		list.setActiveElement(e);
		list.setView((AddElement)views.get(new Integer(e.getType())));
		
		switch(e.getType()) {
		case TypedElement.DEFINE_DIR: list.setTitle("defined objects"); break;
		case TypedElement.CONFIGURATION_DIR: case TypedElement.SETTINGS_DIR:
		case TypedElement.RESULTS_DIR: case TypedElement.SERIALIZE_DIR:
			list.setTitle("elements"); break;
		case TypedElement.INPUT_PORT_DIR: list.setTitle("input ports"); break;
		case TypedElement.OUTPUT_PORT_DIR: list.setTitle("output ports"); break;
		
		}
	}

	public TypedElement getElement() {
		e.removeContent();
		
		LabelListModel model = list.getModel();
		e.addContent(model.getAll());
		return e;
	}
	
	private ListAddDeletePanel list = new ListAddDeletePanel();
	private void initDialog() {
		setLayout(new java.awt.BorderLayout());
		list.setRenderer(new ListRenderer());
		add(list);
	}

	private static final int [] handledTypes = {TypedElement.DEFINE_DIR, 
		TypedElement.CONFIGURATION_DIR, TypedElement.SETTINGS_DIR,
		TypedElement.RESULTS_DIR, TypedElement.SERIALIZE_DIR,
		TypedElement.INPUT_PORT_DIR, TypedElement.OUTPUT_PORT_DIR
	};
	
	public int[] handledTypes() {
		return handledTypes;
	}

	private static final Map views = initViews();
	private static Map initViews() {
		Map m = new java.util.HashMap();
		m.put(new Integer(TypedElement.DEFINE_DIR), new AddClassPanel());
		m.put(new Integer(TypedElement.CONFIGURATION_DIR), new AddInstancePanel());
		m.put(new Integer(TypedElement.SETTINGS_DIR), new AddInstancePanel());
		m.put(new Integer(TypedElement.SERIALIZE_DIR), new AddInstancePanel());
		m.put(new Integer(TypedElement.RESULTS_DIR), new AddInstancePanel());
		m.put(new Integer(TypedElement.INPUT_PORT_DIR), new AddPortPanel());
		m.put(new Integer(TypedElement.OUTPUT_PORT_DIR), new AddPortPanel());
		
		return m;
	}
	
	private class ListRenderer extends JLabel implements ListCellRenderer {
		private ImageIcon [] imageIcons = xml.XmlHelper.elementIcons;
			
		public ListRenderer() {
			super();
			setOpaque(true);
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int pos, boolean isSelected, boolean arg4) {
			TypedElement e = (TypedElement)value;
			if(isSelected)
				setBackground(list.getSelectionBackground());
			else setBackground(list.getBackground());
			
			setText(e.getChildText("name"));
			setIcon(imageIcons[e.getType()]);
			
			return this;
		}		
	}
}
