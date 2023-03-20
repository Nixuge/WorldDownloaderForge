package me.nixuge.worlddownloader;

import me.nixuge.worlddownloader.notifications.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventTest {
    public Minecraft mc;

    public EventTest()
    {
        System.out.println("registered");
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        // System.out.println("hello there::");
        NotificationManager.render(event.partialTicks);
    }
}
