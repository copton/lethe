package plugins.debugger;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;

import javax.swing.*;
import event.DefaultEventHandler;

public class DebuggerListRenderer extends JLabel implements ListCellRenderer {
	
	public DebuggerListRenderer() {
		super();
		setFont(new Font("SansSarif", Font.PLAIN, 12));
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int pos, boolean isSelected, boolean arg4) {
		if(isSelected)
			setBackground(list.getSelectionBackground());
		else setBackground(list.getBackground());
	
		ListElement le = (ListElement)value;
		setText(le.text);
		switch(le.type) {
		case DefaultEventHandler.DEBUG_EVENT: setForeground(Color.GRAY); break;
		case DefaultEventHandler.STATUS_EVENT: setForeground(Color.GREEN); break;
		case DefaultEventHandler.WARNING_EVENT: setForeground(Color.BLUE); break;
		case DefaultEventHandler.ERROR_EVENT: setForeground(Color.RED); break;
		case DefaultEventHandler.EXCEPTION_EVENT: setForeground(Color.RED); break;
		default: setForeground(Color.BLACK);
		}
		return this;
	}
}
