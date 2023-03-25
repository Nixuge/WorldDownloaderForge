package wdl.gui.notifications;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class NotificationManager {
    // TODO: store that somewhere
    public static NotificationManager instance;

    public static NotificationManager getInstance() {
        if (NotificationManager.instance == null) {
            NotificationManager.instance = new NotificationManager();
        }
        return instance;
    }

    private ArrayList<Notification> notes = new ArrayList<Notification>();

    private int guiScale = 2;
    private float scale = 1f;
    private float scaleDown = 1f;
    private float space = 5;

    private Minecraft mc = Minecraft.getMinecraft();

    public NotificationManager() {

    }

    public void addNotification(Notification note) {
        if (note == null) {
            notes.add(new Notification(
                Level.ERROR,
                "notification is null",
                100
            ));
            System.out.println("Notification is null.");
            return;
        }
        try {
            notes.add(note);
        } catch (Exception e) {
            try {
                Notification notification = new Notification(Level.ERROR,
                        "Error displaying note, please report: "
                                + e.getMessage());
                notes.add(notification);

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void removeAll() {
        Iterator<Notification> iter = notes.iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
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

    public void draw(float partialTicks) {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return;
        }
        

        GlStateManager.pushMatrix();

        // Scale opengl calls down/up to Minecraft scale
        // Now using function below which scales thing dynamically,
        // unlike this which acts like the scale is always "Normal"
        // GlStateManager.scale(2d /
        //         Minecraft.getMinecraft().gameSettings.guiScale,
        //         2d / Minecraft.getMinecraft().gameSettings.guiScale,
        //         1
        // );

        int currentGuiScale = mc.gameSettings.guiScale;
        if (this.guiScale != currentGuiScale) {
            // Seem to need to recreate a new instance everytime scale changes?
            // Note: using ScaledResolutions because unlike gameSettings.guiScale, it supports "AUTO" scaling
            this.guiScale = new ScaledResolution(mc).getScaleFactor();
            scale = this.guiScale / 2f;
            scaleDown = 1 / scale;
            space = 5 * scaleDown;
        }

        // start above chat
        int heightOffset = 20;
        

        for (int i = notes.size() - 1; i > -1; i--) {
            NotificationWindow window = notes.get(i).getWindow();
            heightOffset += space + window.getHeight();
            // GlStateManager.translate(0, space, 0);
            window.setPosition((int)((mc.displayWidth >> 1) * scaleDown), (int)(((mc.displayHeight >> 1) * scaleDown)) - heightOffset);
            // System.out.println("i: " + i + " space:" + space);

            window.draw(partialTicks);
            
        }
        GlStateManager.popMatrix();
        // GlStateManager.enableTexture2D();
    }
}
