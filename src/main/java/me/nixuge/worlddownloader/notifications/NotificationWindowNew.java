package me.nixuge.worlddownloader.notifications;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.opengl.GL11;

// import org.lwjgl.opengl.GL11;

import lombok.Getter;
import me.nixuge.worlddownloader.ReflectionUtils;

public class NotificationWindowNew {
	public static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

	private int x;
	private int y;
	
	private int width;
	private int height;
	
	private float time = 0;
	private float preTime = 0;
	
	@Getter
	private NotificationOld notification;
	
	private int stringWidth = 0;
	
	Tessellator t = Tessellator.getInstance();
	WorldRenderer b = t.getWorldRenderer();


	public NotificationWindowNew(NotificationOld notification) {
		this.notification = notification;
		stringWidth = fontRenderer.getStringWidth(this.notification.getText());
		if(this.getNotification().getLevel().getHeader().isEmpty()) {
			this.width = stringWidth + 11;
		}
		else {
			this.width = stringWidth + 15 + fontRenderer.getStringWidth(this.getNotification().getLevel().getHeader());
		}
		
		this.height = 16;
		// Minecraft _mc = Minecraft.getMinecraft();
		// setWorldAndResolution(_mc, _mc.displayWidth, _mc.displayHeight);
	}

	public void update() {
		preTime = time;
		time += 1f;
		if(time < 0f) {
			time = 0f;
		}
		System.out.println("updated.");
	}

	public void draw() {
		// // var tessellator = Tessellator.getInstance();
		// // tessellator.
		// // t.getWorldRenderer().ge
		// Timer timer = ReflectionUtils.findAndGetPrivateField(Minecraft.getMinecraft(), Timer.class);

		// float time = (this.preTime + (this.time - this.preTime) * timer.renderPartialTicks);
		// float maxTime = getNotification().getText().length() * 3;
		// float timePercent = (time / maxTime);
		// GlStateManager.enableBlend();
		// // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// // glEnable(GL_BLEND);
		// GlStateManager.disableCull();
		// // glDisable(GL_CULL_FACE);
		// GlStateManager.disableTexture2D();
		// // glDisable(GL_TEXTURE_2D);
		// // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// float alpha = 1;
		
		// if(timePercent < 0.1f) {
		// 	alpha = timePercent * 10f;
		// 	System.out.println("alpha changed1!");
		// }
		
		// if(timePercent > 0.9f) {
		// 	alpha = -(timePercent - 1f) * 10f;
		// 	System.out.println("alpha changed2!");
		// }

		// // alpha = 1;
		// GL11.glColor4f(0.1f, 0.1f, 0.1f, alpha);
		// // GlStateManager.color();
		// GL11.glBegin(GL11.GL_QUADS);
		
		// {
		// 	GL11.glVertex2d(x, y);
		// 	GL11.glVertex2d(x + width, y);
		// 	GL11.glVertex2d(x + width, y + height);
		// 	GL11.glVertex2d(x, y + height);
		// }
		
		// GL11.glEnd();
		// GL11.glFlush();
		// // GL11.glDrawBuffer(7);
		// GL11.glLineWidth(6f);
		// GL11.glColor4f(0.8f, 0.1f, 0.1f, alpha);
		// GL11.glFlush();
		// // GL11.glDrawBuffer(7);
		// GL11.glBegin(GL11.GL_LINES);
		// {
		// 	float width = timePercent * this.width;
			
		// 	GL11.glVertex2d(x, y + height - 2);
		// 	GL11.glVertex2d(x + width, y + height - 2);
		// }
		// GL11.glEnd();
		// GL11.glFlush();
		// // GL11.glDrawBuffer(7);
		
		// if(alpha > 1) {
		// 	alpha = 1;
		// }
		// if(alpha < 0) {
		// 	alpha = 0;
		// }
		// Level l = getNotification().getLevel();
		// // glEnable(GL_CULL_FACE);
		// GlStateManager.enableCull();
		// if(alpha > 0.5f) {
		// 	if(l == Level.ERROR) {
		// 		fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
		// 	}
		// 	if(l == Level.INFO) {
		// 		fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
		// 	}
		// 	if(l == Level.WARNING) {
		// 		fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
		// 	}
			
		// 	fontRenderer.drawStringWithShadow(notification.getText(), x + -(stringWidth - width) - 5, y + 3f, 0xffffffff);
		// }
		// b.
		// t.draw();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(0f, 0f, 0f, .6f);
		GL11.glRecti(10, 10, 10 + 50, 10 + 50);
		GL11.glFlush();
		System.out.println("drew window.");
		
	}

	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public float getLifeTime() {
		return time;
	}
}
