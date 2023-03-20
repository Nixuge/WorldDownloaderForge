package me.nixuge.worlddownloader.command.commands;


import me.nixuge.worlddownloader.command.MessageBuilder;
import me.nixuge.worlddownloader.command.AbstractCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import wdl.gui.notifications.Level;
import wdl.gui.notifications.NotificationManager;
import wdl.gui.notifications.Notification;
import wdl.gui.notifications.NotificationWindow;

import java.util.ArrayList;
import java.util.List;

public class ShowNotification extends AbstractCommand {

    public ShowNotification() {
        super("sn");
    }

    @Override
    public List<String> getCommandAliases() {
        ArrayList<String> al = new ArrayList<>();
        al.add("shownotification");
        return al;
    }

    @Override
    public void onCommand(final ICommandSender sender, final String[] args) {
        // this.tell(new MessageBuilder("Notification should now be showing", EnumChatFormatting.GRAY));
        NotificationWindow window =  new NotificationWindow(new Notification(Level.INFO, "ayoooo"));
        NotificationManager mgr = NotificationManager.getInstance();
        mgr.addNotification(new Notification(Level.INFO, "uwuu"));
        // for (int i = 0; i < 50_000; i++) {
            // window.draw();
            // Minecraft.getMinecraft().fontRendererObj.drawString(getCommandName(), 500, 500, 0xffffffff, false);
        // }
        this.tell(new MessageBuilder("Notification should now be showing", EnumChatFormatting.GRAY));
       
    }
}
