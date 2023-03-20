package me.nixuge.worlddownloader.command.commands;


import me.nixuge.worlddownloader.command.MessageBuilder;
import me.nixuge.worlddownloader.notifications.Level;
import me.nixuge.worlddownloader.notifications.Notification;
import me.nixuge.worlddownloader.notifications.NotificationManager;
import me.nixuge.worlddownloader.command.AbstractCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

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
        // NotificationWindowNew window =  new NotificationWindowNew(new Notification(Level.INFO, "ayoooo"));
        // NotificationManagerOld mgr = NotificationManagerOld.getInstance();
        // mgr.addNotification(new NotificationOld(Level.INFO, "uwdsfsd sdfsdfsdfsdfuu"));
        NotificationManager.show(new Notification(Level.INFO, "title here", "msg here", 1));
        // for (int i = 0; i < 50_000; i++) {
            // window.draw();
            // Minecraft.getMinecraft().fontRendererObj.drawString(getCommandName(), 500, 500, 0xffffffff, false);
        // }
        this.tell(new MessageBuilder("Notification should now be showing", EnumChatFormatting.GRAY));
       
    }
}
