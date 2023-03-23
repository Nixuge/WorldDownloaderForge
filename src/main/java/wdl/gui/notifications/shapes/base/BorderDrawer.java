package wdl.gui.notifications.shapes.base;

import org.lwjgl.opengl.GL11;

// Workaround since we can't use multiple "extends", this needs to be instanciated directly
public class BorderDrawer {
    float borderWidth = 5;

    public BorderDrawer(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public BorderDrawer() {}
    
    public void beforeDraw() {
        GL11.glLineWidth(borderWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }
    public void afterDraw() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}
