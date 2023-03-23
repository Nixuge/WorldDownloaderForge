package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.raw.RectangleShape;

public class RectangleFill extends RectangleShape {
    public RectangleFill(int color) {
        super(color);
    }

    public RectangleFill(Position position, int color) {
        this(color);
        setPosition(position);
    }

    @Override
    public void draw(int xOffset) {
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawShapeVertexes(xOffset);
        
        tessellator.draw();
    }
}
