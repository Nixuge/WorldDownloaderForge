/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.io.Files;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import wdl.WDL;
import wdl.WorldBackup;
import wdl.WorldBackup.ICustomBackupProgressMonitor;
import wdl.WorldBackup.WorldBackupType;
import wdl.config.IConfiguration;
import wdl.config.settings.MiscSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;
import wdl.gui.widget.WDLTextField;

/**
 * GUI allowing control over the way the world is backed up.
 */
public class GuiWDLBackup extends WDLScreen {
	@Nullable
	private final GuiScreen parent;
	private final WDL wdl;
	private final IConfiguration config;

	private String description;

	private WorldBackupType backupType;
	private WDLButton backupTypeButton;
	private WDLButton doneButton;
	private WDLTextField customBackupCommandTemplateFld;
	private String customBackupCommandTemplate;
	private WDLTextField customBackupExtensionFld;
	private String customBackupExtension;
	private long checkValidTime = 0;
	private volatile boolean checkingCommandValid = false;
	private volatile boolean isCommandValid = true;
	private volatile @Nullable String commandInvalidReason;

	public GuiWDLBackup(@Nullable GuiScreen parent, WDL wdl) {
		super("wdl.gui.backup.title");
		this.parent = parent;
		this.wdl = wdl;
		this.config = WDL.serverProps;

		this.backupType = config.getValue(MiscSettings.BACKUP_TYPE);
		this.customBackupCommandTemplate = config.getValue(MiscSettings.BACKUP_COMMAND_TEMPLATE);
		this.customBackupExtension = config.getValue(MiscSettings.BACKUP_EXTENSION);

		this.description = I18n.format("wdl.gui.backup.description1") + "\n\n"
				+ I18n.format("wdl.gui.backup.description2") + "\n\n"
				+ I18n.format("wdl.gui.backup.description3");
	}

	@Override
	public void initGui() {
		backupTypeButton = this.addButton(new WDLButton(this.width / 2 - 100, 32,
				200, 20, getBackupButtonText()) {
			public @Override void performAction() {
				switch (backupType) {
				case NONE: backupType = WorldBackupType.FOLDER; break;
				case FOLDER: backupType = WorldBackupType.ZIP; break;
				case ZIP: backupType = WorldBackupType.CUSTOM; break;
				case CUSTOM: backupType = WorldBackupType.NONE; break;
				}

				updateFieldVisibility();
				setMessage(getBackupButtonText());
			}
		});

		customBackupCommandTemplateFld = this.addTextField(new WDLTextField(fontRendererObj,
				width / 2 - 100, 54, 200, 20,
				new ChatComponentTranslation("wdl.gui.backup.customCommandTemplate")));
		customBackupCommandTemplateFld.setMaxStringLength(255);
		customBackupCommandTemplateFld.setText(this.customBackupCommandTemplate);
		customBackupExtensionFld = this.addTextField(new WDLTextField(fontRendererObj,
				width / 2 + 160, 54, 40, 20,
				new ChatComponentTranslation("wdl.gui.backup.customExtension")));
		customBackupExtensionFld.setText(this.customBackupExtension);

		updateFieldVisibility();

		doneButton = this.addButton(new ButtonDisplayGui(this.width / 2 - 100, height - 29,
				200, 20, this::getParentOrWarning));
	}

	private String getBackupButtonText() {
		return I18n.format("wdl.gui.backup.backupMode",
				backupType.getDescription());
	}

	private void updateFieldVisibility() {
		boolean isCustom = (backupType == WorldBackupType.CUSTOM);
		customBackupCommandTemplateFld.setVisible(isCustom);
		customBackupExtensionFld.setVisible(isCustom);
		if (isCustom) {
			isCommandValid = false;
			checkValidTime = System.currentTimeMillis();
		} else {
			// Non-custom ones are always valid
			isCommandValid = true;
			checkValidTime = 0;
		}
	}

	@Override
	public void anyKeyPressed() {
		if (customBackupCommandTemplateFld.getVisible() &&
				(customBackupCommandTemplateFld.isFocused() || customBackupExtensionFld.isFocused())) {
			String newTemplate = customBackupCommandTemplateFld.getText();
			String newExt = customBackupExtensionFld.getText();
			if (checkValidTime != 0 || !newTemplate.equals(customBackupCommandTemplate) ||
					!newExt.equals(customBackupExtension)) {
				customBackupCommandTemplate = newTemplate;
				customBackupExtension = newExt;
				// If a check is already queued, delay it until the user stops typing, even if the typing isn't changing anything.
				// Otherwise, only recheck if the typing changes something.
				isCommandValid = false;
				checkValidTime = System.currentTimeMillis() + 1000; // 1 second later
			}
		}
	}

	@Override
	public void onGuiClosed() {
		if (isCommandValid) {
			config.setValue(MiscSettings.BACKUP_TYPE, backupType);
			config.setValue(MiscSettings.BACKUP_COMMAND_TEMPLATE, customBackupCommandTemplate);
			config.setValue(MiscSettings.BACKUP_EXTENSION, customBackupExtension);

			wdl.saveProps();
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		long now = System.currentTimeMillis();
		if (checkValidTime != 0 && now >= checkValidTime) {
			checkCustomBackupConfig();
		}

		// A check is neither queued nor in progress
		doneButton.setEnabled(checkValidTime == 0 && !checkingCommandValid);

		int color = 0x40E040;
		if (checkValidTime != 0 || checkingCommandValid) {
			color = 0xE0E040; // Pending checking
		} else if (!isCommandValid) {
			color = 0xE04040; // Invalid
		}

		customBackupCommandTemplateFld.setTextColor(color);
		customBackupExtensionFld.setTextColor(color);
	}

	/**
	 * Checks if the configuration for the custom backup is correct.
	 */
	private void checkCustomBackupConfig() {
		// Now in progress, no longer queueud
		checkValidTime = 0;
		checkingCommandValid = true;

		class BackupTestRunnable implements ICustomBackupProgressMonitor, Runnable {
			public BackupTestRunnable() {
				this.origCommandTemplate = customBackupCommandTemplate;
				this.origExtension = customBackupExtension;
				this.endTime = System.currentTimeMillis() + 5000; // 5 seconds later
			}
			public final String origCommandTemplate, origExtension;
			public StringBuilder output = new StringBuilder();
			public final long endTime;
			@Override
			public void incrementNumerator() { }
			@Override
			public void onTextUpdate(String text) {
				if (output.length() != 0) {
					output.append("\n");
				}
				output.append(text);
			}
			@Override
			public void setDenominator(int value, boolean show) { }
			@Override
			public void setNumerator(int value) { }
			@Override
			public boolean shouldCancel() {
				// True if too much time passed, the command changed, or the GUI was closed.
				return customSettingsChanged() ||
						System.currentTimeMillis() >= endTime ||
						mc.currentScreen != GuiWDLBackup.this;
			}

			private boolean customSettingsChanged() {
				return !origCommandTemplate.equals(customBackupCommandTemplate) ||
						!origExtension.equals(customBackupExtension);
			}

			@Override
			public void run() {
				File tempDir = null, tempOptions = null, tempDest = null;
				boolean valid;
				String invalidReason;

				try {
					tempDir = Files.createTempDir();
					File optionsTxt = new File(mc.mcDataDir, "options.txt"); // Should exist
					tempOptions = new File(tempDir, "options.txt");
					Files.copy(optionsTxt, tempOptions);
					tempDest = File.createTempFile("wdlbackuptest", "." + customBackupExtension);
					tempDest.delete(); // We only want it for the file name; the empty file actually causes other problems

					WorldBackup.runCustomBackup(customBackupCommandTemplate, tempDir, tempDest, this);

					valid = true;
					invalidReason = null;
				} catch (Exception ex) {
					valid = false;
					invalidReason = ex.getMessage() + "\n\n" + output.toString();
				}
				if (tempOptions != null) tempOptions.delete();
				if (tempDir != null) tempDir.delete();
				if (tempDest != null) tempDest.delete();

				if (!customSettingsChanged()) {
					isCommandValid = valid;
					commandInvalidReason = invalidReason;
					checkingCommandValid = false;
				}
			}
		}

		new Thread(new BackupTestRunnable()).start();
	}

        @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);

		super.drawScreen(mouseX, mouseY, partialTicks);

		if (customBackupCommandTemplateFld.getVisible()) {
			String text = I18n.format("wdl.gui.backup.customCommandTemplate");
			int x = customBackupCommandTemplateFld.xPosition - 3 - fontRendererObj.getStringWidth(text);
			int y = customBackupCommandTemplateFld.yPosition + 6;
			this.drawString(fontRendererObj, text, x, y, 0xFFFFFF);
		}
		if (customBackupExtensionFld.getVisible()) {
			String text = I18n.format("wdl.gui.backup.customExtension");
			int x = customBackupExtensionFld.xPosition - 3 - fontRendererObj.getStringWidth(text);
			int y = customBackupExtensionFld.yPosition + 6;
			this.drawString(fontRendererObj, text, x, y, 0xFFFFFF);
			if (customBackupExtensionFld.getText().equalsIgnoreCase("rar")) {
				x = customBackupExtensionFld.xPosition + customBackupExtensionFld.getWidth() + 14;
				this.drawString(fontRendererObj, "ಠ_ಠ", x, y, 0xFF0000); // See some of my experiences with dealing with rar files.  Use a non-proprietary format, please, it's for your own good!
			}
		}
		if (!isCommandValid && commandInvalidReason != null) {
			List<String> lines = Utils.wordWrap(commandInvalidReason, this.width - 50);
			int y = 80;
			for (String line : lines) {
				this.drawString(fontRendererObj, line, 50, y, 0xFF0000);
				y += fontRendererObj.FONT_HEIGHT;
			}
		}

		if (customBackupCommandTemplateFld.isMouseOver()) {
			Utils.drawGuiInfoBox(I18n.format("wdl.gui.backup.customCommandTemplate.description"), width, height, 48);
		} else if (customBackupExtensionFld.isMouseOver()) {
			Utils.drawGuiInfoBox(I18n.format("wdl.gui.backup.customExtension.description"), width, height, 48);
		} else if (commandInvalidReason == null || backupTypeButton.isMouseOver()) {
			// Only draw the large description if the command is valid (i.e. there isn't other text)
			// or the mouse is directly over the backup type button (i.e. the info is useful)
			Utils.drawGuiInfoBox(description, width - 50, 3 * this.height / 5, width,
					height, 48);
		}
	}

	private GuiScreen getParentOrWarning() {
		if (this.isCommandValid) {
			return parent;
		} else {
			return new GuiYesNo((result, id) -> {
				if (result) {
					mc.displayGuiScreen(parent);
				} else {
					mc.displayGuiScreen(GuiWDLBackup.this);
				}
			}, I18n.format("wdl.gui.backup.customCommandFailed.line1"),
					I18n.format("wdl.gui.backup.customCommandFailed.line2"),
					I18n.format("gui.yes"), I18n.format("gui.cancel"), 0);
		}
	}
}
