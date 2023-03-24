package wdl.gui.notifications.shapes.roundedcorner;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.base.ShapeNoDraw;
import wdl.gui.notifications.shapes.base.ShapeRounded;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.Position;

@Getter
public class RoundedCornerShape extends ShapeRounded implements ShapeNoDraw {
    protected CornerType cornerType;

    public RoundedCornerShape(CornerType cornerType, Position position, int radius, int color) {
        super(color, radius);
        this.cornerType = cornerType;
        setPosition(position);
    }
    
    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);

        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);

        // Draw every pre-calculated position
        for (double[] position : this.positions) {
            worldrenderer.pos(position[0] - xOffset, position[1] , 0).endVertex();
        }
        // Draw the point at the angle start to fill the rest
        worldrenderer.pos(position.left() - xOffset, position.top() , 0).endVertex();

        // Note:
        // GL_POLYGON_SMOOTH is having an issue, 
        // due to the number of polygans, the opacity is greatly reduced.
        // I can't seem to find any easy fix.
        // However, this already looks "good enough".
        // GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        // GlStateManager.enableBlend();
        tessellator.draw();
        // GlStateManager.disableBlend();
        // GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
    }

    @Override
    public void setPosition(Position position) {
        if (position == null)
            return;
        calculateRoundPositions(position.left(), position.top(), cornerType.getStartingDegree(), cornerType.getEndingDegree());
        super.setPosition(position);
    }

    @Override
    public void drawToggleAttribs(int xOffset) {
        // toggleOnAttribs();
        
        // draw(xOffset);

        // toggleOffAttribs();
    }

    // @Override
    public void drawPositions(int xOffset) {
        for (double[] position : this.positions) {
            worldrenderer.pos(position[0] - xOffset, position[1] , 0).endVertex();
        }
    }

    @Override
    public void toggleOnAttribs() {
        GlStateManager.pushAttrib();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    @Override
    public void toggleOffAttribs() {
        GlStateManager.popAttrib();
    }
}
