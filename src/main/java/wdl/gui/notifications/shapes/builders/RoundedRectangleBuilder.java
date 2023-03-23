package wdl.gui.notifications.shapes.builders;

import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.roundedrectangle.RoundedRectangleBorder;
import wdl.gui.notifications.shapes.roundedrectangle.RoundedRectangleFill;

public class RoundedRectangleBuilder extends BaseBuilder<RoundedRectangleBuilder> {
    int radius = 5;
    CornerType[] enabledCorners;

    public RoundedRectangleBuilder setEnabledCorners(CornerType[] enabledCorners) {
        this.enabledCorners = enabledCorners;
        return this;
    }

    public RoundedRectangleFill buildRectangleFill() {        
        return new RoundedRectangleFill(this.position, this.radius, this.color, this.enabledCorners);
    }

    // public RoundedRectangleBorder buildRectangleBorder() {        
        // return new RoundedRectangleBorder(this.position, this.radius, this.color, this.enabledCorners, this.borderWidth);
    // }
}
