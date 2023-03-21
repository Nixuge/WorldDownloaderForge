package wdl.gui.notifications;

import lombok.Getter;

public enum Level {
	
	INFO("Info:"), WARNING("Warning:"), ERROR("ERROR:"), NONE("");
	
	@Getter
	private String header;
	
	Level(String s) {
		this.header = s;
	}
}
