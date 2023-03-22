package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.base.Shape;
import wdl.gui.notifications.shapes.data.Position;

public class RectangleFill extends Shape {
    public RectangleFill(int color) {
        super(color);
    }

    public RectangleFill(Position position, int color) {
        this(color);
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

    @Override
    public void drawToggleAttribs(int xOffset) {
        // GlStateManager.pushMatrix();
        // GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        // GlStateManager.popAttrib();
        // GlStateManager.popMatrix();
    }
}
