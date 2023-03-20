package me.nixuge.worlddownloader.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class MessageBuilder {
    private final IChatComponent messageObject;

    public MessageBuilder(final String message) {
        this.messageObject = new ChatComponentText(message);
    }

    public MessageBuilder(final String message, final EnumChatFormatting colour) {
        this.messageObject = new ChatComponentText(message);
        this.messageObject.getChatStyle().setColor(colour);
    }

    public MessageBuilder addMessage(final MessageBuilder messageBuilder) {
        this.messageObject.appendSibling(messageBuilder.build());
        return this;
    }

    public MessageBuilder addMessage(final String message, final EnumChatFormatting colour) {
        final IChatComponent messageNew = new ChatComponentText(message);
        messageNew.getChatStyle().setColor(colour);
        this.messageObject.appendSibling(messageNew);
        return this;
    }

    public IChatComponent build() {
        return this.messageObject;
    }

    public void sendToPlayer() {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            player.addChatMessage(this.messageObject);
        }
    }
}
