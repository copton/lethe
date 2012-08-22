package service;

import Comm.SourceService.ProtectedInterfacePrx;

public class SourceGrabber {

	private ProtectedInterfacePrx source;
	public SourceGrabber(ProtectedInterfacePrx sourceService) {
		source = sourceService;
	}
	
	
	public boolean grabSource(String jobId, String dir) {
		boolean startedSourceService = false;
		try {
			String patchProxy = source.startSourceService(jobId);
			startedSourceService = true;
			
			String command = "icepath2client --IcePatch2.Endpoints=\""+patchProxy+"\" "+dir;
			Process p = Runtime.getRuntime().exec(command);
			int exitCode = p.waitFor();
			
			source.stopSourceService(jobId);
			if(exitCode == 0) return true;
			else return false;
		} catch(Exception e) {
			// log on breeze
			e.printStackTrace();
			if(startedSourceService) 
				source.stopSourceService(jobId);
			return false;
		}
	}
}
