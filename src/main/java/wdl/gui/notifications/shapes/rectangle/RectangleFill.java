package wdl.gui.notifications.shapes.rectangle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;

public class RectangleFill extends RectangleShape {
    public RectangleFill(Position position, int color) {
        setColor(color);
        setPosition(position);
    }

    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        worldrenderer.pos(position.left() - xOffset, position.bottom(), 0).endVertex();
        worldrenderer.pos(position.right() - xOffset, position.bottom(), 0).endVertex();
        worldrenderer.pos(position.right() - xOffset, position.top(), 0).endVertex();
        worldrenderer.pos(position.left() - xOffset, position.top(), 0).endVertex();

        tessellator.draw();
    }
}
