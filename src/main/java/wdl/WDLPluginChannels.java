/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForSigned;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import net.minecraft.world.chunk.Chunk;

/**
 * World Downloader permission system implemented with Plugin Channels.
 *
 * This system is used to configure the mod, and disable certain features,
 * at a server's decision.  I've made this system because there already were
 * other (more esoteric) methods of finding the mod, based off of forge and
 * lightloader handshakes.  I think that a system like this, where there are
 * <em>degrees of control</em>, is better than one where the player is
 * indiscriminately kicked.  For instance, this system allows for permission
 * requests, which would be hard to do with another mechanism.
 *
 * This system makes use of plugin channels (hence the class name).  If you
 * haven't read <a href="https://wiki.vg/Plugin_channels">their info</a>,
 * they're a vanilla minecraft packet intended for mods.  But they <em>do</em>
 * need each channel to be REGISTERed before use, so the server does know
 * when the mod is installed.  As such, I actually did a second step and
 * instead send a second packet when the data is ready to be received by the
 * client, since mid-download permission changes can be problematic.
 *
 * Theoretically, these could be implemented with chat-based codes or even
 * MOTD-based codes.  However, I <em>really</em> do not like that system, as
 * it is really rigid.  So I chose this one, which is also highly expandable.
 *
 * This system is also used to fetch a few things from willing servers, mainly
 * entity track distances so that things can be saved correctly.
 *
 * And yes, this is the fabled "backdoor" / "back door"; I don't like that term
 * but people will call it that.  I think it's the best possible system out
 * of the available options (and doing nothing wouldn't work - as I said,
 * there <em>are</em> weird ways of finding mods).
 *
 * <a href="https://wiki.vg/Plugin_channels/World_downloader">Packet
 * documentation is on wiki.vg</a>, if you're interested.
 */
public class WDLPluginChannels {
	// private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Packets that have been received.
	 */
	private static HashSet<Integer> receivedPackets = new HashSet<>();

	private static int saveRadius = -1;

	/**
	 * Map of entity ranges.
	 *
	 * Key is the entity string, int is the range.
	 */
	private static Map<String, Integer> entityRanges =
			new HashMap<>();

	/**
	 * Chunk overrides. Any chunk within a range is allowed to be downloaded in.
	 */
	private static Map<String, Multimap<String, ChunkRange>> chunkOverrides = new HashMap<>();

	/**
	 * Permission request fields that take boolean parameters.
	 */
	public static final List<String> BOOLEAN_REQUEST_FIELDS = Arrays.asList(
			"downloadInGeneral", "cacheChunks", "saveEntities",
			"saveTileEntities", "saveContainers", "getEntityRanges");
	/**
	 * Permission request fields that take integer parameters.
	 */
	public static final List<String> INTEGER_REQUEST_FIELDS = Arrays.asList(
			"saveRadius");

	/**
	 * List of new chunk override requests.
	 */
	private static List<ChunkRange> chunkOverrideRequests = new ArrayList<>();

	/**
	 * Gets the server-set range for the given entity.
	 *
	 * @param entity The entity's name (via {@link EntityUtils#getEntityType}).
	 * @return The entity's range, or -1 if no data was recieved.
	 */
	@CheckForSigned
	public static int getEntityRange(String entity) {
		if (receivedPackets.contains(2)) {
			if (entityRanges.containsKey(entity)) {
				return entityRanges.get(entity);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Gets the save radius.
	 *
	 * Note that using {@link #canSaveChunk(Chunk)} is generally better
	 * as it handles most of the radius logic.
	 *
	 * @return {@link #saveRadius}.
	 */
	public static int getSaveRadius() {
		return saveRadius;
	}

	/**
	 * Checks if the server-set entity range is configured.
	 */
	public static boolean hasServerEntityRange() {
		return receivedPackets.contains(2) && entityRanges.size() > 0;
	}

	public static Map<String, Integer> getEntityRanges() {
		return new HashMap<>(entityRanges);
	}

	/**
	 * Is the given chunk part of a chunk override?
	 */
	public static boolean isChunkOverridden(Chunk chunk) {
		if (chunk == null) {
			return false;
		}

		return isChunkOverridden(chunk.getChunkCoordIntPair().chunkXPos, chunk.getChunkCoordIntPair().chunkZPos);
	}
	/**
	 * Is the given chunk location part of a chunk override?
	 */
	public static boolean isChunkOverridden(int x, int z) {
		for (Multimap<String, ChunkRange> map : chunkOverrides.values()) {
			for (ChunkRange range : map.values()) {
				if (x >= range.x1 &&
						x <= range.x2 &&
						z >= range.z1 &&
						z <= range.z2) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Are there any chunk overrides present?
	 */
	public static boolean hasChunkOverrides() {
		if (!receivedPackets.contains(4)) {
			// XXX It's possible that some implementations may not send
			// packet 4, but still send ranges. If so, that may lead to issues.
			// But right now, I'm not checking that.
			return false;
		}
		if (chunkOverrides == null || chunkOverrides.isEmpty()) {
			return false;
		}
		for (Multimap<String, ChunkRange> m : chunkOverrides.values()) {
			if (!m.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets an immutable copy of the {@link #chunkOverrides} map.
	 */
	public static Map<String, Multimap<String, ChunkRange>> getChunkOverrides() {
		Map<String, Multimap<String, ChunkRange>> returned = new
				HashMap<>();

		for (Map.Entry<String, Multimap<String, ChunkRange>> e : chunkOverrides
				.entrySet()) {
			// Create a copy of the given map.
			Multimap<String, ChunkRange> map = ImmutableMultimap.copyOf(e.getValue());

			returned.put(e.getKey(), map);
		}

		return ImmutableMap.copyOf(returned);
	}

	/**
	 * Gets the current list of chunk override requests.
	 */
	public static List<ChunkRange> getChunkOverrideRequests() {
		return ImmutableList.copyOf(chunkOverrideRequests);
	}
	/**
	 * Adds a new chunk override request for the given range.
	 */
	public static void addChunkOverrideRequest(ChunkRange range) {
		chunkOverrideRequests.add(range);
	}

	/**
	 * The state for {@link #sendInitPacket(String)} if it was called when no channels were registered.
	 */
	@Nullable
	private static String deferredInitState = null;

	/**
	 * A range of chunks.
	 */
	public static class ChunkRange {
		public ChunkRange(String tag, int x1, int z1, int x2, int z2) {
			this.tag = tag;

			// Ensure that the order is correct
			if (x1 > x2) {
				this.x1 = x2;
				this.x2 = x1;
			} else {
				this.x1 = x1;
				this.x2 = x2;
			}
			if (z1 > z2) {
				this.z1 = z2;
				this.z2 = z1;
			} else {
				this.z1 = z1;
				this.z2 = z2;
			}
		}

		/**
		 * The tag of this chunk range.
		 */
		public final String tag;
		/**
		 * Range of coordinates.  x1 will never be higher than x2, as will z1
		 * with z2.
		 */
		public final int x1, z1, x2, z2;

		/**
		 * Reads and creates a new ChunkRange from the given
		 * {@link ByteArrayDataInput}.
		 */
		public static ChunkRange readFromInput(ByteArrayDataInput input) {
			String tag = input.readUTF();
			int x1 = input.readInt();
			int z1 = input.readInt();
			int x2 = input.readInt();
			int z2 = input.readInt();

			return new ChunkRange(tag, x1, z1, x2, z2);
		}

		/**
		 * Writes this ChunkRange to the given {@link ByteArrayDataOutput}.
		 *
		 * Note that I expect most serverside implementations will ignore the
		 * tag, but it still is included for clarity.  The value in it can be
		 * anything so long as it is not null - an empty string will do.
		 */
		public void writeToOutput(ByteArrayDataOutput output) {
			output.writeUTF(this.tag);

			output.writeInt(this.x1);
			output.writeInt(this.z1);
			output.writeInt(this.x2);
			output.writeInt(this.z2);
		}

		@Override
		public String toString() {
			return "ChunkRange [tag=" + tag + ", x1=" + x1 + ", z1=" + z1
					+ ", x2=" + x2 + ", z2=" + z2 + "]";
		}
	}
}
