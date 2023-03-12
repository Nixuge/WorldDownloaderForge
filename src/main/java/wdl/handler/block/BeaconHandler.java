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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import wdl.handler.HandlerException;

public class BeaconHandler extends BlockHandler<TileEntityBeacon, ContainerBeacon> {
	public BeaconHandler() {
		super(TileEntityBeacon.class, ContainerBeacon.class, "container.beacon");
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, ContainerBeacon container,
			TileEntityBeacon blockEntity, IBlockAccess world,
			BiConsumer<BlockPos, TileEntityBeacon> saveMethod) throws HandlerException {
		// NOTE: beacons do not have custom names, see https://bugs.mojang.com/browse/MC-124395
		IInventory beaconInventory = container.getTileEntity();
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(beaconInventory, blockEntity);
		saveMethod.accept(clickedPos, blockEntity);
		return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedTileEntity.beacon");
	}
}
