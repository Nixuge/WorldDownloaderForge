package me.nixuge.worlddownloader.notifications;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

public class Notification extends GuiIngame {
    private Level type;
    private String title;
    private String messsage;
    private long start;

    private long fadedIn;
    private long fadeOut;
    private long end;

    private static Minecraft mc = Minecraft.getMinecraft();

    public Notification(Level type, String title, String messsage, int length) {
        super(mc);
        this.type = type;
        this.title = title;
        this.messsage = messsage;

        fadedIn = 200 * length;
        fadeOut = fadedIn + 500 * length;
        end = fadeOut + fadedIn;
    }

    public void show() {
        start = System.currentTimeMillis();
    }

    public boolean isShown() {
        return getTime() <= end;
    }

    private long getTime() {
        return System.currentTimeMillis() - start;
    }

    @Override
    public void renderGameOverlay(float partialTicks) {
        this.drawCenteredString(mc.fontRendererObj, "owoowowo", 50, 50, 0xffffff);
        render();
        super.renderGameOverlay(partialTicks);
    }


    public void render() {
        System.out.println("rendering...");
        // double offset = 0;
        int offset = 0;
        int width = 120;
        int height = 30;
        long time = getTime();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        // fontRenderer.drawStringWithShadow("please wokr why lol", width, time, 0xffffff);

        GL11.glPushMatrix();
        // mc.renderEngine.bindTexture(overlay);
        // GL11.glScalef(scalefact,scalefact, 1);

        // this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
    
        drawRect(0, 0, mc.displayWidth, mc.displayHeight, 0xffffffff);
        // this.drawRect((int) (xPos+scalefact*256), 0, mc.displayWidth, mc.displayHeight, 0);
        GL11.glPopMatrix();

        // if (time < fadedIn) {
        //     offset = (int)Math.tanh(time / (double) (fadedIn) * 3.0) * width;
        // } else if (time > fadeOut) {
        //     offset = (int)(Math.tanh(3.0 - (time - fadeOut) / (double) (end - fadeOut) * 3.0) * width);
        // } else {
        //     offset = width;
        // }

        Color color = new Color(0, 0, 0, 220);
        Color color1;

        if (type == Level.INFO)
            color1 = new Color(0, 26, 169);
        else if (type == Level.WARNING)
            color1 = new Color(204, 193, 0);
        else {
            color1 = new Color(204, 0, 18);
            int i = Math.max(0, Math.min(255, (int) (Math.sin(time / 100.0) * 255.0 / 2 + 127.5)));
            color = new Color(i, 0, 0, 220);
        }

        drawRect(0, 0, 500, 500, color.getRGB());
        drawRect(0, 0, 500, 500, color1.getRGB());

        fontRenderer.drawString(title, (int) (mc.displayWidth - offset + 8), mc.displayHeight - 2 - height, -1);
        fontRenderer.drawString(messsage, (int) (mc.displayWidth - offset + 8), mc.displayHeight - 15, -1);
        System.out.println("rendered!");
    }
    // l
    // t
    // r
    // b
}
