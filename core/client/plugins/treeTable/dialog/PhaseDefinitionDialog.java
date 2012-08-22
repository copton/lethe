package plugins.treeTable.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.*;

import org.jdom.Element;

import plugins.treeTable.TreeTableModel;
import util.EnumerationWrapper;
import xml.TypedElement;

public class PhaseDefinitionDialog extends AbstractDialog {

	public PhaseDefinitionDialog() {
		super();
		initDialog();
	}
	
	private TypedElement e, graph;
	public void init(TypedElement e) {
		this.e = e;
		graph = (TypedElement)e.getParent().getParent();
		
		initActiveNodes();
		initObservedNodes();
		initEdgePanel();
	}

	public TypedElement getElement() {
		e.removeContent();
		
		getActiveNodes(e);
		
		return e;
	}
	
	private void getActiveNodes(TypedElement parent) {
		Iterator it = activeNodes.keySet().iterator();
		while(it.hasNext()) {
			String nodeName = (String)it.next();
			TypedElement node = new TypedElement("node", TypedElement.NODE);
			node.addContent(new TypedElement("name", nodeName, TypedElement.NAME));
			if(isNodeObserved(nodeName))
				node.addContent(new TypedElement("observed", TypedElement.META_DATA));
			
			node.addContent(getPorts(nodeName));
			parent.addContent(node);
		}
	}
	
	private boolean isNodeObserved(String nodeName) {
		return ((JCheckBox)observedNodes.get(nodeName)).isSelected();
	}
	
	private List getPorts(String nodeName) {
		List ports = new ArrayList();
		if(edgeModel == null) return ports;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)edgeModel.getRoot();
	
		Iterator it = EnumerationWrapper.createList(root.children()).iterator();
		while(it.hasNext()) {
			Object o = it.next();
			Edge edge = (Edge)o;
			ports.addAll(edge.getPorts(nodeName));
		}
		return ports;
	}
	
	private JPanel activeNodesPanel, observedNodesPanel, activeEdgesPanel;
	private void initDialog() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		activeNodesPanel = new JPanel(new BorderLayout());
		observedNodesPanel = new JPanel(new BorderLayout());
		activeEdgesPanel =  new JPanel(new BorderLayout());
		
		activeNodesPanel.setBorder(BorderFactory.createTitledBorder("active nodes"));
		observedNodesPanel.setBorder(BorderFactory.createTitledBorder("observed nodes"));
		activeEdgesPanel.setBorder(BorderFactory.createTitledBorder("active ports"));
		
		JPanel nodesPanel = new JPanel();
		nodesPanel.setLayout(new BoxLayout(nodesPanel, BoxLayout.LINE_AXIS));
		nodesPanel.add(activeNodesPanel);
		nodesPanel.add(observedNodesPanel);
		
		add(activeEdgesPanel);
		add(nodesPanel);
	}
	
	private EdgeModel edgeModel;
	private void initEdgePanel() {
		edgeModel = new EdgeModel(new EdgeNode(e.getParentElement().getParentElement()));
		plugins.treeTable.TreeTable table = new plugins.treeTable.TreeTable(edgeModel);
	
		table.setDefaultEditor(EdgeModel.class, new CellEditor());
		table.setDefaultRenderer(EdgeModel.class, new CellRenderer());
		table.setSelectionBackground(table.getBackground());
		
		activeEdgesPanel.removeAll();
		activeEdgesPanel.add(new JScrollPane(table));
	}
	
	private class EdgeModel extends DefaultTreeModel implements TreeTableModel {
		public EdgeModel(TreeNode root) {
			super(root);
		}
	
		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int pos) {
			return " ";
		}

		public Class getColumnClass(int pos) {
			if(pos == 0) return TreeTableModel.class;
			else return EdgeModel.class;
		}

		public Object getValueAt(Object node, int column) {
			if (node instanceof Writer) return node;
			else if(node instanceof Reader) return node;
			else return null;
		}

		public void setValueAt(Object value, Object node, int column) {
			if(node instanceof Writer) {
				((Writer)node).setSelected((String)value);
			} else if(node instanceof Reader) {
				Reader r = (Reader)node;
				int num = ((Integer)value).intValue();
				r.set((num&1)>0, (num&2)>0);
			}
		}

		public boolean isCellEditable(Object node, int column) {
			if(node instanceof Reader) return true;
			else if(node instanceof Writer) return true;
			else return (column == 0);
		}
	}

	private class EdgeNode extends DefaultMutableTreeNode {
		public EdgeNode(Element graph) {
			super("base");
			Iterator it = graph.getChild("edges").getChildren().iterator();
			while(it.hasNext()) add(new Edge((Element)it.next(), graph));
		}
		
		public List getPorts(String nodeName) {
			List l = new ArrayList();
			Iterator it = EnumerationWrapper.createList(children()).iterator();
			while(it.hasNext()) {
				Edge edge = (Edge)it.next();
				l.addAll(edge.getPorts(nodeName));
			}
			return l;
		}
	}
	
	private class CellRenderer implements TableCellRenderer {
		JLabel label = new JLabel();
		public Component getTableCellRendererComponent(JTable table, Object object, boolean arg2, boolean arg3, int arg4, int arg5) {
			if(object instanceof Writer) label.setText(((Writer)object).getWriter());
			else if(object instanceof Reader) label.setText(((Reader)object).getReader());
			else return null;
			
			return label;
		}
		
	}
	
	private class CellEditor extends AbstractCellEditor implements TableCellEditor {

		private boolean isWriter;
		private JComboBox writerSelection;
		private JCheckBox readerActive=new JCheckBox("active");
		private JCheckBox readerReset=new JCheckBox("reset");
		public Component getTableCellEditorComponent(JTable table, Object object, boolean arg2, int arg3, int arg4) {
			if(object instanceof Writer) {
				isWriter = true;
				
				Writer w = (Writer)object;
				writerSelection = new JComboBox((String[])w.outputPorts.toArray(new String[0]));
				writerSelection.setSelectedItem(w.selected);
				writerSelection.setBorder(BorderFactory.createEmptyBorder());
				return writerSelection;
			} else if(object instanceof Reader) {
				isWriter = false;
				
				Reader r = (Reader)object;
				readerActive.setSelected(r.active);
				readerReset.setSelected(r.reset);
				
				readerActive.setOpaque(false);
				readerReset.setOpaque(false);
				
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
				p.setOpaque(false);
				
				p.add(readerActive);
				p.add(readerReset);
				return p;
			} else return null;
		}

		public Object getCellEditorValue() {
			if(isWriter) return writerSelection.getSelectedItem();
			else {
				int value = 0;
				if(readerActive.isSelected()) value += 1;
				if(readerReset.isSelected()) value += 2;
				
				return new Integer(value);
			}
		}
		
	}
	
	private class Edge extends DefaultMutableTreeNode {
		public Edge(Element edge, Element graph) {
			super(edge.getChildText("name"));
			
			add(new Writer(edge));
			DefaultMutableTreeNode reader = new DefaultMutableTreeNode("reader");
			createReader(edge, reader);
			add(reader);
		}
		
		private List getPorts(String nodeName) {
			List l = new ArrayList();
			if(getChildCount() == 0) return l;
			
			Writer writer = (Writer)getChildAt(0);
			if(writer.getNodeName() == null) return l;
			
			if(writer.getNodeName().equals(nodeName)) {
				TypedElement port = new TypedElement("port", TypedElement.PORT);
				port.addContent(new TypedElement("name", writer.getPortName(), TypedElement.NAME));
				l.add(port);
			}
			Iterator it = EnumerationWrapper.createList(getChildAt(1).children()).iterator();
			while(it.hasNext()) {
				Reader reader = (Reader)it.next();
				if(reader.getNodeName().equals(nodeName)) {
					TypedElement port = new TypedElement("port", TypedElement.PORT);
					port.addContent(new TypedElement("name", reader.getPortName(), TypedElement.NAME));
					if(reader.reset) port.addContent(new TypedElement("reset", TypedElement.META_DATA));
					l.add(port);
				}
			}
			return l;
		}
		
		private void createReader(Element edge, DefaultMutableTreeNode base) {
			Iterator it = edge.getChildren("node").iterator();
			while(it.hasNext()) {
				Element node = (Element)it.next();
				String nodeName = node.getChildText("name");
				
				Iterator it2 = node.getChildren("port").iterator();
				while(it2.hasNext()) {
					Element port = (Element)it2.next();
					if(port.getAttributeValue("direction") == null) 
						System.out.println("huch!: "+port.getName());
					else if(port.getAttributeValue("direction").equals("input"))
						base.add(new Reader(nodeName, port.getText()));
				}
			}
		}
	}
	
	private class Reader extends DefaultMutableTreeNode {
		boolean active, reset;
		private String nodeName, portName;
		public Reader(String node, String port) {
			super(port+" ("+node+")");
		
			nodeName = node;
			portName = port;
			set(true, false);
		}
		
		public String getReader() {
			return (!active?"not ":"")+"active, "+(!reset?"no ":"")+"reset";
		}
		
		public String getNodeName() {
			return nodeName;
		}
		
		public String getPortName() {
			return portName;
		}
		
		public void set(boolean active, boolean reset) {	
			if(active && !this.active) addLock(nodeName);
			else if(!active && this.active) removeLock(nodeName);
			
			JCheckBox cb = (JCheckBox)activeNodes.get(nodeName);
			if(active || cb.isEnabled()) cb.setSelected(active);
			
			this.active = active;
			this.reset = reset;
		}
	}
	
	private class Writer extends DefaultMutableTreeNode {
		private List outputPorts = new ArrayList();
		public Writer(Element edge) {
			super("writer");
		
			setOutputPorts(edge);
			if(outputPorts.size() > 0)
				setSelected(outputPorts.get(0).toString());
			outputPorts.add(null);
		}
		
		String selected;
		
		public String getWriter() {
			return selected;
		}
		
		public String getNodeName() {
			return (String)selected2node.get(selected);
		}
		
		public String getPortName() {
			return (String)selected2port.get(selected);
		}
		
		private Map selected2node = new HashMap();
		private Map selected2port = new HashMap();
		public void setSelected(String newWriter) {
			if(newWriter == selected) return;
			
			String nodeName = (String)selected2node.get(selected);
			if(nodeName != null) {
				JCheckBox cb = (JCheckBox)activeNodes.get(nodeName);
				
				removeLock(nodeName);
				if(cb.isEnabled()) cb.setSelected(false);
			}
		
			selected = newWriter;
			nodeName = (String)selected2node.get(selected);
			if(nodeName != null) {
				JCheckBox cb = (JCheckBox)activeNodes.get(nodeName);
			
				cb.setSelected(true);
				addLock(nodeName);
			}
		}
		
		private void setOutputPorts(Element edge) {
			Iterator it = edge.getChildren("node").iterator();
			while(it.hasNext()) {
				Element node = (Element)it.next();
				String nodeName = node.getChildText("name");
				
				Iterator it2 = node.getChildren("port").iterator();
				while(it2.hasNext()) {
					Element port = (Element)it2.next();
					if(port.getAttributeValue("direction") == null) {
						System.out.println("huch! "+port.getChildText("name"));
					}
					else if(port.getAttributeValue("direction").equals("output")) {
						String text = port.getText() + " ("+nodeName+")";
						selected2node.put(text, nodeName);
						selected2port.put(text, port.getText());
						outputPorts.add(text);
					}
				}
			}
		}
	}
	
	private Map activeNodesLock = new HashMap();
	private void addLock(String name) {
		Integer locks = (Integer)activeNodesLock.get(name);
		if(locks == null) locks = new Integer(0);
		activeNodesLock.put(name, new Integer(locks.intValue()+1));
		((JCheckBox)activeNodes.get(name)).setEnabled(false);
	}
	
	private void removeLock(String name) {
		Integer locks = (Integer)activeNodesLock.get(name);
		
		((JCheckBox)activeNodes.get(name)).
			setEnabled((locks ==  null || locks.intValue() <= 1));
	
		if(locks.intValue()-1 == 0) activeNodesLock.remove(name);
		else activeNodesLock.put(name, new Integer(locks.intValue()-1));
	}
	
	private Map activeNodes = new HashMap();
	private Map observedNodes = new HashMap();
	private NodeItemListener activeNodeListener = new NodeItemListener(false);
	private NodeItemListener observedNodeListener = new NodeItemListener(true);
	
	private void initActiveNodes() {
		List availableNodes = getNodeNames();
		List activeNodeList = getNodes(false);
		
		activeNodes.clear();
		activeNodesPanel.removeAll();
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		
		Iterator it = availableNodes.iterator();
		int i=0;
		while(it.hasNext()) {
			String nodeName = (String)it.next();
			
			JCheckBox activeNode = new JCheckBox(nodeName);
			activeNode.setSelected(activeNodeList.contains(nodeName));
			activeNode.addItemListener(activeNodeListener);
			activeNodes.put(nodeName, activeNode);
			buttonPanel.add(activeNode);
			i++;
		}
		
		activeNodesPanel.add(buttonPanel);
	}
	
	private List getNodes(boolean observedOnly) {
		List nodes = new ArrayList();
		
		Iterator it = e.getChildren().iterator();
		while(it.hasNext()) {
			TypedElement node = (TypedElement)it.next();
			if(observedOnly && node.getChild("observed") == null) continue;
			nodes.add(node.getChildText("name"));
		}
		
		return nodes;
	}
	
	private List getNodeNames() {
		List names = new java.util.ArrayList();
		
		Iterator it = graph.getChild("nodes").getChildren().iterator();
		while(it.hasNext()) {
			TypedElement node = (TypedElement)it.next();
			names.add(node.getChildText("name"));
		}
		
		return names;
	}
	private void initObservedNodes() {
		List availableNodes = getNodeNames();
		List observedNodeList = getNodes(true);
		
		observedNodes.clear();
		observedNodesPanel.removeAll();
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		
		Iterator it = availableNodes.iterator();
		int i=0;
		while(it.hasNext()) {
			String nodeName = (String)it.next();
			
			JCheckBox observedNode = new JCheckBox(nodeName);
			observedNode.setSelected(observedNodeList.contains(nodeName));
			observedNode.addItemListener(observedNodeListener);
			observedNodes.put(nodeName, observedNode);
			buttonPanel.add(observedNode);
			i++;
		}
		
		observedNodesPanel.add(buttonPanel);
	}
	
	// ???
	private class NodeItemListener implements ItemListener {

		private boolean isResetListener;
		public NodeItemListener(boolean isResetListener) {
			super();
			this.isResetListener = isResetListener;
		}
		
		private boolean internHandling = false;
		public synchronized void itemStateChanged(ItemEvent event) {
			if(internHandling) return;
			
			JCheckBox source = (JCheckBox)event.getSource();
			if(isResetListener && source.isSelected()) {
				internHandling = true;
				((JCheckBox)activeNodes.get(source.getText())).setSelected(true);
			} else if(!isResetListener && !source.isSelected()) {
				internHandling = true;
				((JCheckBox)observedNodes.get(source.getText())).setSelected(false);
			}
			internHandling = false;
		}
	}
	
	private static int [] handledTypes = {TypedElement.PHASE};
	public int[] handledTypes() {
		return handledTypes;
	}

}
