package service;

public class SourceNotFoundException extends Exception {
	static final long serialVersionUID = 0;
	
	public SourceNotFoundException(String error) {
		super(error);
	}
}
