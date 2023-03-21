package wdl.gui.notifications.shapes.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Size {
    private final int width;
    private final int height;
}
