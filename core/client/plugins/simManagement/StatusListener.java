package plugins.simManagement;

import util.IceNetworkConnection;
import Comm.Job.Persistence;
import Comm.Manager.ChangedId;
import Comm.Manager.PersistantJobs;
import Comm.Manager._PublisherDisp;
import Ice.Current;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import IceStorm.NoSuchTopic;
import IceStorm.TopicManagerPrx;
import IceStorm.TopicManagerPrxHelper;
import IceStorm.TopicPrx;

public class StatusListener extends _PublisherDisp {

	private ManagementModel model;
	private String [] topics;
	public StatusListener(ManagementModel model, String [] topics) {
		this.model = model;
		this.topics = topics;
		register();
	}
	
	private ObjectPrx proxy;
	private ObjectAdapter adapter;
	private void register() {
		try {
			ObjectPrx topicProxy = IceNetworkConnection.get().stringToProxy("IceStorm/TopicManager@IceStorm.TopicManager");
			TopicManagerPrx topicManager = TopicManagerPrxHelper.checkedCast(topicProxy);
			
			adapter = IceNetworkConnection.get().createObjectAdapter("StormAdapter.Subscriber");
			proxy = adapter.addWithUUID(this);
			adapter.activate();
		
			for(int i=0; i<topics.length; i++) {
				try {
					TopicPrx topic = topicManager.retrieve(topics[i]);
					topic.subscribe(null, proxy);
					System.out.println("subscribed for "+topics[i]);
				} catch(NoSuchTopic e) {
					System.err.println("NoSuchTopic: "+e.getMessage());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void report(ChangedId changes, Current __current) {
		model.updateJob(changes);
	}

	protected void deregister() {
		ObjectPrx topicProxy = IceNetworkConnection.get().stringToProxy("IceStorm/TopicManager@IceStorm.TopicManager");
		TopicManagerPrx topicManager = TopicManagerPrxHelper.checkedCast(topicProxy);
	
		for(int i=0; i<topics.length; i++) {
			try {
				TopicPrx topic = topicManager.retrieve(topics[i]);
				topic.unsubscribe(proxy);
			} catch(NoSuchTopic e) {}
		}
		adapter.deactivate();
	}

	public void simulationFinished(Persistence simulation, Current __current) {
		System.out.println("SIMULATION FINISHED");
		Freeze.Connection connection =          
			 Freeze.Util.createConnection(IceNetworkConnection.get(), "db");
		PersistantJobs results = new PersistantJobs(connection, "results", true);
		results.put(simulation.job.jobId, simulation);
		results.close();
		connection.close();
	}
}
