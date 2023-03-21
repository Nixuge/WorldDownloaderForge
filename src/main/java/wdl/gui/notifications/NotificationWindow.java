package wdl.gui.notifications;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.RoundedRectangle;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.Position;

public class NotificationWindow {
	@Getter
	@Setter
	private static int xOffsetMaxTime = 60;

	// static positions (no offset, just base ones)
	@Getter
	private int leftS;
	@Getter
	private int topS;

	//TODO: make this work
	private int max_width = Minecraft.getMinecraft().displayWidth >> 2;

	@Getter
	private int width;
	@Getter
	private int height;

	@Getter
	private float lifeTime = 0;
	private float preTime = 0;

	private Notification notification;

	private int stringWidthText;
	private int stringWidthHeader;

	private Minecraft mc = Minecraft.getMinecraft();
	private FontRenderer fontRenderer = mc.fontRendererObj;

	private Tessellator tessellator = Tessellator.getInstance();
	private WorldRenderer worldrenderer = tessellator.getWorldRenderer();

	private RoundedRectangle roundedRectangle;

	public NotificationWindow(Notification notification) {
		this.notification = notification;

		stringWidthText = fontRenderer.getStringWidth(this.notification.getText());
		stringWidthHeader = fontRenderer.getStringWidth(this.notification.getLevel().getHeader());

		if (this.notification.getLevel().getHeader().isEmpty()) {
			this.width = stringWidthText + 15 + stringWidthText;
		} else {
			this.width = stringWidthText + 15 + Math.min(stringWidthText, stringWidthHeader);
		}

		// Maybe eventually make notification size a constant?
		if (this.width > max_width)
			this.width = max_width;

		this.height = 32;

		this.roundedRectangle = new RoundedRectangle(
			null,
			5,
			0x33111111, 
			new CornerType[] {CornerType.TOP_LEFT, CornerType.BOTTOM_LEFT}
		);

	}

	public void update() {
		preTime = lifeTime;
		lifeTime += 1f;
		if (lifeTime < 0f) {
			lifeTime = 0f;
		}
	}

	/**
	 * Function to get the x offset for the notification animation based on how
	 * advanced the notification display is.
	 * Triggers at 0->10% and 90->100% of the notification display.
	 * Unless notification maxTime > 60, in which case it's calculated
	 * as if the maxTime was 60.
	 * 
	 * @return the X offset
	 */
	public int getXoffset(float partialTicks) {
		//TODO: fix small weird bump after slide in animation finished
		// percentages of the notification display at which the animation triggers
		float lowPercent = .1f;
		float highPercent = .9f;

		// calculate time based on both the worldTick & the renderPartialTicks
		// without the renderPartialTicks, the animation looks choppy bc it only runs 20
		// times/sec
		float time = (this.preTime + (this.lifeTime - this.preTime) * partialTicks);
		float timePercent;

		// if notification maxtime is more than ~100, the slide in & out animations look
		// weird, & if more than ~60, isn't fast enough (for me at least)
		// this if/else just makes it so that it's normalized to xOffsetMaxTime if more than xOffsetMaxTime
		if (notification.getMaxTime() > xOffsetMaxTime) {
			timePercent = (time / xOffsetMaxTime);
			float offsetPercentages = notification.getMaxTime() / (float)xOffsetMaxTime;
			lowPercent = lowPercent * offsetPercentages;
			highPercent = highPercent * offsetPercentages;
		} else {
			timePercent = (time / notification.getMaxTime());
		}

		// If trouble understanding this, replace lowPercent & highPercent by
		// their default values from above
		// How this works:
		// 1 - From the total time%, calculate the animation% (from 0 to 1)
		// 2 - Apply a tanh (! needs to be *3 for it, see desmos graph)
		// 3 - Multiply tanh by width, & remove 1x the width
		if (timePercent < lowPercent) {
			double tanh = Math.tanh(timePercent * 10 * 3);
			return (int) (tanh * this.width) - this.width;
		} else if (timePercent > highPercent) {
			// invert the animation percent, so that it goes
			// first slow then fast
			float animationPercent = 1 - ((timePercent - highPercent) * 10);
			double tanh = Math.tanh(animationPercent * 3);
			return (int) (tanh * this.width) - this.width;
		}

		return 0;
	}

	public void draw(float partialTicks) {
		int left = leftS - getXoffset(partialTicks);
		int top = topS;
		int right = left + width;
		int bottom = top + height;

		if (bottom > mc.displayHeight)
			return;
		
		// drawRect(left, top, right, bottom);
		roundedRectangle.draw(getXoffset(partialTicks));
		// drawRect(left, top, right, bottom);
		// drawRounded(left, top, right, bottom);

		fontRenderer.drawString(notification.getText(), left + 3, top + 3 + 16, 0xFFFFFFFF);
		
		Level l = notification.getLevel();
		if (l != Level.NONE) {
			fontRenderer.drawString(l.getHeader(),
				left + (width - (fontRenderer.getStringWidth(l.getHeader()))) / 2, top + 3, l.getColor(), false);
		}
		// Level l = getNotification().getLevel();
		// if (l == Level.ERROR) {
		// fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(),
		// x - xOffset - width + 5, y,
		// 0xffffffff);
		// }
		// if (l == Level.INFO) {
		// fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(),
		// x - xOffset - width + 5, y,
		// 0xffffffff);
		// }
		// if (l == Level.WARNING) {
		// fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(),
		// x - xOffset - width + 5, y,
		// 0xffffffff);
		// }
	}

	public void drawRect(int left, int top, int right, int bottom) {
		// System.out.println("drawing. x:" + left + " y:" + top + " x2:" + right + " y2:" + bottom);
		if (left < right) {
			int i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			int j = top;
			top = bottom;
			bottom = j;
		}
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0.066f, 0.066f, 0.066f, 0.2f);
		
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(left, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, top, 0.0D).endVertex();
		worldrenderer.pos(left, top, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public void fancyPrint(int charLimit, Object... things) {
		String full = "";
		for (int i = 0; i < things.length; i++) {
			String current = things[i].toString();
			if (current.length() > charLimit) {
				full += current.substring(0, charLimit);
			} else {
				full += current;
			}
			if (i % 2 != 0) {
				full += " ";
			}
		}
		System.out.println(full);
	}

	public void drawRounded(int left, int top, int right, int bottom) {
		// roundedRectangle.draw(0);
		// int radius = 5;
		// // System.out.println("drawing rounded:!");
		// // GL11.glEnable(GL11.GL_BLEND);
		// // GL11.glDisable(GL11.GL_TEXTURE_2D);
		// // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// GlStateManager.enableBlend();
		// GlStateManager.disableTexture2D();
		// GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// GlStateManager.color(.2f, .2f, 1.0f, 1.0f);
		// // GL11.glColor4f(.1f, .1f, .6f, .3f);
		// // for (int i = 0; i <= 90; i += 5) {
		// // 	double angle = i * Math.PI / 180.0;
		// // 	double xHere = left + radius + radius * Math.cos(angle);
		// // 	double yHere = top + radius + radius * Math.sin(angle);
		// // 	GL11.glVertex2d(xHere, yHere);
		// // }
		// // GL11.glVertex2i(0,0);

		// worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
		// // worldrenderer.pos(left, bottom, 0.0D).endVertex();
		// // GL11.glBegin(GL11.GL_POLYGON);
		

		// // GL11.glVertex2i(100,100);
		// for (int i = 180; i < 270; i+=2) {
		// 	double angleRad = i * Math.PI / 180.0;
		// 	double xHere = 50 + radius * Math.cos(angleRad);
		// 	double yHere = 50 + radius * Math.sin(angleRad);
			
		// 	worldrenderer.pos(xHere, yHere, 0).endVertex();
		// 	worldrenderer.pos(50, 50 , 0).endVertex();
		// }
		
		// // for (int i = 90; i <= 180; i += 5) {
		// // 	double angle = i * Math.PI / 180;
		// // 	GL11.glVertex2d(right - radius + radius * Math.cos(angle), top + radius + radius * Math.sin(angle));
		// // }
		// // for (int i = 180; i <= 270; i += 5) {
		// // 	double angle = i * Math.PI / 180;
		// // 	GL11.glVertex2d(right - radius + radius * Math.cos(angle), bottom - radius + radius * Math.sin(angle));
		// // }
		// // for (int i = 270; i <= 360; i += 5) {
		// // 	double angle = i * Math.PI / 180;
		// // 	GL11.glVertex2d(left + radius + radius * Math.cos(angle), bottom - radius + radius * Math.sin(angle));
		// // }
		// tessellator.draw();
		// // GL11.glEnd();
		// GlStateManager.enableTexture2D();
		// GlStateManager.disableBlend();
	}

	public void setPosition(int x, int y) {
		int newLeftS = x - width;
		int newTopS = y - height;
		if (this.leftS == newLeftS && this.topS == newTopS)
			return;
		
		this.leftS = newLeftS;
		this.topS = newTopS;

		// this.max_width = x;
		this.roundedRectangle.setPosition(new Position(leftS, topS, leftS + width, topS + height));
	}
}
