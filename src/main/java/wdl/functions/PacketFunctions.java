/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.functions;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import wdl.functions.VersionedFunctions.ChannelName;

/**
 * Contains functions related to packets. This version is used between Minecraft
 * 1.9 and Minecraft 1.12.2.
 */
public final class PacketFunctions {
	private PacketFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#PLUGIN_CHANNEL_REGEX
	 *
	 * Note: the max length was shorter in some earlier version (before 1.9).
	 */
	public static final String CHANNEL_NAME_REGEX = ".{1,20}";

	/* (non-javadoc)
	 * @see VersionedFunctions#makePluginMessagePacket
	 */
	public static C17PacketCustomPayload makePluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new C17PacketCustomPayload(channel, new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeServerPluginMessagePacket
	 */
	public static S3FPacketCustomPayload makeServerPluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new S3FPacketCustomPayload(channel, new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getRegisterChannel
	 */
	@ChannelName
	public static String getRegisterChannel() {
		return "REGISTER";
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getUnregisterChannel
	 */
	@ChannelName
	public static String getUnregisterChannel() {
		return "UNREGISTER";
	}
}
