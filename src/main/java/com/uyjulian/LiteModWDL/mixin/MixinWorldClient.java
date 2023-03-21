/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
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
 * 
 * Modified by Nixuge
 */
package com.uyjulian.LiteModWDL.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import wdl.ducks.IBaseChangesApplied;
import wdl.gui.notifications.NotificationManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World implements IBaseChangesApplied {
    private NotificationManager notificationManager = NotificationManager.getInstance();

	protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn,
			Profiler profilerIn, boolean client) {
		super(saveHandlerIn, info, providerIn, profilerIn, client);
	}

	@Inject(method="tick", at=@At("RETURN"))
	private void onTick(CallbackInfo ci) {
		wdl.WDLHooks.onWorldClientTick((WorldClient)(Object)this);
		
		notificationManager.update();		
	}

	@Inject(method="removeEntityFromWorld", at=@At("HEAD"))
	private void onRemoveEntityFromWorld(int entityID, CallbackInfoReturnable<Entity> ci) {
		wdl.WDLHooks.onWorldClientRemoveEntityFromWorld((WorldClient)(Object)this, entityID);
	}
}
