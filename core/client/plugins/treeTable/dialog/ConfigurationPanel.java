package plugins.treeTable.dialog;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import plugins.treeTable.*;
import xml.TypedElement;
import xml.XmlHelper;

public class ConfigurationPanel extends JPanel {
	
	private TreeTable table;
	public ConfigurationPanel() {
		super(new BorderLayout());
		initPanel();
	}
	
	public ConfigurationModel getModel() {
		return model;
	}

	
	private JPanel tablePanel;
	private TreeRenderer renderer = new TreeRenderer();
	private TypedElement diff=null;
	
	
	public void init(TypedElement original, TypedElement settings) {
		
		diff = (TypedElement)original.clone();
		if(settings == null)
			settings = (TypedElement)original.clone();
		else {
			Iterator it = original.getChildren().iterator();
			int pos=0;
			while(it.hasNext()) {
				TypedElement child = (TypedElement)it.next();
				TypedElement settingsChild = (TypedElement)settings.getChild(child.getName());
		
				if(settingsChild == null) 
					settings.addContent(pos, (TypedElement)child.clone());
				pos++;
			}
		}
		
		init(settings);
	}
	
	public void init(TypedElement e) {
		model = new ConfigurationModel(new ElementWrapper(e));
		table = new TreeTable(model);
		table.setSelectionBackground(table.getBackground());
		table.setTreeRenderer(renderer);
		table.setDefaultRenderer(TypedElement.class, new TypedElementRenderer());
		table.setDefaultEditor(TypedElement.class, new TypedElementEditor());
		
		
		tablePanel.removeAll();
		tablePanel.add(new JScrollPane(table));
	}
	
	public TypedElement getElement() {
		return ((ElementWrapper)model.getRoot()).getElement();
	}
	
	public TypedElement getDiffElement() {
		TypedElement newElement = (TypedElement)getElement().clone();
		if(diff == null) return newElement;
		
		Iterator it = newElement.getChildren().iterator();
	
		while(it.hasNext()) {
			TypedElement child = (TypedElement)it.next();
			String childName = child.getName();
			if(child.isEqual(diff.getChild(childName))) 
				it.remove();
		}
		
		if(newElement.getChildren().size() == 0) return null;
		else return newElement;
	}
	
	private ConfigurationModel model;
	private void initPanel() {
		tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		add(tablePanel);
	}
	
	
	private class ConfigurationModel extends DefaultTreeModel implements TreeTableModel {

		public ConfigurationModel(TreeNode root) {
			super(root);
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		private String [] columnNames = {"name", "value"};
		public String getColumnName(int pos) {
			return columnNames[pos];
		}

		public Class getColumnClass(int col) {
			if(col == 0) return TreeTableModel.class;
			else return TypedElement.class;
		}

		public Object getValueAt(Object node, int column) {
			return ((ElementWrapper)node).getElement();
		}

		public void setValueAt(Object value, Object node, int column) {
			TypedElement e = ((ElementWrapper)node).getElement();
			
			switch(column) {
			case 1: e.setText((String)value); 
				System.out.println("set value to: "+value); break;
			}
		}

		public boolean isCellEditable(Object node, int column) {
			ElementWrapper obj = (ElementWrapper)node;
			if(obj.getElement().getType() == TypedElement.ROW) return true;
			return column==0?!obj.isLeaf():obj.isLeaf();
		}
	}
	
	private class ElementWrapper extends XmlTreeNode {

		public ElementWrapper(TypedElement e) {
			super(e);
		}
		
		public void createChildren(TypedElement e) {
			Iterator it = e.getChildren().iterator();
			while(it.hasNext())
				add(new ElementWrapper((TypedElement)it.next()));
		}

		public void update(TypedElement e) {
			removeAllChildren();
			createChildren(e);
		}
		
		public TypedElement prepareForSaving(String fileName) {
			return ((ElementWrapper)getRoot()).getElement();
		}	
	}
	
	public class TreeRenderer implements TreeCellRenderer {
		private ImageIcon [] imageIcons = XmlHelper.elementIcons;
		
		private JLabel label = new JLabel();
		public TreeRenderer() {
			super();
			setOpaque(false);
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			ElementWrapper wrapper = (ElementWrapper)value;
			TypedElement e = wrapper.getElement();
			
			int type;
			switch(e.getType()) {
			case TypedElement.ROW: type = TypedElement.getTypeForString(e.getName()); break;
			default: type = TypedElement.getTypeForString(e.getAttributeValue("type"));
			}
			
			label.setText(e.getName());
			label.setIcon(type>=0?(ImageIcon)imageIcons[type]:null);
			label.setToolTipText(wrapper.getToolTip());
			
			switch(e.getType()) {
			case TypedElement.ROW: {
				JButton addButton = new JButton("add");
				addButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						System.out.println("add row");
					}
				});
				JPanel panel = new JPanel(new BorderLayout());
				panel.setOpaque(false);
				panel.add(label);
				panel.add(addButton, BorderLayout.EAST);
				panel.setToolTipText(wrapper.getToolTip());
				return panel;
			}
			default: return label;
			}
		}
	}
	
	public class TypedElementRenderer implements TableCellRenderer {

		private JLabel label = new JLabel();
		public Component getTableCellRendererComponent(JTable table, Object value, boolean arg2, boolean arg3, int arg4, int arg5) {
			TypedElement e = (TypedElement)value;
	
			label.setText(e.getText());
			label.setToolTipText(e.getAttributeValue("description"));
			return label;
		}
		
	}
	
	public class TypedElementEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {

		private JTextField label = new JTextField();
		private JComboBox comboBox;
		private final String [] trueFalse = {"true", "false"};	
		public TypedElementEditor() {
			super();
			label.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		}
		
		private int type;
		public Component getTableCellEditorComponent(JTable table, Object value, boolean arg2, int arg3, int arg4) {
			TypedElement e = (TypedElement)value;
			comboBox = null;
			type = TypedElement.getTypeForString(e.getAttributeValue("type"));
			switch(type) {
			case TypedElement.ROW: {
				System.err.println("edit row");
				label.setText(e.getName());
				JButton addButton = new JButton("add");
				addButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						System.out.println("add row");
					}
				});
				JPanel panel = new JPanel(new BorderLayout());
				panel.setOpaque(false);
				panel.add(label);
				panel.add(addButton, BorderLayout.EAST);
				return panel;
			}
			case TypedElement.BOOLEAN:
				comboBox = new JComboBox(trueFalse);
				
				comboBox.setSelectedItem(e.getText());
				comboBox.setToolTipText(e.getAttributeValue("description"));
				break;
			case TypedElement.ENUM:
			case TypedElement.ENUM_INSTANCE: {
				String [] types = e.getAttributeValue("elements").split(" ");
				comboBox = new JComboBox(types);
				comboBox.setSelectedItem(e.getText());
				break;
			}
			default:
				label.setText(e.getText());
				label.setToolTipText(e.getAttributeValue("description"));
				return label;
			}
			
			if(comboBox != null) { 
				comboBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent event) {
						TypedElementEditor.this.fireEditingStopped();
					}
				});
				return comboBox;
			} else return null;
		}
		
		public Object getCellEditorValue() {
			switch(type) {
			case TypedElement.BOOLEAN: 
			case TypedElement.ENUM:
			case TypedElement.ENUM_INSTANCE:
				return comboBox.getSelectedItem();
			default: return label.getText();
			}
		}	
	}
}
