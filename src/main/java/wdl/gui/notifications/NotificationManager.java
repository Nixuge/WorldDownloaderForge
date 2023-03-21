package wdl.gui.notifications;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class NotificationManager {
	public static NotificationManager instance;

	public static NotificationManager getInstance() {
		if (NotificationManager.instance == null) {
			NotificationManager.instance = new NotificationManager();
		}
		return instance;
	}

	private ArrayList<Notification> notes = new ArrayList<Notification>();

	private boolean adding = false;

	private int addingCount = 0;

	private Minecraft mc = Minecraft.getMinecraft();

	public NotificationManager() {

	}

	public void addNotification(Notification note) {
		try {
			note.getWindow().setPosition(mc.displayWidth,
					mc.displayHeight);
			notes.add(note);
			adding = true;
		} catch (Exception e) {
			try {
				Notification notification = new Notification(Level.ERROR,
						"Error displaying note, please report: "
								+ e.getMessage());
				notification.getWindow().setPosition(mc.displayWidth,
						mc.displayHeight);
				notes.add(notification);
				adding = true;

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	public void update() {
		if (Minecraft.getMinecraft().thePlayer == null) {
			return;
		}
		for (Notification notification : notes) {
			notification.getWindow().update();
		}
		Iterator<Notification> iter = notes.iterator();
		while (iter.hasNext()) {
			Notification notification = iter.next();
			if (notification.getWindow().getLifeTime() > notification.getMaxTime()) {
				iter.remove();
			}
		}
	}

	public void draw() {
		if (Minecraft.getMinecraft().thePlayer == null) {
			return;
		}
		int space = -18;
		if (adding) {
			adding = false;
			addingCount = -space;
		}
		if (addingCount > 0) {
			addingCount--;
		}

		GlStateManager.pushMatrix();

		// Scale opengl calls to minecraft scale
		GlStateManager.scale(2d /
				Minecraft.getMinecraft().gameSettings.guiScale,
				2d / Minecraft.getMinecraft().gameSettings.guiScale,
				1
		);

		// Above chat
		GlStateManager.translate(0, -21, 0);
		GlStateManager.translate(0, addingCount, 0);

		for (int i = notes.size() - 1; i > -1; i--) {
			NotificationWindow window = notes.get(i).getWindow();
			GlStateManager.translate(0, space, 0);
			window.setPosition(mc.displayWidth,
					mc.displayHeight);
			window.draw();
		}
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
	}
}
