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

import com.google.gson.JsonObject;

/**
 * An individual GitHub release.
 * <br/>
 * Does not contain all of the info, but the used info is included.
 *
 * @see https://developer.github.com/v3/repos/releases/#get-a-release-by-tag-name
 */
public class Release {
	public Release(JsonObject object) {
		this.object = object;
		this.URL = object.get("html_url").getAsString();
		this.textOnlyBody = object.get("body").getAsString();
		this.tag = object.get("tag_name").getAsString();
		this.title = object.get("name").getAsString();
		this.date = object.get("published_at").getAsString();
		this.prerelease = object.get("prerelease").getAsBoolean();
	}

	/**
	 * {@link JsonObject} used to create this.
	 */
	public final JsonObject object;
	/**
	 * URL to the release page.
	 */
	public final String URL;
	/**
	 * Tag name.
	 */
	public final String tag;
	/**
	 * Title for this release.
	 */
	public final String title;
	/**
	 * Date that the release was published on.
	 */
	public final String date;
	/**
	 * Whether the release is a prerelease.
	 */
	public final boolean prerelease;
	/**
	 * Text-only body.
	 */
	public final String textOnlyBody;

}
