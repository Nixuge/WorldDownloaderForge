package wdl.gui.notifications.drawing.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Position {
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;
}
