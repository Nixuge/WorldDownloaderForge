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
        notificationManager.draw(event.partialTicks);
    }
}
