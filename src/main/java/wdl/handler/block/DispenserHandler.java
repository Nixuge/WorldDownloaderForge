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

import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import wdl.handler.HandlerException;
import wdl.reflection.ReflectionUtils;

public class DispenserHandler extends BlockHandler<TileEntityDispenser, ContainerDispenser> {
	public DispenserHandler() {
		super(TileEntityDispenser.class, ContainerDispenser.class, "container.dispenser");
	}

	@Override
	public IChatComponent handle(BlockPos clickedPos, ContainerDispenser container,
			TileEntityDispenser blockEntity, IBlockAccess world,
			BiConsumer<BlockPos, TileEntityDispenser> saveMethod) throws HandlerException {
		IInventory dispenserInventory = ReflectionUtils.findAndGetPrivateField(
				container, IInventory.class);
		String title = getCustomDisplayName(dispenserInventory);
		saveContainerItems(container, blockEntity, 0);
		saveMethod.accept(clickedPos, blockEntity);
		if (title != null) {
			blockEntity.setCustomName(title);
		}
		return new ChatComponentTranslation("wdl.messages.onGuiClosedInfo.savedTileEntity.dispenser");
	}
}
