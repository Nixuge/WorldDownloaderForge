package me.nixuge.worlddownloader;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import wdl.WDL;

@Mod(
        modid = McMod.MOD_ID,
        name = McMod.NAME,
        version = McMod.VERSION,
        clientSideOnly = true,
        guiFactory = "me.nixuge.worlddownloader.gui.GuiFactory"
)
@Getter
@Setter
public class McMod {
    public static final String MOD_ID = "worlddownloader";
    public static final String NAME = "World Downloader";
    public static final String VERSION = "1.0.1";
    public static final String ORIGINAL_VERSION = "4.1.1.1-SNAPSHOT";
    public static WDL wdl;

    @Getter
    @Mod.Instance(value = McMod.MOD_ID)
    private static McMod instance;
    
    private Configuration configuration;
    private String configDirectory;

    public static String getFullConfigString() {
        return VERSION + " (Nixuge/WorldDownloaderForge), " + ORIGINAL_VERSION + " (Pokechu22/WorldDownloader)";
    }
}