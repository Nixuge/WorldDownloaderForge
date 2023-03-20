package wdl.gui.notifications;

public enum Level {
	
	INFO("Info:"), WARNING("Warning:"), ERROR("ERROR:"), NONE("");
	
	private String header;
	
	Level(String s) {
		this.header = s;
	}
	
	public String getHeader() {
		return header;
	}
}
