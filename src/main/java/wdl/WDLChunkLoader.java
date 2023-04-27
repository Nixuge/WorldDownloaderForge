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

import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.entity.DataWatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.SaveHandler;
import wdl.api.IEntityEditor;
import wdl.api.ITileEntityEditor;
import wdl.api.ITileEntityEditor.TileEntityCreationMode;
import wdl.api.ITileEntityImportationIdentifier;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;

import java.io.IOException;
import net.minecraft.world.MinecraftException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.NibbleArray;
import wdl.config.settings.MiscSettings;
import wdl.functions.HandlerFunctions;
import wdl.reflection.ReflectionUtils;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 *
 * Extends the class in either WDLChunkLoader12.java or WDLChunkLoader13.java,
 * depending on the Minecraft version.
 */
public class WDLChunkLoader extends AnvilChunkLoader {
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
			if (dimension.getDimensionId() == -1) { // Nether
				File file = new File(baseFolder, "DIM-1");
				file.mkdirs();
				return file;
			} else if (dimension.getDimensionId() == 1) { // The end
				File file = new File(baseFolder, "DIM1");
				file.mkdirs();
				return file;
			}

			return baseFolder;
		}
	}

	protected final WDL wdl;
	protected final Map<ChunkCoordIntPair, NBTTagCompound> chunksToSave;
	/**
	 * Location where chunks are saved.
	 *
	 * In this version, this directly is parent to the region folder for the given dimension;
	 * in the overworld it is simply world and for other dimensions it is world/DIM#.
	 */
	protected final File chunkSaveLocation;

	protected WDLChunkLoader(WDL wdl, File file) {
		super(file);
		this.wdl = wdl;
		@SuppressWarnings("unchecked")
		Map<ChunkCoordIntPair, NBTTagCompound> chunksToSave =
				ReflectionUtils.findAndGetPrivateField(this, AnvilChunkLoader.class, Map.class);
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
		
		compound.setByte("V", (byte) 1);
		compound.setInteger("xPos", chunk.getChunkCoordIntPair().chunkXPos);
		compound.setInteger("zPos", chunk.getChunkCoordIntPair().chunkZPos);
		compound.setLong("LastUpdate", world.getWorldTime());
		compound.setIntArray("HeightMap", chunk.getHeightMap());
		compound.setBoolean("TerrainPopulated", true);  // We always want this
		compound.setBoolean("LightPopulated", chunk.isLightPopulated());
		compound.setLong("InhabitedTime", chunk.getInhabitedTime());
		
		ExtendedBlockStorage[] chunkSections = chunk.getBlockStorageArray();
		NBTTagList chunkSectionList = new NBTTagList();
		boolean hasSky = HandlerFunctions.hasSkyLight(world);
		
		for (ExtendedBlockStorage blockStorage : chunkSections) {
			// Part ripped from the 1.8.9a WorldDownloader, which only handles actually writing the blocks
			if (blockStorage != null) {
				NBTTagCompound blockData = new NBTTagCompound();
				blockData.setByte("Y",
						(byte) (blockStorage.getYLocation() >> 4 & 255));
				byte[] var12 = new byte[blockStorage.getData().length];
				NibbleArray var13 = new NibbleArray();
				NibbleArray var14 = null;

				for (int var15 = 0; var15 < blockStorage.getData().length; ++var15) {
					char var16 = blockStorage.getData()[var15];
					int var17 = var15 & 15;
					int var18 = var15 >> 8 & 15;
					int var19 = var15 >> 4 & 15;

					if (var16 >> 12 != 0) {
						if (var14 == null) {
							var14 = new NibbleArray();
						}

						var14.set(var17, var18, var19, var16 >> 12);
					}

					var12[var15] = (byte) (var16 >> 4 & 255);
					var13.set(var17, var18, var19, var16 & 15);
				}

				blockData.setByteArray("Blocks", var12);
				blockData.setByteArray("Data", var13.getData());

				if (var14 != null) {
					blockData.setByteArray("Add", var14.getData());
				}

				blockData.setByteArray("BlockLight", blockStorage
						.getBlocklightArray().getData());

				// End of part ripped from the 1.8.9a WorldDownloader
				NibbleArray blocklightArray = blockStorage.getBlocklightArray();
				int lightArrayLen = blocklightArray.getData().length;
				if (hasSky) {
					NibbleArray skylightArray = blockStorage.getSkylightArray();
					if (skylightArray != null) {
						blockData.setByteArray("SkyLight", skylightArray.getData());
					} else {
						// Shouldn't happen, but if it does, handle it smoothly.
						LOGGER.error("[WDL] Skylight array for chunk at " +
								chunk.getChunkCoordIntPair().chunkXPos + ", " + chunk.getChunkCoordIntPair().chunkZPos +
								" is null despite VersionedProperties " +
								"saying it shouldn't be!");
							blockData.setByteArray("SkyLight", new byte[lightArrayLen]);
					}
				} else {
					blockData.setByteArray("SkyLight", new byte[lightArrayLen]);
				}

				chunkSectionList.appendTag(blockData);
			}
		}
		// for (ExtendedBlockStorage chunkSection : chunkSections) {
		// 	if (chunkSection != null) {
		// 		NBTTagCompound sectionNBT = new NBTTagCompound();
		// 		sectionNBT.setByte("Y",
		// 				(byte) (chunkSection.getYLocation() >> 4 & 255));
		// 		byte[] buffer = new byte[4096];
		// 		NibbleArray nibblearray = new NibbleArray();
		// 		// getData() is juste a byte array in 1.8.9
		// 		// NibbleArray nibblearray1 = chunkSection.getData()
		// 		// 		.getDataForNBT(buffer, nibblearray);

		// 		sectionNBT.setByteArray("Blocks", buffer);
		// 		sectionNBT.setByteArray("Data", nibblearray.getData());
				
		// 		// if (nibblearray1 != null) {
		// 		// 	sectionNBT.setByteArray("Add", nibblearray1.getData());
		// 		// }
				
		// 		sectionNBT.setByteArray("BlockLight", blocklightArray.getData());
		// 		chunkSectionList.appendTag(sectionNBT);
		// 	}
		// }

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
				ResourceLocation location = Block.blockRegistry.getNameForObject(entry.getBlock());
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


	/**
	 * Gets a count of how many chunks there are that still need to be written to
	 * disk. (Does not include any chunk that is currently being written to disk)
	 *
	 * @return The number of chunks that still need to be written to disk
	 */
	public synchronized int getNumPendingChunks() {
		return this.chunksToSave.size();
	}

	public static WDLChunkLoader create(WDL wdl,
			SaveHandler handler, WorldProvider dimension) {
		return new WDLChunkLoader(wdl, getWorldSaveFolder(handler, dimension));
	}



	/**
	 * Creates an NBT list of all entities in this chunk, adding in custom entities.
	 * @param chunk
	 * @return
	 */
	protected NBTTagList getEntityList(Chunk chunk) {
		NBTTagList entityList = new NBTTagList();

		// if (!WDLPluginChannels.canSaveEntities(chunk)) {
		// 	return entityList;
		// }

		// Build a list of all entities in the chunk.
		List<Entity> entities = new ArrayList<>();
		// Add the entities already in the chunk.
		for (ClassInheritanceMultiMap<Entity> map : chunk.getEntityLists()) {
			entities.addAll(map);
		}
		// Add the manually saved entities.
		for (Entity e : wdl.newEntities.get(chunk.getChunkCoordIntPair())) {
			assert chunk.getChunkCoordIntPair().equals(wdl.entityPositions.get(e.getUniqueID())) :
					"Mismatch between position of " + e + " in "
					+ chunk.getChunkCoordIntPair() + " and position recorded in entityPositions of "
					+ wdl.entityPositions.get(e.getUniqueID());
			// "Unkill" the entity, since it is killed when it is unloaded.
			e.isDead = false;
			// e.removed = false;
			entities.add(e);
		}

		for (Entity entity : entities) {
			if (entity == null) {
				LOGGER.warn("[WDL] Null entity in chunk at "
						+ chunk.getChunkCoordIntPair());
				continue;
			}

			if (!shouldSaveEntity(entity)) {
				continue;
			}

			// Apply any editors.
			for (ModInfo<IEntityEditor> info : WDLApi
					.getImplementingExtensions(IEntityEditor.class)) {
				try {
					if (info.mod.shouldEdit(entity)) {
						info.mod.editEntity(entity);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Failed to edit entity "
							+ entity + " for chunk at "
							+ chunk.getChunkCoordIntPair() + " with extension "
							+ info, ex);
				}
			}

			NBTTagCompound entityData = new NBTTagCompound();
			try {
				if (entity.writeToNBTOptional(entityData)) {
					// For some fucking reason, at least in a dev env, 
					// the tagCompund.setTag("Pos", ...) from entity.writeToNBT doesn't work,
					// as entity.posX, Y, Z have got some absolute nonsense as their values.
					// To fix this, the "Pos" tag is reimplemented here using "prevPosX" instead of "posX"
					// which works correctly
					NBTTagList nbttaglist = new NBTTagList();
					nbttaglist.appendTag(new NBTTagDouble(entity.prevPosX));
					nbttaglist.appendTag(new NBTTagDouble(entity.prevPosY));
					nbttaglist.appendTag(new NBTTagDouble(entity.prevPosZ));

					entityData.setTag("Pos", nbttaglist);
					// Uncomment those lines to undestant the nonsense of the pos values
					// System.out.println("====================");
					// System.out.println("entity chunkcoords: " + entity.chunkCoordX + " " + entity.chunkCoordY + " " + entity.chunkCoordZ);
					// System.out.println("entity prevcoords: " + entity.prevPosX + " " + entity.prevPosY + " " + entity.prevPosZ);
					// System.out.println("entity coords: " + entity.posX + " " + entity.posY + " " + entity.posZ);
					// System.out.println("entity type: " + entity.getName());
					// System.out.println(entityData.getTagList("Pos", 6));

					// TODO: add toggle for that
					entityData.setBoolean("NoAI", true);

					chunk.setHasEntities(true);
					entityList.appendTag(entityData);
				}
			} catch (Exception e) {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.ERROR,
						"wdl.messages.generalError.failedToSaveEntity", entity, chunk.getChunkCoordIntPair().chunkXPos, chunk.getChunkCoordIntPair().chunkZPos, e);
				LOGGER.warn("Compound: " + entityData);
				LOGGER.warn("Entity metadata dump:");
				try {
					// EntityDataManager doesn't exist here
					List<DataWatcher.WatchableObject> objects = entity
							.getDataWatcher().getAllWatched();
					if (objects == null) {
						LOGGER.warn("No entries (getAllWatched() returned null)");
					} else {
						LOGGER.warn(objects);
						for (DataWatcher.WatchableObject obj : objects) {
							if (obj != null) {
								LOGGER.warn("WatchableObject [getDataValueId()="
										+ obj.getDataValueId()
										+ ", getObject()="
										+ obj.getObject()
										+ ", getObjectType()="
										+ obj.getObjectType()
										+ ", isWatched()="
										+ obj.isWatched() + "]");
							}
						}
					}
				} catch (Exception e2) {
					LOGGER.warn("Failed to complete dump: ", e);
				}
				LOGGER.warn("End entity metadata dump");
				continue;
			}
		}
		// if (entityList.tagCount() > 0) {
		// 	System.out.println("count:" + entityList.tagCount());
		// 	System.out.println(entityList.toString());
		// 	System.out.println(chunk.getChunkCoordIntPair().toString());
		// }

		return entityList;
	}
	

	/**
	 * Checks if the given entity should be saved, putting a message into the
	 * chat if it can't.
	 *
	 * @param e
	 *            The entity to check
	 * @return True if the entity should be saved.
	 */
	protected static boolean shouldSaveEntity(Entity e) {
		if (e instanceof EntityPlayer) {
			// Players shouldn't be saved, and it's dangerous to mess with them.
			return false;
		}

		if (!EntityUtils.isEntityEnabled(e)) {
			WDLMessages.chatMessageTranslated(
					WDL.serverProps,
					WDLMessageTypes.REMOVE_ENTITY,
					"wdl.messages.removeEntity.notSavingUserPreference", e);
			return false;
		}

		return true;
	}

	/**
	 * Creates an NBT list of all tile entities in this chunk, importing tile
	 * entities as needed.
	 */
	protected NBTTagList getTileEntityList(Chunk chunk) {
		NBTTagList tileEntityList = new NBTTagList();

		// if (!WDLPluginChannels.canSaveTileEntities(chunk)) {
		// 	return tileEntityList;
		// }

		Map<BlockPos, TileEntity> chunkTEMap = chunk.getTileEntityMap();
		Map<BlockPos, NBTTagCompound> oldTEMap = getOldTileEntities(chunk);
		Map<BlockPos, TileEntity> newTEMap = wdl.newTileEntities.get(chunk.getChunkCoordIntPair());
		if (newTEMap == null) {
			newTEMap = new HashMap<>();
		}

		// All the locations of tile entities in the chunk.
		Set<BlockPos> allTELocations = new HashSet<>();
		allTELocations.addAll(chunkTEMap.keySet());
		allTELocations.addAll(oldTEMap.keySet());
		allTELocations.addAll(newTEMap.keySet());

		for (BlockPos pos : allTELocations) {
			// Now, add all the tile entities, using the "best" map
			// if it's in multiple.
			if (newTEMap.containsKey(pos)) {
				NBTTagCompound compound = new NBTTagCompound();

				TileEntity te = newTEMap.get(pos);
				
				try {
					te.writeToNBT(compound);
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.ERROR,
							"wdl.messages.generalError.failedToSaveTE", te, pos, chunk.getChunkCoordIntPair().chunkXPos, chunk.getChunkCoordIntPair().chunkZPos, e);
					LOGGER.warn("Compound: " + compound);
					continue;
				}

				String entityType = compound.getString("id") +
						" (" + te.getClass().getCanonicalName() +")";
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.LOAD_TILE_ENTITY,
						"wdl.messages.tileEntity.usingNew", entityType, pos);

				editTileEntity(pos, compound, TileEntityCreationMode.NEW);

				tileEntityList.appendTag(compound);
			} else if (oldTEMap.containsKey(pos)) {
				NBTTagCompound compound = oldTEMap.get(pos);
				String entityType = compound.getString("id");

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.LOAD_TILE_ENTITY,
						"wdl.messages.tileEntity.usingOld", entityType, pos);

				editTileEntity(pos, compound, TileEntityCreationMode.IMPORTED);

				tileEntityList.appendTag(compound);
			} else if (chunkTEMap.containsKey(pos)) {
				// TODO: Do we want a chat message for this?
				// It seems unnecessary.
				TileEntity te = chunkTEMap.get(pos);
				NBTTagCompound compound = new NBTTagCompound();
				try {
					te.writeToNBT(compound);
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.ERROR,
							"wdl.messages.generalError.failedToSaveTE", te, pos, chunk.getChunkCoordIntPair().chunkXPos, chunk.getChunkCoordIntPair().chunkZPos, e);
					LOGGER.warn("Compound: " + compound);
					continue;
				}

				editTileEntity(pos, compound, TileEntityCreationMode.EXISTING);

				tileEntityList.appendTag(compound);
			}
		}
		return tileEntityList;
	}

	/**
	 * Gets a map of all tile entities in the previous version of that chunk.
	 * Only "problematic" tile entities (those that require manual opening) will
	 * be imported, and the tile entity must be in the correct position (IE, the
	 * block at the tile entity's position must match the block normally used
	 * with that tile entity). See
	 * {@link #shouldImportBlockEntity(String, BlockPos)} for details.
	 *
	 * @param chunk
	 *            The chunk that currently exists in that location
	 * @return A map of positions to tile entities.
	 */
	protected Map<BlockPos, NBTTagCompound> getOldTileEntities(Chunk chunk) {
		Map<BlockPos, NBTTagCompound> returned = new HashMap<>();

		try {
			NBTTagCompound chunkNBT;

			// The reason for the weird syntax here rather than containsKey is because
			// chunksToSave can be accessed from multiple threads.  Note that this still
			// doesn't handle the MC-119971-like case of the chunk being in chunksBeingSaved
			// (but that should be rare, and this condition should not happen in the first place)
			if ((chunkNBT = chunksToSave.get(chunk.getChunkCoordIntPair())) != null) {
				LOGGER.warn("getOldTileEntities (and thus saveChunk) was called while a chunk was already in chunksToSave!  (location: {})", chunk.getChunkCoordIntPair(), new Exception());
			} else try (DataInputStream dis = RegionFileCache.getChunkInputStream(
					chunkSaveLocation, chunk.getChunkCoordIntPair().chunkXPos, chunk.getChunkCoordIntPair().chunkZPos)) {
				if (dis == null) {
					// This happens whenever the chunk hasn't been saved before.
					// It's a normal case.
					return returned;
				}

				chunkNBT = CompressedStreamTools.read(dis);
				dis.close();
			}

			NBTTagCompound levelNBT = chunkNBT.getCompoundTag("Level");
			NBTTagList oldList = levelNBT.getTagList("TileEntities", 10);

			if (oldList != null) {
				for (int i = 0; i < oldList.tagCount(); i++) {
					NBTTagCompound oldNBT = oldList.getCompoundTagAt(i);

					String entityID = oldNBT.getString("id");
					BlockPos pos = new BlockPos(oldNBT.getInteger("x"),
							oldNBT.getInteger("y"), oldNBT.getInteger("z"));
					Block block = chunk.getBlock(pos);

					if (shouldImportBlockEntity(entityID, pos, block, oldNBT, chunk)) {
						returned.put(pos, oldNBT);
					} else {
						// Even if this tile entity is saved in another way
						// later, we still want the player to know we did not
						// import something in that chunk.

						WDLMessages.chatMessageTranslated(
								WDL.serverProps,
								WDLMessageTypes.LOAD_TILE_ENTITY,
								"wdl.messages.tileEntity.notImporting", entityID, pos);
					}
				}
			}
		} catch (Exception e) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ERROR,
					"wdl.messages.generalError.failedToImportTE", chunk.getChunkCoordIntPair().chunkXPos, chunk.getChunkCoordIntPair().chunkZPos, e);
		}
		return returned;
	}

	/**
	 * Checks if the block entity should be imported. Only "problematic" (IE,
	 * those that require manual interaction such as chests) block entities will
	 * be imported. Additionally, the block at the block entity's coordinates
	 * must be one that would normally be used with that block entity.
	 *
	 * @param entityID
	 *            The block entity's ID, as found in the 'id' tag.
	 * @param pos
	 *            The location of the block entity, as created by its 'x', 'y',
	 *            and 'z' tags.
	 * @param block
	 *            The block in the current world at the given position.
	 * @param blockEntityNBT
	 *            The full NBT tag of the existing block entity. May be used if
	 *            further identification is needed.
	 * @param chunk
	 *            The (current) chunk for which entities are being imported. May be used
	 *            if further identification is needed (e.g. nearby blocks).
	 * @return <code>true</code> if that block entity should be imported.
	 */
	protected boolean shouldImportBlockEntity(String entityID, BlockPos pos,
			Block block, NBTTagCompound blockEntityNBT, Chunk chunk) {
		if (HandlerFunctions.shouldImportBlockEntity(entityID, pos, block, blockEntityNBT, chunk)) {
			return true;
		}

		for (ModInfo<ITileEntityImportationIdentifier> info : WDLApi
				.getImplementingExtensions(ITileEntityImportationIdentifier.class)) {
			if (info.mod.shouldImportTileEntity(entityID, pos, block,
					blockEntityNBT, chunk)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Applies all registered {@link ITileEntityEditor}s to the given tile entity.
	 */
	protected static void editTileEntity(BlockPos pos, NBTTagCompound compound,
			TileEntityCreationMode creationMode) {
		for (ModInfo<ITileEntityEditor> info : WDLApi
				.getImplementingExtensions(ITileEntityEditor.class)) {
			try {
				if (info.mod.shouldEdit(pos, compound, creationMode)) {
					info.mod.editTileEntity(pos, compound, creationMode);

					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.LOAD_TILE_ENTITY,
							"wdl.messages.tileEntity.edited", pos, info.getDisplayName());
				}
			} catch (Exception ex) {
				throw new RuntimeException("Failed to edit tile entity at "
						+ pos + " with extension " + info
						+ "; NBT is now " + compound + " (this may be the "
						+ "initial value, an edited value, or a partially "
						+ "edited value)", ex);
			}
		}
	}

	public RegionFile getRegionFileIfExists(int regionX, int regionZ) {
		// 1.13.0 and earlier have func_191065_b or getRegionFileIfExists, but 1.13 doesn't,
		// so we get this...
		File regionFolder = new File(this.chunkSaveLocation, "region");
		File region = new File(regionFolder, "r." + regionX + "." + regionZ + ".mca");
		if (region.exists()) {
			// This takes chunk coordinates and shifts them back into region ones, so convert
			// our region coordinates to chunk ones to appease it
			return RegionFileCache.createOrLoadRegionFile(this.chunkSaveLocation, regionX << 5, regionZ << 5);
		} else {
			return null;
		}
	}
}