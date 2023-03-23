package wdl.gui.notifications.shapes.raw;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.base.Shape;
import wdl.gui.notifications.shapes.data.Position;

public abstract class RectangleShape extends Shape {
    public RectangleShape(int color) {
        super(color);
    }

    public RectangleShape(Position position, int color) {
        this(color);
        setPosition(position);
    }

    @Override
    public void draw(int xOffset) {
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        tessellator.draw();
    }

    public void drawShapeVertexes(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);
        worldrenderer.pos(position.left() - xOffset, position.bottom(), 0).endVertex();
        worldrenderer.pos(position.right() - xOffset, position.bottom(), 0).endVertex();
        worldrenderer.pos(position.right() - xOffset, position.top(), 0).endVertex();
        worldrenderer.pos(position.left() - xOffset, position.top(), 0).endVertex();
    }

    @Override
    public void drawToggleAttribs(int xOffset) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
