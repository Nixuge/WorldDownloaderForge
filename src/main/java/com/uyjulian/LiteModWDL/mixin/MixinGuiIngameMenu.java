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
 */
package com.uyjulian.LiteModWDL.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import wdl.ducks.IBaseChangesApplied;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.uyjulian.LiteModWDL.AddButtonCallback;

@Mixin(GuiIngameMenu.class)
public abstract class MixinGuiIngameMenu extends GuiScreen implements IBaseChangesApplied {

	@Inject(method="initGui", at=@At("RETURN"))
	private void onInitGui(CallbackInfo ci) {
		wdl.WDLHooks.injectWDLButtons((GuiIngameMenu)(Object)this, buttonList, new AddButtonCallback(this.buttonList));
	}
	@Inject(method="actionPerformed", at=@At("HEAD"))
	private void onActionPerformed(GuiButton guibutton, CallbackInfo ci) {
		wdl.WDLHooks.handleWDLButtonClick((GuiIngameMenu)(Object)this, guibutton);
	}
}
