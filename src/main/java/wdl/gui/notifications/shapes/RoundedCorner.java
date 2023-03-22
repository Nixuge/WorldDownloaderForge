package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.data.CornerType;

@Getter
public class RoundedCorner extends Shape {
    private static int ROUNDING_STEP = 1;

    private int radius;
    private CornerType cornerType;

    public RoundedCorner(CornerType cornerType, Position position, int radius, int color) {
        super(color);
        this.cornerType = cornerType;
        this.radius = radius;
        setPosition(position);
    }
    
    // 2do: pre calculate this & save calculations
    // Will see since xOffset changes & performance is negligible
    // TODO: fix function apparently drawing twice?
    @Override
    public void draw(int xOffset) {
        // float tempA = alpha / 2;
        // System.out.println("alpha:" + alpha);
        GlStateManager.color(red, green, blue, alpha);
        
        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        
        int posLeft = position.left() - xOffset;
        int posTop = position.top();
        // worldrenderer.pos(posLeft + radius, posTop + radius, 0).endVertex();
        // worldrenderer.pos(posLeft + (radius/2), posTop, 0).endVertex();
        // worldrenderer.pos(posLeft, posTop + (radius/2), 0).endVertex();
        // top ; left+ (radius/2)
        // top + (radius/2) ; left
        // top + radius ; left + radius;

        for (int i = cornerType.getStartingDegree(); i <= cornerType.getEndingDegree() ; i += ROUNDING_STEP) {
            double angleRad = i * Math.PI / 180.0;
            double xHere = posLeft + radius * Math.cos(angleRad);
            double yHere = posTop + radius * Math.sin(angleRad);
            worldrenderer.pos(xHere, yHere, 0).endVertex();
            // if (i % 2 == 0) {
                worldrenderer.pos(posLeft, posTop , 0).endVertex();
            // }
        }
        // worldrenderer.pos(posLeft + radius, posTop + radius, 0).endVertex();

        // Can't see any way to enable that without using 
        // GL11 directly unfortunately
        // Doesn't seem to cause any issue tho.
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH); 
        tessellator.draw();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
    }
    @Override
    public void drawToggleAttribs(int xOffset) {
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
    }

    @Override
    public void removeShape() {
        // TODO Auto-generated method stub
    }
}
