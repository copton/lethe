package plugins.treeTable.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.*;

import org.jdom.Element;

import plugins.treeTable.EditorDialog;

import util.DialogHelper;
import xml.TypedElement;
import xml.XmlHelper;

public class EdgeCreationDialog extends AbstractDialog {

	public EdgeCreationDialog() {
		super();
		initDialog();
	}
	
	private TypedElement e;
	public void init(TypedElement e) {
		this.e = e;
		availableNodes = initNodes();
		
		// set type-checkbox
		setTypeCheckbox();
		
		// set connected vertices
		model.set(e.getChildren("node"));
		nodeList.setDefaultEditor(String.class,
				new NodeTableEditor());
		
		name.setText(e.getChildText("name"));
		description.setText(e.getChildText("description"));
	
		// set base-type
		Element node = e.getChild("node");
		baseType = null;
		if(node != null) {
			Module m = (Module)availableNodes.get(node.getChildText("name"));
			baseType = m.getPort(node.getChildText("port")).getChildText("type");
		
			if(baseType != null) {
				types.setSelectedItem(baseType);
				types.setEnabled(false);
			} else types.setEnabled(true);
		} else types.setEnabled(true);	
	}

	private void setTypeCheckbox() {
		Set availableTypes = new HashSet();
		Iterator it = availableNodes.values().iterator();
		while(it.hasNext()) {
			Module m = (Module)it.next();
			availableTypes.addAll(m.availableTypes);
		}
		
		types.setModel(new DefaultComboBoxModel((String[])availableTypes.toArray(new String[0])));
		if(availableTypes.size() > 0)
			types.setSelectedIndex(0);
	}	
	
	public TypedElement getElement() {
		e.removeContent();
		setElementValue("name", name.getText(), TypedElement.NAME, e);
		setElementValue("description", description.getText(), TypedElement.DESCRIPTION, e);
		e.addContent(model.getAll());
		
		return e;
	}
	
	private Map initNodes() {
		Element root = e.getDocument().getRootElement();
		Element nodes = root.getChild("graph").getChild("nodes");

		Map l = new HashMap();
		Iterator it = nodes.getChildren().iterator();
		while(it.hasNext()) {
			Element node = (Element)it.next();
			l.put(node.getChildText("name"), new Module(node.getChildText("name"), 
					node.getChildText("module")));
		}
		
		return l;
	}
	
	private void initDialog() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder("edge"));
		
		JPanel nodePanel = new JPanel();
		nodePanel.setBorder(BorderFactory.createTitledBorder("connected vertices"));
		
		createEdgePanel(mainPanel);
		createNodeList(nodePanel);
		
		add(mainPanel);
		add(nodePanel);
	}
	
	private JTextField name = new JTextField();
	private JTextField description = new JTextField();
	private JComboBox types = new JComboBox();
	private JTextField minSize = new JTextField();
	private void createEdgePanel(JPanel panel) {
		GridBagLayout gl = new GridBagLayout();
		panel.setLayout(gl);
		
		GridBagConstraints cLabel = EditorDialog.getConstraints(EditorDialog.LABEL);
		GridBagConstraints cText = EditorDialog.getConstraints(EditorDialog.TEXT);
		
		String [] labelNames = {"name:", "description:", "type:", "min size:"};
		JLabel [] labels = new JLabel[labelNames.length];
		for(int i=0; i<labels.length; i++) {
			labels[i] = new JLabel(labelNames[i]);
			gl.setConstraints(labels[i], cLabel);
		}
		labels[0].setFont(labels[0].getFont().deriveFont(java.awt.Font.BOLD));
		
		gl.setConstraints(name, cText);
		gl.setConstraints(description, cText);
		gl.setConstraints(types, cText);
		gl.setConstraints(minSize, cText);
		
		panel.add(labels[0]); panel.add(name);
		panel.add(labels[1]); panel.add(description);
		panel.add(labels[2]); panel.add(types);
		panel.add(labels[3]); panel.add(minSize);
	}
	
	private NodeTableModel model = new NodeTableModel();
	private JTable nodeList = new JTable(model) {
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}
	};
	private void createNodeList(JPanel panel) {
		panel.setLayout(new BorderLayout());
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		JScrollPane scrollPane = new JScrollPane(nodeList);
	
		listPanel.add(scrollPane);
		panel.add(listPanel);
		
		nodeList.setDefaultRenderer(String.class, new NodeTableRenderer());
		nodeList.setIntercellSpacing(new Dimension(0, 0));
	
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		String [] buttons = {"delete", "add"};
		for(int i=0; i<buttons.length; i++) {
			JButton b = new JButton(buttons[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(new ActionListener() {
				private static final int DELETE=0, ADD=1;
				public void actionPerformed(ActionEvent event) {
					int action = Integer.parseInt(event.getActionCommand());
					switch(action) {
					case DELETE: 
						model.removeAll(nodeList.getSelectedRows());
						break;
					case ADD: 
						List nodes = getAvailableNodes();
						if(nodes.size() == 0) return;
						
						Module m = (Module)availableNodes.get(nodes.get(0));
						Element filteredPort = (Element)m.getFilteredPorts(baseType).get(0);
						
						TypedElement e = new TypedElement("node", TypedElement.NODE);
						e.addContent(new TypedElement("name", m.moduleName, TypedElement.NAME));
						e.addContent(new TypedElement("port", filteredPort.getChildText("name"), TypedElement.PORT));
						
						model.add(e);
						break;
					}
				}
				
			});
			buttonPanel.add(b);
		}
		
		panel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private final static int [] handledTypes = {TypedElement.EDGE};
	public int[] handledTypes() {
		return handledTypes;
	}

	private Map availableNodes;
	private class NodeTableModel extends AbstractTableModel {
		private java.util.List nodes = new java.util.ArrayList();
		
		public void add(TypedElement node) {
			nodes.add(node);
			this.fireTableRowsInserted(nodes.size()-1, nodes.size());
		}
		
		public void set(java.util.List l) {
			nodes = new java.util.ArrayList(l);
			fireTableDataChanged();
		}
		
		public java.util.List getAll() {
			return nodes;
		}
		
		public void removeAll(int [] indices) {
			for(int i=indices.length-1; i>=0;i--)
				nodes.remove(indices[i]);
			fireTableRowsDeleted(indices[0], 
					Math.min(indices[indices.length-1], nodes.size()));
			
			if(nodes.isEmpty()) baseType = null;
		}
		
		public int getRowCount() {
			return nodes.size();
		}

		public int getColumnCount() {
			return columns.length;
		}
		
		private String [] columns = {"vertex", "port"};
		public String getColumnName(int col) {
			return columns[col];
		}
		
		public Class getColumnClass(int col) {
			return String.class;
		}
		
	    public boolean isCellEditable(int row, int col) { 
	    		return true; 
	    	}

		public Object getValueAt(int row, int col) {
			TypedElement e = (TypedElement)nodes.get(row);
			return e.getChildText(col==0?"name":"port");
		}
		
		public void setValueAt(Object value, int row, int col) {
			TypedElement e = (TypedElement)nodes.get(row);
			if(col == 0) e.getChild("name").setText((String)value);
			else {
				Element port = (Element)value;
				e.getChild("port").setText(port.getChildText("name"));
				e.getChild("port").setAttribute("direction", port.getParentElement().getName());
			}
			
			nodes.set(row, e);
		}
	}
	
	private class NodeTableRenderer extends JLabel implements TableCellRenderer {
		
		public NodeTableRenderer() {
			super();
			setOpaque(true);
		}
		
		public String selectedModule;
		public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean arg3, int row, int col) {
			if(col == 0) selectedModule = (String)value;
		
			if(isSelected) setBackground(table.getSelectionBackground());
			else setBackground(table.getBackground());
	
			setText((String)value);
			// use selectedModule for icons
			return this;
		}	
	}
	
	private String baseType = null;
	private class NodeTableEditor extends AbstractCellEditor implements TableCellEditor {
		public NodeTableEditor() {
			super();
			nodeBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					NodeTableEditor.this.fireEditingStopped();
				}
			});
			
			portBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					NodeTableEditor.this.fireEditingStopped();
				}
			});
			
			portBox.setRenderer(new PortComboBoxRenderer());
		}
		
		
		public boolean isCellEditable(java.util.EventObject e) {
			if(e instanceof MouseEvent) {
				MouseEvent event = (MouseEvent)e;
				return (event.getClickCount() >= 2);
			} 
			return false;
		}
		
		StringComboBoxModel nodeList = new StringComboBoxModel();
		private JComboBox nodeBox = new JComboBox(nodeList);
		private JComboBox portBox = new JComboBox();
		private int colNum;
		String activeNode;
		public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean arg2, int row, int col) {
			colNum = col;
			if(col == 0) {
				
				nodeList.set(getAvailableNodes());
				nodeBox.setSelectedItem(value);
				return nodeBox;
			
			} else {
				Module module = (Module)availableNodes.get(activeNode);
				portBox.setModel(new PortComboBoxModel(module.getFilteredPorts(baseType)));
				portBox.setSelectedIndex(0);
				return portBox;
			}
		}

		public Object getCellEditorValue() {
			if(colNum == 0) {
				activeNode = (String)nodeBox.getSelectedItem();
				return activeNode;
			} else {
				Element port = (Element)portBox.getSelectedItem();
				if(baseType == null) baseType = port.getChildText("type");
				return port;
			}
		}
	}
	
	private java.util.List getAvailableNodes() {
		java.util.List l = new java.util.ArrayList();
		if(baseType == null) {
			l.addAll(availableNodes.keySet());
			return l;
		}
		
		Iterator it = availableNodes.values().iterator();
		while(it.hasNext()) {
			Module m = (Module)it.next();
			if(m.availableTypes.contains(baseType)) l.add(m.moduleName);
		}
		return l;
	}
	
	private class PortComboBoxModel extends AbstractListModel implements ComboBoxModel {

		List ports;
		public PortComboBoxModel(List ports) {
			this.ports = ports;
		}
		
		Object selectedItem;
		public void setSelectedItem(Object item) {
			selectedItem = item;
		}

		public Object getSelectedItem() {
			return selectedItem;
		}

		public int getSize() {
			return ports.size();
		}

		public Object getElementAt(int pos) {
			return ports.get(pos);
		}	
	}
	
	private class PortComboBoxRenderer implements ListCellRenderer {

		private JLabel label = new JLabel();
		private ImageIcon inputIcon, outputIcon;
		
		public PortComboBoxRenderer() {
			inputIcon = XmlHelper.elementIcons[TypedElement.INPUT_PORT_DIR];
			outputIcon = XmlHelper.elementIcons[TypedElement.OUTPUT_PORT_DIR];
		}
		
		public Component getListCellRendererComponent(JList list, Object obj, int arg2, boolean arg3, boolean arg4) {
			Element e = (Element)obj;
			
			label.setText(e.getChildText("name"));
			if(e.getParentElement().getName().equals("input"))
				label.setIcon(inputIcon);
			else label.setIcon(outputIcon);
			return label;
		}
		
	}
	
	private class Module {
		String moduleName;
		String moduleType;
		List ports = new ArrayList();
		Set availableTypes;
	
		public Module(String name, String type) {
			System.out.println("type="+type);
			this.moduleName = name;
			this.moduleType = type;
			readPorts();
			
			availableTypes = new HashSet();
			Iterator it = ports.iterator();
			while(it.hasNext()) {
				Element e = (Element)it.next();
				availableTypes.add(e.getChildText("type"));
			}
		}
		
		private void readPorts() {
			Element e = DialogHelper.getModule(moduleType);
			
			Element input = e.getChild("ports").getChild("input");
			if(input != null) ports.addAll(input.getChildren());
			
			Element output = e.getChild("ports").getChild("output");
			if(output != null) ports.addAll(output.getChildren());
		}
		
		public Element getPort(String name) {
			Iterator it = ports.iterator();
			while(it.hasNext()) {
				Element port = (Element)it.next();
				if(port.getChildText("name").equals(name)) 
					return port;
			}
		
			// shouldn't happen, since every listed module should have ports!
			return null;
		}
		
		public List getFilteredPorts(String type) {
			if(type == null) return ports;
			
			List filtered = new ArrayList();
			
			Iterator it = ports.iterator();
			while(it.hasNext()) {
				Element port = (Element)it.next();
				if(port.getChildText("type").equals(type))
					filtered.add(port);
			}
			return filtered;
		}
	}
}
