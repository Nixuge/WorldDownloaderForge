package me.nixuge.worlddownloader;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
    public static final String VERSION = "4.1.1.0";
    public static WDL wdl;

    @Getter
    @Mod.Instance(value = McMod.MOD_ID)
    private static McMod instance;
    
    private Configuration configuration;
    private String configDirectory;

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        this.configDirectory = event.getModConfigurationDirectory().toString();
        final File path = new File(this.configDirectory + File.separator + McMod.MOD_ID + ".cfg");
        this.configuration = new Configuration(path);
    }
}