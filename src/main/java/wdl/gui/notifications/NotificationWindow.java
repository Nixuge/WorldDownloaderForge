package wdl.gui.notifications; 

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Timer;
import wdl.ReflectionUtils;

public class NotificationWindow {
	private int x;
	private int y;
	
	private int width;
	private int height;
	
	private float time = 0;
	private float preTime = 0;
	
	private Notification notification;
	
	private int stringWidth = 0;

	private FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
	
	public NotificationWindow(Notification notification) {
		this.notification = notification;
		stringWidth = fontRenderer.getStringWidth(this.notification.getText());
		if(this.getNotification().getLevel().getHeader().isEmpty()) {
			this.width = stringWidth + 11;
		}
		else {
			this.width = stringWidth + 15 + fontRenderer.getStringWidth(this.getNotification().getLevel().getHeader());
		}
		
		this.height = 16;
	}
	
	public void update() {
		preTime = time;
		time += 1f;
		if(time < 0f) {
			time = 0f;
		}
	}
	
	public Notification getNotification() {
		return notification;
	}
	
	public void draw() {
		Timer timer = ReflectionUtils.findAndGetPrivateField(Minecraft.getMinecraft(), Timer.class);
		float time = (this.preTime + (this.time - this.preTime) * timer.renderPartialTicks);
		float maxTime = getNotification().getText().length() * 3;
		float timePercent = (time / maxTime);
		glEnable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		float alpha = 1;
		
		if(timePercent < 0.1f) {
			alpha = timePercent * 10f;
		}
		
		if(timePercent > 0.9f) {
			alpha = -(timePercent - 1f) * 10f;
		}
		
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
			if(l == Level.ERROR) {
				fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
			}
			if(l == Level.INFO) {
				fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
			}
			if(l == Level.WARNING) {
				fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x + 5, y + 3f, 0xffffffff);
			}
			
			fontRenderer.drawStringWithShadow(notification.getText(), x + -(stringWidth - width) - 5, y + 3f, 0xffffffff);
		}
		
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
