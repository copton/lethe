package gui;

public interface Pluggable {
	void init();
	void release();
	
	javax.swing.JPanel show();
	void hide();
}
