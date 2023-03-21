package wdl.gui.notifications.shapes;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.RoundedCornerType;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.data.PositionSize;

@SuppressWarnings("unused")
public class RoundedRectangle extends Shape {
    private final RoundedCornerType[] enabledCorners;
    private RoundedCorner[] corners;
    private int radius;

    public RoundedRectangle(PositionSize positionSize, int radius, RoundedCornerType[] enabledCorners) {
        this.radius = radius;
        this.enabledCorners = enabledCorners;
        this.corners = new RoundedCorner[enabledCorners.length];
        for(int i = 0; i < enabledCorners.length; i++) {
            corners[i] = new RoundedCorner(enabledCorners[i], positionSize.getAsPosition(), radius);
        } 
    }

    @Override
    public void draw(int xOffset) {
        for (int i = 0; i < corners.length; i++) {
            corners[i].draw(xOffset);
        }
    }

    public void setPosition(Position position) {
        if (position == null)
            return;
        
        this.position = position;
        // update corners positions
        for(int i = 0; i < corners.length; i++) {
            RoundedCorner currentCorner = corners[i];
            currentCorner.setPosition(currentCorner.getCornerType().getFixedPosition(position, radius));
        } 
    }

    @Override
    public void removeShape() {
    }
}
