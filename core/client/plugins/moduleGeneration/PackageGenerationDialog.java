package plugins.moduleGeneration;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import client.PackageGenerator;

public class PackageGenerationDialog extends JDialog implements ActionListener {
	
	private int status = 0;
	public PackageGenerationDialog(JFrame frame) {
		super(frame, true); // modal dialog
		initDialog();
	}
	
	public int getGenerationCode() {
		return status;
	}
	
	private static final int CANCEL=0, OK=1;
	private static final int ALL=0, SELECTED=1;
	private final static int ICE=0, CPP=1, SCHEMA=2, DEFAULT=3, 
			XML=4, CONFIG=5, ANZ_TYPES=6; 	

	private JCheckBox [] checkBoxes = new JCheckBox[ANZ_TYPES];
	private JRadioButton [] radioButtons = new JRadioButton[2];
	private void initDialog() {
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.PAGE_AXIS));
	
		selectionPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 0, 5),
				BorderFactory.createTitledBorder("generatiing")
			));
		
		String [] radioButtonLabels = {"generate all", "generate selected"};
		String [] checkBoxLabels = {"ICE definitions", "code stubs only", 
				"create xml-schema", "create default module/type",
				"copy module/type", "create config.mak"};
	
		ButtonGroup radioGroup = new ButtonGroup();
		for(int i=0; i<2; i++) {
			radioButtons[i] = new JRadioButton(radioButtonLabels[i]);
			radioButtons[i].addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					boolean enableCheckboxes = radioButtons[SELECTED].isSelected();
					for(int i=0; i<ANZ_TYPES; i++)
						checkBoxes[i].setEnabled(enableCheckboxes);
				}
			});
			radioGroup.add(radioButtons[i]);
			selectionPanel.add(radioButtons[i]);
		}
		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));
		for(int i=0; i<ANZ_TYPES; i++) {
			checkBoxes[i] = new JCheckBox(checkBoxLabels[i]);
			checkBoxPanel.add(checkBoxes[i]);
		}
		checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 5));
		selectionPanel.add(checkBoxPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		String [] buttonName = {"cancel", "ok"};
		for(int i=0; i<=1; i++) {
			JButton b = new JButton(buttonName[i]);
			b.setActionCommand(Integer.toString(i));
			b.addActionListener(this);
			buttonPanel.add(b);
			if(i == OK)
				getRootPane().setDefaultButton(b);
		}
		
		radioButtons[ALL].setSelected(true);
		getContentPane().add(selectionPanel);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		pack();
		setResizable(false);
	}

	private void createGenerationCode() {
		if(radioButtons[ALL].isSelected()) status = PackageGenerator.ALL;
		else {
			status = 0;
			for(int i=0; i<ANZ_TYPES; i++) 
				if(checkBoxes[i].isSelected()) {
					switch(i) {
					case ICE: status += PackageGenerator.ICE; break;
					case CPP: status += PackageGenerator.CPP; break;
					case SCHEMA: status += PackageGenerator.SCHEMA; break;
					case DEFAULT: status += PackageGenerator.DEFAULT; break;
					case XML: status += PackageGenerator.XML; break;
					case CONFIG: status += PackageGenerator.CONFIG; break;
					}
				}
		}
	}
	
	public void actionPerformed(ActionEvent event) {
		int action = Integer.parseInt(event.getActionCommand());
		switch(action) {
		case CANCEL: break;
		case OK: createGenerationCode(); break;
		}
		hide();
	}
}
