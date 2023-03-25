package wdl.api.addition;

import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.gui.notifications.Level;
import wdl.gui.notifications.Notification;
import wdl.gui.notifications.NotificationManager;
import wdl.gui.pages.GuiWDLChunkOverrides;
import wdl.gui.pages.GuiWDLPermissions;

// TODO: clean up both the current api & this

public class DownloadActions {
    private static NotificationManager notificationManager = NotificationManager.getInstance();
    
    // those functions return the state the lb button should be in after clicking
    // nonfunctional for now, as idk how to prevent a toggle in liquidbounce

    public static boolean startDownload() {
        WDL wdl = WDL.getInstance(); // may change so calling every time

        if (wdl.minecraft.isIntegratedServerRunning()) {
            notificationManager.addNotification(new Notification(Level.ERROR, "Integrated server running. This mod can only download servers."));
            return false;
        }
        if (WDL.downloading) {
            notificationManager.addNotification(new Notification(Level.ERROR, "Already downloading."));
            return true;
        }

        if (WDLPluginChannels.hasChunkOverrides()
                && !WDLPluginChannels.canDownloadInGeneral()) {
            wdl.minecraft.displayGuiScreen(new GuiWDLChunkOverrides(null, wdl));
        } else {
            wdl.startDownload();
        }
        return true;
    }

    public static boolean stopDownload() {
        WDL wdl = WDL.getInstance();

        if (wdl.minecraft.isIntegratedServerRunning()) {
            notificationManager.addNotification(new Notification(Level.ERROR, "Integrated server running. This mod can only download servers."));
            return false; 
        }
        if (!WDL.downloading) {
            notificationManager.addNotification(new Notification(Level.ERROR, "Not currently downloading."));
            return false;
        }

        wdl.stopDownload();

        return false;
    }

    public static boolean toggleDownload() {
        WDL wdl = WDL.getInstance();
        if (wdl.minecraft.isIntegratedServerRunning()) {
            return false; // WDL not available if in singleplayer or LAN server mode
        }

        if (WDL.downloading) {
            wdl.stopDownload();
        } else {
            if (!WDLPluginChannels.canDownloadAtAll()) {
                // If they don't have any permissions, let the player
                // request some.
                if (WDLPluginChannels.canRequestPermissions()) {
                    wdl.minecraft.displayGuiScreen(new GuiWDLPermissions(null, wdl));
                } else {
                    // Should never happen
                }
            } else if (WDLPluginChannels.hasChunkOverrides()
                    && !WDLPluginChannels.canDownloadInGeneral()) {
                // Handle the "only has chunk overrides" state - notify
                // the player of limited areas.
                wdl.minecraft.displayGuiScreen(new GuiWDLChunkOverrides(null, wdl));
            } else {
                wdl.startDownload();
            }
        }
        return false;
    }
}
