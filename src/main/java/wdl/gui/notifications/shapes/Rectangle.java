package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;

public class Rectangle extends Shape {

    public Rectangle(Position position, int color) {
        super(color);
        setPosition(position);
    }

    @Override
    public void draw(int xOffset) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(red, green, blue, alpha);
		
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

		worldrenderer.pos(position.left() - xOffset, position.bottom(), 0).endVertex();
		worldrenderer.pos(position.right() - xOffset, position.bottom(), 0).endVertex();
		worldrenderer.pos(position.right() - xOffset, position.top(), 0).endVertex();
		worldrenderer.pos(position.left() - xOffset, position.top(), 0).endVertex();

		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
    }

    @Override
    public void removeShape() {
    }
}
