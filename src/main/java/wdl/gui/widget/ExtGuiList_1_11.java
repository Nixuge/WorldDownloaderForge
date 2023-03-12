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
package wdl.gui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import wdl.gui.widget.ExtGuiList.ExtGuiListEntry;

abstract class ExtGuiList<T extends ExtGuiListEntry<T>> extends GuiListExtended implements IExtGuiList<T> {

	static abstract class ExtGuiListEntry<T extends ExtGuiListEntry<T>> implements IExtGuiListEntry<T>, IGuiListEntry {

		@Override
		public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }

		private static class ButtonWrapper {
			public final GuiButton button;
			public final int x;
			public final int y;
			public ButtonWrapper(GuiButton button, int x, int y) {
				this.button = button;
				this.x = x;
				this.y = y;
			}
		}
		private static class TextFieldWrapper {
			public final GuiTextField field;
			public final int x;
			public final int y;
			public TextFieldWrapper(GuiTextField field, int x, int y) {
				this.field = field;
				this.x = x;
				this.y = y;
			}
		}

		private final List<ButtonWrapper> buttonList = new ArrayList<>();
		private final List<TextFieldWrapper> fieldList = new ArrayList<>();
		@Nullable
		private ButtonWrapper activeButton;

		@Override
		public final <B extends GuiButton> B addButton(B button, int x, int y) {
			this.buttonList.add(new ButtonWrapper(button, x, y));
			return button;
		}

		@Override
		public final <B extends GuiTextField> B addTextField(B field, int x, int y) {
			this.fieldList.add(new TextFieldWrapper(field, x, y));
			return field;
		}

		@Override
		public final void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
				boolean isSelected) {
			this.drawEntry(x, y, listWidth, slotHeight, mouseX, mouseY);
			for (ButtonWrapper button : this.buttonList) {
				button.button.xPosition = button.x + x + (listWidth / 2);
				button.button.yPosition = button.y + y;
				button.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
			}
			for (TextFieldWrapper field : this.fieldList) {
				field.field.xPosition = field.x + x + (listWidth / 2);
				field.field.yPosition = field.y + y;
				// field.field.render();
				field.field.drawTextBox();
			}
		}

		@Override
		public final boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX,
				int relativeY) {
			boolean result = false;
			for (ButtonWrapper button : this.buttonList) {
				if (button.button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
					this.activeButton = button;
					// button.button.playDownSound(Minecraft.getMinecraft().getSoundHandler());
					button.button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
					result = true;
				}
			}
			for (TextFieldWrapper field : this.fieldList) {
				if (field.field.getVisible()) {
					field.field.mouseClicked(mouseX, mouseY, mouseEvent);
				}
			}
			result |= this.mouseDown(mouseX, mouseY, mouseEvent);
			return result;
		}

		@Override
		public final void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (this.activeButton != null) {
				this.activeButton.button.mouseReleased(x, y);
				this.activeButton = null;
				return;
			}
			this.mouseUp(x, y, mouseEvent);
		}

		final void keyTyped(char typedChar, int keyCode) {
			for (TextFieldWrapper field : this.fieldList) {
				if (field.field.getVisible()) {
					field.field.textboxKeyTyped(typedChar, keyCode);
				}
			}
			if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
				typedChar = '\n';
			}
			if (typedChar != 0) {
				this.charTyped(typedChar);
			}
		}

		final void tick() {
			for (TextFieldWrapper field : this.fieldList) {
				// field.field.tick();
				field.field.updateCursorCounter();
			}
		}
	}

	public ExtGuiList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
		super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
	}

	private final List<T> entries = new ArrayList<>();
	private int y = 0;

	@Override
	public final List<T> getEntries() {
		return entries;
	}

	@Override
	public final void setY(int pos) {
		this.y = pos;
	}

	@Override
	public final int getY() {
		return this.y;
	}

	@Override
	protected final boolean isSelected(int slotIndex) {
		return entries.get(slotIndex).isSelected();
	}

	@Override
	public final IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}

	@Override
	protected final int getSize() {
		return entries.size();
	}

	final void tick() {
		for (T t : this.entries) {
			t.tick();
		}
	}

	final void keyTyped(char typedChar, int keyCode) {
		for (T t : this.entries) {
			t.keyTyped(typedChar, keyCode);
		}
	}

	final void anyKeyPressed() {
		for (T t : this.entries) {
			t.anyKeyPressed();
		}
	}

	@Override
	public final int getListWidth() {
		return this.getEntryWidth();
	}

	// Note:
	// visibility changed & "final" removed for GuiList's last function
	@Override
	public int getScrollBarX() {
		return this.getScrollBarX();
	}

	@Override
	public final int getWidth() {
		return this.width;
	}

	// Hacks for y offsetting
	@Override
	public final boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		if (mouseY - y >= top && mouseY - y <= bottom) {
			return super.mouseClicked(mouseX, mouseY - y, mouseEvent);
		} else {
			return false;
		}
	}

	@Override
	public final boolean mouseReleased(int x, int y, int mouseEvent) {
		return super.mouseReleased(x, y - this.y, mouseEvent);
	}

	@Override
	public final void handleMouseInput() {
		super.handleMouseInput();
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void render(int mouseXIn, int mouseYIn, float partialTicks) {
		GlStateManager.translate(0, y, 0);
		super.drawScreen(mouseXIn, mouseYIn - y, partialTicks);
		GlStateManager.translate(0, -y, 0);
	}

	// Make the dirt background use visual positions that match the screen
	// so that dragging looks less weird
	// was renderHoleBackground before
	@Override
	protected final void overlayBackground(int y1, int y2,
			int alpha1, int alpha2) {
		if (y1 == 0) {
			super.overlayBackground(y1, y2, alpha1, alpha2);
			return;
		} else {
			GlStateManager.translate(0, -y, 0);
			
			// super.renderHoleBackground(y1 + y, y2 + y, alpha1, alpha2);
			super.overlayBackground(y1 + y, y2 + y, alpha1, alpha2);

			GlStateManager.translate(0, y, 0);
		}
	}
}
