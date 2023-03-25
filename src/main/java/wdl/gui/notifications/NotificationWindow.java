package wdl.gui.notifications;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.builders.RoundedCornerBuilder;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.roundedcorner.RoundedCornerBorder;
import wdl.gui.notifications.shapes.roundedrectangle.RoundedRectangleFill;

public class NotificationWindow {
    @Getter
    @Setter
    private static int xOffsetMaxTime = 80;

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

    private RoundedRectangleFill roundedRectangle;
    private RoundedCornerBorder borderTest;

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

        this.roundedRectangle = new RoundedRectangleFill(
            null,
            5,
            0x33111111, 
            new CornerType[] {CornerType.TOP_LEFT, CornerType.BOTTOM_LEFT}
        );
        // this.borderTest = new 
        this.borderTest = new RoundedCornerBuilder()
                .setCornerType(CornerType.TOP_LEFT)
                .setColor(0xFFFFFFFF)
                .setRadius(5)
                // .setBorderWidth(3)
                .buildBorder();
        


        // this.borderTest = new RoundedCornerBorder(CornerType.TOP_LEFT, null, 5, 0xFFFFFFFF, 3);

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
        // percentages of the notification display at which the animation triggers
        float lowPercent = .1f;
        float highPercent = .9f;

        // calculate time based on both the worldTick & the renderPartialTicks
        // without the renderPartialTicks, the animation looks choppy bc it only runs 20
        // times/sec
        float time = (this.preTime + (this.lifeTime - this.preTime) * partialTicks);
        float timePercent;

        // if notification maxtime is more than ~100, the slide in & out animations look
        // weird, & if more than ~80, isn't fast enough (for me at least)
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
            if (tanh > .99f) // avoid having a "bump" when the animation is already "finished"
                return 0;
            return (int) (tanh * this.width) - this.width;

        } else if (timePercent > highPercent) {
            // invert the animation percent, so that it goes
            // first slow then fast
            float animationPercent = 1 - ((timePercent - highPercent) * 10);
            double tanh = Math.tanh(animationPercent * 3);
            if (tanh > .99f) // same as above
                return 0; 
            return (int) (tanh * this.width) - this.width;
        }

        return 0;
    }

    public void draw(float partialTicks) {
        int left = leftS - getXoffset(partialTicks);
        // fancyPrint(50, "ok:", left);
        int top = topS;
        // int right = left + width;
        int bottom = top + height;

        if (bottom > mc.displayHeight)
            return;
        
        // drawRect(left, top, right, bottom);
        int xOffset = getXoffset(partialTicks);

        GlStateManager.pushAttrib();
        // GL11.


        GlStateManager.popAttrib();




        roundedRectangle.drawToggleAttribs(xOffset);
        borderTest.drawToggleAttribs(xOffset);
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

    public void drawRounded(int left, int top, int right, int bottom) {}

    public void setPosition(int x, int y) {
        // + 1 because of scaling problems 
        int newLeftS = x - width + 1;
        int newTopS = y - height;
        if (this.leftS == newLeftS && this.topS == newTopS)
            return;
        
        this.leftS = newLeftS;
        this.topS = newTopS;

        // this.max_width = x;
        this.roundedRectangle.setPosition(new Position(leftS, topS, leftS + width, topS + height));
        this.borderTest.setPosition(new Position(leftS + 5, topS + 5, 0, 0));
    }
}
