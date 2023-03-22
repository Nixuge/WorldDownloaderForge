package wdl.gui.notifications.drawing.shapes;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.drawing.base.ShapeRounded;
import wdl.gui.notifications.drawing.data.CornerType;
import wdl.gui.notifications.drawing.data.Position;

@Getter
public class RoundedCorner extends ShapeRounded {
    private CornerType cornerType;

    public RoundedCorner(CornerType cornerType, int radius, int color) {
        super(color, radius);
        this.cornerType = cornerType;
    }

    public RoundedCorner(CornerType cornerType, Position position, int radius, int color) {
        this(cornerType, radius, color);
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
        
        // GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        // GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 1);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        // GlStateManager.popAttrib();
    }

    @Override
    public void removeShape() {
        // TODO Auto-generated method stub
    }
}
