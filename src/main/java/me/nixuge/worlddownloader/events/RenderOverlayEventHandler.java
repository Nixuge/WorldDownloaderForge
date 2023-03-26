package me.nixuge.worlddownloader.events;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wdl.gui.notifications.NotificationManager;

public class RenderOverlayEventHandler {
    public NotificationManager notificationManager;

    public RenderOverlayEventHandler() {
        this.notificationManager = NotificationManager.getInstance();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        // this event returns a "type" which is the step of the overlay at which it's currently on.
        // ElementType.ALL means after everything is rendered, and avoids messing with other GUI elements.
        // (eg could use ElementType.CROSSHAIR to draw right after the crosshair)
        // See net.minecraftforge.client.GuiIngameForge for Post() and Pre() calls
        if (event.type.equals(RenderGameOverlayEvent.ElementType.ALL))
            notificationManager.draw(event.partialTicks);
    }
}