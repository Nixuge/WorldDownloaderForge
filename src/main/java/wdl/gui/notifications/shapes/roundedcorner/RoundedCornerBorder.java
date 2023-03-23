package wdl.gui.notifications.shapes.roundedcorner;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.base.ShapeRounded;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.Position;

@Getter
public class RoundedCornerBorder extends ShapeRounded {
    private CornerType cornerType;

    public RoundedCornerBorder(CornerType cornerType, Position position, int radius, int color) {
        super(color, radius);
        this.cornerType = cornerType;
        setPosition(position);
    }
    
    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(3);

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        // Draw every pre-calculated position
        for (double[] position : this.positions) {
            worldrenderer.pos(position[0] - xOffset, position[1] , 0).endVertex();
        }

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        tessellator.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
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
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
    }
}
