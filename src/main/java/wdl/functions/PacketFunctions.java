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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.MatchesPattern;
import javax.annotation.RegEx;
import javax.annotation.meta.TypeQualifierNickname;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * Contains functions related to packets. This version is used between Minecraft
 * 1.9 and Minecraft 1.12.2.
 */
public final class PacketFunctions {
	private PacketFunctions() { throw new AssertionError(); }

	/**
	 * A regex that indicates whether a name is valid for a plugin channel.
	 * In 1.13, channels are namespaced identifiers; in 1.12 they are not.
	 * Note: the max length was shorter in some earlier version (before 1.9).
	 */
	@RegEx
	public static final String CHANNEL_NAME_REGEX = ".{1,20}";

	/**
	 * Marks a parameter as requiring a valid channel name for a plugin message.
	 */
	@Documented
	@TypeQualifierNickname @MatchesPattern(CHANNEL_NAME_REGEX)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE_USE})
	public static @interface ChannelName { }

	/**
	 * Creates a plugin message packet.
	 * @param channel The channel to send on.
	 * @param bytes The payload.
	 * @return The new packet.
	 */
	public static C17PacketCustomPayload makePluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new C17PacketCustomPayload(channel, new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}


	/**
	 * Gets the name of the channel that is used to register plugin messages.
	 * @return The channel name.
	 */
	@ChannelName
	public static String getRegisterChannel() {
		return "REGISTER";
	}

		/**
	 * Creates a list of channel names based on the given list, but with names that
	 * are not valid for this version removed.
	 *
	 * @param names The names list.
	 * @return A sanitized list of names.
	 */
	public static ImmutableList<@ChannelName String> removeInvalidChannelNames(String... names) {
		ImmutableList.Builder<@ChannelName String> list = ImmutableList.builder();
		for (String name : names) {
			if (name.matches(CHANNEL_NAME_REGEX)) {
				list.add(name);
			}
		}
		return list.build();
	}
}
