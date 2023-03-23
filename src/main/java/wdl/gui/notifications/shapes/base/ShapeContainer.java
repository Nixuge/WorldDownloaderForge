package wdl.gui.notifications.shapes.base;

import lombok.Setter;
import wdl.gui.notifications.shapes.data.Position;

@Setter
public abstract class ShapeContainer {
    protected int xOffset;
    protected Position position;

    protected int rawColor;
    protected float alpha;
    protected float red;
    protected float green;
    protected float blue;


    public ShapeContainer(Position position, int color) {
        this();
        setPosition(position);
        setColor(color);
    }

    public ShapeContainer(int color) {
        this();
        setColor(color);
    }

    public ShapeContainer(){}

    public void setColor(int color) {
        this.rawColor = color;
        alpha = (float)(color >> 24 & 255) / 255.0F;
        red = (float)(color >> 16 & 255) / 255.0F;
        green = (float)(color >> 8 & 255) / 255.0F;
        blue = (float)(color & 255) / 255.0F;
    }

    /**
     * Must be overriden to set the position of sub shapes present in the container.
     * Note: same as Shape's setPosition, this must be called at least once before
     * actually drawing the elements
     * 
     * @param position position to set
     */
    public abstract void setPosition(Position position);
    
    /**
     * Draw the element on the screen, without toggling on/off GlStateManager attribs.
     * Use only if you have another function calling those toggles, otherwise use drawToggleAttribs(...)
     * 
     * @param xOffset x position of the shape will be reduced by xOffset
     */
    public abstract void draw(int xOffset);
    
    /**
     * Wrapper for draw(...) that toggles on & off GlStateManager attribs.
     * 
     * @param xOffset x position of the shape will be reduced by xOffset
     */
    public abstract void drawToggleAttribs(int xOffset);
}
