package plugins.treeTable.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import xml.TypedElement;

public class EdgeNodeListDialog extends AbstractDialog implements ActionListener {

	public EdgeNodeListDialog() {
		super();
		initDialog();
	}
	
	private TypedElement e;
	public void init(TypedElement e) {
		this.e = e;
		LabelListModel model = list.getModel();
		model.set(e.getChildren());
		
		list.setView((AddElement)views.get(new Integer(e.getType())));
		
		switch(e.getType()) {
		case TypedElement.NODES: list.setTitle("vertices"); break;
		case TypedElement.EDGES: list.setTitle("edges"); break;
		case TypedElement.PHASES: 
			list.setTitle("phases"); 
			list.setCallback(this);
			break;
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

	private static final int [] handledTypes = {TypedElement.NODES, 
		TypedElement.EDGES, TypedElement.PHASES};
	public int[] handledTypes() {
		return handledTypes;
	}

	private static final Map views = initViews();
	private static Map initViews() {
		Map m = new java.util.HashMap();
		m.put(new Integer(TypedElement.NODES), new AddNodePanel());
		m.put(new Integer(TypedElement.EDGES), new AddEdgePanel());
		m.put(new Integer(TypedElement.PHASES), new AddNodePanel());
	
		return m;
	}
	
	private class ListRenderer extends JLabel implements ListCellRenderer {

		public ListRenderer() {
			super();
			setOpaque(true);
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int pos, boolean isSelected, boolean arg4) {
			TypedElement e = (TypedElement)value;
			if(isSelected)
				setBackground(list.getSelectionBackground());
			else setBackground(list.getBackground());
			
			switch(e.getType()) {
			case TypedElement.PHASE: {
				setText("Phase "+(pos+1)); 
				e.setAttribute("nr", Integer.toString(pos+1));
				break;
			}
			default: setText(e.getChildText("name"));
			}
			return this;
		}		
	}

	public void actionPerformed(ActionEvent e) {
		TypedElement newPhase = new TypedElement("phase", TypedElement.PHASE);
		list.getModel().add(newPhase);
	}	
}
