package wdl.gui.notifications;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Timer;
import wdl.ReflectionUtils;

public class NotificationWindow {
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
	private Timer timer = ReflectionUtils.findAndGetPrivateField(mc, Timer.class);

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
	 * Unless notification maxTime > 100, in which case it's calculated
	 * as if the maxTime was 100.
	 * 
	 * @return the X offset
	 */
	public int getXoffset() {
		// percentages of the notification display at which the animation triggers
		float lowPercent = .1f;
		float highPercent = .9f;

		// calculate time based on both the worldTick & the renderPartialTicks
		// without the renderPartialTicks, the animation looks choppy bc it only runs 20
		// times/sec
		float time = (this.preTime + (this.lifeTime - this.preTime) * timer.renderPartialTicks);
		float timePercent;

		// if notification maxtime is more than ~100, the slide in & out animations look
		// weird
		// this if/else just makes it so that it's normalized to 100 if more than 100
		if (notification.getMaxTime() > 100) {
			timePercent = (time / 100);
			float offsetPercentages = notification.getMaxTime() / 100f;
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
			double ttan = Math.tanh((-(timePercent - highPercent)) * 10 * 3);
			return (int) (ttan * this.width);
		}

		return 0;
	}

	public void draw() {
		// For some reason need this, even w the color in the drawRect
		GlStateManager.color(0f, 0f, 0f);

		int xOffset = getXoffset();

		Gui.drawRect(x - xOffset, y, x - width - xOffset, y - height, 0x33111111);

		
		// Level l = getNotification().getLevel();
		// if (l == Level.ERROR) {
		// 	fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x - xOffset - width + 5, y,
		// 			0xffffffff);
		// }
		// if (l == Level.INFO) {
		// 	fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x - xOffset - width + 5, y,
		// 			0xffffffff);
		// }
		// if (l == Level.WARNING) {
		// 	fontRenderer.drawStringWithShadow(this.getNotification().getLevel().getHeader(), x - xOffset - width + 5, y,
		// 			0xffffffff);
		// }

		fontRenderer.drawString(notification.getText(), x - xOffset - width, y - height + 3f, 0xffffffff, false);
	}

	public void setPosition(int displayWidth, int displayHeight) {
		// x >> 1 == x/2

		this.x = displayWidth >> 1;
		this.y = displayHeight >> 1;

		this.max_width = displayWidth;
	}
}
