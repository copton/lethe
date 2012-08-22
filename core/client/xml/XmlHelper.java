package xml;

import java.io.FileOutputStream;

import javax.swing.ImageIcon;

import org.jdom.Document;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.Element;
import util.IceNetworkConnection;

import util.DialogHelper;

import Ice.Properties;

public class XmlHelper implements Types {
	public static ImageIcon [] elementIcons = initIcons();
	
	public static Element getRootElement(String fileName, boolean validate) 
	throws org.jdom.JDOMException, java.io.IOException {
		
		SAXBuilder builder = new SAXBuilder();	
		if(validate) {
			builder.setValidation(true);
			builder.setFeature("http://apache.org/xml/features/validation/schema", true);
		}
		
		Document doc = builder.build(fileName);
		return doc.getRootElement();
	}
	
	public static void writeRootElement(Element root, String fileName) throws java.io.IOException {
		if(root == null) return;
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		java.io.File file = new java.io.File(fileName);
		if(!file.exists()) {
			if(file.getParentFile() != null)
				file.getParentFile().mkdirs();
			file.createNewFile();
		}
	
		outputter.output(root, new FileOutputStream(file));	
	}
	
	public static void getIncludeFiles(Element e, java.util.List l, String path2types) {
		if(e == null) return;
		
		Element include = e.getChild("include");
		if(include == null) return;
		
		java.util.Iterator it = include.getChildren().iterator();
		while(it.hasNext()) {
			Element file = (Element)it.next();
			String dirName = path2types +java.io.File.separator + file.getText()+java.io.File.separator;
			String fileName = dirName+file.getText()+".xml";
			if(!l.contains(file.getText())) {
				l.add(file.getText());
				Element typeElement = DialogHelper.getGlobalType(fileName);
				getIncludeFiles(typeElement, l, path2types);
			}
		}		
	}
	
	private static ImageIcon[] initIcons() {
		Properties properties = IceNetworkConnection.getProperties();
		String path = properties.getProperty("gui.iconDir")+"/";
		
		ImageIcon [] icons = new ImageIcon[ANZ_TYPES];
		icons[TYPE_DEF] = icons[TYPE_INSTANCE] = icons[MODULE_INSTANCE] = icons[MODULE_DEF] = new ImageIcon(path+"FlowGraph.png"); 
		icons[STRING] = new ImageIcon(path+"RedBall.png");
		icons[INT] = icons[LONG] = icons[BYTE] = icons[FLOAT] = icons[DOUBLE] = new ImageIcon(path+"BlueBall.png");
		icons[BOOLEAN] = new ImageIcon(path+"BrightBlueBall.png");
		icons[CLASS] = new ImageIcon(path+"OrangeBall.png");
		icons[ENUM] = new ImageIcon(path+"GreenBall.png");
		icons[ARRAY] = new ImageIcon(path+"PinkBall.png");
		icons[TABLE] = new ImageIcon(path+"PinkBall.png");
		icons[DICTIONARY] = new ImageIcon(path+"YellowBall.png");
		icons[INSTANCE] = new ImageIcon(path+"VioletBall.png");
		icons[PORT_DIR] = new ImageIcon(path+"InOutPort.png");
		icons[INPUT_PORT_DIR] = new ImageIcon(path+"InPort.png");
		icons[OUTPUT_PORT_DIR] = new ImageIcon(path+"OutPort.png");
		icons[PORT] = new ImageIcon(path+"Port.png");
		icons[CONFIGURATION_DIR] = new ImageIcon(path+"Hammer2.png");
		icons[SETTINGS_DIR] = new ImageIcon(path+"Gearwheel.png");
		icons[DEFINE_DIR] = new ImageIcon(path+"Object.png");

		
		icons[JOB] = icons[TYPE_DEF];
		return icons;
	}
}
