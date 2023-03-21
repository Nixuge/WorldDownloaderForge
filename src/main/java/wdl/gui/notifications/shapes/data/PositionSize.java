package wdl.gui.notifications.shapes.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PositionSize {
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;

    private final int width;
    private final int height;

    private Position position;

    public Position getAsPosition() {
        if (this.position == null)
            this.position = new Position(left, top, right, bottom);
        return position;
    }
}
