package wdl.gui.notifications.shapes;

import lombok.Data;

@Data
public class RoundedRectangle {
    private int radiusTopLeft;
    private int radiusTopRight;
    private int radiusBottomRight;
    private int radiusBottomLeft;
}
