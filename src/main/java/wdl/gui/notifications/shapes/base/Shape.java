package wdl.gui.notifications.shapes.base;

import lombok.Setter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import wdl.gui.notifications.shapes.data.Position;

@Setter
public abstract class Shape {
    protected static Tessellator tessellator = Tessellator.getInstance();
    protected static WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    protected int xOffset;
    protected Position position;

    protected int rawColor;
    protected float alpha;
    protected float red;
    protected float green;
    protected float blue;

    public Shape(Position position, int color) {
        setPosition(position);
        setColor(color);
    }

    public Shape(int color) {
        setColor(color);
    }

    public Shape(){}

    public void setColor(int color) {
        this.rawColor = color;
        alpha = (float)(color >> 24 & 255) / 255.0F;
        red = (float)(color >> 16 & 255) / 255.0F;
        green = (float)(color >> 8 & 255) / 255.0F;
        blue = (float)(color & 255) / 255.0F;
    }

    // Note:
    // Due to how the code is structured here, it is required that setPosition is called
    // at least ONCE with a non-null position before drawing, otherwise a crash would happen.
    // This always happens here because of how NotificationWindow.setPosition(...) and
    // NotificationManager.draw(...) work, but need to keep that in mind if reusing it
    // somewhere else.
    public void setPosition(Position position) {
        if (position == null)
            return;
        this.position = position;
    }
    
    /**
     * Draw the element on the screen, without toggling on/off GlStateManager attribs.
     * Use only if you have another function calling those toggles, otherwise use drawToggleAttribs(...).
     * Meant to be overwritten only in the final (filled/border/...) shape.
     * 
     * @param xOffset x position of the shape will be reduced by xOffset
     */
    public abstract void draw(int xOffset);
    /**
     * Wrapper for draw(...) that toggles on & off GlStateManager/GL11 attribs.
     * 
     * @param xOffset x position of the shape will be reduced by xOffset
     */
    public abstract void drawToggleAttribs(int xOffset);
}
