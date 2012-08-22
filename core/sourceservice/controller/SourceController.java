package controller;

import Comm.Exceptions.RessourceNotFoundException;
import Comm.SourceService.*;
public interface SourceController {
	public void getSource(SourceDesc source, String [] files, String directory) 
			throws RessourceNotFoundException;
	
	public String [] getSourceList(SourceDesc source, String directory);
}
