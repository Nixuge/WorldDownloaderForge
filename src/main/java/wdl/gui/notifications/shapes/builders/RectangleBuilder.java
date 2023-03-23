package wdl.gui.notifications.shapes.builders;

import wdl.gui.notifications.shapes.rectangle.RectangleBorder;
import wdl.gui.notifications.shapes.rectangle.RectangleFill;

public class RectangleBuilder extends BaseBuilder<RectangleBuilder> {
    public RectangleFill buildRectangleFill() {        
        return new RectangleFill(this.position, this.color);
    }

    public RectangleBorder buildRectangleBorder() {        
        return new RectangleBorder(this.position, this.color, this.borderWidth);
    }
}
