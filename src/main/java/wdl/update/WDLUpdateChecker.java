/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.update;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import wdl.VersionConstants;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.config.settings.MiscSettings;
import wdl.functions.ChatFunctions;

/**
 * Performs the update checking.
 * Note: all multi-version & class hashing code has been removed entirely.
 */
public class WDLUpdateChecker extends Thread {
	/**
	 * Has the update check started?
	 */
	private static volatile boolean started = false;
	/**
	 * Has the update check finished?
	 */
	private static volatile boolean finished = false;
	/**
	 * Did something go wrong with the update check?
	 */
	private static volatile boolean failed = false;
	/**
	 * If something went wrong with the update check, what was it?
	 */
	@Nullable
	private static volatile String failReason = null;

	/**
	 * List of releases.  May be null if the checker has not finished.
	 */
	@Nullable
	private static volatile List<Release> releases;

	/**
	 * The release that is currently running.
	 *
	 * May be null.
	 */
	@Nullable
	private static volatile Release runningRelease;

	/**
	 * Gets the current list of releases. May be null if the checker has not
	 * finished.
	 */
	@Nullable
	public static List<Release> getReleases() {
		return releases;
	}

	/**
	 * Gets the current release.  May be null if the checker has not finished
	 * or if the current version isn't released.
	 */
	@Nullable
	public static Release getRunningRelease() {
		return runningRelease;
	}

	/**
	 * Calculates the release that should be used based off of the user's options.
	 *
	 * May be null if the checker has not finished.
	 */
	@Nullable
	public static Release getRecomendedRelease() {
		if (releases == null || releases.isEmpty()) {
			return null;
		}
		///
		String version = VersionConstants.getForgeModVersion();
		if (isSnapshot(version)) {
			// Running a snapshot version?  Check if a full version was released.
			String realVersion = getRealVersion(version);
			boolean hasRelease = false;
			for (Release release : releases) {
				if (realVersion.equals(release.tag)) {
					hasRelease = true;
				}
			}
			if (!hasRelease) {
				// No full release?  OK, don't recommend they go backwards.
				return null;
				// If there is a full release, we'd recommend the latest release.
			}
		}
		return releases.get(0);
	}

	/**
	 * Is there a new version that should be used?
	 *
	 * True if the running release is not null and if the recommended
	 * release is not the running release.
	 *
	 * The return value of this method may change as the update checker
	 * runs.
	 */
	public static boolean hasNewVersion() {
		if (releases == null || releases.isEmpty()) {
			// Hasn't finished running yet.
			return false;
		}
		Release recomendedRelease = getRecomendedRelease();
		// Note: runningRelease may be unknown; getRecomendedRelease handles that (for snapshots)
		// However, if both are null, we don't want to recommend updating to null; that's pointless
		if (recomendedRelease == null) {
			return false;
		}

		return runningRelease != recomendedRelease;
	}

	/**
	 * Call once the world has loaded.  Will check and start a new update checker
	 * if needed.
	 */
	public static void startIfNeeded() {
		if (!started) {
			started = true;

			new WDLUpdateChecker().start();
		}
	}

	/**
	 * Has the update check finished?
	 */
	public static boolean hasFinishedUpdateCheck() {
		return finished;
	}

	/**
	 * Did something go wrong with the update check?
	 */
	public static boolean hasUpdateCheckFailed() {
		return failed;
	}
	/**
	 * If the update check failed, why?
	 */
	public static String getUpdateCheckFailReason() {
		return failReason;
	}

	private static final String FORUMS_THREAD_USAGE_LINK = "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds#Usage";
	private static final String WIKI_LINK = "https://github.com/pokechu22/WorldDownloader/wiki";
	private static final String GITHUB_LINK = "https://github.com/pokechu22/WorldDownloader";
	private static final String REDISTRIBUTION_LINK = "https://pokechu22.github.io/WorldDownloader/redistribution";
	private static final String SMR_LINK = "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/mods-discussion/2314237-list-of-sites-stealing-minecraft-content";

	private WDLUpdateChecker() {
		super("World Downloader update check thread");
	}

	@Override
	public void run() {
		try {
			if (!WDL.globalProps.getValue(MiscSettings.TUTORIAL_SHOWN)) {
				sleep(2000);

				ChatComponentTranslation success = new ChatComponentTranslation(
						"wdl.intro.success");
				ChatComponentTranslation mcfThread = new ChatComponentTranslation(
						"wdl.intro.forumsLink");
				mcfThread.setChatStyle(ChatFunctions.createLinkFormatting(FORUMS_THREAD_USAGE_LINK));
				ChatComponentTranslation wikiLink = new ChatComponentTranslation(
						"wdl.intro.wikiLink");
				wikiLink.setChatStyle(ChatFunctions.createLinkFormatting(WIKI_LINK));
				ChatComponentTranslation usage = new ChatComponentTranslation(
						"wdl.intro.usage", mcfThread, wikiLink);
				ChatComponentTranslation githubRepo = new ChatComponentTranslation(
						"wdl.intro.githubRepo");
				githubRepo.setChatStyle(ChatFunctions.createLinkFormatting(GITHUB_LINK));
				ChatComponentTranslation contribute = new ChatComponentTranslation(
						"wdl.intro.contribute", githubRepo);
				ChatComponentTranslation redistributionList = new ChatComponentTranslation(
						"wdl.intro.redistributionList");
				redistributionList.setChatStyle(ChatFunctions.createLinkFormatting(REDISTRIBUTION_LINK));
				ChatComponentTranslation warning = new ChatComponentTranslation(
						"wdl.intro.warning");
				warning.getChatStyle().setColor(EnumChatFormatting.DARK_RED).setBold(true);
				ChatComponentTranslation illegally = new ChatComponentTranslation(
						"wdl.intro.illegally");
				illegally.getChatStyle().setColor(EnumChatFormatting.DARK_RED).setBold(true);
				ChatComponentTranslation stolen = new ChatComponentTranslation(
						"wdl.intro.stolen", warning, redistributionList, illegally);
				ChatComponentTranslation smr = new ChatComponentTranslation(
						"wdl.intro.stopModReposts");
				smr.setChatStyle(ChatFunctions.createLinkFormatting(SMR_LINK));
				ChatComponentTranslation stolenBeware = new ChatComponentTranslation(
						"wdl.intro.stolenBeware", smr);

				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, success);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, usage);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, contribute);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, stolen);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, stolenBeware);

				WDL.globalProps.setValue(MiscSettings.TUTORIAL_SHOWN, true);
				WDL.saveGlobalProps();
			}

			sleep(2000);

			releases = GithubInfoGrabber.getReleases();
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.releaseCount", releases.size());

			if (releases.isEmpty()) {
				failed = true;
				failReason = "No releases found.";
				return;
			}

			String version = VersionConstants.getForgeModVersion();
			for (int i = 0; i < releases.size(); i++) {
				Release release = releases.get(i);

				if (release.tag.equalsIgnoreCase(version)) {
					runningRelease = release;
				}
			}

			if (runningRelease == null) {
				if (!isSnapshot(version)) {
					WDLMessages.chatMessageTranslated(WDL.serverProps,
							WDLMessageTypes.UPDATES,
							"wdl.messages.updates.failedToFindMatchingRelease", version);
				} else {
					WDLMessages.chatMessageTranslated(WDL.serverProps,
							WDLMessageTypes.UPDATES,
							"wdl.messages.updates.failedToFindMatchingRelease.snapshot", version, getRealVersion(version));
				}
				// Wait until the new version check finishes before returning.
			}

			if (hasNewVersion()) {
				Release recomendedRelease = getRecomendedRelease();

				ChatComponentTranslation updateLink = new ChatComponentTranslation(
						"wdl.messages.updates.newRelease.updateLink");
				updateLink.setChatStyle(ChatFunctions.createLinkFormatting(recomendedRelease.URL));

				// Show the new version available message, and give a link.
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATES, "wdl.messages.updates.newRelease",
						version, recomendedRelease.tag, updateLink);
			}

			if (runningRelease == null) {
				// Can't hash without a release, but that's a normal condition (unlike below)
				return;
			}

		} catch (Exception e) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.updateCheckError", e);

			failed = true;
			failReason = e.toString();
		} finally {
			finished = true;
		}
	}

	private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

	/**
	 * Checks if a version is a snapshot build.
	 *
	 * @param version
	 *            The version to check
	 * @return true if the version is a SNAPSHOT build
	 */
	private static boolean isSnapshot(@Nonnull String version) {
		return version.endsWith(SNAPSHOT_SUFFIX);
	}

	/**
	 * For a snapshot version, gets the version name for the real version.
	 *
	 * @param version
	 *            The version to use. <strong>Must</strong>
	 *            {@linkplain #isSnapshot(String) be a snapshot version}.
	 * @return the regular version name for that snapshot, without the SNAPSHOT suffix.
	 */
	@Nonnull
	private static String getRealVersion(@Nonnull String version) {
		assert isSnapshot(version) : "getRealVersion should only be used with snapshots; got " + version;

		return version.substring(0, version.length() - SNAPSHOT_SUFFIX.length());
	}
}
