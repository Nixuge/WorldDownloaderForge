package me.nixuge.worlddownloader.notifications;

import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class NotificationManagerOld {
    private static NotificationManagerOld instance;
    public static NotificationManagerOld getInstance() {
        if (instance == null) {
            instance = new NotificationManagerOld();
        }
        return instance;
    }
    
	private ArrayList<NotificationWindowNew> notes = new ArrayList<NotificationWindowNew>();

	private boolean adding = false;

	private int addingCount = 0;

	public NotificationManagerOld() {

	}

	public void addNotification(NotificationOld note) {
		// if (Jigsaw.ghostMode) {
		// 	return;
		// }
		// if (Jigsaw.getUIRenderer() == null) {
		// 	return;
		// }
		try {
			NotificationWindowNew window = new NotificationWindowNew(note);
			// window.setPosition(Jigsaw.getUIRenderer().getWidth() - window.getWidth() - 3,
			// 		Jigsaw.getUIRenderer().getHeight());
			window.setPosition(Minecraft.getMinecraft().displayWidth - window.getWidth() - 3,
					Minecraft.getMinecraft().displayHeight);
			notes.add(window);
			adding = true;
		} catch (Exception e) {
            System.out.println("ERROR!!");
			try {
				// if (Jigsaw.devVersion) {
                    if (true) {
					NotificationWindowNew window = new NotificationWindowNew(new NotificationOld(Level.ERROR,
							"Error displaying note, please report to the creator of the client along with this message: "
									+ e.getMessage()));
					window.setPosition(Minecraft.getMinecraft().displayWidth - window.getWidth() - 3,
                        Minecraft.getMinecraft().displayHeight);
					notes.add(window);
					adding = true;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	public void update() {
		// if (Jigsaw.ghostMode) {
		// 	return;
		// }
		if (Minecraft.getMinecraft().thePlayer == null) {
			return;
		}
		for (NotificationWindowNew window : notes) {
			window.update();
		}
		Iterator<NotificationWindowNew> iter = notes.iterator();
		while (iter.hasNext()) {
			NotificationWindowNew window = iter.next();
			if (window.getLifeTime() > window.getNotification().getText().length() * 3) {
				iter.remove();
			}
		}
	}

	public void draw() {
		// if (Jigsaw.ghostMode) {
		// 	return;
		// }
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

		GlStateManager.scale(2d /
		Minecraft.getMinecraft().gameSettings.guiScale, 2d /
		Minecraft.getMinecraft().gameSettings.guiScale, 1);

		GlStateManager.translate(0, -21, 0);
		GlStateManager.translate(0, addingCount, 0);
		for (int i = notes.size() - 1; i > -1; i--) {
			NotificationWindowNew window = notes.get(i);
			GlStateManager.translate(0, space, 0);
			window.setPosition(Minecraft.getMinecraft().displayWidth - window.getWidth() - 3,
                Minecraft.getMinecraft().displayHeight);
			window.draw();
		}
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GL11.glColor4f(1f, 1f, 1f, 1f);
		// System.out.println("done drawing.");
	}
}
