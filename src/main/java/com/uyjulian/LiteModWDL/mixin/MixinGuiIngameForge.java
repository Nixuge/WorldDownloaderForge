package com.uyjulian.LiteModWDL.mixin;

import net.minecraftforge.client.GuiIngameForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import wdl.gui.notifications.NotificationManager;

@Mixin(GuiIngameForge.class)
public class MixinGuiIngameForge {

	@Inject(method="renderGameOverlay", at = @At("RETURN"), remap = false)
	private void renderGameOverlay(float partialTicks, CallbackInfo ci) {
        NotificationManager.getInstance().draw(partialTicks);
	}
}
