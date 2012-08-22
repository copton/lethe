package gui;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.input.SAXBuilder;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import event.*;

public class SettingsManager {
	private String fileName;
	private Element rootElement;
	
	private DefaultEventHandler exceptionHandler =
		new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	public SettingsManager(String fileName) {
		this.fileName = fileName;
		
		SAXBuilder builder = new SAXBuilder();
		try {
			builder.setValidation(false);
			Document doc = builder.build(fileName);
			rootElement = doc.getRootElement();
		} catch(Exception e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
			rootElement = new Element("root");
			
		}
	}
	
	public Element get(String name) {
		return rootElement.getChild(name);
	}
	
	public void set(Element element) {
		rootElement.removeChild(element.getName());
		rootElement.addContent(element);
	}
		
	public void save() {
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());		
		try {
			OutputStream outputStream = new FileOutputStream(fileName);
			outputter.output(rootElement, outputStream);
		} catch(IOException e) {
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}
}
