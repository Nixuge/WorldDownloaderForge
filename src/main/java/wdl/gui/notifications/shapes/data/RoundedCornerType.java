package wdl.gui.notifications.shapes.data;

import lombok.Getter;

enum Offset {
    X_PLUS,
    X_MINUS,
    Y_PLUS,
    Y_MINUS
}

@Getter
public enum RoundedCornerType {
	TOP_LEFT(180, Offset.X_PLUS, Offset.Y_PLUS), 
	TOP_RIGHT(270, null, null), 
	BOTTOM_LEFT(90, null, null), 
	BOTTOM_RIGHT(0, null, null);
	
	private final int startingDegree;
	private final int endingDegree;
    private final Offset xOffset;
    private final Offset yOffset;
	
	RoundedCornerType(int startingDegree, Offset xOffset, Offset yOffset) {
		this.startingDegree = startingDegree;
        this.endingDegree = startingDegree + 90;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
	}

    public Position getFixedPosition(Position position, int radius) {
        int xChange = (this.xOffset == Offset.X_PLUS) ? radius : -radius;
        int yChange = (this.yOffset == Offset.Y_PLUS) ? radius : -radius;
        return new Position(
            position.left() + xChange,
            position.top() + yChange,
            position.right() + xChange,
            position.bottom() + yChange
        );
    }
}
