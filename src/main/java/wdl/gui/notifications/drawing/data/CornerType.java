package wdl.gui.notifications.drawing.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

enum Offset {
    NONE,
    X,
    Y,
    X_Y
}

@Getter
public enum CornerType {
    TOP_LEFT(180, Offset.NONE), 
    TOP_RIGHT(270, Offset.X), 
    BOTTOM_LEFT(90, Offset.Y), 
    BOTTOM_RIGHT(0, Offset.X_Y);
    
    private final int startingDegree;
    private final int endingDegree;
    private final Offset offset;
    
    CornerType(int startingDegree, Offset offset) {
        this.startingDegree = startingDegree;
        this.endingDegree = startingDegree + 90;
        this.offset = offset;
    }

    /**
     * @param position Position of the containing RoundedRectangle
     * @param size Dimensions of the containing RoundedRectangle 
     * @param radius Radius of the rounded corner
     * @return Position fixed to match the corner
     */
    public Position getFixedPositionRounded(Position position, int radius) {
        int width = position.right() - position.left();
        int height = position.top() - position.bottom();

        int xChange = (this.offset == Offset.X || this.offset == Offset.X_Y) ? position.right() - radius : position.left() + radius;
        int yChange = (this.offset == Offset.Y || this.offset == Offset.X_Y) ? position.bottom() - radius : position.top() + radius;
        return new Position(
            xChange,
            yChange,
            xChange + width,
            yChange + height
        );
    }

    public Position getRectanglePosition(Position position, int radius) {
        // straight up logic
        switch (this) {
            case TOP_LEFT:
                return new Position(position.left(), position.top(), position.left() + radius, position.top() + radius);
            case TOP_RIGHT:
                return new Position(position.right() - radius, position.top(), position.right(), position.top() + radius);
            case BOTTOM_LEFT:
                return new Position(position.left(), position.bottom() - radius, position.left() + radius, position.bottom());
            case BOTTOM_RIGHT:
                return new Position(position.right() - radius, position.bottom() - radius, position.right(), position.bottom());
        }
        return null;
    }


    public static List<CornerType> getOtherCorners(CornerType[] corners) {
        List<CornerType> otherCorners = new ArrayList<>();
        // values().
        boolean contains;
        for (CornerType corner : values()) {
            contains = false;

            for (CornerType currentCorner : corners) {
                if (corner.equals(currentCorner))
                    contains = true;
            }

            if (!contains) 
                otherCorners.add(corner);
        }

        return otherCorners;
    }
}
