package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;

public class GuiIngameMenu extends GuiScreen/* WDL >>> */ implements wdl.ducks.IBaseChangesApplied/* <<< WDL */ {
	private int field_146445_a;
	private int field_146444_f;

	public void init() {
		this.field_146445_a = 0;
		this.buttons.clear();
		int i = -16;
		int j = 98;
		this.buttons.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + i, I18n.format("menu.returnToMenu", new Object[0])));

		if (!this.minecraft.isIntegratedServerRunning()) {
			((GuiButton)this.buttons.get(0)).message = I18n.format("menu.disconnect", new Object[0]);
		}

		this.buttons.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + i, I18n.format("menu.returnToGame", new Object[0])));
		this.buttons.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.options", new Object[0])));
		GuiButton guibutton;
		this.buttons.add(guibutton = new GuiButton(7, this.width / 2 + 2, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.shareToLan", new Object[0])));
		this.buttons.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.achievements", new Object[0])));
		this.buttons.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.stats", new Object[0])));
		guibutton.active = this.minecraft.isSingleplayer() && !this.minecraft.getIntegratedServer().getPublic();

		/* WDL >>> */
		wdl.WDLHooks.injectWDLButtons(this, buttons, buttons::add);
		/* <<< WDL */
	}

	protected void actionPerformed(GuiButton p_146284_1_) throws IOException {
		/* WDL >>> */
		wdl.WDLHooks.handleWDLButtonClick(this, p_146284_1_);
		/* <<< WDL */

		switch (p_146284_1_.id) {
		case 0:
			this.minecraft.displayGuiScreen(new GuiOptions(this, this.minecraft.gameSettings));
			break;
		case 1:
			boolean flag = this.minecraft.isIntegratedServerRunning();
			boolean flag1 = this.minecraft.isConnectedToRealms();
			p_146284_1_.active = false;
			this.minecraft.world.sendQuittingDisconnectingPacket();
			this.minecraft.loadWorld((WorldClient)null);

			if (flag) {
				this.minecraft.displayGuiScreen(new GuiMainMenu());
			} else if (flag1) {
				RealmsBridge realmsbridge = new RealmsBridge();
				realmsbridge.switchToRealms(new GuiMainMenu());
			} else {
				this.minecraft.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
			}

		case 2:
		case 3:
		default:
			break;
		case 4:
			this.minecraft.displayGuiScreen((GuiScreen)null);
			this.minecraft.func_71381_h();
			break;
		case 5:
			this.minecraft.displayGuiScreen(new GuiAchievements(this, this.minecraft.player.getStats()));
			break;
		case 6:
			this.minecraft.displayGuiScreen(new GuiStats(this, this.minecraft.player.getStats()));
			break;
		case 7:
			this.minecraft.displayGuiScreen(new GuiShareToLan(this));
		}
	}

	public void tick() {
		super.tick();
		++this.field_146444_f;
	}

	public void render(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
		this.renderBackground();
		this.drawCenteredString(this.font, I18n.format("menu.game", new Object[0]), this.width / 2, 40, 16777215);
		super.render(p_73863_1_, p_73863_2_, p_73863_3_);
	}
}
