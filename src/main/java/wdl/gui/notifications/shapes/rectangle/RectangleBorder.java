package wdl.gui.notifications.shapes.rectangle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;

public class RectangleBorder extends RectangleShape {
    private float borderWidth;
    public RectangleBorder(Position position, int color, float borderWidth) {
        setColor(color);
        setPosition(position);
        this.borderWidth = borderWidth;
    }

    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(borderWidth);
        
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        drawPositions(xOffset);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        tessellator.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}
