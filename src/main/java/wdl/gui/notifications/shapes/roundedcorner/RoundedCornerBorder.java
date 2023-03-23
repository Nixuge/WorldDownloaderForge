package wdl.gui.notifications.shapes.roundedcorner;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.Position;

@Getter
public class RoundedCornerBorder extends RoundedCornerShape {
    private float borderWidth;

    public RoundedCornerBorder(CornerType cornerType, Position position, int radius, int color, float borderWidth) {
        super(cornerType, position, radius, color);
        this.borderWidth = borderWidth;
    }
    
    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(3);

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        drawPositions(xOffset);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        tessellator.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}
