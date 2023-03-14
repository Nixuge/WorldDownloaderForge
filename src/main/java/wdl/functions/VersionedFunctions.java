/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.functions;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.MatchesPattern;
import javax.annotation.Nullable;
import javax.annotation.RegEx;
import javax.annotation.meta.TypeQualifierNickname;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;

// import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import wdl.config.settings.GeneratorSettings.Generator;
import wdl.functions.PacketFunctions.ChannelName;

/**
 * Helper that determines version-specific information about things, such
 * as whether a world has skylight.
 */
public final class VersionedFunctions {
	private VersionedFunctions() { throw new AssertionError(); }


	/**
	 * Opens a link using the default browser.
	 * @param url The URL to open.
	 * @see GuiScreen#openWebLink()
	 */
	public static void openLink(String url) {
		GuiFunctions.openLink(url);
	}

	/**
	 * Creates a link style for the given URL: blue, underlined, and with the right
	 * click event.
	 *
	 * @param url The URL to open.
	 * @return A new style
	 */
	public static ChatStyle createLinkFormatting(String url) {
		// Forwards-compatibility with 1.14
		return new ChatStyle()
				.setColor(EnumChatFormatting.BLUE)
				.setUnderlined(true)
				.setChatClickEvent(new ClickEvent(Action.OPEN_URL, url));
	}

	/**
	 * Gets the numeric ID for the given block.
	 * @return A numeric ID, the meaning and value of which is unspecified.
	 */
	public static int getBlockId(Block block) {
		return RegistryFunctions.getBlockId(block);
	}

	/**
	 * Gets the numeric ID for the given biome.
	 * @return A numeric ID, the meaning and value of which is unspecified.
	 */
	public static int getBiomeId(BiomeGenBase biome) {
		return RegistryFunctions.getBiomeId(biome);
	}

	// /**
	//  * Gets the class used to store the list of chunks in ChunkProviderClient
	//  * ({@link ChunkProviderClient#loadedChunks}).
	//  */
	// public static Class<?> getChunkListClass() {
	// 	return TypeFunctions.getChunkListClass();
	// }


	/**
	 * (EVIL) Converts name to the appropriate type for a custom name on this
	 * version.
	 *
	 * @param name The name. Non-null.
	 * @param <T> The type that is expected to be returned, based on the method
	 *             being called.
	 * @return Either a String or a ChatComponentText, depending on the version; T
	 *         should be inferred to the right one of those.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T customName(String name) {
		return (T)TypeFunctions.customName(name);
	}

	/**
	 * (EVIL) Returns a new instance of GameSettings. This class was repackaged in
	 * 1.13, partially due to my own request out of confusion of how remapping
	 * works. Unfortunately, we can't actually update our own mappings for 1.12,
	 * because liteloader uses this class in various mixins.
	 *
	 * Intended for use in unit tests.
	 *
	 * @param <T> The type that is expected to be returned, which is inferred.
	 * @return A GameSettings instance, but the package containing GameSettings
	 *         might vary.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createNewGameSettings() {
		return (T)TypeFunctions.createNewGameSettings();
	}

	/**
	 * Gets the class used for GameRenderer, né EntityRenderer. This was renamed in
	 * 1.13.2. However, since liteloader uses it, we can't remap it in older
	 * versions.
	 *
	 * Intended for use in unit tests. Unfortunately, we can't use mock(...)
	 * directly on this class, since we don't have mockito on actual release code
	 * (which this file is). We also can't do the ugly <T> thing here, since that
	 * won't carry through the call to mock. And because this is a wildcard,
	 * mock(...) doesn't necessarily match what we've got either (as far as the
	 * compiler knows).  The only real solution is raw types, though that's ugly.
	 */
	@SuppressWarnings("rawtypes")
	public static Class getGameRendererClass() {
		return TypeFunctions.getGameRendererClass();
	}
}
