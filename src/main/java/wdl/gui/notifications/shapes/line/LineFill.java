package wdl.gui.notifications.shapes.line;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.base.Shape;
import wdl.gui.notifications.shapes.data.Position;

public class LineFill extends Shape {
    float lineWidth = 2;

    public LineFill(Position position, float lineWidth) {
        setPosition(position);
        this.lineWidth = lineWidth;
    }

    // Note:
    // Position used as:
    // left, top = x1, y1
    // right, bottom = x2, y2

    @Override
    public void draw(int xOffset) {
        GL11.glLineWidth(lineWidth);
        
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        worldrenderer.pos(position.left(), position.top(), 0);
        worldrenderer.pos(position.right(), position.bottom(), 0);

        tessellator.draw();
    }

    @Override
    public void drawToggleAttribs(int xOffset) {
        // TODO 
    }
}
