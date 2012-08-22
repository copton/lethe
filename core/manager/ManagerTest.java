import Comm.Job.*;
import Comm.Job.Graph.*;
import Comm.Manager.PublicInterfacePrx;
import Comm.Manager.PublicInterfacePrxHelper;
import Comm.Manager.SchedulerInformation;
import Comm.SourceService.ProtectedInterfacePrx;
import Ice.Application;
import Ice.ObjectPrx;
import java.util.*;

public class ManagerTest extends Application {

	public int run(String[] args) {
		
		Specification job = new Specification();
		job.owner = "phia";
		job.description = "";
		job.name = "";
		job.jobId = "";
		
		job.theGraph = new SimulationGraph();
		job.theGraph.theEdges = new Edge[0];
		job.theGraph.thePhases = new Phase[0];
		job.theGraph.theVertices = new Vertex[0];
		job.theGraph.thePorts = new Port[0];
		
		job.settings = new java.util.Map[2]; // 2 runden
		job.settings[0] = new HashMap();
		job.settings[1] = new HashMap();
		
		job.schedulerInfo = new SchedulerInformation(0, 0, 0, 0);
		job.sourceServers = new ProtectedInterfacePrx[0];
	
		Map ctx = communicator().getDefaultContext();
		ctx.put("user", "phia");
		communicator().setDefaultContext(ctx);
		
		ObjectPrx proxy = communicator().stringToProxy("manager@Manager.ServiceAdapter");
		PublicInterfacePrx manager = PublicInterfacePrxHelper.checkedCast(proxy);
	
		if(manager == null) System.out.println("manager is nulL!");
		else System.out.println("manager should be ok");
		try {
			System.out.println("scheduled: "+manager.startSimulation(job));
		} catch(Exception e) {
			//e.printStackTrace();
			System.out.println(e+": "+e.getMessage());
		}	
		
		return 0;
	}

	public static void main(String [] args) {
		ManagerTest t = new ManagerTest();
		System.exit(t.main("Test", args));
	}
}
