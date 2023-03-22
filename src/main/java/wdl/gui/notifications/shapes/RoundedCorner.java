package wdl.gui.notifications.shapes;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.data.CornerType;
import wdl.gui.notifications.shapes.data.FrameBufferPlus;

@Getter
public class RoundedCorner extends Shape {
    private static int ROUNDING_STEP = 1;

    private int radius;
    private CornerType cornerType;

    public RoundedCorner(CornerType cornerType, int radius, int color) {
        this(cornerType, null, radius, color);
    }

    public RoundedCorner(CornerType cornerType, Position position, int radius, int color) {
        super(color);
        this.cornerType = cornerType;
        this.radius = radius;
        setPosition(position);
    }
    
    // 2do: pre calculate this & save calculations
    // Will see since xOffset changes & performance is negligible
    // TODO: fix function apparently drawing twice?
    @Override
    public void draw(int xOffset) {
        // float tempA = alpha / 2;
        // System.out.println("alpha:" + alpha);
        // GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.color(red, green, blue, alpha / 2);

        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        // GL11.glEnable(GL11.GL_STENCIL_TEST); // Turn on da test
        // GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // Flush old data

        // GL11.glStencilMask(0xFF); // Writing = ON
        // GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF); // Always "add" to frame
        // GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP); // Replace on success
        // GL11.glStencilMask(0x00); // Writing = OFF
        // GL11.glStencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF); // Anything that wasn't defined above will not be rendered.
        
        // GlStateManager.pushAttrib();
        // GlStateManager.pushMatrix();
        
        // Minecraft.getMinecraft().getFramebuffer().unbindFramebuffer();
        // FrameBuff
        
        // Framebuffer fb = new Framebuffer(50, 50, false);

        // https://www.google.com/search?client=firefox-b-d&q=gl11+render+framebuffer
        // https://forums.minecraftforge.net/topic/67692-how-do-you-create-a-camera-and-render-whatever-it-sees-on-a-custom-gui/
        //maybe: https://forums.minecraftforge.net/topic/66496-112-render-what-entity-sees-on-screen/?do=findComment&comment=318960
        // TODO: CREATE FRAMEBUFFER ON MAIN GL THREAD (2ND LINK ABOVE)

        //TODO:
        // either figure out how to use a FrameBuffer (more promising)
        // OR
        // Use Stencil buffer
        // https://community.khronos.org/t/overlapping-alpha-blended-polygons/65744/4
        
        // GlStateManager.clearColor(0, 0, 0, 1);


        // framebuffer.bindFramebuffer(true);
        // fb.setFramebufferColor(.5f, .5f, .8f, .5f);
        // fb.framebufferRenderExt(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, true);
        // fb.framebufferRender(Minecraft.getMinecraft().displayWidth, 50);
        
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        Framebuffer fbo = mc.getFramebuffer();
        int width = scaledRes.getScaledWidth();
        int height = scaledRes.getScaledHeight();
        

        FrameBufferPlus framebuffer = new FrameBufferPlus(width, height, false);
        framebuffer.setFramebufferColor(1f, 1f, 1f, 1f);



        // GlStateManager.popAttrib();
        // GlStateManager.popMatrix();
        // GL11.frameBuffer
        // GL11.glBlendFunc(255, 255);
        // GL11.glBegin(GL11.GL_POLYGON);
        // Note:
        // posLeft;posTop is actually the place where the angle starts to simplify things
        int posLeft = position.left() - xOffset;
        int posTop = position.top();
        worldrenderer.pos(0 + (50), 0, 0).endVertex();
        worldrenderer.pos(0, 0 + (50), 0).endVertex();
        worldrenderer.pos(0 + 50, 0 + 50, 0).endVertex();

        // GL11.glVertex2d(0 + (50), 0);
        // GL11.glVertex2d(0, 0 + (50));
        // GL11.glVertex2d(0 + 50, 0 + 50);

        // GL11.glVertex2d(0 + (50), 0);
        // GL11.glVertex2d(0, 0 + (50));
        // GL11.glVertex2d(0 + 50, 0 + 50);

        // GlStateManager.tryBlendFuncSeparate(1, 1, 1, 0);
        // GL11.glEnable(GL11.GL_STENCIL_TEST);

        // GL11.glStencilFunc(GL11.GL_ALWAYS, 0x1, 0x1);
        // GL11.glStencilFunc(GL11.GL_EQUAL, 0x0, 0x1);
        // GL11.glDepthMask(false);
        // GL11.glEnable(GL11.GL_BLEND);

        //Anything rendered here will be cut if goes beyond frame defined before.

        // for (int i = cornerType.getStartingDegree(); i <= cornerType.getEndingDegree() ; i += 10) {
        //     double angleRad = i * Math.PI / 180.0;
        //     double xHere = posLeft + radius * Math.cos(angleRad);
        //     double yHere = posTop + radius * Math.sin(angleRad);
        //     // worldrenderer.pos(xHere, yHere, 0).endVertex();
        //     // GL11.glVertex2d(xHere, yHere);
        //     if (i % 10 == 0) {
        //         // worldrenderer.pos(posLeft , posTop  , 0).endVertex();
        //         // GL11.glVertex2d(posLeft , posTop);
        //     }
        // }

        // GL11.glDisable(GL11.GL_DEPTH_TEST);
        // GlStateManager.enable
        // worldrenderer.pos(posLeft + radius, posTop + radius, 0).endVertex();

        // Can't see any way to enable that without using 
        // GL11 directly unfortunately
        // Doesn't seem to cause any issue tho.
        // GL11.glEnable(GL11.GL_POLYGON_SMOOTH); 

        tessellator.draw();
        
        
        // framebuffer.bindFramebufferTexture();
        
        // GL11.glBindTexture(1, 1);
        
        framebuffer.unbindFramebuffer();
        framebuffer.deleteFramebuffer();
        

        if (fbo != null){
            fbo.bindFramebuffer(true);
        }
        else {
            System.out.println("FUCK !");
        }

        // framebuffer.framebufferRender(width, height);
        // framebuffer.deleteFramebuffer();
        // framebuffer.bindFramebufferTexture();
        // framebuffer.deleteFramebuffer();



        // GL11.glDisable(GL11.GL_STENCIL_TEST);
        // GL11.glEnd();

        // GlStateManager.bindTexture(framebuffer.framebufferTexture);

        // framebuffer.framebufferRender(1920, 1080);
        // framebuffer.deleteFramebuffer();

        // if (fbo != null){
        //     // Restore the original framebuffer. The parameter set to true also restores the viewport.
        //     fbo.bindFramebuffer(true);
        // }
        // else {
        //     System.out.println("FUCK !");
        // }
        // fb.unbindFramebuffer();
        // fb.deleteFramebuffer();
        // GL11.glDisable(GL11.GL_STENCIL_TEST); // Turn this shit off!

        
        // GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
    }

    @Override
    public void drawToggleAttribs(int xOffset) {
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        // GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 1);
        
        draw(xOffset);

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    @Override
    public void removeShape() {
        // TODO Auto-generated method stub
    }
}
