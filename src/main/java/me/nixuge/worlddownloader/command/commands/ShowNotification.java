package me.nixuge.worlddownloader.command.commands;


import me.nixuge.worlddownloader.command.MessageBuilder;
import me.nixuge.worlddownloader.command.AbstractCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import wdl.gui.notifications.Level;
import wdl.gui.notifications.Notification;
import wdl.gui.notifications.Window;

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
        Window window =  new Window(new Notification(Level.INFO, "ayoooo"));
        for (int i = 0; i < 50_000; i++) {
            window.drawScreen(0, 0, 0);
            // Minecraft.getMinecraft().fontRendererObj.drawString(getCommandName(), 500, 500, 0xffffffff, false);
        }
        this.tell(new MessageBuilder("Notification should now be showing", EnumChatFormatting.GRAY));
       
    }
}
