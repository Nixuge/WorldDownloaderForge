package me.nixuge.worlddownloader;

@Mod(
        modid = McMod.MOD_ID,
        name = McMod.NAME,
        version = McMod.VERSION,
        guiFactory = "me.nixuge.nochunkunload.gui.GuiFactory",
        clientSideOnly = true
)
public class McMod {
    public static final String MOD_ID = "worlddownloader";
    public static final String NAME = " World Downloader";
    public static final String VERSION = "4.1.1.0";
}