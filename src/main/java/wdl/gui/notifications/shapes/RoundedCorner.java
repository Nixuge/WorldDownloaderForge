package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.data.RoundedCornerType;

@Getter
public class RoundedCorner extends Shape {
    private int radius;
    private RoundedCornerType cornerType;
    
    public RoundedCorner(RoundedCornerType cornerType, Position position, int radius) {
        this.cornerType = cornerType;
        this.radius = radius;
        setPosition(position);
    }
    
    //TODO: pre calculate this & save calculations
    @Override
    public void draw(int xOffset) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(.2f, .2f, 1.0f, 1.0f);

		worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);

	
        int posLeft = position.left() - xOffset;
        int posTop = position.top();
		for (int i = cornerType.getStartingDegree(); i < cornerType.getEndingDegree(); i+=2) {
			double angleRad = i * Math.PI / 180.0;
			double xHere = posLeft + radius * Math.cos(angleRad);
			double yHere = posTop + radius * Math.sin(angleRad);
			
			worldrenderer.pos(xHere, yHere, 0).endVertex();
			worldrenderer.pos(posLeft, posTop , 0).endVertex();
		}
		tessellator.draw();
		// GL11.glEnd();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public void removeShape() {
        // TODO Auto-generated method stub
    }
}
