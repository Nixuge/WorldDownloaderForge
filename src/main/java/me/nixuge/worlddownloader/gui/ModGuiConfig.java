package me.nixuge.worlddownloader.gui;

import me.nixuge.worlddownloader.McMod;
import net.minecraft.client.gui.GuiScreen;
import wdl.gui.pages.GuiWDL;

public class ModGuiConfig extends GuiWDL {
    public ModGuiConfig(final GuiScreen guiScreen) {
        super(guiScreen, McMod.wdl);
    }
}