package wdl.gui.notifications.shapes.builders;

import wdl.gui.notifications.shapes.rectangle.RectangleBorder;
import wdl.gui.notifications.shapes.rectangle.RectangleFill;

public class RectangleBuilder extends BaseBuilder<RectangleBuilder> {
    public RectangleFill buildFill() {        
        return new RectangleFill(this.position, this.color);
    }

    public RectangleBorder buildBorder() {        
        return new RectangleBorder(this.position, this.color, this.enabledBorders);
    }
}
