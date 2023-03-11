package net.minecraft.client;

public class ClientBrandRetriever {
	public static String getClientModName() {
		return "WorldDownloader-" + wdl.VersionConstants.getModVersion() + "-mc" + wdl.VersionConstants.getExpectedVersion();
	}
}
