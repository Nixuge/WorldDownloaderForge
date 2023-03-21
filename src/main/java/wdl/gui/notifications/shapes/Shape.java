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

    public Shape(Position position) {
        this();
        setPosition(position);
    }
    public Shape(){
        shapeInstances.add(this);
    }

    public void setPosition(Position position) {
        if (position == null)
            return;
        this.position = position;
    }
    
    public abstract void draw(int xOffset);
    public abstract void removeShape();
}
