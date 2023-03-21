package wdl.gui.notifications.shapes;

import wdl.gui.notifications.shapes.data.CornerType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.data.Size;

// @SuppressWarnings("unused")
public class RoundedRectangle extends Shape {
    // private final RoundedCornerType[] enabledCorners;
    private RoundedCorner[] corners;
    private Rectangle mainRectangle;
    private Rectangle[] sideRectangles = new Rectangle[2];
    private Map<CornerType, Rectangle> straightCorners = new HashMap<>();
    // private Rectangle[] straightCorners;
    private Size size;
    private int radius;

    public RoundedRectangle(Position position, Size size, int radius, int color, CornerType[] enabledCorners) {
        super();
        this.radius = radius;
        this.size = size;
        // this.enabledCorners = enabledCorners;
        // Create corners
        this.corners = new RoundedCorner[enabledCorners.length];
        for(int i = 0; i < enabledCorners.length; i++) {
            corners[i] = new RoundedCorner(enabledCorners[i], null, radius, color);
        }
        
        // Create rectangle spanning from top to bottom in the middle
        mainRectangle = new Rectangle(null, color);
        // Create side rectangles
        sideRectangles[0] = new Rectangle(null, color);
        sideRectangles[1] = new Rectangle(null, color);
        // Create map w corner rectangles
        for (CornerType cornerType : CornerType.getOtherCorners(enabledCorners)) {
            straightCorners.put(cornerType, new Rectangle(null, color));
        }

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

    public void setPosition(Position position) {
        if (position == null)
            return;
        
        this.position = position;

        // Update corners positions
        for(int i = 0; i < corners.length; i++) {
            RoundedCorner currentCorner = corners[i];
            currentCorner.setPosition(currentCorner.getCornerType().getFixedPositionRounded(position, size, radius));
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
