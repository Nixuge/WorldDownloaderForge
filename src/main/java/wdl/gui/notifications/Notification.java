package wdl.gui.notifications; 

import lombok.Getter;
import net.minecraft.client.gui.Gui;

@Getter
public class Notification extends Gui {
	
	private String text;
	private Level level;
	private int maxTime;


	public Notification(Level level, String text, int maxTime) {
		this.level = level;
		this.text = text;
		this.maxTime = maxTime;
	}

	public Notification(Level level, String text) {
		this.level = level;
		this.text = text;
		maxTime = text.length() * 3;
	}
}
