/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import java.util.function.BiConsumer;

import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import wdl.handler.HandlerException;

public class BeaconHandler extends BlockHandler<TileEntityBeacon, ContainerBeacon> {
	public BeaconHandler() {
		super(TileEntityBeacon.class, ContainerBeacon.class, "container.beacon");
	}

	@Override
	public IChatComponent handle(BlockPos clickedPos, ContainerBeacon container,
			TileEntityBeacon blockEntity, IBlockAccess world,
			BiConsumer<BlockPos, TileEntityBeacon> saveMethod) throws HandlerException {
		// NOTE: beacons do not have custom names, see https://bugs.mojang.com/browse/MC-124395
		// func_180611_e() = getTileEntity()
		IInventory beaconInventory = container.func_180611_e();
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(beaconInventory, blockEntity);
		saveMethod.accept(clickedPos, blockEntity);
		return new ChatComponentTranslation("wdl.messages.onGuiClosedInfo.savedTileEntity.beacon");
	}
}
