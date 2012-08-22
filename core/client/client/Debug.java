package client;

import Ice.ObjectPrx;
import util.IceNetworkConnection;
import xml.JobSpecification;

import event.*;

public class Debug {

	private DefaultEventHandler exceptionHandler;
    private Comm.Job.Specification job;

    private void create(String proxy) throws Exception
    {
        ObjectPrx iceObject = IceNetworkConnection.get().stringToProxy(proxy);
        Comm.Generator.InterfacePrx generator = Comm.Generator.InterfacePrxHelper.checkedCast(iceObject);
        System.out.println("--> connected to generator");
        generator.createSimulation(job, "SUFFIX", true);
        System.out.println("--> created simulation");
    }

    private void connectAndInit(String proxy) throws Exception
    {
        Comm.Simulation.InterfacePrx simulation;
        ObjectPrx iceObject = IceNetworkConnection.get().stringToProxy(proxy);
        simulation = Comm.Simulation.InterfacePrxHelper.checkedCast(iceObject);
        assert (simulation != null);

        System.out.println("--> connected to simulation");
        simulation.init(job);
        System.out.println("--> initialized simulation");
    }

    private void createJob(String xmlFile) throws Exception
    {
        job = new JobSpecification(xmlFile).getJob();
        job.owner = "alex";
        job.jobId = "JOBID";
        System.out.println("--> created job");
    }

    Debug(String[] args) throws Exception
    {
        exceptionHandler = new DefaultEventHandler(DefaultEventHandler.EXCEPTION_EVENT);
        exceptionHandler.addEventListener(new DefaultConsoleEventListener());

        createJob(args[0]);

        if (args[1].equals("create")) {
            create(args[2]);
        } else if (args[1].equals("start")) {
            connectAndInit(args[2]);
        } else {
            System.out.println("exceptec create or start, got " + args[1]);
            System.exit(1);
        }
    }
	
    public static void usage()
    {
        System.out.println("usage: java client.Debug <job.xml> create|start [<stringified proxy>]");
        System.exit(1);
    }
    
    public static void main(String [] args) 
    {
        try {
            if(args.length != 3) {
                usage();
            }

            new Debug(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
