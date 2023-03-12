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
package wdl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.minecraft.world.MinecraftException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.SaveHandler;
import wdl.config.settings.MiscSettings;
import wdl.versioned.VersionedFunctions;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 *
 * This variant is used for chunks from 1.12 and earlier.
 */
abstract class WDLChunkLoaderBase extends AnvilChunkLoader {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Gets the save folder for the given WorldProvider, respecting Forge's
	 * dimension names if forge is present.
	 */
	protected static File getWorldSaveFolder(SaveHandler handler,
			WorldProvider dimension) {
		File baseFolder = handler.getWorldDirectory();

		if (WDL.serverProps.getValue(MiscSettings.FORCE_DIMENSION_TO_OVERWORLD)) {
			return baseFolder;
		}

		// Based off of AnvilSaveHandler.getChunkLoader, but also accounts
		// for forge changes.
		try {
			// See forge changes here:
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/WorldProvider.java.patch#L85-L93
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/chunk/storage/AnvilSaveHandler.java.patch
			Method forgeGetSaveFolderMethod = dimension.getClass().getMethod(
					"getSaveFolder");

			String name = (String) forgeGetSaveFolderMethod.invoke(dimension);
			if (name != null) {
				File file = new File(baseFolder, name);
				file.mkdirs();
				return file;
			}
			return baseFolder;
		} catch (Exception e) {
			// Not a forge setup - emulate the vanilla method in
			// AnvilSaveHandler.getChunkLoader.
			// TODO: not sure if this is right
			if (dimension.getDimensionType().equals(DimensionType.NETHER)) {
				File file = new File(baseFolder, "DIM-1");
				file.mkdirs();
				return file;
			} else if (dimension.getDimensionType().equals(DimensionType.THE_END)) {
				File file = new File(baseFolder, "DIM1");
				file.mkdirs();
				return file;
			}

			return baseFolder;
		}
	}

	protected final WDL wdl;
	protected final Map<ChunkPos, NBTTagCompound> chunksToSave;
	/**
	 * Location where chunks are saved.
	 *
	 * In this version, this directly is parent to the region folder for the given dimension;
	 * in the overworld it is simply world and for other dimensions it is world/DIM#.
	 */
	protected final File chunkSaveLocation;

	protected WDLChunkLoaderBase(WDL wdl, File file) {
		super(file, null);
		this.wdl = wdl;
		@SuppressWarnings("unchecked")
		Map<ChunkPos, NBTTagCompound> chunksToSave =
				ReflectionUtils.findAndGetPrivateField(this, AnvilChunkLoader.class, VersionedFunctions.getChunksToSaveClass());
		this.chunksToSave = chunksToSave;
		this.chunkSaveLocation = file;
	}

	/**
	 * Saves the given chunk.
	 *
	 * Note that while the normal implementation swallows Exceptions, this
	 * version does not.
	 */
	@Override
	public void saveChunk(World world, Chunk chunk) throws IOException, MinecraftException {
		wdl.saveHandler.checkSessionLock();
		
		NBTTagCompound levelTag = writeChunkToNBT(chunk, world);

		NBTTagCompound rootTag = new NBTTagCompound();
		rootTag.setTag("Level", levelTag);
		rootTag.setInteger("DataVersion", VersionConstants.getDataVersion());

		addChunkToPending(chunk.getChunkCoordIntPair(), rootTag);

		wdl.unloadChunk(chunk.getChunkCoordIntPair());
	}

	/**
	 * Writes the given chunk, creating an NBT compound tag.
	 *
	 * Note that this does <b>not</b> override the private method
	 * {@link AnvilChunkLoader#writeChunkToNBT(Chunk, World, NBTCompoundTag)}.
	 * That method is private and cannot be overridden; plus, this version
	 * returns a tag rather than modifying the one passed as an argument.
	 *
	 * @param chunk
	 *            The chunk to write
	 * @param world
	 *            The world the chunk is in, used to determine the modified
	 *            time.
	 * @return A new NBTTagCompound
	 */
	private NBTTagCompound writeChunkToNBT(Chunk chunk, World world) {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("xPos", chunk.getChunkCoordIntPair().chunkXPos);
		compound.setInteger("zPos", chunk.getChunkCoordIntPair().chunkZPos);
		compound.setLong("LastUpdate", world.getWorldTime());
		compound.setIntArray("HeightMap", chunk.getHeightMap());
		compound.setBoolean("TerrainPopulated", true);  // We always want this
		compound.setBoolean("LightPopulated", chunk.isLightPopulated());
		compound.setLong("InhabitedTime", chunk.getInhabitedTime());
		
		ExtendedBlockStorage[] chunkSections = chunk.getBlockStorageArray();
		NBTTagList chunkSectionList = new NBTTagList();
		boolean hasSky = VersionedFunctions.hasSkyLight(world);
		
		for (ExtendedBlockStorage chunkSection : chunkSections) {
			if (chunkSection != null && !chunkSection.isEmpty()) {
				NBTTagCompound sectionNBT = new NBTTagCompound();
				sectionNBT.setByte("Y",
						(byte) (chunkSection.getYLocation() >> 4 & 255));
				byte[] buffer = new byte[4096];
				NibbleArray nibblearray = new NibbleArray();
				NibbleArray nibblearray1 = chunkSection.getData()
						.getDataForNBT(buffer, nibblearray);
				sectionNBT.setByteArray("Blocks", buffer);
				sectionNBT.setByteArray("Data", nibblearray.getData());
				
				if (nibblearray1 != null) {
					sectionNBT.setByteArray("Add", nibblearray1.getData());
				}
				
				NibbleArray blocklightArray = chunkSection.getBlocklightArray();
				int lightArrayLen = blocklightArray.getData().length;
				sectionNBT.setByteArray("BlockLight", blocklightArray.getData());
				
				if (hasSky) {
					NibbleArray skylightArray = chunkSection.getSkylightArray();
					if (skylightArray != null) {
						sectionNBT.setByteArray("SkyLight", skylightArray.getData());
					} else {
						// Shouldn't happen, but if it does, handle it smoothly.
						LOGGER.error("[WDL] Skylight array for chunk at " +
								chunk.getChunkCoordIntPair().chunkXPos + ", " + chunk.getChunkCoordIntPair().chunkZPos +
								" is null despite VersionedProperties " +
								"saying it shouldn't be!");
						sectionNBT.setByteArray("SkyLight", new byte[lightArrayLen]);
					}
				} else {
					sectionNBT.setByteArray("SkyLight", new byte[lightArrayLen]);
				}

				chunkSectionList.appendTag(sectionNBT);
			}
		}

		compound.setTag("Sections", chunkSectionList);
		compound.setByteArray("Biomes", chunk.getBiomeArray());

		chunk.setHasEntities(false);
		NBTTagList entityList = getEntityList(chunk);
		compound.setTag("Entities", entityList);

		NBTTagList tileEntityList = getTileEntityList(chunk);
		compound.setTag("TileEntities", tileEntityList);

		List<NextTickListEntry> updateList = world.getPendingBlockUpdates(
				chunk, false);
		if (updateList != null) {
			long worldTime = world.getWorldTime();
			NBTTagList entries = new NBTTagList();

			for (NextTickListEntry entry : updateList) {
				NBTTagCompound entryTag = new NBTTagCompound();
				ResourceLocation location = Block.REGISTRY
						.getNameForObject(entry.getBlock());
				entryTag.setString("i",
						location == null ? "" : location.toString());
				entryTag.setInteger("x", entry.position.getX());
				entryTag.setInteger("y", entry.position.getY());
				entryTag.setInteger("z", entry.position.getZ());
				entryTag.setInteger("t",
						(int) (entry.scheduledTime - worldTime));
				entryTag.setInteger("p", entry.priority);
				entries.appendTag(entryTag);
			}

			compound.setTag("TileTicks", entries);
		}

		return compound;
	}

	protected abstract NBTTagList getEntityList(Chunk chunk);
	protected abstract NBTTagList getTileEntityList(Chunk chunk);

	/**
	 * Gets a count of how many chunks there are that still need to be written to
	 * disk. (Does not include any chunk that is currently being written to disk)
	 *
	 * @return The number of chunks that still need to be written to disk
	 */
	public synchronized int getNumPendingChunks() {
		return this.chunksToSave.size();
	}
}
