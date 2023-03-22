package wdl.gui.notifications.shapes;

import wdl.gui.notifications.shapes.data.CornerType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import wdl.gui.notifications.shapes.data.Position;

// @SuppressWarnings("unused")
public class RoundedRectangle extends Shape {
    private RoundedCorner[] corners;
    private Rectangle mainRectangle;
    private Rectangle[] sideRectangles = new Rectangle[2];
    private Map<CornerType, Rectangle> straightCorners = new HashMap<>();
    // private Rectangle[] straightCorners;
    private int radius;

    public RoundedRectangle(Position position, int radius, int color, CornerType[] enabledCorners) {
        super();
        this.radius = radius;

        // Create rounded corners
        this.corners = new RoundedCorner[enabledCorners.length];
        for(int i = 0; i < enabledCorners.length; i++) {
            corners[i] = new RoundedCorner(enabledCorners[i], null, radius, color);
        }
        // Create map w square corners
        for (CornerType cornerType : CornerType.getOtherCorners(enabledCorners)) {
            straightCorners.put(cornerType, new Rectangle(color));
        }

        // Create rectangle spanning from top to bottom in the middle
        mainRectangle = new Rectangle(color);

        // Create side rectangles
        sideRectangles[0] = new Rectangle(color);
        sideRectangles[1] = new Rectangle(color);

        // Finally, set the position of the rect
        setPosition(position);
    }

    @Override
    public void draw(int xOffset) {
        for (int i = 0; i < corners.length; i++) {
            corners[i].draw(xOffset);
        }

        mainRectangle.draw(xOffset);

        sideRectangles[0].draw(xOffset);
        sideRectangles[1].draw(xOffset);

        for (Rectangle rectangle : straightCorners.values()) {
            rectangle.draw(xOffset);
        }
    }
    @Override
    public void drawToggleAttribs(int xOffset) {
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
    }

    public void setPosition(Position position) {
        if (position == null)
            return;
        
        if (this.radius * 2 > position.right() - position.left())
            System.out.println("2*radius is bigger than the whole rectangle width");
        if (this.radius * 2 > position.top() - position.bottom())
            System.out.println("2*radius is bigger than the whole rectangle height");

        this.position = position;

        // Update corners positions
        for(int i = 0; i < corners.length; i++) {
            RoundedCorner currentCorner = corners[i];
            currentCorner.setPosition(currentCorner.getCornerType().getFixedPositionRounded(position, radius));
        }

        // Update main rectangle position
        mainRectangle.setPosition(new Position(position.left() + radius, position.top(), position.right() - radius, position.bottom()));
        // Update side rectangles positions
        sideRectangles[0].setPosition(new Position(position.left(), position.top() + radius, position.left() + radius, position.bottom() - radius));
        sideRectangles[1].setPosition(new Position(position.right() - radius, position.top() + radius, position.right(), position.bottom() - radius));
        // Update straight corners
        for (Entry<CornerType, Rectangle> entry : straightCorners.entrySet()) {
            entry.getValue().setPosition(
                entry.getKey().getRectanglePosition(position, radius));
        }
    }

    @Override
    public void removeShape() {
    }
}
