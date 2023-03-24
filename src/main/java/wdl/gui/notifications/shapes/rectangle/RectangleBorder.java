package wdl.gui.notifications.shapes.rectangle;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.BorderPosition;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.line.LineFill;

public class RectangleBorder extends RectangleShape {
    private static Map<BorderPosition, Method> borderPosMethodMap;
    static {
        borderPosMethodMap = new HashMap<>();
        borderPosMethodMap.put(BorderPosition.TOP, null);
        borderPosMethodMap.put(BorderPosition.BOTTOM, null);
        borderPosMethodMap.put(BorderPosition.LEFT, null);
        borderPosMethodMap.put(BorderPosition.RIGHT, null);
    }

    private Map<BorderPosition, LineFill> borderLines;

    // public void 


    
    
    public RectangleBorder(Position position, int color, Map<BorderPosition, Float> enabledBorders) {
        setColor(color);
        setPosition(position);

        this.borderLines = new HashMap<>();
        for (Entry<BorderPosition, Float> entry : enabledBorders.entrySet()) {
            this.borderLines.put(entry.getKey(), new LineFill(null, entry.getValue()));
        }
    }

    public void calculateLinePositions() {
        for (Entry<BorderPosition, LineFill> entry : borderLines.entrySet()) {
            // kinda ugly but idk of another way to do it for now
            LineFill line = entry.getValue();
            switch (entry.getKey()) {
                case TOP:
                    line.setPosition(new Position(position.left(), position.top(), position.right(), position.top()));
                    break;
                case BOTTOM:
                    line.setPosition(new Position(position.left(), position.bottom(), position.right(), position.bottom()));
                    break;
                case LEFT:
                    line.setPosition(new Position(position.left(), position.top(), position.left(), position.bottom()));
                    break;
                case RIGHT:
                    line.setPosition(new Position(position.right(), position.top(), position.right(), position.bottom()));
                    break;
                default:
                    break;
            }
            
        }
    }

    @Override
    public void draw(int xOffset) {
        GlStateManager.color(red, green, blue, alpha);
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        for (LineFill line : this.borderLines.values()) {
            line.draw(xOffset);
        }

        // GL11.glEnable(GL11.GL_LINE_SMOOTH);
        tessellator.draw();
        // GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public void setPosition(Position position) {
        if (position == null)
            return;
        this.position = position;
        calculateLinePositions();
    }
}
