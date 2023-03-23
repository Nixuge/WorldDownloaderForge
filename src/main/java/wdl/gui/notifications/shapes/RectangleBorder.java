package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.raw.RectangleShape;

public class RectangleBorder extends RectangleShape {
    float borderWidth = 5;
    public RectangleBorder(int color) {
        super(color);
    }

    public RectangleBorder(int color, float borderWidth) {
        this(color);
        this.borderWidth = borderWidth;
    }

    public RectangleBorder(Position position, int color) {
        this(color);
        setPosition(position);
    }

    public RectangleBorder(Position position, int color, float borderWidth) {
        this(color, borderWidth);
        setPosition(position);
    }

    @Override
    public void draw(int xOffset) {
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        GL11.glLineWidth(borderWidth);

        drawShapeVertexes(xOffset);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        tessellator.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}