package wdl.gui.notifications;

import lombok.Getter;

@Getter
public enum Level {
	// See https://htmlcolorcodes.com or another color picker
	INFO("Info", 0x3498db), 
	WARNING("Warning", 0xf1c40f ), 
	ERROR("ERROR", 0xe74c3c ), 
	NONE("");
	
	private String header;
	private int color;
	
	Level(String s, int color) {
		this.header = s;
		this.color = color;
	}
	Level(String s) {
		this.header = s;
		this.color = 0;
	}
}
