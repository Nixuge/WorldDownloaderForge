package wdl.gui.notifications.shapes.base;

public interface ShapeNoDraw {

    /**
     * Function to be overriden only in "xShape" classes, to be called to
     * draw the positions without beginning/ending the renderer
     * 
     * @param xOffset 
     */
    public void drawPositions(int xOffset);

    /**
     * Function to be overriden only in "xShape" classes, to be called to
     * toggle on the GlStateManager/GL11 attributes.
     * Use toggleOffAttribs() to revert
     */
    public void toggleOnAttribs();

    /**
     * Function to be overriden only in "xShape" classes, to be called to
     * toggle on the GlStateManager/GL11 attributes.
     * Use after toggleOnAttribs() 
     */
    public void toggleOffAttribs();
}
