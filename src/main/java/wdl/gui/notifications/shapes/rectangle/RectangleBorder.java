package wdl.gui.notifications.shapes.rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.gui.notifications.shapes.data.BorderPosition;
import wdl.gui.notifications.shapes.data.Position;
import wdl.gui.notifications.shapes.line.LineFill;

public class RectangleBorder extends RectangleShape {
    // Honestly could've made a static function w a hashmap
    // but this looks funnier
    private static Map<BorderPosition, Function<Position, Position>> borderPosMap;
    static {
        borderPosMap = new HashMap<>();
        borderPosMap.put(BorderPosition.TOP, (position) -> { return new Position(position.left(), position.top(), position.right(), position.top()); });
        borderPosMap.put(BorderPosition.BOTTOM, (position) -> { return new Position(position.left(), position.bottom(), position.right(), position.bottom()); });
        borderPosMap.put(BorderPosition.LEFT, (position) -> { return new Position(position.left(), position.top(), position.left(), position.bottom()); });
        borderPosMap.put(BorderPosition.RIGHT, (position) -> { return new Position(position.right(), position.top(), position.right(), position.bottom()); });
    }

    private Map<BorderPosition, LineFill> borderLines;

    public RectangleBorder(Position position, int color, Map<BorderPosition, Float> enabledBorders) {
        setColor(color);
        setPosition(position);

        this.borderLines = new HashMap<>();
        for (Entry<BorderPosition, Float> entry : enabledBorders.entrySet()) {
            this.borderLines.put(entry.getKey(), new LineFill(null, entry.getValue()));
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
        for (Entry<BorderPosition, LineFill> entry : this.borderLines.entrySet()) {
            // one liner to get the position from the functions map & set it to the entry value
            entry.getValue().setPosition(
                borderPosMap.get(entry.getKey()).apply(position)
            );
        }
    }
}
