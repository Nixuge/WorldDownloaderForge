package wdl.gui.notifications.shapes.rectangle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import wdl.gui.notifications.shapes.base.Shape;
import wdl.gui.notifications.shapes.base.ShapeNoDraw;

public abstract class RectangleShape extends Shape implements ShapeNoDraw {
    @Override
    public void drawPositions(int xOffset) {
        worldrenderer.pos(position.left() - xOffset, position.bottom(), 0).endVertex();
        worldrenderer.pos(position.right() - xOffset, position.bottom(), 0).endVertex();
        worldrenderer.pos(position.right() - xOffset, position.top(), 0).endVertex();
        worldrenderer.pos(position.left() - xOffset, position.top(), 0).endVertex();
    }

    @Override
    public void drawToggleAttribs(int xOffset) {
        toggleOnAttribs();
        
        draw(xOffset);

        toggleOffAttribs();
    }

    @Override
    public void toggleOnAttribs() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void toggleOffAttribs() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
