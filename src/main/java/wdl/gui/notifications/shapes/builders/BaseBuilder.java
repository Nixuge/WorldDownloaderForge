package wdl.gui.notifications.shapes.builders;

import java.util.Map;

import wdl.gui.notifications.shapes.data.BorderPosition;
import wdl.gui.notifications.shapes.data.Position;

// SuppressWarnings to avoid the "unchecked cast" warnings when returning the builder
@SuppressWarnings("unchecked")
public abstract class BaseBuilder<T extends BaseBuilder<T>> {
    protected Position position;
    protected int color;
    protected Map<BorderPosition, Float> enabledBorders;
    protected int radius = 5;

    /**
     * Sets the position of the shape
     * 
     * @param position Position
     */
    public T setPosition(Position position) {
        this.position = position;
        return (T) this;
    }

    /**
     * Sets the color of the shape
     * 
     * @param color Color in ARGB format (eg. 0xFF111188 = max opacity blue)
     */
    public T setColor(int color) {
        this.color = color;
        return (T) this;
    }

    /**
     * Sets the border width, only useful on "xBorder" shapes
     * 
     * @param borderWidth Border width
     */
    public T setEnabledBorders(Map<BorderPosition, Float> enabledBorders) {
        this.enabledBorders = enabledBorders;
        return (T) this;
    }

    /**
     * 
     * Sets the corner radius(es), only useful on "RoundedX" shapes
     * 
     * @param setRadius Corner radius
     */
    public T setRadius(int radius) {
        this.radius = radius;
        return (T) this;
    }
}
