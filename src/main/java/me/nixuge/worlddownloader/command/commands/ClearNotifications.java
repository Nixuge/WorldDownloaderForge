package me.nixuge.worlddownloader.command.commands;


import me.nixuge.worlddownloader.command.AbstractCommand;
import me.nixuge.worlddownloader.command.MessageBuilder;
import net.minecraft.command.ICommandSender;
import wdl.gui.notifications.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public class ClearNotifications extends AbstractCommand {

    public ClearNotifications() {
        super("cn");
    }

    @Override
    public List<String> getCommandAliases() {
        ArrayList<String> al = new ArrayList<>();
        al.add("clearnotifications");
        return al;
    }

    @Override
    public void onCommand(final ICommandSender sender, final String[] args) {
        NotificationManager.getInstance().removeAll();
        tell(new MessageBuilder("Notifications cleared."));
    }
}
