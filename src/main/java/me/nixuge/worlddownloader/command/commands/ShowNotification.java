package me.nixuge.worlddownloader.command.commands;


import me.nixuge.worlddownloader.command.AbstractCommand;
import net.minecraft.command.ICommandSender;
import wdl.gui.notifications.Level;
import wdl.gui.notifications.Notification;
import wdl.gui.notifications.NotificationManager;

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
       NotificationManager.getInstance().addNotification(new Notification(Level.INFO, "New notification !", 100));
    }
}
