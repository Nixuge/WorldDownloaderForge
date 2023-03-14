/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.functions;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiCustomizeWorldScreen;
import net.minecraft.client.gui.GuiFlatPresets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagString;
import wdl.config.settings.GeneratorSettings.Generator;

public final class GeneratorFunctions {
	private GeneratorFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Returns true if the given generator can be used in this Minecraft version.
	 * @param generator The generator
	 * @return True if it is usable.
	 */
	public static boolean isAvaliableGenerator(Generator generator) {
		return generator != Generator.BUFFET;
	}

	/**
	 * Creates a settings GUI for the given world generator (e.g. superflat
	 * options).
	 *
	 * @param generator       The generator.
	 * @param parent          The GUI to return to when the settings GUI is closed.
	 * @param generatorConfig The configuration for the generator, which depends on
	 *                        the generator.
	 * @param callback        Called with the new generator config.
	 * @return The new GUI, or the parent if there is no valid GUI.
	 */
	public static GuiScreen makeGeneratorSettingsGui(Generator generator, GuiScreen parent,
			String generatorConfig, Consumer<String> callback) {
		switch (generator) {
		case FLAT:
			return new GuiFlatPresets(new GuiCreateFlatWorldProxy(parent, generatorConfig, callback));
		case CUSTOMIZED:
			return new GuiCustomizeWorldScreen(new GuiCreateWorldProxy(parent, generatorConfig, callback), generatorConfig);
		default:
			LOGGER.warn("Generator lacks extra settings; cannot make a settings GUI: " + generator);
			return parent;
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateFlatWorld} that allows use of
	 * {@link GuiFlatPresets}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private static class GuiCreateFlatWorldProxy extends GuiCreateFlatWorld {
		private final GuiScreen parent;
		private final String generatorConfig;
		private final Consumer<String> callback;

		public GuiCreateFlatWorldProxy(GuiScreen parent, String generatorConfig, Consumer<String> callback) {
			super(null, generatorConfig);
			this.parent = parent;
			this.generatorConfig = generatorConfig;
			this.callback = callback;
		}

		@Override
		public void initGui() {
			this.mc.displayGuiScreen(parent);
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			// Do nothing
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}

		/**
		 * Gets the current flat preset.
		 */
		// func_146384_e() -> getPreset()
		@Override
		public String func_146384_e() {
			return generatorConfig;
		}

		/**
		 * Sets the current flat preset.
		 */
		// func_146383_a() -> setPreset()
		@Override
		public void func_146383_a(@Nullable String preset) {
			callback.accept(preset == null ? "" : preset);
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateWorld} that allows use of
	 * {@link GuiCustomizeWorldScreen}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private static class GuiCreateWorldProxy extends GuiCreateWorld {
		private final GuiScreen parent;
		private final Consumer<String> callback;

		public GuiCreateWorldProxy(GuiScreen parent, String generatorConfig, Consumer<String> callback) {
			super(parent);

			this.parent = parent;
			this.callback = callback;

			this.chunkProviderSettingsJson = generatorConfig;
		}

		@Override
		public void initGui() {
			callback.accept(this.chunkProviderSettingsJson);
			this.mc.displayGuiScreen(this.parent);
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			// Do nothing
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}
	}

	/**
	 * Makes a backup toast, on versions that support it.
	 *
	 * @param name The name of the world
	 * @param fileSize The size of the file in bytes
	 */
	public static void makeBackupToast(String name, long fileSize) {
		// No toasts in this version
		// TODO: remove (or adapt w future notifications UI?)
	}

	/**
	 * Makes a backup failed toast, on versions that support it.
	 *
	 * @param ex The exception.
	 */
	public static void makeBackupFailedToast(IOException ex) {
		// No toasts in this version
		// TODO: remove (or adapt w future notifications UI?)
	}

	/**
	 * A superflat configuration that generates only air.
	 *
	 * This should be similar to the "the void" preset present since 1.9 (including the
	 * "void" biome), but shouldn't include any decoration (the 31x31 platform).  In versions
	 * without the void biome, another biome (ocean since it's 0, maybe) should be used.
	 */
	public static final String VOID_FLAT_CONFIG = "3;minecraft:air;127";

	// static {
		// Make sure that the void biome exists
		// (this check partially exists so that this class will not compile in versions without it)
		// REMOVED AS THERE DOESNT SEEM TO BE AN ENUM WITH VOID
		// if (Biome.getIdForBiome(Biomes.VOID) != 127) {
		// 	LOGGER.warn("[WDL] Mismatched ID for void biome: " + Biomes.VOID + " = " + Biome.getIdForBiome(Biomes.VOID));
		// }
	// }

	/**
	 * Creates the generator options tag,
	 *
	 * @param generatorOptions The content.  Either a string or an SNBT representation of the data.
	 * @return An NBT tag of some type.
	 */
	public static NBTTagString createGeneratorOptionsTag(String generatorOptions) {
		return new NBTTagString(generatorOptions);
	}
}
