package wdl.gui.notifications;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class NotificationWindow {
	@Getter
	@Setter
	private static int xOffsetMaxTime = 60;

	@Getter
	private int x;
	@Getter
	private int y;

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

	public NotificationWindow(Notification notification) {
		this.notification = notification;

		stringWidthText = fontRenderer.getStringWidth(this.notification.getText());
		stringWidthHeader = fontRenderer.getStringWidth(this.notification.getLevel().getHeader());

		if (this.getNotification().getLevel().getHeader().isEmpty()) {
			this.width = stringWidthText + 15 + stringWidthText;
		} else {
			this.width = stringWidthText + 15 + Math.max(stringWidthText, stringWidthHeader);
		}

		// Maybe eventually make notification size a constant?
		if (this.width > max_width)
			this.width = max_width;

		this.height = 16;
	}

	public void update() {
		preTime = lifeTime;
		lifeTime += 1f;
		if (lifeTime < 0f) {
			lifeTime = 0f;
		}
	}

	public Notification getNotification() {
		return notification;
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
		if (timePercent < lowPercent) {
			double ttan = Math.tanh(timePercent * 10 * 3);
			return (int) (ttan * this.width - this.width);
		} else if (timePercent > highPercent) {
			// TODO: try and invert that
			// (starts slow & gets fast)
			double ttan = Math.tanh((-(timePercent - highPercent)) * 10 * 3);
			return (int) (ttan * this.width);
		}

		return 0;
	}

	public void draw(float partialTicks) {
		// For some reason need this, even w the color in the drawRect
		// GlStateManager.color(0f, 0f, 0f);
		

		int left = x - getXoffset(partialTicks);
		int top = y;
		int right = left + width;
		int bottom = top + height;


		if (bottom > mc.displayHeight)
			return;
		
		drawRect(left, top, right, bottom);

		fontRenderer.drawString(notification.getText(), 0, 0, 0xFFFFFFFF);

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

		// fontRenderer.drawString(notification.getText(), x - xOffset - width, y -
		// height + 3f, 0xffffffff, false);
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

	public void setPosition(int x, int y) {
		// x >> 1 == x/2

		this.x = x - width;
		this.y = y - height;

		this.max_width = x;
	}
}
