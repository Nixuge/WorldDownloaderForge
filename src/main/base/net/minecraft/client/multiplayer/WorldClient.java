package net.minecraft.client.multiplayer;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

public class WorldClient extends World/* WDL >>> */ implements wdl.ducks.IBaseChangesApplied/* <<< WDL */ {
	private NetHandlerPlayClient connection;
	private ChunkProviderClient field_73033_b;
	private final Set<Entity> field_73032_d = Sets.<Entity>newHashSet();
	private final Set<Entity> field_73036_L = Sets.<Entity>newHashSet();
	private final Minecraft mc = Minecraft.getInstance();
	private final Set<ChunkPos> field_73038_N = Sets.<ChunkPos>newHashSet();
	private int ambienceTicks;
	protected Set<ChunkPos> field_184157_a;

	public WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_) {
		super(new SaveHandlerMP(), new WorldInfo(p_i45063_2_, "MpServer"), DimensionType.getById(p_i45063_3_).func_186070_d(), p_i45063_5_, true);
		this.ambienceTicks = this.rand.nextInt(12000);
		this.field_184157_a = Sets.<ChunkPos>newHashSet();
		this.connection = p_i45063_1_;
		this.getWorldInfo().setDifficulty(p_i45063_4_);
		this.setSpawnPoint(new BlockPos(8, 64, 8));
		this.dimension.func_76558_a(this);
		this.chunkProvider = this.createChunkProvider();
		this.field_72988_C = new SaveDataMemoryStorage();
		this.calculateInitialSkylight();
		this.calculateInitialWeather();
	}

	/**
	 * Runs a single tick for the world
	 */
	public void tick() {
		super.tick();
		this.setGameTime(this.getGameTime() + 1L);

		if (this.getGameRules().getBoolean("doDaylightCycle")) {
			this.setDayTime(this.getDayTime() + 1L);
		}

		this.profiler.startSection("reEntryProcessing");

		for (int i = 0; i < 10 && !this.field_73036_L.isEmpty(); ++i) {
			Entity entity = (Entity)this.field_73036_L.iterator().next();
			this.field_73036_L.remove(entity);

			if (!this.loadedEntityList.contains(entity)) {
				this.spawnEntity(entity);
			}
		}

		this.profiler.endStartSection("chunkCache");
		this.field_73033_b.tick();
		this.profiler.endStartSection("blocks");
		this.func_147456_g();
		this.profiler.endSection();

		/* WDL >>> */
		wdl.WDLHooks.onWorldClientTick(this);
		/* <<< WDL */
	}

	public void func_73031_a(int p_73031_1_, int p_73031_2_, int p_73031_3_, int p_73031_4_, int p_73031_5_, int p_73031_6_) {
	}

	protected IChunkProvider createChunkProvider() {
		this.field_73033_b = new ChunkProviderClient(this);
		return this.field_73033_b;
	}

	protected boolean func_175680_a(int p_175680_1_, int p_175680_2_, boolean p_175680_3_) {
		return p_175680_3_ || !this.getChunkProvider().getChunk(p_175680_1_, p_175680_2_).isEmpty();
	}

	protected void func_184154_a() {
		this.field_184157_a.clear();
		int i = this.mc.gameSettings.renderDistanceChunks;
		this.profiler.startSection("buildList");
		int j = MathHelper.floor(this.mc.player.posX / 16.0D);
		int k = MathHelper.floor(this.mc.player.posZ / 16.0D);

		for (int l = -i; l <= i; ++l) {
			for (int i1 = -i; i1 <= i; ++i1) {
				this.field_184157_a.add(new ChunkPos(l + j, i1 + k));
			}
		}

		this.profiler.endSection();
	}

	protected void func_147456_g() {
		this.func_184154_a();

		if (this.ambienceTicks > 0) {
			--this.ambienceTicks;
		}

		this.field_73038_N.retainAll(this.field_184157_a);

		if (this.field_73038_N.size() == this.field_184157_a.size()) {
			this.field_73038_N.clear();
		}

		int i = 0;

		for (ChunkPos chunkpos : this.field_184157_a) {
			if (!this.field_73038_N.contains(chunkpos)) {
				int j = chunkpos.x * 16;
				int k = chunkpos.z * 16;
				this.profiler.startSection("getChunk");
				Chunk chunk = this.getChunk(chunkpos.x, chunkpos.z);
				this.func_147467_a(j, k, chunk);
				this.profiler.endSection();
				this.field_73038_N.add(chunkpos);
				++i;

				if (i >= 10) {
					return;
				}
			}
		}
	}

	public void doPreChunk(int p_73025_1_, int p_73025_2_, boolean p_73025_3_) {
		if (p_73025_3_) {
			this.field_73033_b.loadChunk(p_73025_1_, p_73025_2_);
		} else {
			this.field_73033_b.unloadChunk(p_73025_1_, p_73025_2_);
			this.func_147458_c(p_73025_1_ * 16, 0, p_73025_2_ * 16, p_73025_1_ * 16 + 15, 256, p_73025_2_ * 16 + 15);
		}
	}

	public boolean spawnEntity(Entity entityIn) {
		boolean flag = super.spawnEntity(entityIn);
		this.field_73032_d.add(entityIn);

		if (!flag) {
			this.field_73036_L.add(entityIn);
		} else if (entityIn instanceof EntityMinecart) {
			this.mc.getSoundHandler().play(new MovingSoundMinecart((EntityMinecart)entityIn));
		}

		return flag;
	}

	public void func_72900_e(Entity p_72900_1_) {
		super.func_72900_e(p_72900_1_);
		this.field_73032_d.remove(p_72900_1_);
	}

	protected void onEntityAdded(Entity p_72923_1_) {
		super.onEntityAdded(p_72923_1_);

		if (this.field_73036_L.contains(p_72923_1_)) {
			this.field_73036_L.remove(p_72923_1_);
		}
	}

	protected void func_72847_b(Entity p_72847_1_) {
		super.func_72847_b(p_72847_1_);
		boolean flag = false;

		if (this.field_73032_d.contains(p_72847_1_)) {
			if (p_72847_1_.isAlive()) {
				this.field_73036_L.add(p_72847_1_);
				flag = true;
			} else {
				this.field_73032_d.remove(p_72847_1_);
			}
		}
	}

	public void func_73027_a(int p_73027_1_, Entity p_73027_2_) {
		Entity entity = this.getEntityByID(p_73027_1_);

		if (entity != null) {
			this.func_72900_e(entity);
		}

		this.field_73032_d.add(p_73027_2_);
		p_73027_2_.setEntityId(p_73027_1_);

		if (!this.spawnEntity(p_73027_2_)) {
			this.field_73036_L.add(p_73027_2_);
		}

		this.entitiesById.put(p_73027_1_, p_73027_2_);
	}

	/**
	 * Returns the Entity with the given ID, or null if it doesn't exist in this World.
	 */
	@Nullable
	public Entity getEntityByID(int id) {
		return (Entity)(id == this.mc.player.getEntityId() ? this.mc.player : super.getEntityByID(id));
	}

	public Entity removeEntityFromWorld(int p_73028_1_) {
		/* WDL >>> */
		wdl.WDLHooks.onWorldClientRemoveEntityFromWorld(this, p_73028_1_);
		/* <<< WDL */

		Entity entity = (Entity)this.entitiesById.remove(p_73028_1_);

		if (entity != null) {
			this.field_73032_d.remove(entity);
			this.func_72900_e(entity);
		}

		return entity;
	}

	@Deprecated
	public boolean func_180503_b(BlockPos p_180503_1_, IBlockState p_180503_2_) {
		int i = p_180503_1_.getX();
		int j = p_180503_1_.getY();
		int k = p_180503_1_.getZ();
		this.func_73031_a(i, j, k, i, j, k);
		return super.setBlockState(p_180503_1_, p_180503_2_, 3);
	}

	/**
	 * If on MP, sends a quitting packet.
	 */
	public void sendQuittingDisconnectingPacket() {
		this.connection.getNetworkManager().closeChannel(new TextComponentString("Quitting"));
	}

	protected void func_72979_l() {
	}

	protected void func_147467_a(int p_147467_1_, int p_147467_2_, Chunk p_147467_3_) {
		super.func_147467_a(p_147467_1_, p_147467_2_, p_147467_3_);

		if (this.ambienceTicks == 0) {
			this.updateLCG = this.updateLCG * 3 + 1013904223;
			int i = this.updateLCG >> 2;
			int j = i & 15;
			int k = i >> 8 & 15;
			int l = i >> 16 & 255;
			BlockPos blockpos = new BlockPos(j + p_147467_1_, l, k + p_147467_2_);
			IBlockState iblockstate = p_147467_3_.getBlockState(blockpos);
			j = j + p_147467_1_;
			k = k + p_147467_2_;

			if (iblockstate.getMaterial() == Material.AIR && this.func_175699_k(blockpos) <= this.rand.nextInt(8) && this.func_175642_b(EnumSkyBlock.SKY, blockpos) <= 0 && this.mc.player != null && this.mc.player.getDistanceSq((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D) > 4.0D) {
				this.playSound((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + this.rand.nextFloat() * 0.2F, false);
				this.ambienceTicks = this.rand.nextInt(12000) + 6000;
			}
		}
	}

	public void animateTick(int posX, int posY, int posZ) {
		int i = 32;
		Random random = new Random();
		ItemStack itemstack = this.mc.player.getHeldItemMainhand();
		boolean flag = this.mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == Blocks.BARRIER;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < 667; ++j) {
			this.animateTick(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
			this.animateTick(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
		}
	}

	public void animateTick(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
		int i = x + this.rand.nextInt(offset) - this.rand.nextInt(offset);
		int j = y + this.rand.nextInt(offset) - this.rand.nextInt(offset);
		int k = z + this.rand.nextInt(offset) - this.rand.nextInt(offset);
		pos.setPos(i, j, k);
		IBlockState iblockstate = this.getBlockState(pos);
		iblockstate.getBlock().animateTick(iblockstate, this, pos, random);

		if (holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER) {
			this.func_175688_a(EnumParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}

	/**
	 * also releases skins.
	 */
	public void removeAllEntities() {
		this.loadedEntityList.removeAll(this.field_72997_g);

		for (int i = 0; i < this.field_72997_g.size(); ++i) {
			Entity entity = (Entity)this.field_72997_g.get(i);
			int j = entity.chunkCoordX;
			int k = entity.chunkCoordZ;

			if (entity.addedToChunk && this.func_175680_a(j, k, true)) {
				this.getChunk(j, k).removeEntity(entity);
			}
		}

		for (int i1 = 0; i1 < this.field_72997_g.size(); ++i1) {
			this.func_72847_b((Entity)this.field_72997_g.get(i1));
		}

		this.field_72997_g.clear();

		for (int j1 = 0; j1 < this.loadedEntityList.size(); ++j1) {
			Entity entity1 = (Entity)this.loadedEntityList.get(j1);
			Entity entity2 = entity1.getRidingEntity();

			if (entity2 != null) {
				if (!entity2.removed && entity2.isPassenger(entity1)) {
					continue;
				}

				entity1.stopRiding();
			}

			if (entity1.removed) {
				int k1 = entity1.chunkCoordX;
				int l = entity1.chunkCoordZ;

				if (entity1.addedToChunk && this.func_175680_a(k1, l, true)) {
					this.getChunk(k1, l).removeEntity(entity1);
				}

				this.loadedEntityList.remove(j1--);
				this.func_72847_b(entity1);
			}
		}
	}

	/**
	 * Adds some basic stats of the world to the given crash report.
	 */
	public CrashReportCategory fillCrashReport(CrashReport report) {
		CrashReportCategory crashreportcategory = super.fillCrashReport(report);
		crashreportcategory.addDetail("Forced entities", new ICrashReportDetail<String>() {
			public String call() {
				return WorldClient.this.field_73032_d.size() + " total; " + WorldClient.this.field_73032_d.toString();
			}
		});
		crashreportcategory.addDetail("Retry entities", new ICrashReportDetail<String>() {
			public String call() {
				return WorldClient.this.field_73036_L.size() + " total; " + WorldClient.this.field_73036_L.toString();
			}
		});
		crashreportcategory.addDetail("Server brand", new ICrashReportDetail<String>() {
			public String call() throws Exception {
				return WorldClient.this.mc.player.getServerBrand();
			}
		});
		crashreportcategory.addDetail("Server type", new ICrashReportDetail<String>() {
			public String call() throws Exception {
				return WorldClient.this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
			}
		});
		return crashreportcategory;
	}

	public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		if (player == this.mc.player) {
			this.playSound(x, y, z, soundIn, category, volume, pitch, false);
		}
	}

	public void playSound(BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
		this.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch, distanceDelay);
	}

	public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
		double d0 = this.mc.getRenderViewEntity().getDistanceSq(x, y, z);
		SimpleSound simplesound = new SimpleSound(soundIn, category, volume, pitch, (float)x, (float)y, (float)z);

		if (distanceDelay && d0 > 100.0D) {
			double d1 = Math.sqrt(d0) / 40.0D;
			this.mc.getSoundHandler().playDelayed(simplesound, (int)(d1 * 20.0D));
		} else {
			this.mc.getSoundHandler().play(simplesound);
		}
	}

	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compound) {
		this.mc.particles.addEffect(new ParticleFirework.Starter(this, x, y, z, motionX, motionY, motionZ, this.mc.particles, compound));
	}

	public void sendPacketToServer(Packet<?> packetIn) {
		this.connection.sendPacket(packetIn);
	}

	public void setScoreboard(Scoreboard scoreboardIn) {
		this.field_96442_D = scoreboardIn;
	}

	/**
	 * Sets the world time.
	 */
	public void setDayTime(long time) {
		if (time < 0L) {
			time = -time;
			this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
		} else {
			this.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
		}

		super.setDayTime(time);
	}

	/**
	 * Gets the world's chunk provider
	 */
	public ChunkProviderClient getChunkProvider() {
		return (ChunkProviderClient)super.getChunkProvider();
	}
}
