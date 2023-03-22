package wdl.gui.notifications; 

import lombok.Getter;
import lombok.Setter;

@Getter
public class Notification {
    
    @Getter
    private NotificationWindow window;

    @Getter
    @Setter
    private String text;

    @Getter
    @Setter
    private Level level;

    @Setter
    private int maxTime;

    /* =============== Tasklist - Notifications ===============
     * - Borders
     * - Proper centered header & text under
     * - Split text at the right moment to avoid
     *   overflowing when above max size (see below)
     * - Proper multi-line support
     * - Progress bar
     * --> Either time (by default) or custom (eg saved chunks)
     * - Custom color, apart from level
     * --> Maybe a "NotificationMetadata" dataclass that has
     * --> "headerText" & "color" as its parameters
     * --> with a "DefaultNotificationMetadata" enum?
     * - change color & text of the notification 
     *   & even header text when already shown
     * - hover/click support
     * 
     * ========== Bug fixes to do ==========
     * - Width can go over maxWidth
     * - Colors can flicker, "normal" mc GL11 behavior, just need to re check GlStateManager
     *   calls & make sure everything gets enabled/disabled at the right time.
     * 
     * 
     * ========== Unsure TODOs ==========
     * - Animation when notifications go up (/down)
     * --> Can cause problems such as appearing one clipping + unneccessary rn, will see later
     * 
     * (Not notification) LiquidBounce's TP script -> try w blink before, see if that works?
     */


    public Notification(Level level, String text, int maxTime) {
        this.level = level;
        this.text = text;
        this.maxTime = maxTime;
        this.window = new NotificationWindow(this);
    }

    public Notification(Level level, String text) {
        this.level = level;
        this.text = text;
        maxTime = text.length() * 3;
        this.window = new NotificationWindow(this);
    }
}
