package client;

import org.jdom.*;
import Ice.Properties;

import event.DefaultEventHandler;
import event.UnexpectedExceptionEvent;
import xml.XmlHelper;
import xml.XsltTransformation;
import xml.TypedElement;
import util.IceNetworkConnection;

import java.io.*;
import java.util.*;
import net.sf.saxon.jdom.DocumentWrapper;
import net.sf.saxon.Configuration;

public class PackageGenerator implements event.EventHandler {	
	Properties properties = IceNetworkConnection.getProperties();
	
	private int build;
	public final static int ICE=1, CPP=2, SCHEMA=4, DEFAULT=8, 
			DEFAULT_SCHEMA=16, XML=32, CONFIG=64, ALL=127, JOB=128; 	
	
	public PackageGenerator() {
		this(ALL);
	}
	
	public PackageGenerator(int types) {
		build = types;
	}
	
	private boolean checkJobFile = true;
	public void generatePackage(String xmlFile) {
		try {
			checkJobFile = false;
	     	generatePackage(XmlHelper.getRootElement(xmlFile, true), xmlFile);
        	} catch(Exception e) {
            e.printStackTrace();
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}
	
	private DefaultEventHandler exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
	public void generatePackage(Element root, String fileName) {
		
		try {		
			String directory;
			String rootName = root.getChildText("name");
			
			if(root.getName().equals("module"))
				directory = properties.getProperty("xml.path2modules");
			else directory = properties.getProperty("xml.path2types");
			File baseDirectory = new File(directory, rootName);
			
			System.setProperty("javax.xml.transform.TransformerFactory",
					"net.sf.saxon.TransformerFactoryImpl");
			DocumentWrapper docWrap = new DocumentWrapper(root.getDocument(), "", new Configuration());
			String path2xsls = properties.getProperty("xml.path2xsl")+"/";	
			String path2xsd = properties.getProperty("xml.path2xsd")+"/";
			
            System.out.println("build Ice " + build);
			if((build&ICE) != 0) {
				if(!baseDirectory.exists()) baseDirectory.mkdirs();
				File iceFile = new File(baseDirectory, "gen_"+rootName+".ice");
				XsltTransformation.generate(path2xsls+"xml2ice.xsl", docWrap, iceFile);	
			}
			
			if((build&CPP) != 0) {
				if(!baseDirectory.exists()) baseDirectory.mkdirs();
				File cppFile = new File(baseDirectory, rootName+".h");
				checkForUniqueFilename(cppFile);
				
				XsltTransformation.generate(path2xsls+"xml2cpp.xsl", docWrap, cppFile);		
		
				if((new TypedElement(root)).getType() == TypedElement.MODULE_DEF) {
					File adapterFile = new File(baseDirectory, "gen_adapter.h");	
					XsltTransformation.generate(path2xsls+"xml2adapter.xsl", docWrap, adapterFile);					
				}
			}
	
			List xmlParameter=null;
			if((build&DEFAULT) != 0) {
				File instances = new File(baseDirectory, "instances");
				if(!instances.exists()) instances.mkdirs();
				
				xmlParameter = new ArrayList();
				xmlParameter.add("path2types");
				xmlParameter.add(properties.getProperty("xml.path2types"));
				
				if(root.getName().equals("parameter")) {
					xmlParameter.add("imprinting");
					xmlParameter.add(root.getChildText("name"));
				}
				
				File defaultFile = new File(new File(baseDirectory, "instances"), rootName+".xml");
				XsltTransformation.generate(path2xsls+"xml2xml.xsl", docWrap, defaultFile, xmlParameter);						
			}
			
			if((build&SCHEMA) != 0) {
				File instances = new File(baseDirectory, "instances");
				if(!instances.exists()) instances.mkdirs();
	
				if(xmlParameter == null) {
					xmlParameter = new ArrayList();
					xmlParameter.add("path2types");
					xmlParameter.add(properties.getProperty("xml.path2types"));
				}
				
				List xsdParameter = new ArrayList();
				xsdParameter.addAll(xmlParameter);
				xsdParameter.add("path2specs");
				xsdParameter.add("../"+path2xsd);
				
				File schemaFile = new File(new File(baseDirectory, "instances"), rootName+".xsd");
				XsltTransformation.generate(path2xsls+"xml2xsd.xsl", docWrap, schemaFile, xsdParameter);		
			}
			
			if((build&CONFIG) != 0) {
				if(!baseDirectory.exists()) baseDirectory.mkdirs();
				File config = new File(baseDirectory, "config.mak");
				if(config.exists()) config.delete();
				config.createNewFile();
			}
				
			if((build&XML) != 0) {
				if(!baseDirectory.exists()) baseDirectory.mkdirs();
				File xmlFile = new File(baseDirectory, rootName+".xml");
				
				String type = root.getName().equals("module")?"moduleDescription.xsd":"typeDescription.xsd";
				root.setAttribute("schemaLocation", "https://proj.5nord.org/simmit "+path2xsd+type, root.getNamespace("xsi"));
				XmlHelper.writeRootElement(root, xmlFile.getAbsolutePath());
			}
			
			if((build&JOB) != 0) {
				fileName = (new java.io.File(fileName)).getName();
				root.setAttribute("schemaLocation", "https://proj.5nord.org/simmit "+path2xsd+"jobDescription.xsd", root.getNamespace("xsi"));
				
				baseDirectory = new File(properties.getProperty("xml.path2jobs")+"/"+fileName);
				if(!baseDirectory.exists()) baseDirectory.mkdirs();
				
				String baseName = baseDirectory.getAbsolutePath()+"/"+fileName;
				
				XmlHelper.writeRootElement(root, baseName+".xml.orig");
				File jobFile = new File(baseName+".xsd");
				if(checkJobFile) {
					try {
						root = XmlHelper.getRootElement(baseName+".xml.orig", true);
					} catch(Exception e) {
						throw e;
					}
				} else checkJobFile = true;
				
				XsltTransformation.generate(path2xsls+"job2xsd.xsl", docWrap, jobFile);	
				root.setAttribute("schemaLocation", "https://proj.5nord.org/simmit "+(new java.io.File(baseName+".xsd")).getName(), root.getNamespace("xsi"));
				XmlHelper.writeRootElement(root, baseName+".xml");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			exceptionHandler.fireEvent(new UnexpectedExceptionEvent(e));
		}
	}

	private void checkForUniqueFilename(File f) {
		if(!f.exists()) return;
		
		int fileNo=1;
		String name = f.getName();
		String prefix = name.substring(0, name.lastIndexOf("."));
		String suffix = name.substring(name.lastIndexOf("."));
		
		File newFile = new File(f.getParentFile(), prefix+"-"+fileNo+suffix); 
		while(newFile.exists()) {
			fileNo++;
			newFile = new File(f.getParentFile(), prefix+"-"+fileNo+suffix);
		}
		f.renameTo(newFile);
	}
	
	private static boolean valid(String [] args) {
		File f = new File(args[args.length-1]);
		if(!f.exists()) {
			System.out.println("File "+args[args.length-1]+" doesn't exist");
			return false;
		} else return true;
	}
	
	
	private static String getUsage() {
		String classpath = "-classpath includedJars/saxon8.jar:includedJars/saxon8-jdom.jar:includedJars/jdom.jar";
		String usage = "usage: java "+classpath+" PackageGenerator [-icsdxm |-j] <file>\n"
				+ "\t-i creates ICE files\n"
				+ "\t-c creates c-stubs\n"
				+ "\t-s creates schema-description\n"
				+ "\t-d creates default-instance\n"
				+ "\t-x copies xml-file\n"
				+ "\t-m creates config.mak\n"
				+ "\t-j creates job schema\n";
		
		return usage;	
	}
	
	private static int parseArgs(String [] args) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<args.length-1; i++)
			sb.append(args[i]);
		
		String typeString = sb.toString();
		int types = 0;
		if(typeString.indexOf("i") > -1) types |= ICE;
		if(typeString.indexOf("c") > -1) types |= CPP;
		if(typeString.indexOf("s") > -1) types |= SCHEMA;
		if(typeString.indexOf("d") > -1) types |= DEFAULT | DEFAULT_SCHEMA;
		if(typeString.indexOf("x") > -1) types |= XML;
		if(typeString.indexOf("m") > -1) types |= CONFIG;
		if(typeString.indexOf("j") > -1) types = JOB;
		
		if(types == 0) return ALL;
		else return types;
	}
	
	public static void main(String [] args) throws Exception {
		if(args.length < 1) {
			System.out.println(getUsage());
			System.out.println("args < 1");
			System.exit(1);
		} else if(!valid(args)) System.exit(2);
		
		PackageGenerator pg;
		if(args.length > 1) pg = new PackageGenerator(parseArgs(args));
		else pg = new PackageGenerator();
		
		pg.generatePackage(args[args.length-1]);
	
		IceNetworkConnection.get().shutdown();
		System.exit(0);
	}

	private DefaultEventHandler statusHandler = new DefaultEventHandler(DefaultEventHandler.STATUS_EVENT);
	public void addEventListener(Object l) {
		statusHandler.addEventListener(l);
	}

	public void removeEventListener(Object l) {
		statusHandler.removeEventListener(l);
	}

	public void fireEvent(Object e) {
		statusHandler.fireEvent(e);
	}
}
