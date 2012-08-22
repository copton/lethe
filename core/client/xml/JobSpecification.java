package xml;
import net.sf.saxon.Configuration;
import net.sf.saxon.jdom.DocumentWrapper;

import org.jdom.input.SAXBuilder;
import org.jdom.*;

import Ice.ObjectPrx;
import Ice.Properties;
import Comm.Job.Specification;
import Comm.Job.Graph.*;
import Comm.Manager.SchedulerInformation;
import Comm.SourceService.Filter;
import Comm.SourceService.LocalSource;
import Comm.SourceService.ProtectedInterfacePrx;
import Comm.SourceService.PublicInterfacePrx;
import Comm.SourceService.PublicInterfacePrxHelper;
import Comm.SourceService.SvnSource;
import util.DialogHelper;
import util.IceNetworkConnection;

import java.io.IOException;
import java.util.*;

public class JobSpecification {
	Comm.Job.Specification job;
	public JobSpecification() {}
	
	private SAXBuilder builder = new SAXBuilder();
	private Element root;
	public JobSpecification(String fileName) throws JDOMException, java.io.IOException {
		root = getJobRoot(fileName);
		if(root != null) setJob(root);
	}
		
	public void setJob(Element root) throws JDOMException, java.io.IOException {
		job = new Comm.Job.Specification();
		job.theGraph = new SimulationGraph();
		
		this.path2modules = IceNetworkConnection.getProperties().getProperty("xml.path2modules");
        
		parseModules(root);
		parseEdges(root);
		parsePhases(root);
		parseSettings(root);
		setTypes(root);
		
		System.out.println(job.settings[0].size());
	}	
	
	private void setTypes(Element root) {
		job.name = root.getChildText("name");
		job.description = root.getChildText("description");
		job.jobId = job.owner = "";
		job.sourceServers = new ProtectedInterfacePrx[0];
		job.schedulerInfo = new SchedulerInformation();
		job.schedulerInfo.startTime = (new Date()).getTime();
	}
	
	public Specification getJob() {
		return job;
	}
	
	private HashMap vertices = new HashMap();
	private void parseModules(Element root) throws JDOMException, java.io.IOException {
		Iterator it = root.getChild("graph").getChild("nodes").getChildren("node").iterator();
		List vertexList = new ArrayList();
		List portList = new ArrayList();
		
		Ice.Properties properties = IceNetworkConnection.getProperties();
		
		String path2modules = properties.getProperty("xml.path2modules");
		while(it.hasNext()) {
			Element node = (Element)it.next();
	
			String nodeFile = path2modules+"/"+node.getChildText("module")+"/instances/"+node.getChildText("instance")+".xml";
			Element nodeRoot = getRootElement(nodeFile);
			
			if(nodeRoot != null) {
				VertexWrapper v = VertexWrapper.newWrapper(node.getChildText("name"), nodeRoot);			
				v.configure(node.getChild("configuration"));
				vertexList.add(v.get());
				portList.addAll(v.getPorts());
				vertices.put(node.getChildText("name"), v);
				System.out.println("configured "+v.vertex.vertexName);
			} // else throw Exception!
		}
		job.theGraph.theVertices = (Vertex[])vertexList.toArray(new Vertex[0]); 
		job.theGraph.thePorts = (Port[])portList.toArray(new Port[0]);
	}
	
	private void parseEdges(Element root) {
		Element edges = root.getChild("graph").getChild("edges");
		Iterator it = edges.getChildren().iterator();
	
		List edgeList = new ArrayList();
		while(it.hasNext()) {
			Element edge = (Element)it.next();
			Comm.Job.Graph.Edge newEdge = new Comm.Job.Graph.Edge();
			
			//newEdge.minSize = Integer.parseInt(edge.getChildText ("minSize"));
			newEdge.minSize = 0;
			List inputPorts = new ArrayList();
			List outputPorts = new ArrayList();
			
			Iterator it2 = edge.getChildren("node").iterator();
			while(it2.hasNext()) {
				Element node = (Element)it2.next();
				PortDescriptor p = new PortDescriptor();
				p.vertexName = node.getChildText("name");
				p.portName = node.getChildText("port");
			
				if(node.getChild("port").getAttributeValue("direction").equals("input"))
					inputPorts.add(p);
				else outputPorts.add(p);
			}
			
			newEdge.readerPorts = (PortDescriptor[])inputPorts.toArray(new PortDescriptor[0]);
			newEdge.writerPorts = (PortDescriptor[])outputPorts.toArray(new PortDescriptor[0]);
			edgeList.add(newEdge);
		}
		
		job.theGraph.theEdges = (Comm.Job.Graph.Edge[])edgeList.toArray(new Comm.Job.Graph.Edge[0]);
	}
	
	private void parsePhases(Element root) {
		Element phases = root.getChild("graph").getChild("phases");
		List phaseList = new ArrayList();
		
		Iterator it = phases.getChildren().iterator();
		while(it.hasNext()) {
			List activePorts = new ArrayList();
			List resetedPorts = new ArrayList();
			List observedModules = new ArrayList();
			List resultProducingModules = new ArrayList();
			
			Element phase = (Element)it.next();
			Iterator it2 = phase.getChildren().iterator();
			while(it2.hasNext()) {
				Element node = (Element)it2.next();
				String nodeName = node.getChildText("name");
				if(node.getChild("observed") != null) observedModules.add(nodeName);
				
				if(createsResults(nodeName, root))
					resultProducingModules.add(nodeName);
					
				Iterator it3 = node.getChildren("port").iterator();
				while(it3.hasNext()) {
					Element port = (Element)it3.next();
					PortDescriptor desc = new PortDescriptor();
					desc.vertexName = nodeName;
					desc.portName = port.getChildText("name");
					
					activePorts.add(desc);
					if(port.getChild("reset") != null) resetedPorts.add(desc);
				}
			}
			
			Phase p = new Phase();
			p.activePorts = (PortDescriptor[])activePorts.toArray(new PortDescriptor[0]);
			p.resetPorts = (PortDescriptor[])resetedPorts.toArray(new PortDescriptor[0]);
			p.observedVertices = (String[])observedModules.toArray(new String[0]);
			p.resultVertices = (String[])resultProducingModules.toArray(new String[0]);
			
			phaseList.add(p);
		}
		
		job.theGraph.thePhases = (Phase[])phaseList.toArray(new Phase[0]);
	}
	
	private boolean createsResults(String name, Element root) {
		String moduleName = getModuleName(name, root);	
		Element module = DialogHelper.getModule(moduleName);
		
		return module.getChild("parameter").getChild("results").getChildren().size() > 0;
	}
	
	private String getModuleName(String nodeName, Element root) {
		Iterator it = root.getChild("graph").getChild("nodes").getChildren().iterator();
		while(it.hasNext()) {
			Element node = (Element)it.next();
			if(node.getChildText("name").equals(nodeName)) 
				return node.getChildText("module");
		}
		return null;
	}
	
	private void parseSettings(Element root) {
		Element defaultSettings = root.getChild("settings").getChild("default");
		if(defaultSettings != null) {
			Iterator it = defaultSettings.getChildren().iterator();
			while(it.hasNext()) {
				Element module = (Element)it.next();
				VertexWrapper v = (VertexWrapper)vertices.get(module.getChildText("name"));
				module.removeChild("name");
				v.setSettings(module); // set default settings
			}
		}
		
		Element rounds = root.getChild("settings").getChild("rounds");
		int anzRounds = Integer.parseInt(rounds.getAttributeValue("anz"));
		job.settings = new Map[anzRounds];
	
		for(int i=0; i<anzRounds; i++)
			job.settings[i] = new HashMap();
			
		for(Iterator it = rounds.getChildren("round").iterator(); it.hasNext();) {
			Element round = (Element)it.next();
			int nr = Integer.parseInt(round.getAttributeValue("nr"))-1;
			
			// set default settings
			Iterator it2 = vertices.values().iterator();
			while(it2.hasNext()) {
				VertexWrapper v = (VertexWrapper)it2.next();
				job.settings[nr].put(v.vertex.vertexName, v.settings);
			}
			
			// set special settings
			it2 = round.getChildren("node").iterator();
			while(it2.hasNext()) {
				Element module = (Element)it2.next();
				VertexWrapper v = (VertexWrapper)vertices.get(module.getChildText("name"));
				module.removeChild("name");
				byte [] config = v.getSettings(module); // set round settings
				job.settings[nr].put(v.vertex.vertexName, config);
			}
		}
	}
	
	private Element getRootElement(String fileName) throws JDOMException, java.io.IOException {
		return getRootElement(fileName, true);
	}
	
	private Element getRootElement(String fileName, boolean validate) throws JDOMException, java.io.IOException {
		System.out.println("read "+fileName);
	
		if(validate) {
			builder.setValidation(true);
			builder.setFeature("http://apache.org/xml/features/validation/schema", true);
		}
		Document doc = builder.build(fileName);
		return doc.getRootElement();
	}
	
	private Element getJobRoot(String fileName) throws java.io.IOException {
		try {
			return getRootElement(fileName);		
		} catch (JDOMException e) {
            e.printStackTrace();
		}
		return null;
	}
	
	protected String createJobSchema(Element root, String fileName) {	
		String schemaName = fileName.substring(0, fileName.length()-3)+"xsd";
		Properties properties = IceNetworkConnection.getProperties();
		String xslSource = properties.getProperty("path2xsl")+"job2xsd.xsl";
		
		DocumentWrapper docWrap = new DocumentWrapper(root.getDocument(), fileName, new Configuration());
		try {
			XsltTransformation.generate(xslSource, docWrap, new java.io.File(schemaName));
		} catch(Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
			
		return schemaName;
	}
	

	
	public ProtectedInterfacePrx[] announceSources(String jobId) throws Exception {
		Set servers = new HashSet();
		
		// get core
		Element core = root.getChild("core");
		Element serverElement = core.getChild("server");
		SourceServer server = createServer(serverElement);
		
		if(!server.init()) return new ProtectedInterfacePrx[0];
		String [] coreFile = {"core"};
		announceSource(server, core, coreFile, jobId);
		servers.add(server);
		
		// get modules and types
		Iterator it = root.getChild("graph").getChild("nodes").getChildren("node").iterator();
		while(it.hasNext()) {
			Element module = (Element)it.next();
			Element location = module.getChild("location");
			
			server = createServer(location.getChild("server"));
			if(!server.init()) return new ProtectedInterfacePrx[0];
			
			String [] files = getDependingFiles(module);
			announceSource(server, location, files, jobId);
			servers.add(server);
		}
		
		// get interfaces
		List interfaces = new ArrayList();
		it = servers.iterator();
		while(it.hasNext()) {
			server = (SourceServer)it.next();
			interfaces.add(server.sourceService.getInterface());
		}
		return (ProtectedInterfacePrx[])interfaces.toArray(new ProtectedInterfacePrx[0]);
	}
	
	private SourceServer createServer(Element serverElement) {
		if(serverElement != null) 
			return new SourceServer(serverElement.getChildText("host"),
					serverElement.getChildText("port"));
		else return new SourceServer("default", "default");
	}
	
	private void announceSource(SourceServer server, Element node, String [] files, String jobId) 
		throws Exception {
		
		Comm.SourceService.SourceDesc sourceDesc = null;
		if(node.getChild("LocalSource") != null) {
			LocalSource localSource = new LocalSource();
			localSource.path = node.getChild("LocalSource").getChildText("pathToExtensions");
			
			sourceDesc = localSource;
		} else if(node.getChild("SvnSource") != null) {
			SvnSource svnSource = new SvnSource();
			Element svn = node.getChild("SvnSource");
			svnSource.revision = Integer.parseInt(svn.getChildText("revision"));
			
			sourceDesc = svnSource;
		} else throw new NullPointerException("unknown source");
	
		server.sourceService.announce(jobId, files, Filter.CODE, sourceDesc);
	}
	
	private String path2modules;
	private String [] getDependingFiles(Element module) throws Exception {
		Set files = new HashSet();
		String moduleName = module.getChildText("module");
		files.add("modules/"+moduleName);
		
		Element moduleRoot = getRootElement(path2modules+"/"+moduleName+"/"+moduleName+".xml");
		Iterator it = moduleRoot.getChild("include").getChildren().iterator();
		while(it.hasNext()) {
			Element type = (Element)it.next();
			files.add("types/"+type.getText());
		}
		
		it = moduleRoot.getChild("ports").getChild("input").getChildren("port").iterator();
		while(it.hasNext()) {
			Element port = (Element)it.next();
			files.add("types/"+port.getChildText("type"));
		}
		
		it = moduleRoot.getChild("ports").getChild("output").getChildren("port").iterator();
		while(it.hasNext()) {
			Element port = (Element)it.next();
			files.add("types/"+port.getChildText("type"));
		}
		
		return (String[])files.toArray(new String[0]);
	}
	
	
	private class SourceServer {
		String host, port;
		PublicInterfacePrx sourceService;
		
		public SourceServer(String h, String p) {
			host = h;
			port = p;			
		}
		
		public boolean init() {
			ObjectPrx prx;
			if(host.equals("default"))
				prx = IceNetworkConnection.get().stringToProxy("Service@Codedistribution.SourceAdapter");
			else prx = IceNetworkConnection.get().stringToProxy("Service@SourceAdapter -h "+host+" -p "+port);
			
			if(prx == null) return false;
		
			sourceService = PublicInterfacePrxHelper.checkedCast(prx);	
			return sourceService != null;
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof SourceServer)) return false;
			SourceServer server = (SourceServer)o;
			
			return host.equals(server.host) && port.equals(server.port);
		}
	}
	
	public static void main(String [] args) throws JDOMException, IOException{
		new JobSpecification("/Users/phia/testJob.xml");
		System.out.println("created job");
		System.exit(0);
	}
}

