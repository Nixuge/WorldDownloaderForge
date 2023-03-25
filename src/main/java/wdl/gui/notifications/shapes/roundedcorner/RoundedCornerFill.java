package wdl.gui.notifications.shapes.roundedcorner;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.Position;

@Getter
public class RoundedCornerFill extends RoundedCornerShape {
    public RoundedCornerFill(CornerType cornerType, Position position, int radius, int color) {
        super(cornerType, position, radius, color);
    }
    
    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);

        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);

        drawPositions(xOffset);

        // Draw the point at the angle start to fill the rest
        // This isn't in the superclass because the border doesn't need it
        worldrenderer.pos(position.left() - xOffset, position.top() , 0).endVertex();

        // Note:
        // GL_POLYGON_SMOOTH is having an issue. When used, the opacity is greatly reduced.
        // I can't seem to find any easy fix. However, this already looks "good enough".
        // GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        // GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        tessellator.draw();
        // GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
    }
}
