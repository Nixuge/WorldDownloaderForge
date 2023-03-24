package wdl.gui.notifications.shapes.base;

import lombok.Setter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import wdl.gui.notifications.shapes.data.Position;

// ========== Rant about how mc is done ==========
//            ======= A good idea =======
// GlStateManager, pushAttribs, popAttribs and GL11.
// Mc is built with GL11 calls. However, since 1.8, to save some precious CPU time,
// a GlStateManager class was introduced. This serves the purpose of saving every OpenGL
// enable/disable to avoid calling it if you for example enable something already enabled.
// Now, instead of using
// GL11.glEnable(GL11.<property>)
// you need to write:
// GlStateManager.enable<property>()
// [Note: GlStateManager doesn't have every GL11 enable/disable call implemented, but it's good enough]
//
// From some random graph on MinecraftForums, this is great, it saves CPU time and doesn't
// seem to prevent anything.
//
//          ======= Some edge cases =======
// Imagine you want to write something that needs blending.
//
// The logical approach is:
// GlStateManager.enableBlending()
// <... code that draws here ...>
// GlStateManager.disableBlending()
//
// That can work great. However, now imagine that blending is already enabled on the first call.
// This'll cause the first enableBlending() to basically do nothing, as GlStateManager knows
// blending is already on. This itself isn't a problem. However, the disableBlending()
// will now actually disable something that was enabled before, 
// maybe (probably) affecting some other part of the game.
// This is known as an "OpenGL state leak"
//
//         ======= Vanilla GL11 fix =======
// GL11 contains 2 functions, a pushAttrib() and a popAttrib().
// The pushAttrib() function saves the currently enabled OpenGL attributes, while the
// popAttrib() restores them (bit more complicated but more than enough to understand)
// With those 2 functions alone, we can fix our code above:
//
// GL11.pushAttrib() // Save the attributes
// GL11.glEnable(GL11.BLENDING)
// <... code that draws here ...>
// GL11.popAttrib() // Restore the attributes from before
// 
// And this works... perfectly, if we ignore the fact that glEnable may be called to enable
// something already enabled (which is honestly not that bad compaired to the next part)
//
//         ======= GlStateManager case =======
// Now imagine the previous code but using GlStateManager:
//
// GlStateManager.pushAttrib() // Basically just calls GL11.pushAttrib(), nothing else
// GlStateManager.enableBlending()
// <... code that draws here ...>
// GlStateManager.popAttrib() // Same as pushAttrib() but with popAttrib()
//
// But now we get an issue:
// Imagine blending was disabled at first.
//
// pushAttrib() saves the state in GL11, but NOT in GlStateManager
// enableBlending() sets the "blending" state to "enabled" in GlStateManager
// popAttrib() restores the state from before in GL11, but NOT in GlStateManager
//
// What this does is make blending appear as "enabled" in GlStateManager, while
// it's actually "disabled" in OpenGL itself.
// This obviously causes issues with render after, since the state itself is set wrong.
// 
//         ======= Final fix =======
// Some people already noted this (partially), see:
// https://gist.github.com/JamiesWhiteShirt/ff2521936a83ebc10fd6893e206a6770
// https://web.archive.org/web/20230324183221/<link>
// However, I can only see 2 solutions from here:
// - Since every field in GlStateManager is private, make a wrapper class that uses
//   reflections to save all of its fields when using pushAttrib() & actually restore them
//   in both OpenGL and GlStateManager.
// - Use raw Gl11 calls. This isn't "recommended" and may cause a few unneccessary GL11 
//   calls, but it's honestly not that important & looks better than the other solution
//
//         ======= Conclusion =======
// For now, the 2nd solution is the one I'm choosing.
// This could've all been avoided if GlStateManager's pushAttrib() & popAttrib() were done properly,
// but they aren't and that's how it is.



@Setter
public abstract class Shape {
    protected static Tessellator tessellator = Tessellator.getInstance();
    protected static WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    protected int xOffset;
    protected Position position;

    protected int rawColor;
    protected float alpha;
    protected float red;
    protected float green;
    protected float blue;

    public Shape(Position position, int color) {
        setPosition(position);
        setColor(color);
    }

    public Shape(int color) {
        setColor(color);
    }

    public Shape(){}

    public void setColor(int color) {
        this.rawColor = color;
        alpha = (float)(color >> 24 & 255) / 255.0F;
        red = (float)(color >> 16 & 255) / 255.0F;
        green = (float)(color >> 8 & 255) / 255.0F;
        blue = (float)(color & 255) / 255.0F;
    }

    // Note:
    // Due to how the code is structured here, it is required that setPosition is called
    // at least ONCE with a non-null position before drawing, otherwise a crash would happen.
    // This always happens here because of how NotificationWindow.setPosition(...) and
    // NotificationManager.draw(...) work, but need to keep that in mind if reusing it
    // somewhere else.
    public void setPosition(Position position) {
        if (position == null)
            return;
        this.position = position;
    }
    
    /**
     * Draw the element on the screen, without toggling on/off GlStateManager attribs.
     * Use only if you have another function calling those toggles, otherwise use drawToggleAttribs(...).
     * Meant to be overwritten only in the final (filled/border/...) shape.
     * 
     * @param xOffset x position of the shape will be reduced by xOffset
     */
    public abstract void draw(int xOffset);
    /**
     * Wrapper for draw(...) that toggles on & off GlStateManager/GL11 attribs.
     * 
     * @param xOffset x position of the shape will be reduced by xOffset
     */
    public abstract void drawToggleAttribs(int xOffset);
}
