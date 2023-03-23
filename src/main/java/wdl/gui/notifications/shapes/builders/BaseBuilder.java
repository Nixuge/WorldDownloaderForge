package wdl.gui.notifications.shapes.builders;

import wdl.gui.notifications.shapes.data.Position;

// SuppressWarnings to avoid the "unchecked cast" warnings when returning the builder
@SuppressWarnings("unchecked")
public abstract class BaseBuilder<T extends BaseBuilder<T>> {
    protected Position position;
    protected int color;
    protected float borderWidth = 1f;

    public T setPosition(Position position) {
        this.position = position;
        return (T) this;
    }
    
    public T setColor(int color) {
        this.color = color;
        return (T) this;
    }

    public T setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        return (T) this;
    }
}
