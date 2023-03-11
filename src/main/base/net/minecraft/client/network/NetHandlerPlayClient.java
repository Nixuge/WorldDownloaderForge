package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiScreenDemo;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleItemPickup;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.player.inventory.LocalBlockIntercommunication;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EquineEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketCooldown;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketStatistics;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayClient implements INetHandlerPlayClient/* WDL >>> */, wdl.ducks.IBaseChangesApplied/* <<< WDL */ {
	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * The NetworkManager instance used to communicate with the server, used to respond to various packets (primarilly
	 * movement and plugin channel related ones) and check the status of the network connection externally
	 */
	private final NetworkManager netManager;
	private final GameProfile profile;
	/**
	 * Seems to be either null (integrated server) or an instance of either GuiMultiplayer (when connecting to a server) or
	 * GuiScreenReamlsTOS (when connecting to MCO server)
	 */
	private final GuiScreen guiScreenServer;
	/** Reference to the Minecraft instance, which many handler methods operate on */
	private Minecraft client;
	/** Reference to the current ClientWorld instance, which many handler methods operate on */
	private WorldClient world;
	/**
	 * True if the client has finished downloading terrain and may spawn. Set upon receipt of S08PacketPlayerPosLook, reset
	 * upon respawning
	 */
	private boolean doneLoadingTerrain;
	/** A mapping from player names to their respective GuiPlayerInfo (specifies the clients response time to the server) */
	private final Map<UUID, NetworkPlayerInfo> playerInfoMap = Maps.<UUID, NetworkPlayerInfo>newHashMap();
	public int field_147304_c = 20;
	private boolean field_147308_k = false;
	/**
	 * Just an ordinary random number generator, used to randomize audio pitch of item/orb pickup and randomize both
	 * particlespawn offset and velocity
	 */
	private final Random avRandomizer = new Random();

	public NetHandlerPlayClient(Minecraft mcIn, GuiScreen previousGuiScreen, NetworkManager networkManagerIn, GameProfile profileIn) {
		this.client = mcIn;
		this.guiScreenServer = previousGuiScreen;
		this.netManager = networkManagerIn;
		this.profile = profileIn;
	}

	/**
	 * Clears the WorldClient instance associated with this NetHandlerPlayClient
	 */
	public void cleanup() {
		this.world = null;
	}

	/**
	 * Registers some server properties (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a new
	 * WorldClient and sets the player initial dimension
	 */
	public void handleJoinGame(SPacketJoinGame packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.playerController = new PlayerControllerMP(this.client, this);
		this.world = new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.func_149194_f(), packetIn.func_149192_g(), this.client.profiler);
		this.client.gameSettings.difficulty = packetIn.func_149192_g();
		this.client.loadWorld(this.world);
		this.client.player.dimension = packetIn.func_149194_f();
		this.client.displayGuiScreen(new GuiDownloadTerrain(this));
		this.client.player.setEntityId(packetIn.getPlayerId());
		this.field_147304_c = packetIn.func_149193_h();
		this.client.player.setReducedDebug(packetIn.isReducedDebugInfo());
		this.client.playerController.setGameType(packetIn.getGameType());
		this.client.gameSettings.sendSettingsToServer();
		this.netManager.sendPacket(new CPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
	}

	/**
	 * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
	 */
	public void handleSpawnObject(SPacketSpawnObject packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		Entity entity = null;

		if (packetIn.func_148993_l() == 10) {
			entity = EntityMinecart.create(this.world, d0, d1, d2, EntityMinecart.Type.func_184955_a(packetIn.getData()));
		} else if (packetIn.func_148993_l() == 90) {
			Entity entity1 = this.world.getEntityByID(packetIn.getData());

			if (entity1 instanceof EntityPlayer) {
				entity = new EntityFishHook(this.world, d0, d1, d2, (EntityPlayer)entity1);
			}

			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 60) {
			entity = new EntityTippedArrow(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 91) {
			entity = new EntitySpectralArrow(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 61) {
			entity = new EntitySnowball(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 71) {
			entity = new EntityItemFrame(this.world, new BlockPos(d0, d1, d2), EnumFacing.byHorizontalIndex(packetIn.getData()));
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 77) {
			entity = new EntityLeashKnot(this.world, new BlockPos(MathHelper.floor(d0), MathHelper.floor(d1), MathHelper.floor(d2)));
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 65) {
			entity = new EntityEnderPearl(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 72) {
			entity = new EntityEnderEye(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 76) {
			entity = new EntityFireworkRocket(this.world, d0, d1, d2, (ItemStack)null);
		} else if (packetIn.func_148993_l() == 63) {
			entity = new EntityLargeFireball(this.world, d0, d1, d2, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 93) {
			entity = new EntityDragonFireball(this.world, d0, d1, d2, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 64) {
			entity = new EntitySmallFireball(this.world, d0, d1, d2, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 66) {
			entity = new EntityWitherSkull(this.world, d0, d1, d2, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 67) {
			entity = new EntityShulkerBullet(this.world, d0, d1, d2, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 62) {
			entity = new EntityEgg(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 73) {
			entity = new EntityPotion(this.world, d0, d1, d2, (ItemStack)null);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 75) {
			entity = new EntityExpBottle(this.world, d0, d1, d2);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 1) {
			entity = new EntityBoat(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 50) {
			entity = new EntityTNTPrimed(this.world, d0, d1, d2, (EntityLivingBase)null);
		} else if (packetIn.func_148993_l() == 78) {
			entity = new EntityArmorStand(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 51) {
			entity = new EntityEnderCrystal(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 2) {
			entity = new EntityItem(this.world, d0, d1, d2);
		} else if (packetIn.func_148993_l() == 70) {
			entity = new EntityFallingBlock(this.world, d0, d1, d2, Block.func_176220_d(packetIn.getData() & 65535));
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 3) {
			entity = new EntityAreaEffectCloud(this.world, d0, d1, d2);
		}

		if (entity != null) {
			EntityTracker.func_187254_a(entity, d0, d1, d2);
			entity.rotationPitch = (float)(packetIn.getPitch() * 360) / 256.0F;
			entity.rotationYaw = (float)(packetIn.getYaw() * 360) / 256.0F;
			Entity[] aentity = entity.func_70021_al();

			if (aentity != null) {
				int i = packetIn.getEntityID() - entity.getEntityId();

				for (int j = 0; j < aentity.length; ++j) {
					aentity[j].setEntityId(aentity[j].getEntityId() + i);
				}
			}

			entity.setEntityId(packetIn.getEntityID());
			entity.setUniqueId(packetIn.getUniqueId());
			this.world.func_73027_a(packetIn.getEntityID(), entity);

			if (packetIn.getData() > 0) {
				if (packetIn.func_148993_l() == 60 || packetIn.func_148993_l() == 91) {
					Entity entity2 = this.world.getEntityByID(packetIn.getData() - 1);

					if (entity2 instanceof EntityLivingBase && entity instanceof EntityArrow) {
						((EntityArrow)entity).shootingEntity = entity2;
					}
				}

				entity.setVelocity((double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			}
		}
	}

	/**
	 * Spawns an experience orb and sets its value (amount of XP)
	 */
	public void handleSpawnExperienceOrb(SPacketSpawnExperienceOrb packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		Entity entity = new EntityXPOrb(this.world, d0, d1, d2, packetIn.getXPValue());
		EntityTracker.func_187254_a(entity, d0, d1, d2);
		entity.rotationYaw = 0.0F;
		entity.rotationPitch = 0.0F;
		entity.setEntityId(packetIn.getEntityID());
		this.world.func_73027_a(packetIn.getEntityID(), entity);
	}

	/**
	 * Handles globally visible entities. Used in vanilla for lightning bolts
	 */
	public void handleSpawnGlobalEntity(SPacketSpawnGlobalEntity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		Entity entity = null;

		if (packetIn.getType() == 1) {
			entity = new EntityLightningBolt(this.world, d0, d1, d2, false);
		}

		if (entity != null) {
			EntityTracker.func_187254_a(entity, d0, d1, d2);
			entity.rotationYaw = 0.0F;
			entity.rotationPitch = 0.0F;
			entity.setEntityId(packetIn.getEntityId());
			this.world.func_72942_c(entity);
		}
	}

	/**
	 * Handles the spawning of a painting object
	 */
	public void handleSpawnPainting(SPacketSpawnPainting packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPainting entitypainting = new EntityPainting(this.world, packetIn.getPosition(), packetIn.getFacing(), packetIn.func_148961_h());
		entitypainting.setUniqueId(packetIn.getUniqueId());
		this.world.func_73027_a(packetIn.getEntityID(), entitypainting);
	}

	/**
	 * Sets the velocity of the specified entity to the specified value
	 */
	public void handleEntityVelocity(SPacketEntityVelocity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityID());

		if (entity != null) {
			entity.setVelocity((double)packetIn.getMotionX() / 8000.0D, (double)packetIn.getMotionY() / 8000.0D, (double)packetIn.getMotionZ() / 8000.0D);
		}
	}

	/**
	 * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
	 * changed -> Registers any changes locally
	 */
	public void handleEntityMetadata(SPacketEntityMetadata packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityId());

		if (entity != null && packetIn.getDataManagerEntries() != null) {
			entity.getDataManager().setEntryValues(packetIn.getDataManagerEntries());
		}
	}

	/**
	 * Handles the creation of a nearby player entity, sets the position and held item
	 */
	public void handleSpawnPlayer(SPacketSpawnPlayer packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		float f = (float)(packetIn.getYaw() * 360) / 256.0F;
		float f1 = (float)(packetIn.getPitch() * 360) / 256.0F;
		EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(this.client.world, this.getPlayerInfo(packetIn.getUniqueId()).getGameProfile());
		entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = d0;
		entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = d1;
		entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = d2;
		EntityTracker.func_187254_a(entityotherplayermp, d0, d1, d2);
		entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
		this.world.func_73027_a(packetIn.getEntityID(), entityotherplayermp);
		List < EntityDataManager.DataEntry<? >> list = packetIn.func_148944_c();

		if (list != null) {
			entityotherplayermp.getDataManager().setEntryValues(list);
		}
	}

	/**
	 * Updates an entity's position and rotation as specified by the packet
	 */
	public void handleEntityTeleport(SPacketEntityTeleport packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityId());

		if (entity != null) {
			double d0 = packetIn.getX();
			double d1 = packetIn.getY();
			double d2 = packetIn.getZ();
			EntityTracker.func_187254_a(entity, d0, d1, d2);

			if (!entity.canPassengerSteer()) {
				float f = (float)(packetIn.getYaw() * 360) / 256.0F;
				float f1 = (float)(packetIn.getPitch() * 360) / 256.0F;

				if (Math.abs(entity.posX - d0) < 0.03125D && Math.abs(entity.posY - d1) < 0.015625D && Math.abs(entity.posZ - d2) < 0.03125D) {
					entity.setPositionAndRotationDirect(entity.posX, entity.posY, entity.posZ, f, f1, 0, true);
				} else {
					entity.setPositionAndRotationDirect(d0, d1, d2, f, f1, 3, true);
				}

				entity.onGround = packetIn.isOnGround();
			}
		}
	}

	/**
	 * Updates which hotbar slot of the player is currently selected
	 */
	public void handleHeldItemChange(SPacketHeldItemChange packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (InventoryPlayer.isHotbar(packetIn.getHeldItemHotbarIndex())) {
			this.client.player.inventory.currentItem = packetIn.getHeldItemHotbarIndex();
		}
	}

	/**
	 * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
	 * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
	 * rotation or both).
	 */
	public void handleEntityMovement(SPacketEntity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = packetIn.getEntity(this.world);

		if (entity != null) {
			entity.serverPosX += (long)packetIn.getX();
			entity.serverPosY += (long)packetIn.getY();
			entity.serverPosZ += (long)packetIn.getZ();
			double d0 = (double)entity.serverPosX / 4096.0D;
			double d1 = (double)entity.serverPosY / 4096.0D;
			double d2 = (double)entity.serverPosZ / 4096.0D;

			if (!entity.canPassengerSteer()) {
				float f = packetIn.isRotating() ? (float)(packetIn.getYaw() * 360) / 256.0F : entity.rotationYaw;
				float f1 = packetIn.isRotating() ? (float)(packetIn.getPitch() * 360) / 256.0F : entity.rotationPitch;
				entity.setPositionAndRotationDirect(d0, d1, d2, f, f1, 3, false);
				entity.onGround = packetIn.getOnGround();
			}
		}
	}

	/**
	 * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
	 * rotation of the entity itself
	 */
	public void handleEntityHeadLook(SPacketEntityHeadLook packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = packetIn.getEntity(this.world);

		if (entity != null) {
			float f = (float)(packetIn.getYaw() * 360) / 256.0F;
			entity.setRotationYawHead(f);
		}
	}

	/**
	 * Locally eliminates the entities. Invoked by the server when the items are in fact destroyed, or the player is no
	 * longer registered as required to monitor them. The latter  happens when distance between the player and item
	 * increases beyond a certain treshold (typically the viewing distance)
	 */
	public void handleDestroyEntities(SPacketDestroyEntities packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		for (int i = 0; i < packetIn.getEntityIDs().length; ++i) {
			this.world.removeEntityFromWorld(packetIn.getEntityIDs()[i]);
		}
	}

	public void handlePlayerPosLook(SPacketPlayerPosLook packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPlayer entityplayer = this.client.player;
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		float f = packetIn.getYaw();
		float f1 = packetIn.getPitch();

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X)) {
			d0 += entityplayer.posX;
		} else {
			entityplayer.field_70159_w = 0.0D;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) {
			d1 += entityplayer.posY;
		} else {
			entityplayer.field_70181_x = 0.0D;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) {
			d2 += entityplayer.posZ;
		} else {
			entityplayer.field_70179_y = 0.0D;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
			f1 += entityplayer.rotationPitch;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
			f += entityplayer.rotationYaw;
		}

		entityplayer.setPositionAndRotation(d0, d1, d2, f, f1);
		this.netManager.sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
		this.netManager.sendPacket(new CPacketPlayer.PositionRotation(entityplayer.posX, entityplayer.getBoundingBox().minY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch, false));

		if (!this.doneLoadingTerrain) {
			this.client.player.prevPosX = this.client.player.posX;
			this.client.player.prevPosY = this.client.player.posY;
			this.client.player.prevPosZ = this.client.player.posZ;
			this.doneLoadingTerrain = true;
			this.client.displayGuiScreen((GuiScreen)null);
		}
	}

	/**
	 * Received from the servers PlayerManager if between 1 and 64 blocks in a chunk are changed. If only one block
	 * requires an update, the server sends S23PacketBlockChange and if 64 or more blocks are changed, the server sends
	 * S21PacketChunkData
	 */
	public void handleMultiBlockChange(SPacketMultiBlockChange packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		for (SPacketMultiBlockChange.BlockUpdateData spacketmultiblockchange$blockupdatedata : packetIn.getChangedBlocks()) {
			this.world.func_180503_b(spacketmultiblockchange$blockupdatedata.getPos(), spacketmultiblockchange$blockupdatedata.getBlockState());
		}
	}

	/**
	 * Updates the specified chunk with the supplied data, marks it for re-rendering and lighting recalculation
	 */
	public void handleChunkData(SPacketChunkData packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.isFullChunk()) {
			this.world.doPreChunk(packetIn.getChunkX(), packetIn.getChunkZ(), true);
		}

		this.world.func_73031_a(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);
		Chunk chunk = this.world.getChunk(packetIn.getChunkX(), packetIn.getChunkZ());
		chunk.func_186033_a(packetIn.getReadBuffer(), packetIn.getAvailableSections(), packetIn.isFullChunk());
		this.world.func_147458_c(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);

		if (!packetIn.isFullChunk() || !(this.world.dimension instanceof OverworldDimension)) {
			chunk.func_76613_n();
		}

		for (NBTTagCompound nbttagcompound : packetIn.getTileEntityTags()) {
			BlockPos blockpos = new BlockPos(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
			TileEntity tileentity = this.world.getTileEntity(blockpos);

			if (tileentity != null) {
				tileentity.read(nbttagcompound);
			}
		}
	}

	public void processChunkUnload(SPacketUnloadChunk packetIn) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleChunkUnload(this, this.world, packetIn);
		/* <<< WDL */

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.world.doPreChunk(packetIn.getX(), packetIn.getZ(), false);
	}

	/**
	 * Updates the block and metadata and generates a blockupdate (and notify the clients)
	 */
	public void handleBlockChange(SPacketBlockChange packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.world.func_180503_b(packetIn.getPos(), packetIn.func_180728_a());
	}

	/**
	 * Closes the network channel
	 */
	public void handleDisconnect(SPacketDisconnect packetIn) {
		this.netManager.closeChannel(packetIn.getReason());
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
	 */
	public void onDisconnect(ITextComponent reason) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCDisconnect(this, reason);
		/* <<< WDL */

		this.client.loadWorld((WorldClient)null);

		if (this.guiScreenServer != null) {
			if (this.guiScreenServer instanceof GuiScreenRealmsProxy) {
				this.client.displayGuiScreen((new DisconnectedRealmsScreen(((GuiScreenRealmsProxy)this.guiScreenServer).getProxy(), "disconnect.lost", reason)).getProxy());
			} else {
				this.client.displayGuiScreen(new GuiDisconnected(this.guiScreenServer, "disconnect.lost", reason));
			}
		} else {
			this.client.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", reason));
		}
	}

	public void sendPacket(Packet<?> packetIn) {
		this.netManager.sendPacket(packetIn);
	}

	public void handleCollectItem(SPacketCollectItem packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getCollectedItemEntityID());
		EntityLivingBase entitylivingbase = (EntityLivingBase)this.world.getEntityByID(packetIn.getEntityID());

		if (entitylivingbase == null) {
			entitylivingbase = this.client.player;
		}

		if (entity != null) {
			if (entity instanceof EntityXPOrb) {
				this.world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F, false);
			} else {
				this.world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F, false);
			}

			this.client.particles.addEffect(new ParticleItemPickup(this.world, entity, entitylivingbase, 0.5F));
			this.world.removeEntityFromWorld(packetIn.getCollectedItemEntityID());
		}
	}

	/**
	 * Prints a chatmessage in the chat GUI
	 */
	public void handleChat(SPacketChat packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.func_179841_c() == 2) {
			this.client.ingameGUI.setOverlayMessage(packetIn.getChatComponent(), false);
		} else {
			this.client.ingameGUI.getChatGUI().printChatMessage(packetIn.getChatComponent());
		}

		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleChat(this, packetIn);
		/* <<< WDL */
	}

	/**
	 * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt or
	 * receiving a critical hit by normal or magical means
	 */
	public void handleAnimation(SPacketAnimation packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityID());

		if (entity != null) {
			if (packetIn.getAnimationType() == 0) {
				EntityLivingBase entitylivingbase = (EntityLivingBase)entity;
				entitylivingbase.swingArm(EnumHand.MAIN_HAND);
			} else if (packetIn.getAnimationType() == 3) {
				EntityLivingBase entitylivingbase1 = (EntityLivingBase)entity;
				entitylivingbase1.swingArm(EnumHand.OFF_HAND);
			} else if (packetIn.getAnimationType() == 1) {
				entity.performHurtAnimation();
			} else if (packetIn.getAnimationType() == 2) {
				EntityPlayer entityplayer = (EntityPlayer)entity;
				entityplayer.func_70999_a(false, false, false);
			} else if (packetIn.getAnimationType() == 4) {
				this.client.particles.func_178926_a(entity, EnumParticleTypes.CRIT);
			} else if (packetIn.getAnimationType() == 5) {
				this.client.particles.func_178926_a(entity, EnumParticleTypes.CRIT_MAGIC);
			}
		}
	}

	public void func_147278_a(SPacketUseBed p_147278_1_) {
		PacketThreadUtil.checkThreadAndEnqueue(p_147278_1_, this, this.client);
		p_147278_1_.func_149091_a(this.world).func_180469_a(p_147278_1_.func_179798_a());
	}

	/**
	 * Spawns the mob entity at the specified location, with the specified rotation, momentum and type. Updates the
	 * entities Datawatchers with the entity metadata specified in the packet
	 */
	public void handleSpawnMob(SPacketSpawnMob packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		float f = (float)(packetIn.getYaw() * 360) / 256.0F;
		float f1 = (float)(packetIn.getPitch() * 360) / 256.0F;
		EntityLivingBase entitylivingbase = (EntityLivingBase)EntityList.func_75616_a(packetIn.getEntityType(), this.client.world);
		EntityTracker.func_187254_a(entitylivingbase, d0, d1, d2);
		entitylivingbase.renderYawOffset = entitylivingbase.rotationYawHead = (float)(packetIn.getHeadPitch() * 360) / 256.0F;
		Entity[] aentity = entitylivingbase.func_70021_al();

		if (aentity != null) {
			int i = packetIn.getEntityID() - entitylivingbase.getEntityId();

			for (int j = 0; j < aentity.length; ++j) {
				aentity[j].setEntityId(aentity[j].getEntityId() + i);
			}
		}

		entitylivingbase.setEntityId(packetIn.getEntityID());
		entitylivingbase.setUniqueId(packetIn.getUniqueId());
		entitylivingbase.setPositionAndRotation(d0, d1, d2, f, f1);
		entitylivingbase.field_70159_w = (double)((float)packetIn.getVelocityX() / 8000.0F);
		entitylivingbase.field_70181_x = (double)((float)packetIn.getVelocityY() / 8000.0F);
		entitylivingbase.field_70179_y = (double)((float)packetIn.getVelocityZ() / 8000.0F);
		this.world.func_73027_a(packetIn.getEntityID(), entitylivingbase);
		List < EntityDataManager.DataEntry<? >> list = packetIn.func_149027_c();

		if (list != null) {
			entitylivingbase.getDataManager().setEntryValues(list);
		}
	}

	public void handleTimeUpdate(SPacketTimeUpdate packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.world.setGameTime(packetIn.getTotalWorldTime());
		this.client.world.setDayTime(packetIn.getWorldTime());
	}

	public void handleSpawnPosition(SPacketSpawnPosition packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.player.func_180473_a(packetIn.getSpawnPos(), true);
		this.client.world.getWorldInfo().setSpawn(packetIn.getSpawnPos());
	}

	public void handleSetPassengers(SPacketSetPassengers packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityId());

		if (entity == null) {
			LOGGER.warn("Received passengers for unknown entity");
		} else {
			boolean flag = entity.isRidingOrBeingRiddenBy(this.client.player);
			entity.removePassengers();

			for (int i : packetIn.getPassengerIds()) {
				Entity entity1 = this.world.getEntityByID(i);

				if (entity1 == null) {
					LOGGER.warn("Received unknown passenger for " + entity);
				} else {
					entity1.startRiding(entity, true);

					if (entity1 == this.client.player && !flag) {
						this.client.ingameGUI.setOverlayMessage(I18n.format("mount.onboard", new Object[] {GameSettings.func_74298_c(this.client.gameSettings.field_74311_E.func_151463_i())}), false);
					}
				}
			}
		}
	}

	public void handleEntityAttach(SPacketEntityAttach packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityId());
		Entity entity1 = this.world.getEntityByID(packetIn.getVehicleEntityId());

		if (entity instanceof EntityLiving) {
			if (entity1 != null) {
				((EntityLiving)entity).setLeashHolder(entity1, false);
			} else {
				((EntityLiving)entity).clearLeashed(false, false);
			}
		}
	}

	/**
	 * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death), MinecartMobSpawner
	 * (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch (spawn particles), Zombie
	 * (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke particles), Sheep (...), Tameable
	 * (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
	 */
	public void handleEntityStatus(SPacketEntityStatus packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = packetIn.getEntity(this.world);

		if (entity != null) {
			if (packetIn.getOpCode() == 21) {
				this.client.getSoundHandler().play(new GuardianSound((EntityGuardian)entity));
			} else {
				entity.handleStatusUpdate(packetIn.getOpCode());
			}
		}
	}

	public void handleUpdateHealth(SPacketUpdateHealth packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.player.setPlayerSPHealth(packetIn.getHealth());
		this.client.player.getFoodStats().setFoodLevel(packetIn.getFoodLevel());
		this.client.player.getFoodStats().setFoodSaturationLevel(packetIn.getSaturationLevel());
	}

	public void handleSetExperience(SPacketSetExperience packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.player.setXPStats(packetIn.getExperienceBar(), packetIn.getTotalExperience(), packetIn.getLevel());
	}

	public void handleRespawn(SPacketRespawn packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.func_149082_c() != this.client.player.dimension) {
			this.doneLoadingTerrain = false;
			Scoreboard scoreboard = this.world.getScoreboard();
			this.world = new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, this.client.world.getWorldInfo().isHardcore(), packetIn.getWorldType()), packetIn.func_149082_c(), packetIn.func_149081_d(), this.client.profiler);
			this.world.setScoreboard(scoreboard);
			this.client.loadWorld(this.world);
			this.client.player.dimension = packetIn.func_149082_c();
			this.client.displayGuiScreen(new GuiDownloadTerrain(this));
		}

		this.client.func_71354_a(packetIn.func_149082_c());
		this.client.playerController.setGameType(packetIn.getGameType());
	}

	/**
	 * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
	 */
	public void handleExplosion(SPacketExplosion packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Explosion explosion = new Explosion(this.client.world, (Entity)null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
		explosion.doExplosionB(true);
		this.client.player.field_70159_w += (double)packetIn.getMotionX();
		this.client.player.field_70181_x += (double)packetIn.getMotionY();
		this.client.player.field_70179_y += (double)packetIn.getMotionZ();
	}

	public void func_147265_a(SPacketOpenWindow p_147265_1_) {
		PacketThreadUtil.checkThreadAndEnqueue(p_147265_1_, this, this.client);
		EntityPlayerSP entityplayersp = this.client.player;

		if ("minecraft:container".equals(p_147265_1_.func_148902_e())) {
			entityplayersp.func_71007_a(new InventoryBasic(p_147265_1_.func_179840_c(), p_147265_1_.func_148898_f()));
			entityplayersp.openContainer.windowId = p_147265_1_.func_148901_c();
		} else if ("minecraft:villager".equals(p_147265_1_.func_148902_e())) {
			entityplayersp.func_180472_a(new NpcMerchant(entityplayersp, p_147265_1_.func_179840_c()));
			entityplayersp.openContainer.windowId = p_147265_1_.func_148901_c();
		} else if ("EntityHorse".equals(p_147265_1_.func_148902_e())) {
			Entity entity = this.world.getEntityByID(p_147265_1_.func_148897_h());

			if (entity instanceof EquineEntity) {
				entityplayersp.openHorseInventory((EquineEntity)entity, new ContainerHorseChest(p_147265_1_.func_179840_c(), p_147265_1_.func_148898_f()));
				entityplayersp.openContainer.windowId = p_147265_1_.func_148901_c();
			}
		} else if (!p_147265_1_.func_148900_g()) {
			entityplayersp.func_180468_a(new LocalBlockIntercommunication(p_147265_1_.func_148902_e(), p_147265_1_.func_179840_c()));
			entityplayersp.openContainer.windowId = p_147265_1_.func_148901_c();
		} else {
			ContainerLocalMenu containerlocalmenu = new ContainerLocalMenu(p_147265_1_.func_148902_e(), p_147265_1_.func_179840_c(), p_147265_1_.func_148898_f());
			entityplayersp.func_71007_a(containerlocalmenu);
			entityplayersp.openContainer.windowId = p_147265_1_.func_148901_c();
		}
	}

	/**
	 * Handles pickin up an ItemStack or dropping one in your inventory or an open (non-creative) container
	 */
	public void handleSetSlot(SPacketSetSlot packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPlayer entityplayer = this.client.player;

		if (packetIn.getWindowId() == -1) {
			entityplayer.inventory.setItemStack(packetIn.getStack());
		} else if (packetIn.getWindowId() == -2) {
			entityplayer.inventory.setInventorySlotContents(packetIn.getSlot(), packetIn.getStack());
		} else {
			boolean flag = false;

			if (this.client.currentScreen instanceof GuiContainerCreative) {
				GuiContainerCreative guicontainercreative = (GuiContainerCreative)this.client.currentScreen;
				flag = guicontainercreative.getSelectedTabIndex() != CreativeTabs.INVENTORY.getIndex();
			}

			if (packetIn.getWindowId() == 0 && packetIn.getSlot() >= 36 && packetIn.getSlot() < 45) {
				ItemStack itemstack = entityplayer.container.getSlot(packetIn.getSlot()).getStack();

				if (packetIn.getStack() != null && (itemstack == null || itemstack.count < packetIn.getStack().count)) {
					packetIn.getStack().animationsToGo = 5;
				}

				entityplayer.container.putStackInSlot(packetIn.getSlot(), packetIn.getStack());
			} else if (packetIn.getWindowId() == entityplayer.openContainer.windowId && (packetIn.getWindowId() != 0 || !flag)) {
				entityplayer.openContainer.putStackInSlot(packetIn.getSlot(), packetIn.getStack());
			}
		}
	}

	/**
	 * Verifies that the server and client are synchronized with respect to the inventory/container opened by the player
	 * and confirms if it is the case.
	 */
	public void handleConfirmTransaction(SPacketConfirmTransaction packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Container container = null;
		EntityPlayer entityplayer = this.client.player;

		if (packetIn.getWindowId() == 0) {
			container = entityplayer.container;
		} else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
			container = entityplayer.openContainer;
		}

		if (container != null && !packetIn.wasAccepted()) {
			this.sendPacket(new CPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
		}
	}

	/**
	 * Handles the placement of a specified ItemStack in a specified container/inventory slot
	 */
	public void handleWindowItems(SPacketWindowItems packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPlayer entityplayer = this.client.player;

		if (packetIn.getWindowId() == 0) {
			entityplayer.container.func_75131_a(packetIn.getItemStacks());
		} else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
			entityplayer.openContainer.func_75131_a(packetIn.getItemStacks());
		}
	}

	/**
	 * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
	 */
	public void handleSignEditorOpen(SPacketSignEditorOpen packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		TileEntity tileentity = this.world.getTileEntity(packetIn.getSignPosition());

		if (!(tileentity instanceof TileEntitySign)) {
			tileentity = new TileEntitySign();
			tileentity.func_145834_a(this.world);
			tileentity.setPos(packetIn.getSignPosition());
		}

		this.client.player.openSignEditor((TileEntitySign)tileentity);
	}

	/**
	 * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
	 * beacons, skulls, flowerpot
	 */
	public void handleUpdateTileEntity(SPacketUpdateTileEntity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (this.client.world.isBlockLoaded(packetIn.getPos())) {
			TileEntity tileentity = this.client.world.getTileEntity(packetIn.getPos());
			int i = packetIn.getTileEntityType();
			boolean flag = i == 2 && tileentity instanceof TileEntityCommandBlock;

			if (i == 1 && tileentity instanceof TileEntityMobSpawner || flag || i == 3 && tileentity instanceof TileEntityBeacon || i == 4 && tileentity instanceof TileEntitySkull || i == 5 && tileentity instanceof TileEntityFlowerPot || i == 6 && tileentity instanceof TileEntityBanner || i == 7 && tileentity instanceof TileEntityStructure || i == 8 && tileentity instanceof TileEntityEndGateway || i == 9 && tileentity instanceof TileEntitySign) {
				tileentity.read(packetIn.getNbtCompound());
			}

			if (flag && this.client.currentScreen instanceof GuiCommandBlock) {
				((GuiCommandBlock)this.client.currentScreen).updateGui();
			}
		}
	}

	/**
	 * Sets the progressbar of the opened window to the specified value
	 */
	public void handleWindowProperty(SPacketWindowProperty packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPlayer entityplayer = this.client.player;

		if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == packetIn.getWindowId()) {
			entityplayer.openContainer.updateProgressBar(packetIn.getProperty(), packetIn.getValue());
		}
	}

	public void handleEntityEquipment(SPacketEntityEquipment packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityID());

		if (entity != null) {
			entity.setItemStackToSlot(packetIn.getEquipmentSlot(), packetIn.getItemStack());
		}
	}

	/**
	 * Resets the ItemStack held in hand and closes the window that is opened
	 */
	public void handleCloseWindow(SPacketCloseWindow packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.player.closeScreenAndDropStack();
	}

	/**
	 * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote for
	 * setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players accessing
	 * a (Ender)Chest
	 */
	public void handleBlockAction(SPacketBlockAction packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.world.addBlockEvent(packetIn.getBlockPosition(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());

		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleBlockAction(this, packetIn);
		/* <<< WDL */
	}

	/**
	 * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
	 */
	public void handleBlockBreakAnim(SPacketBlockBreakAnim packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.world.sendBlockBreakProgress(packetIn.getBreakerId(), packetIn.getPosition(), packetIn.getProgress());
	}

	public void handleChangeGameState(SPacketChangeGameState packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPlayer entityplayer = this.client.player;
		int i = packetIn.getGameState();
		float f = packetIn.getValue();
		int j = MathHelper.floor(f + 0.5F);

		if (i >= 0 && i < SPacketChangeGameState.MESSAGE_NAMES.length && SPacketChangeGameState.MESSAGE_NAMES[i] != null) {
			entityplayer.sendStatusMessage(new TextComponentTranslation(SPacketChangeGameState.MESSAGE_NAMES[i], new Object[0]));
		}

		if (i == 1) {
			this.world.getWorldInfo().setRaining(true);
			this.world.setRainStrength(0.0F);
		} else if (i == 2) {
			this.world.getWorldInfo().setRaining(false);
			this.world.setRainStrength(1.0F);
		} else if (i == 3) {
			this.client.playerController.setGameType(WorldSettings.GameType.getByID(j));
		} else if (i == 4) {
			if (j == 0) {
				this.client.player.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
				this.client.displayGuiScreen(new GuiDownloadTerrain(this));
			} else if (j == 1) {
				this.client.displayGuiScreen(new GuiWinGame());
			}
		} else if (i == 5) {
			GameSettings gamesettings = this.client.gameSettings;

			if (f == 0.0F) {
				this.client.displayGuiScreen(new GuiScreenDemo());
			} else if (f == 101.0F) {
				this.client.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("demo.help.movement", new Object[] {GameSettings.func_74298_c(gamesettings.keyBindForward.func_151463_i()), GameSettings.func_74298_c(gamesettings.keyBindLeft.func_151463_i()), GameSettings.func_74298_c(gamesettings.keyBindBack.func_151463_i()), GameSettings.func_74298_c(gamesettings.keyBindRight.func_151463_i())}));
			} else if (f == 102.0F) {
				this.client.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("demo.help.jump", new Object[] {GameSettings.func_74298_c(gamesettings.keyBindJump.func_151463_i())}));
			} else if (f == 103.0F) {
				this.client.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("demo.help.inventory", new Object[] {GameSettings.func_74298_c(gamesettings.keyBindInventory.func_151463_i())}));
			}
		} else if (i == 6) {
			this.world.playSound(entityplayer, entityplayer.posX, entityplayer.posY + (double)entityplayer.getEyeHeight(), entityplayer.posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18F, 0.45F);
		} else if (i == 7) {
			this.world.setRainStrength(f);
		} else if (i == 8) {
			this.world.setThunderStrength(f);
		} else if (i == 10) {
			this.world.func_175688_a(EnumParticleTypes.MOB_APPEARANCE, entityplayer.posX, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
			this.world.playSound(entityplayer, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0F, 1.0F);
		}
	}

	/**
	 * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
	 * MapItemRenderer for it
	 */
	public void handleMaps(SPacketMaps packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		MapData mapdata = ItemMap.loadMapData(packetIn.getMapId(), this.client.world);
		packetIn.setMapdataTo(mapdata);
		this.client.gameRenderer.getMapItemRenderer().updateMapTexture(mapdata);

		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleMaps(this, packetIn);
		/* <<< WDL */
	}

	public void handleEffect(SPacketEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.isSoundServerwide()) {
			this.client.world.playBroadcastSound(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
		} else {
			this.client.world.func_175718_b(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
		}
	}

	/**
	 * Updates the players statistics or achievements
	 */
	public void handleStatistics(SPacketStatistics packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		boolean flag = false;

		for (Entry<StatBase, Integer> entry : packetIn.getStatisticMap().entrySet()) {
			StatBase statbase = (StatBase)entry.getKey();
			int i = ((Integer)entry.getValue()).intValue();

			if (statbase.func_75967_d() && i > 0) {
				if (this.field_147308_k && this.client.player.getStats().getValue(statbase) == 0) {
					Achievement achievement = (Achievement)statbase;
					this.client.field_71458_u.func_146256_a(achievement);

					if (statbase == AchievementList.field_187982_f) {
						this.client.gameSettings.field_151441_H = false;
						this.client.gameSettings.saveOptions();
					}
				}

				flag = true;
			}

			this.client.player.getStats().setValue(this.client.player, statbase, i);
		}

		if (!this.field_147308_k && !flag && this.client.gameSettings.field_151441_H) {
			this.client.field_71458_u.func_146255_b(AchievementList.field_187982_f);
		}

		this.field_147308_k = true;

		if (this.client.currentScreen instanceof IProgressMeter) {
			((IProgressMeter)this.client.currentScreen).func_146509_g();
		}
	}

	public void handleEntityEffect(SPacketEntityEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityId());

		if (entity instanceof EntityLivingBase) {
			Potion potion = Potion.get(packetIn.getEffectId());

			if (potion != null) {
				PotionEffect potioneffect = new PotionEffect(potion, packetIn.getDuration(), packetIn.getAmplifier(), packetIn.getIsAmbient(), packetIn.doesShowParticles());
				potioneffect.setPotionDurationMax(packetIn.isMaxDuration());
				((EntityLivingBase)entity).func_70690_d(potioneffect);
			}
		}
	}

	public void handleCombatEvent(SPacketCombatEvent packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
			Entity entity = this.world.getEntityByID(packetIn.playerId);

			if (entity == this.client.player) {
				this.client.displayGuiScreen(new GuiGameOver(packetIn.deathMessage));
			}
		}
	}

	public void handleServerDifficulty(SPacketServerDifficulty packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.world.getWorldInfo().setDifficulty(packetIn.getDifficulty());
		this.client.world.getWorldInfo().setDifficultyLocked(packetIn.isDifficultyLocked());
	}

	public void handleCamera(SPacketCamera packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = packetIn.getEntity(this.world);

		if (entity != null) {
			this.client.setRenderViewEntity(entity);
		}
	}

	public void handleWorldBorder(SPacketWorldBorder packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		packetIn.apply(this.world.getWorldBorder());
	}

	@SuppressWarnings("incomplete-switch")
	public void handleTitle(SPacketTitle packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		SPacketTitle.Type spackettitle$type = packetIn.getType();
		String s = null;
		String s1 = null;
		String s2 = packetIn.getMessage() != null ? packetIn.getMessage().getFormattedText() : "";

		switch (spackettitle$type) {
		case TITLE:
			s = s2;
			break;
		case SUBTITLE:
			s1 = s2;
			break;
		case RESET:
			this.client.ingameGUI.displayTitle("", "", -1, -1, -1);
			this.client.ingameGUI.setDefaultTitlesTimes();
			return;
		}

		this.client.ingameGUI.displayTitle(s, s1, packetIn.getFadeInTime(), packetIn.getDisplayTime(), packetIn.getFadeOutTime());
	}

	public void handlePlayerListHeaderFooter(SPacketPlayerListHeaderFooter packetIn) {
		this.client.ingameGUI.getTabList().setHeader(packetIn.getHeader().getFormattedText().isEmpty() ? null : packetIn.getHeader());
		this.client.ingameGUI.getTabList().setFooter(packetIn.getFooter().getFormattedText().isEmpty() ? null : packetIn.getFooter());
	}

	public void handleRemoveEntityEffect(SPacketRemoveEntityEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = packetIn.getEntity(this.world);

		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase)entity).removeActivePotionEffect(packetIn.getPotion());
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void handlePlayerListItem(SPacketPlayerListItem packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		for (SPacketPlayerListItem.AddPlayerData spacketplayerlistitem$addplayerdata : packetIn.getEntries()) {
			if (packetIn.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
				this.playerInfoMap.remove(spacketplayerlistitem$addplayerdata.getProfile().getId());
			} else {
				NetworkPlayerInfo networkplayerinfo = (NetworkPlayerInfo)this.playerInfoMap.get(spacketplayerlistitem$addplayerdata.getProfile().getId());

				if (packetIn.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
					networkplayerinfo = new NetworkPlayerInfo(spacketplayerlistitem$addplayerdata);
					this.playerInfoMap.put(networkplayerinfo.getGameProfile().getId(), networkplayerinfo);
				}

				if (networkplayerinfo != null) {
					switch (packetIn.getAction()) {
					case ADD_PLAYER:
						networkplayerinfo.setGameType(spacketplayerlistitem$addplayerdata.getGameMode());
						networkplayerinfo.setResponseTime(spacketplayerlistitem$addplayerdata.getPing());
						break;
					case UPDATE_GAME_MODE:
						networkplayerinfo.setGameType(spacketplayerlistitem$addplayerdata.getGameMode());
						break;
					case UPDATE_LATENCY:
						networkplayerinfo.setResponseTime(spacketplayerlistitem$addplayerdata.getPing());
						break;
					case UPDATE_DISPLAY_NAME:
						networkplayerinfo.setDisplayName(spacketplayerlistitem$addplayerdata.getDisplayName());
					}
				}
			}
		}
	}

	public void handleKeepAlive(SPacketKeepAlive packetIn) {
		this.sendPacket(new CPacketKeepAlive(packetIn.getId()));
	}

	public void handlePlayerAbilities(SPacketPlayerAbilities packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		EntityPlayer entityplayer = this.client.player;
		entityplayer.abilities.isFlying = packetIn.isFlying();
		entityplayer.abilities.isCreativeMode = packetIn.isCreativeMode();
		entityplayer.abilities.disableDamage = packetIn.isInvulnerable();
		entityplayer.abilities.allowFlying = packetIn.isAllowFlying();
		entityplayer.abilities.func_75092_a(packetIn.getFlySpeed());
		entityplayer.abilities.setWalkSpeed(packetIn.getWalkSpeed());
	}

	public void func_147274_a(SPacketTabComplete p_147274_1_) {
		PacketThreadUtil.checkThreadAndEnqueue(p_147274_1_, this, this.client);
		String[] astring = p_147274_1_.func_149630_c();

		if (this.client.currentScreen instanceof ITabCompleter) {
			((ITabCompleter)this.client.currentScreen).func_184072_a(astring);
		}
	}

	public void handleSoundEffect(SPacketSoundEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.world.playSound(this.client.player, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSound(), packetIn.getCategory(), packetIn.getVolume(), packetIn.getPitch());
	}

	public void handleCustomSound(SPacketCustomSound packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.getSoundHandler().play(new SimpleSound(new ResourceLocation(packetIn.func_186930_a()), packetIn.getCategory(), packetIn.getVolume(), packetIn.getPitch(), false, 0, ISound.AttenuationType.LINEAR, (float)packetIn.getX(), (float)packetIn.getY(), (float)packetIn.getZ()));
	}

	public void handleResourcePack(SPacketResourcePackSend packetIn) {
		final String s = packetIn.getURL();
		final String s1 = packetIn.getHash();

		if (this.func_184335_a(s, s1)) {
			if (s.startsWith("level://")) {
				String s2 = s.substring("level://".length());
				File file1 = new File(this.client.gameDir, "saves");
				File file2 = new File(file1, s2);

				if (file2.isFile()) {
					this.netManager.sendPacket(new CPacketResourcePackStatus(s1, CPacketResourcePackStatus.Action.ACCEPTED));
					Futures.addCallback(this.client.func_110438_M().func_177319_a(file2), this.func_184333_b(s1));
				} else {
					this.netManager.sendPacket(new CPacketResourcePackStatus(s1, CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
				}
			} else {
				if (this.client.getCurrentServerData() != null && this.client.getCurrentServerData().getResourceMode() == ServerData.ServerResourceMode.ENABLED) {
					this.netManager.sendPacket(new CPacketResourcePackStatus(s1, CPacketResourcePackStatus.Action.ACCEPTED));
					Futures.addCallback(this.client.func_110438_M().func_180601_a(s, s1), this.func_184333_b(s1));
				} else if (this.client.getCurrentServerData() != null && this.client.getCurrentServerData().getResourceMode() != ServerData.ServerResourceMode.PROMPT) {
					this.netManager.sendPacket(new CPacketResourcePackStatus(s1, CPacketResourcePackStatus.Action.DECLINED));
				} else {
					this.client.execute(new Runnable() {
						public void run() {
							NetHandlerPlayClient.this.client.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
								/**
								 * Changed for unchangeable 1.13 name
								 */
								public void confirmResult(boolean p_73878_1_, int p_73878_2_) {
									NetHandlerPlayClient.this.client = Minecraft.getInstance();

									if (p_73878_1_) {
										if (NetHandlerPlayClient.this.client.getCurrentServerData() != null) {
											NetHandlerPlayClient.this.client.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.ENABLED);
										}

										NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(s1, CPacketResourcePackStatus.Action.ACCEPTED));
										Futures.addCallback(NetHandlerPlayClient.this.client.func_110438_M().func_180601_a(s, s1), NetHandlerPlayClient.this.func_184333_b(s1));
									} else {
										if (NetHandlerPlayClient.this.client.getCurrentServerData() != null) {
											NetHandlerPlayClient.this.client.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.DISABLED);
										}

										NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(s1, CPacketResourcePackStatus.Action.DECLINED));
									}

									ServerList.saveSingleServer(NetHandlerPlayClient.this.client.getCurrentServerData());
									NetHandlerPlayClient.this.client.displayGuiScreen((GuiScreen)null);
								}
							}, I18n.format("multiplayer.texturePrompt.line1", new Object[0]), I18n.format("multiplayer.texturePrompt.line2", new Object[0]), 0));
						}
					});
				}
			}
		}
	}

	private boolean func_184335_a(String p_184335_1_, String p_184335_2_) {
		try {
			URI uri = new URI(p_184335_1_.replace(' ', '+'));
			String s = uri.getScheme();
			boolean flag = "level".equals(s);

			if (!"http".equals(s) && !"https".equals(s) && !flag) {
				throw new URISyntaxException(p_184335_1_, "Wrong protocol");
			} else if (!flag || !p_184335_1_.contains("..") && p_184335_1_.endsWith("/resources.zip")) {
				return true;
			} else {
				throw new URISyntaxException(p_184335_1_, "Invalid levelstorage resourcepack path");
			}
		} catch (URISyntaxException var6) {
			this.netManager.sendPacket(new CPacketResourcePackStatus(p_184335_2_, CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
			return false;
		}
	}

	private FutureCallback<Object> func_184333_b(final String p_184333_1_) {
		return new FutureCallback<Object>() {
			public void onSuccess(@Nullable Object p_onSuccess_1_) {
				NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(p_184333_1_, CPacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
			}
			public void onFailure(Throwable p_onFailure_1_) {
				NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(p_184333_1_, CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
			}
		};
	}

	public void handleUpdateBossInfo(SPacketUpdateBossInfo packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		this.client.ingameGUI.getBossOverlay().read(packetIn);
	}

	public void handleCooldown(SPacketCooldown packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.getTicks() == 0) {
			this.client.player.getCooldownTracker().removeCooldown(packetIn.getItem());
		} else {
			this.client.player.getCooldownTracker().setCooldown(packetIn.getItem(), packetIn.getTicks());
		}
	}

	public void handleMoveVehicle(SPacketMoveVehicle packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.client.player.getLowestRidingEntity();

		if (entity != this.client.player && entity.canPassengerSteer()) {
			entity.setPositionAndRotation(packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getYaw(), packetIn.getPitch());
			this.netManager.sendPacket(new CPacketVehicleMove(entity));
		}
	}

	/**
	 * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to acquire
	 * a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the player instance
	 * and finally "MC|RPack" which the server uses to communicate the identifier of the default server resourcepack for
	 * the client to load.
	 */
	public void handleCustomPayload(SPacketCustomPayload packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if ("MC|TrList".equals(packetIn.getChannelName())) {
			PacketBuffer packetbuffer = packetIn.getBufferData();

			try {
				int i = packetbuffer.readInt();
				GuiScreen guiscreen = this.client.currentScreen;

				if (guiscreen != null && guiscreen instanceof GuiMerchant && i == this.client.player.openContainer.windowId) {
					IMerchant imerchant = ((GuiMerchant)guiscreen).func_147035_g();
					MerchantRecipeList merchantrecipelist = MerchantRecipeList.func_151390_b(packetbuffer);
					imerchant.setOffers(merchantrecipelist);
				}
			} catch (IOException ioexception) {
				LOGGER.error((String)"Couldn\'t load trade info", (Throwable)ioexception);
			} finally {
				packetbuffer.release();
			}
		} else if ("MC|Brand".equals(packetIn.getChannelName())) {
			this.client.player.setServerBrand(packetIn.getBufferData().readString(32767));
		} else if ("MC|BOpen".equals(packetIn.getChannelName())) {
			EnumHand enumhand = (EnumHand)packetIn.getBufferData().readEnumValue(EnumHand.class);
			ItemStack itemstack = enumhand == EnumHand.OFF_HAND ? this.client.player.getHeldItemOffhand() : this.client.player.getHeldItemMainhand();

			if (itemstack != null && itemstack.getItem() == Items.WRITTEN_BOOK) {
				this.client.displayGuiScreen(new GuiScreenBook(this.client.player, itemstack, false));
			}
		} else if ("MC|DebugPath".equals(packetIn.getChannelName())) {
			PacketBuffer packetbuffer1 = packetIn.getBufferData();
			int j = packetbuffer1.readInt();
			float f = packetbuffer1.readFloat();
			Path path = Path.read(packetbuffer1);
			this.client.debugRenderer.pathfinding.addPath(j, path, f);
		} else if ("MC|StopSound".equals(packetIn.getChannelName())) {
			PacketBuffer packetbuffer2 = packetIn.getBufferData();
			String s = packetbuffer2.readString(32767);
			String s1 = packetbuffer2.readString(256);
			this.client.getSoundHandler().func_189520_a(s1, SoundCategory.func_187950_a(s));
		}

		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleCustomPayload(this, packetIn);
		/* <<< WDL */
	}

	/**
	 * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
	 */
	public void handleScoreboardObjective(SPacketScoreboardObjective packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Scoreboard scoreboard = this.world.getScoreboard();

		if (packetIn.getAction() == 0) {
			ScoreObjective scoreobjective = scoreboard.func_96535_a(packetIn.getObjectiveName(), IScoreCriteria.DUMMY);
			scoreobjective.func_96681_a(packetIn.getDisplayName());
			scoreobjective.func_178767_a(packetIn.func_179817_d());
		} else {
			ScoreObjective scoreobjective1 = scoreboard.getObjective(packetIn.getObjectiveName());

			if (packetIn.getAction() == 1) {
				scoreboard.removeObjective(scoreobjective1);
			} else if (packetIn.getAction() == 2) {
				scoreobjective1.func_96681_a(packetIn.getDisplayName());
				scoreobjective1.func_178767_a(packetIn.func_179817_d());
			}
		}
	}

	/**
	 * Either updates the score with a specified value or removes the score for an objective
	 */
	public void handleUpdateScore(SPacketUpdateScore packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Scoreboard scoreboard = this.world.getScoreboard();
		ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.getObjectiveName());

		if (packetIn.func_180751_d() == SPacketUpdateScore.Action.CHANGE) {
			Score score = scoreboard.getOrCreateScore(packetIn.getPlayerName(), scoreobjective);
			score.setScorePoints(packetIn.getScoreValue());
		} else if (packetIn.func_180751_d() == SPacketUpdateScore.Action.REMOVE) {
			if (StringUtils.isNullOrEmpty(packetIn.getObjectiveName())) {
				scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), (ScoreObjective)null);
			} else if (scoreobjective != null) {
				scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), scoreobjective);
			}
		}
	}

	/**
	 * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below name)
	 */
	public void handleDisplayObjective(SPacketDisplayObjective packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Scoreboard scoreboard = this.world.getScoreboard();

		if (packetIn.getName().isEmpty()) {
			scoreboard.setObjectiveInDisplaySlot(packetIn.getPosition(), (ScoreObjective)null);
		} else {
			ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.getName());
			scoreboard.setObjectiveInDisplaySlot(packetIn.getPosition(), scoreobjective);
		}
	}

	/**
	 * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
	 * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
	 */
	public void handleTeams(SPacketTeams packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Scoreboard scoreboard = this.world.getScoreboard();
		ScorePlayerTeam scoreplayerteam;

		if (packetIn.getAction() == 0) {
			scoreplayerteam = scoreboard.createTeam(packetIn.getName());
		} else {
			scoreplayerteam = scoreboard.getTeam(packetIn.getName());
		}

		if (packetIn.getAction() == 0 || packetIn.getAction() == 2) {
			scoreplayerteam.setDisplayName(packetIn.getDisplayName());
			scoreplayerteam.func_96666_b(packetIn.func_149311_e());
			scoreplayerteam.func_96662_c(packetIn.func_149309_f());
			scoreplayerteam.setColor(TextFormatting.fromColorIndex(packetIn.func_179813_h()));
			scoreplayerteam.setFriendlyFlags(packetIn.getFriendlyFlags());
			Team.EnumVisible team$enumvisible = Team.EnumVisible.getByName(packetIn.getNameTagVisibility());

			if (team$enumvisible != null) {
				scoreplayerteam.setNameTagVisibility(team$enumvisible);
			}

			Team.CollisionRule team$collisionrule = Team.CollisionRule.getByName(packetIn.getCollisionRule());

			if (team$collisionrule != null) {
				scoreplayerteam.setCollisionRule(team$collisionrule);
			}
		}

		if (packetIn.getAction() == 0 || packetIn.getAction() == 3) {
			for (String s : packetIn.getPlayers()) {
				scoreboard.func_151392_a(s, packetIn.getName());
			}
		}

		if (packetIn.getAction() == 4) {
			for (String s1 : packetIn.getPlayers()) {
				scoreboard.removePlayerFromTeam(s1, scoreplayerteam);
			}
		}

		if (packetIn.getAction() == 1) {
			scoreboard.removeTeam(scoreplayerteam);
		}
	}

	/**
	 * Spawns a specified number of particles at the specified location with a randomized displacement according to
	 * specified bounds
	 */
	public void handleParticles(SPacketParticles packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);

		if (packetIn.getParticleCount() == 0) {
			double d0 = (double)(packetIn.getParticleSpeed() * packetIn.getXOffset());
			double d2 = (double)(packetIn.getParticleSpeed() * packetIn.getYOffset());
			double d4 = (double)(packetIn.getParticleSpeed() * packetIn.getZOffset());

			try {
				this.world.func_175682_a(packetIn.func_179749_a(), packetIn.isLongDistance(), packetIn.getXCoordinate(), packetIn.getYCoordinate(), packetIn.getZCoordinate(), d0, d2, d4, packetIn.func_179748_k());
			} catch (Throwable var17) {
				LOGGER.warn("Could not spawn particle effect " + packetIn.func_179749_a());
			}
		} else {
			for (int i = 0; i < packetIn.getParticleCount(); ++i) {
				double d1 = this.avRandomizer.nextGaussian() * (double)packetIn.getXOffset();
				double d3 = this.avRandomizer.nextGaussian() * (double)packetIn.getYOffset();
				double d5 = this.avRandomizer.nextGaussian() * (double)packetIn.getZOffset();
				double d6 = this.avRandomizer.nextGaussian() * (double)packetIn.getParticleSpeed();
				double d7 = this.avRandomizer.nextGaussian() * (double)packetIn.getParticleSpeed();
				double d8 = this.avRandomizer.nextGaussian() * (double)packetIn.getParticleSpeed();

				try {
					this.world.func_175682_a(packetIn.func_179749_a(), packetIn.isLongDistance(), packetIn.getXCoordinate() + d1, packetIn.getYCoordinate() + d3, packetIn.getZCoordinate() + d5, d6, d7, d8, packetIn.func_179748_k());
				} catch (Throwable var16) {
					LOGGER.warn("Could not spawn particle effect " + packetIn.func_179749_a());
					return;
				}
			}
		}
	}

	/**
	 * Updates en entity's attributes and their respective modifiers, which are used for speed bonusses (player sprinting,
	 * animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie maxHealth and
	 * knockback resistance as well as reinforcement spawning chance.
	 */
	public void handleEntityProperties(SPacketEntityProperties packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
		Entity entity = this.world.getEntityByID(packetIn.getEntityId());

		if (entity != null) {
			if (!(entity instanceof EntityLivingBase)) {
				throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
			} else {
				AbstractAttributeMap abstractattributemap = ((EntityLivingBase)entity).getAttributes();

				for (SPacketEntityProperties.Snapshot spacketentityproperties$snapshot : packetIn.getSnapshots()) {
					IAttributeInstance iattributeinstance = abstractattributemap.getAttributeInstanceByName(spacketentityproperties$snapshot.getName());

					if (iattributeinstance == null) {
						iattributeinstance = abstractattributemap.registerAttribute(new RangedAttribute((IAttribute)null, spacketentityproperties$snapshot.getName(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
					}

					iattributeinstance.setBaseValue(spacketentityproperties$snapshot.getBaseValue());
					iattributeinstance.removeAllModifiers();

					for (AttributeModifier attributemodifier : spacketentityproperties$snapshot.getModifiers()) {
						iattributeinstance.applyModifier(attributemodifier);
					}
				}
			}
		}
	}

	/**
	 * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
	 */
	public NetworkManager getNetworkManager() {
		return this.netManager;
	}

	public Collection<NetworkPlayerInfo> getPlayerInfoMap() {
		return this.playerInfoMap.values();
	}

	public NetworkPlayerInfo getPlayerInfo(UUID uniqueId) {
		return (NetworkPlayerInfo)this.playerInfoMap.get(uniqueId);
	}

	/**
	 * Gets the client's description information about another player on the server.
	 */
	@Nullable
	public NetworkPlayerInfo getPlayerInfo(String name) {
		for (NetworkPlayerInfo networkplayerinfo : this.playerInfoMap.values()) {
			if (networkplayerinfo.getGameProfile().getName().equals(name)) {
				return networkplayerinfo;
			}
		}

		return null;
	}

	public GameProfile getGameProfile() {
		return this.profile;
	}
}
