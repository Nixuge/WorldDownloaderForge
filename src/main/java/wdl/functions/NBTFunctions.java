/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.functions;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

/**
 * 1.15 hides some NBT constructors, which is why most of these functions exist.
 * This file exists in prior versions to deal with formatting of NBT as a string.
 */
//TODO: remove the 1.15 stuff
public class NBTFunctions {
	private NBTFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#nbtString
	 */
	public static String nbtString(NBTBase tag) {
		// No equivalent of toFormattedComponent or similar, so just try to make a
		// decent multi-line string
		String result = tag.toString();
		result = result.replaceAll("\\{", "\\{\n");
		result = result.replaceAll("\\}", "\n\\}");
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createFloatListTag
	 */
	public static NBTTagList createFloatListTag(float... values) {
		NBTTagList result = new NBTTagList();
		for (float value : values) {
			result.appendTag(new NBTTagFloat(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createDoubleListTag
	 */
	public static NBTTagList createDoubleListTag(double... values) {
		NBTTagList result = new NBTTagList();
		for (double value : values) {
			result.appendTag(new NBTTagDouble(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createShortListTag
	 */
	public static NBTTagList createShortListTag(short... values) {
		NBTTagList result = new NBTTagList();
		for (short value : values) {
			result.appendTag(new NBTTagShort(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createStringTag
	 */
	public static NBTTagString createStringTag(String value) {
		return new NBTTagString(value);
	}
}