package wdl.gui.notifications.shapes;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import wdl.gui.notifications.shapes.data.Position;

@Setter
public abstract class Shape {
    protected static Tessellator tessellator = Tessellator.getInstance();
    protected static WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    private static List<Shape> shapeInstances = new ArrayList<>();

    protected int xOffset;
    protected Position position;

    protected int rawColor;
    protected float alpha;
    protected float red;
    protected float green;
    protected float blue;


    public Shape(Position position, int color) {
        this();
        setPosition(position);
        setColor(color);
    }

    public Shape(int color) {
        this();
        setColor(color);
    }

    public Shape(){
        shapeInstances.add(this);
    }

    public void setColor(int color) {
        this.rawColor = color;
        alpha = (float)(color >> 24 & 255) / 255.0F;
        red = (float)(color >> 16 & 255) / 255.0F;
        green = (float)(color >> 8 & 255) / 255.0F;
        blue = (float)(color & 255) / 255.0F;
    }

    public void setPosition(Position position) {
        if (position == null)
            return;
        this.position = position;
    }
    
    public abstract void draw(int xOffset);
    public abstract void removeShape();
}
