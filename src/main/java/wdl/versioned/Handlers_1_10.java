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
package wdl.versioned;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;


import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockNote;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
// import net.minecraft.world.WorldProvider;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.SaveHandler;
import wdl.handler.block.BeaconHandler;
// import wdl.handler.block.BlockHandler;
import wdl.handler.block.BrewingStandHandler;
import wdl.handler.block.ChestHandler;
import wdl.handler.block.DispenserHandler;
import wdl.handler.block.DropperHandler;
import wdl.handler.block.FurnaceHandler;
import wdl.handler.block.HopperHandler;
import wdl.handler.blockaction.BlockActionHandler;
import wdl.handler.blockaction.NoteBlockHandler;
import wdl.handler.entity.EntityHandler;
import wdl.handler.entity.HopperMinecartHandler;
import wdl.handler.entity.HorseHandler;
import wdl.handler.entity.StorageMinecartHandler;
import wdl.handler.entity.VillagerHandler;

final class HandlerFunctions {
	private HandlerFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#hasSkyLight
	 */
	static boolean hasSkyLight(World world) {
		// 1.10-: use isNether (hasNoSky)
		return !(world.provider.getDimensionId() == -1);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#BLOCK_HANDLERS
	 */
	// ImmutableList<BlockHandler<?, ?>>

	static final ImmutableList<Object> BLOCK_HANDLERS = ImmutableList.of(
			new BeaconHandler(),
			new BrewingStandHandler(),
			new ChestHandler(),
			new DispenserHandler(),
			new DropperHandler(),
			new FurnaceHandler(),
			new HopperHandler()
	);

	/* (non-javadoc)
	 * @see VersionedFunctions#BLOCK_ACTION_HANDLERS
	 */
	static final ImmutableList<BlockActionHandler<?, ?>> BLOCK_ACTION_HANDLERS = ImmutableList.of(
			new NoteBlockHandler()
	);

	/* (non-javadoc)
	 * @see VersionedFunctions#ENTITY_HANDLERS
	 */
	static final ImmutableList<EntityHandler<?, ?>> ENTITY_HANDLERS = ImmutableList.of(
			new HopperMinecartHandler(),
			new HorseHandler(),
			new StorageMinecartHandler(),
			new VillagerHandler()
	);

	/* (non-javadoc)
	 * @see VersionedFunctions#shouldImportBlockEntity
	 */
	static boolean shouldImportBlockEntity(String entityID, BlockPos pos,
			Block block, NBTTagCompound blockEntityNBT, Chunk chunk) {
		// Older (and stranger) block entity IDs.  Note also that
		// shulker boxes did not exist at this time.
		if (block instanceof BlockChest && entityID.equals("Chest")) {
			return true;
		} else if (block instanceof BlockDispenser && entityID.equals("Trap")) {
			return true;
		} else if (block instanceof BlockDropper && entityID.equals("Dropper")) {
			return true;
		} else if (block instanceof BlockFurnace && entityID.equals("Furnace")) {
			return true;
		} else if (block instanceof BlockNote && entityID.equals("Music")) {
			return true;
		} else if (block instanceof BlockBrewingStand && entityID.equals("Cauldron")) {
			return true;
		} else if (block instanceof BlockHopper && entityID.equals("Hopper")) {
			return true;
		} else if (block instanceof BlockBeacon && entityID.equals("Beacon")) {
			return true;
		} else if (block instanceof BlockCommandBlock && entityID.equals("Control")) {
			// Only import command blocks if the current world doesn't have a command set
			// for the one there, as WDL doesn't explicitly save them so we need to use the
			// one currently present in the world.
			TileEntity temp = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
			if (temp == null || !(temp instanceof TileEntityCommandBlock)) {
				// Bad/missing data currently there, import the old data
				return true;
			}
			TileEntityCommandBlock te = (TileEntityCommandBlock) temp;
			boolean currentBlockHasCommand = !te.getCommandBlockLogic().getCommand().isEmpty();
			// Only import if the current command block has no command.
			return !currentBlockHasCommand;
		} else {
			return false;
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createNewBlockEntity
	 */
	@Nullable
	static TileEntity createNewBlockEntity(World world, BlockContainer block, IBlockState state) {
		return block.createNewTileEntity(world, block.getMetaFromState(state));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getSaveHandler
	 */
	static SaveHandler getSaveHandler(Minecraft minecraft, String worldName) {
		// False => Don't save player data.  This is fine for WDL since
		// we handle player data manually.
		return (SaveHandler)minecraft.getSaveLoader().getSaveLoader(worldName, false);
	}

	// VILLAGED REMOVED
	/* (non-javadoc)
	 * @see VersionedFunctions#VANILLA_VILLAGER_CAREERS
	 */
	static final Map<Integer, BiMap<String, Integer>> VANILLA_VILLAGER_CAREERS = new HashMap<>();
	static {
		BiMap<String, Integer> farmer = HashBiMap.create(4);
		farmer.put("entity.Villager.farmer", 1);
		farmer.put("entity.Villager.fisherman", 2);
		farmer.put("entity.Villager.shepherd", 3);
		farmer.put("entity.Villager.fletcher", 4);
		BiMap<String, Integer> librarian = HashBiMap.create(2);
		librarian.put("entity.Villager.librarian", 1);
		BiMap<String, Integer> priest = HashBiMap.create(1);
		priest.put("entity.Villager.cleric", 1);
		BiMap<String, Integer> blacksmith = HashBiMap.create(3);
		blacksmith.put("entity.Villager.armor", 1);
		blacksmith.put("entity.Villager.weapon", 2);
		blacksmith.put("entity.Villager.tool", 3);
		BiMap<String, Integer> butcher = HashBiMap.create(2);
		butcher.put("entity.Villager.butcher", 1);
		butcher.put("entity.Villager.leather", 2);
		BiMap<String, Integer> nitwit = HashBiMap.create(1);
		nitwit.put("entity.Villager.nitwit", 1);

		VANILLA_VILLAGER_CAREERS.put(0, farmer);
		VANILLA_VILLAGER_CAREERS.put(1, librarian);
		VANILLA_VILLAGER_CAREERS.put(2, priest);
		VANILLA_VILLAGER_CAREERS.put(3, blacksmith);
		VANILLA_VILLAGER_CAREERS.put(4, butcher);
		VANILLA_VILLAGER_CAREERS.put(5, nitwit);
	}
}
