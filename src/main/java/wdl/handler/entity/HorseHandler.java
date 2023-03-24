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
package wdl.handler.entity;

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentTranslation;
import wdl.handler.HandlerException;
import wdl.reflection.ReflectionUtils;

public class HorseHandler extends EntityHandler<EntityHorse, ContainerHorseInventory> {
	/**
	 * The number of slots used for the player inventory, so that the size
	 * of the horse's inventory can be computed.
	 */
	private static final int PLAYER_INVENTORY_SLOTS = 4 * 9;

	public HorseHandler() {
		super(EntityHorse.class, ContainerHorseInventory.class);
	}

	@Override
	public boolean checkRiding(ContainerHorseInventory container, EntityHorse riddenHorse) {
		EntityHorse horseInContainer = ReflectionUtils
				.findAndGetPrivateField(container, EntityHorse.class);

		// Intentional reference equals
		return horseInContainer == riddenHorse;
	}

	@Override
	public IChatComponent copyData(ContainerHorseInventory container, EntityHorse horse, boolean riding) throws HandlerException {
		AnimalChest horseInventory = new AnimalChest(
				horse.getName(), // This was hardcoded to "HorseChest" in 1.12, but the name in 1.13.  The actual value is unused.
				container.inventorySlots.size() - PLAYER_INVENTORY_SLOTS);

		for (int i = 0; i < horseInventory.getSizeInventory(); i++) {
			Slot slot = container.getSlot(i);
			if (slot.getHasStack()) {
				horseInventory.setInventorySlotContents(i, slot.getStack());
			}
		}

		ReflectionUtils.findAndSetPrivateField(horse, EntityHorse.class, AnimalChest.class, horseInventory);

		if (riding) {
			return new ChatComponentTranslation("wdl.messages.onGuiClosedInfo.savedRiddenEntity.horse");
		} else {
			return new ChatComponentTranslation("wdl.messages.onGuiClosedInfo.savedEntity.horse");
		}
	}

}
