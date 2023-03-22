package wdl.gui.notifications.shapes.data;

import net.minecraft.client.shader.Framebuffer;

// Wrapper class that replaces bad names w good ones
// + in the future add functionalities

public class FrameBufferPlus extends Framebuffer {
    public FrameBufferPlus(int width, int height, boolean useDepth) {
        super(width, height, useDepth);
    }

    public void setFramebufferFilter(int framebufferFilter) {
        super.setFramebufferFilter(framebufferFilter);
    }

    public void bindFramebuffer(boolean setbackViewPort) {
        super.bindFramebuffer(setbackViewPort);
    }

    //set right fields 
    public void setFramebufferColor(float p_147604_1_, float p_147604_2_, float p_147604_3_, float p_147604_4_) {
        super.setFramebufferColor(p_147604_1_, p_147604_2_, p_147604_3_, p_147604_4_);
    }
    

    public void framebufferRender(int width, int height){
        super.framebufferRender(width, height);
    }

    // unsure about disableBlend name
    public void framebufferRenderExt(int width, int height, boolean disableBlend) {
        super.framebufferRenderExt(width, height, disableBlend);
    }

}
