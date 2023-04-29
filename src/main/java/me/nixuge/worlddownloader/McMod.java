package me.nixuge.worlddownloader;

import net.minecraftforge.fml.common.Mod;
import wdl.VersionConstants;

import java.io.File;

@Mod(
        modid = McMod.MOD_ID,
        name = McMod.NAME,
        version = McMod.VERSION,
        clientSideOnly = true
)
public class McMod {
    public static final String MOD_ID = "worlddownloader";
    public static final String NAME = " World Downloader";
    public static final String VERSION = "1.0.0";

    // From the litemod, prolly unneeded
    public String getName() {
        return "LiteModWDL";
    }
    public String getVersion() {
        return VersionConstants.getModVersion() + "-" + VersionConstants.getExpectedVersion();
    }
    public void init(File configPath) {

    }
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {

    }
}