package me.nixuge.worlddownloader.notifications;

import net.minecraft.client.gui.Gui;

public class NotificationOld extends Gui {
	
	private String text;
	private Level level;
	
	public NotificationOld(Level level, String text) {
		this.level = level;
		this.text = text;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public String getText() {
		return text;
	}
}
