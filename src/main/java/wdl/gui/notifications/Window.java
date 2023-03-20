package wdl.gui.notifications;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.opengl.GL11;

import lombok.Getter;

import wdl.gui.widget.ExtGuiScreen;
import wdl.gui.widget.WDLScreen;

public class Window extends WDLScreen {
	public static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

	private int x;
	private int y;
	
	private int width;
	private int height;
	
	private float time = 0.5f;
	private float preTime = 0;
	
	@Getter
	private Notification notification;
	
	private int stringWidth = 0;

	public Window(Notification notification) {
		super("ok");
		this.notification = notification;
		stringWidth = fontRenderer.getStringWidth(this.notification.getText());
		if(this.getNotification().getLevel().getHeader().isEmpty()) {
			this.width = stringWidth + 11;
		}
		else {
			this.width = stringWidth + 15 + fontRenderer.getStringWidth(this.getNotification().getLevel().getHeader());
		}
		
		this.height = 16;
		// setPosition(500, 500);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		draw();
		System.out.println("atoo");
		super.drawScreen(500, 500, 20);
	}

    public void draw() {
		
		// drawWorldBackground(0);
		// fontRenderer.drawString("DSGIHOZEURYFUIZOEHEZ", x, y, 0xffffff, false);
		// if (x < 5000000)
		// 	return;
		// float time = (this.preTime + (this.time - this.preTime) * Minecraft.getMinecraft().timer.renderPartialTicks);
		float maxTime = notification.getText().length() * 3;
		float timePercent = (time / maxTime);
		glEnable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		float alpha = .8f;
		
		// if(timePercent < 0.1f) {
		// 	alpha = timePercent * 10f;
		// }
		
		// if(timePercent > 0.9f) {
		// 	alpha = -(timePercent - 1f) * 10f;
		// }
		
		GL11.glColor4f(0.1f, 0.1f, 0.1f, alpha);
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2d(x, y);
			GL11.glVertex2d(x + width, y);
			GL11.glVertex2d(x + width, y + height);
			GL11.glVertex2d(x, y + height);
		}
		GL11.glEnd();
		
		GL11.glLineWidth(6f);
		GL11.glColor4f(0.8f, 0.1f, 0.1f, alpha);
		GL11.glBegin(GL11.GL_LINES);
		{
			float width = timePercent * this.width;
			
			GL11.glVertex2d(x, y + height - 2);
			GL11.glVertex2d(x + width, y + height - 2);
		}
		GL11.glEnd();
		
		if(alpha > 1) {
			alpha = 1;
		}
		if(alpha < 0) {
			alpha = 0;
		}
		Level l = getNotification().getLevel();
		glEnable(GL_CULL_FACE);
		if(alpha > 0.5f) {
			fontRenderer.drawStringWithShadow("owo?", x + 5, y + 3f, 0xffffffff);
			if(l == Level.ERROR) {
				fontRenderer.drawStringWithShadow(this.notification.getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
			}
			if(l == Level.INFO) {
				fontRenderer.drawStringWithShadow(this.notification.getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
			}
			if(l == Level.WARNING) {
				fontRenderer.drawStringWithShadow(this.notification.getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
			}
			// stringWidth = fontRenderer.getStringWidth("owo?");
			fontRenderer.drawStringWithShadow(notification.getText(), x + -(stringWidth - width) - 5, y + 3f, 0xffffffff);
		}
		System.out.println("call done!!");
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
