package service;

import Comm.SourceService.*;
import Ice.Communicator;
import Ice.Current;
public class ProtectedSourceService extends _ProtectedInterfaceDisp {

	private SourceService service;
	public ProtectedSourceService(SourceService service, Communicator communicator) {
		this.service = service;
	} 
	
	//	 start icepatch-server
	public String startSourceService(String jobId, Current current) {
		if(!service.activeSources.containsKey(jobId)) throw new NullPointerException("object for "+jobId+" not found");
	
		PatchServerWrapper wrapper = (PatchServerWrapper)service.activeSources.get(jobId);
		wrapper.counter++;
		service.activeSources.put(jobId, wrapper);
		
		return "-t --IcePatch2.Endpoints="+wrapper.server+" --IcePatch2.InstanceName="+jobId;
	}
	
	// stop icepatch-server
	public void stopSourceService(String jobId, Current current) {
		PatchServerWrapper wrapper = (PatchServerWrapper)service.activeSources.get(jobId);
		if(wrapper == null) return; // source was not active
		
		if(--wrapper.counter == 0) {
			service.removeServer(wrapper);
			service.activeSources.remove(jobId);
		} else service.activeSources.put(jobId, wrapper);	
	}
}
