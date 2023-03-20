package me.nixuge.worlddownloader.notifications;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

public class Notification {
    private Level type;
    private String title;
    private String messsage;
    private long start;

    private long fadedIn;
    private long fadeOut;
    private long end;

    private Minecraft mc;

    public Notification(Level type, String title, String messsage, int length) {
        this.type = type;
        this.title = title;
        this.messsage = messsage;

        fadedIn = 200 * length;
        fadeOut = fadedIn + 500 * length;
        end = fadeOut + fadedIn;
        this.mc = Minecraft.getMinecraft();
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

    public void render() {
        System.out.println("rendering...");
        // double offset = 0;
        int offset = 0;
        int width = 120;
        int height = 30;
        long time = getTime();

        if (time < fadedIn) {
            offset = (int)Math.tanh(time / (double) (fadedIn) * 3.0) * width;
        } else if (time > fadeOut) {
            offset = (int)(Math.tanh(3.0 - (time - fadeOut) / (double) (end - fadeOut) * 3.0) * width);
        } else {
            offset = width;
        }

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

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        drawRect(mc.displayWidth - offset, mc.displayHeight - 5 - height, mc.displayWidth, mc.displayHeight - 5, color.getRGB());
        drawRect(mc.displayWidth - offset, mc.displayHeight - 5 - height, mc.displayWidth - offset + 4, mc.displayHeight - 5, color1.getRGB());

        fontRenderer.drawString(title, (int) (mc.displayWidth - offset + 8), mc.displayHeight - 2 - height, -1);
        fontRenderer.drawString(messsage, (int) (mc.displayWidth - offset + 8), mc.displayHeight - 15, -1);
        System.out.println("rendered...");
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    public static void drawRect(int left, int top, int right, int bottom, int color)
    {
        left = 0;
        top = 0;
        right = 500;
        bottom = 500;
        color = 0xffffffff;
        System.out.println("please");
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


}
