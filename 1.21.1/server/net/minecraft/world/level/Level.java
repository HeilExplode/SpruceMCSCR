/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;

public abstract class Level
implements LevelAccessor,
AutoCloseable {
    public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.DIMENSION);
    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_nether"));
    public static final ResourceKey<Level> END = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_end"));
    public static final int MAX_LEVEL_SIZE = 30000000;
    public static final int LONG_PARTICLE_CLIP_RANGE = 512;
    public static final int SHORT_PARTICLE_CLIP_RANGE = 32;
    public static final int MAX_BRIGHTNESS = 15;
    public static final int TICKS_PER_DAY = 24000;
    public static final int MAX_ENTITY_SPAWN_Y = 20000000;
    public static final int MIN_ENTITY_SPAWN_Y = -20000000;
    protected final List<TickingBlockEntity> blockEntityTickers = Lists.newArrayList();
    protected final NeighborUpdater neighborUpdater;
    private final List<TickingBlockEntity> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean tickingBlockEntities;
    private final Thread thread;
    private final boolean isDebug;
    private int skyDarken;
    protected int randValue = RandomSource.create().nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    public final RandomSource random = RandomSource.create();
    @Deprecated
    private final RandomSource threadSafeRandom = RandomSource.createThreadSafe();
    private final Holder<DimensionType> dimensionTypeRegistration;
    protected final WritableLevelData levelData;
    private final Supplier<ProfilerFiller> profiler;
    public final boolean isClientSide;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;
    private final ResourceKey<Level> dimension;
    private final RegistryAccess registryAccess;
    private final DamageSources damageSources;
    private long subTickCount;

    protected Level(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int n) {
        this.profiler = supplier;
        this.levelData = writableLevelData;
        this.dimensionTypeRegistration = holder;
        final DimensionType dimensionType = holder.value();
        this.dimension = resourceKey;
        this.isClientSide = bl;
        this.worldBorder = dimensionType.coordinateScale() != 1.0 ? new WorldBorder(this){

            @Override
            public double getCenterX() {
                return super.getCenterX() / dimensionType.coordinateScale();
            }

            @Override
            public double getCenterZ() {
                return super.getCenterZ() / dimensionType.coordinateScale();
            }
        } : new WorldBorder();
        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, l);
        this.isDebug = bl2;
        this.neighborUpdater = new CollectingNeighborUpdater(this, n);
        this.registryAccess = registryAccess;
        this.damageSources = new DamageSources(registryAccess);
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Override
    @Nullable
    public MinecraftServer getServer() {
        return null;
    }

    public boolean isInWorldBounds(BlockPos blockPos) {
        return !this.isOutsideBuildHeight(blockPos) && Level.isInWorldBoundsHorizontal(blockPos);
    }

    public static boolean isInSpawnableBounds(BlockPos blockPos) {
        return !Level.isOutsideSpawnableHeight(blockPos.getY()) && Level.isInWorldBoundsHorizontal(blockPos);
    }

    private static boolean isInWorldBoundsHorizontal(BlockPos blockPos) {
        return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 && blockPos.getX() < 30000000 && blockPos.getZ() < 30000000;
    }

    private static boolean isOutsideSpawnableHeight(int n) {
        return n < -20000000 || n >= 20000000;
    }

    public LevelChunk getChunkAt(BlockPos blockPos) {
        return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    @Override
    public LevelChunk getChunk(int n, int n2) {
        return (LevelChunk)this.getChunk(n, n2, ChunkStatus.FULL);
    }

    @Override
    @Nullable
    public ChunkAccess getChunk(int n, int n2, ChunkStatus chunkStatus, boolean bl) {
        ChunkAccess chunkAccess = this.getChunkSource().getChunk(n, n2, chunkStatus, bl);
        if (chunkAccess == null && bl) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        return chunkAccess;
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int n) {
        return this.setBlock(blockPos, blockState, n, 512);
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int n, int n2) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (!this.isClientSide && this.isDebug()) {
            return false;
        }
        LevelChunk levelChunk = this.getChunkAt(blockPos);
        Block block = blockState.getBlock();
        BlockState blockState2 = levelChunk.setBlockState(blockPos, blockState, (n & 0x40) != 0);
        if (blockState2 != null) {
            BlockState blockState3 = this.getBlockState(blockPos);
            if (blockState3 == blockState) {
                if (blockState2 != blockState3) {
                    this.setBlocksDirty(blockPos, blockState2, blockState3);
                }
                if ((n & 2) != 0 && (!this.isClientSide || (n & 4) == 0) && (this.isClientSide || levelChunk.getFullStatus() != null && levelChunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
                    this.sendBlockUpdated(blockPos, blockState2, blockState, n);
                }
                if ((n & 1) != 0) {
                    this.blockUpdated(blockPos, blockState2.getBlock());
                    if (!this.isClientSide && blockState.hasAnalogOutputSignal()) {
                        this.updateNeighbourForOutputSignal(blockPos, block);
                    }
                }
                if ((n & 0x10) == 0 && n2 > 0) {
                    int n3 = n & 0xFFFFFFDE;
                    blockState2.updateIndirectNeighbourShapes(this, blockPos, n3, n2 - 1);
                    blockState.updateNeighbourShapes(this, blockPos, n3, n2 - 1);
                    blockState.updateIndirectNeighbourShapes(this, blockPos, n3, n2 - 1);
                }
                this.onBlockStateChange(blockPos, blockState2, blockState3);
            }
            return true;
        }
        return false;
    }

    public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean bl) {
        FluidState fluidState = this.getFluidState(blockPos);
        return this.setBlock(blockPos, fluidState.createLegacyBlock(), 3 | (bl ? 64 : 0));
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int n) {
        boolean bl2;
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        FluidState fluidState = this.getFluidState(blockPos);
        if (!(blockState.getBlock() instanceof BaseFireBlock)) {
            this.levelEvent(2001, blockPos, Block.getId(blockState));
        }
        if (bl) {
            BlockEntity blockEntity = blockState.hasBlockEntity() ? this.getBlockEntity(blockPos) : null;
            Block.dropResources(blockState, this, blockPos, blockEntity, entity, ItemStack.EMPTY);
        }
        if (bl2 = this.setBlock(blockPos, fluidState.createLegacyBlock(), 3, n)) {
            this.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(entity, blockState));
        }
        return bl2;
    }

    public void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState) {
    }

    public boolean setBlockAndUpdate(BlockPos blockPos, BlockState blockState) {
        return this.setBlock(blockPos, blockState, 3);
    }

    public abstract void sendBlockUpdated(BlockPos var1, BlockState var2, BlockState var3, int var4);

    public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
    }

    public void updateNeighborsAt(BlockPos blockPos, Block block) {
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction) {
    }

    public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
    }

    public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
    }

    @Override
    public void neighborShapeChanged(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int n, int n2) {
        this.neighborUpdater.shapeUpdate(direction, blockState, blockPos, blockPos2, n, n2);
    }

    @Override
    public int getHeight(Heightmap.Types types, int n, int n2) {
        int n3 = n < -30000000 || n2 < -30000000 || n >= 30000000 || n2 >= 30000000 ? this.getSeaLevel() + 1 : (this.hasChunk(SectionPos.blockToSectionCoord(n), SectionPos.blockToSectionCoord(n2)) ? this.getChunk(SectionPos.blockToSectionCoord(n), SectionPos.blockToSectionCoord(n2)).getHeight(types, n & 0xF, n2 & 0xF) + 1 : this.getMinBuildHeight());
        return n3;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.getChunkSource().getLightEngine();
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunk levelChunk = this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
        return levelChunk.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunk levelChunk = this.getChunkAt(blockPos);
        return levelChunk.getFluidState(blockPos);
    }

    public boolean isDay() {
        return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
    }

    public boolean isNight() {
        return !this.dimensionType().hasFixedTime() && !this.isDay();
    }

    public void playSound(@Nullable Entity entity, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        Player player;
        this.playSound(entity instanceof Player ? (player = (Player)entity) : null, blockPos, soundEvent, soundSource, f, f2);
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.playSound(player, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, soundSource, f, f2);
    }

    public abstract void playSeededSound(@Nullable Player var1, double var2, double var4, double var6, Holder<SoundEvent> var8, SoundSource var9, float var10, float var11, long var12);

    public void playSeededSound(@Nullable Player player, double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2, long l) {
        this.playSeededSound(player, d, d2, d3, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), soundSource, f, f2, l);
    }

    public abstract void playSeededSound(@Nullable Player var1, Entity var2, Holder<SoundEvent> var3, SoundSource var4, float var5, float var6, long var7);

    public void playSound(@Nullable Player player, double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource) {
        this.playSound(player, d, d2, d3, soundEvent, soundSource, 1.0f, 1.0f);
    }

    public void playSound(@Nullable Player player, double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.playSeededSound(player, d, d2, d3, soundEvent, soundSource, f, f2, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable Player player, double d, double d2, double d3, Holder<SoundEvent> holder, SoundSource soundSource, float f, float f2) {
        this.playSeededSound(player, d, d2, d3, holder, soundSource, f, f2, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.playSeededSound(player, entity, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), soundSource, f, f2, this.threadSafeRandom.nextLong());
    }

    public void playLocalSound(BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float f2, boolean bl) {
        this.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, soundSource, f, f2, bl);
    }

    public void playLocalSound(Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
    }

    public void playLocalSound(double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2, boolean bl) {
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean bl, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public float getSunAngle(float f) {
        float f2 = this.getTimeOfDay(f);
        return f2 * ((float)Math.PI * 2);
    }

    public void addBlockEntityTicker(TickingBlockEntity tickingBlockEntity) {
        (this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(tickingBlockEntity);
    }

    protected void tickBlockEntities() {
        ProfilerFiller profilerFiller = this.getProfiler();
        profilerFiller.push("blockEntities");
        this.tickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }
        Iterator<TickingBlockEntity> iterator = this.blockEntityTickers.iterator();
        boolean bl = this.tickRateManager().runsNormally();
        while (iterator.hasNext()) {
            TickingBlockEntity tickingBlockEntity = iterator.next();
            if (tickingBlockEntity.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!bl || !this.shouldTickBlocksAt(tickingBlockEntity.getPos())) continue;
            tickingBlockEntity.tick();
        }
        this.tickingBlockEntities = false;
        profilerFiller.pop();
    }

    public <T extends Entity> void guardEntityTick(Consumer<T> consumer, T t) {
        try {
            consumer.accept(t);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being ticked");
            t.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    public boolean shouldTickDeath(Entity entity) {
        return true;
    }

    public boolean shouldTickBlocksAt(long l) {
        return true;
    }

    public boolean shouldTickBlocksAt(BlockPos blockPos) {
        return this.shouldTickBlocksAt(ChunkPos.asLong(blockPos));
    }

    public Explosion explode(@Nullable Entity entity, double d, double d2, double d3, float f, ExplosionInteraction explosionInteraction) {
        return this.explode(entity, Explosion.getDefaultDamageSource(this, entity), null, d, d2, d3, f, false, explosionInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, double d, double d2, double d3, float f, boolean bl, ExplosionInteraction explosionInteraction) {
        return this.explode(entity, Explosion.getDefaultDamageSource(this, entity), null, d, d2, d3, f, bl, explosionInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float f, boolean bl, ExplosionInteraction explosionInteraction) {
        return this.explode(entity, damageSource, explosionDamageCalculator, vec3.x(), vec3.y(), vec3.z(), f, bl, explosionInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean bl, ExplosionInteraction explosionInteraction) {
        return this.explode(entity, damageSource, explosionDamageCalculator, d, d2, d3, f, bl, explosionInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean bl, ExplosionInteraction explosionInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
        return this.explode(entity, damageSource, explosionDamageCalculator, d, d2, d3, f, bl, explosionInteraction, true, particleOptions, particleOptions2, holder);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean bl, ExplosionInteraction explosionInteraction, boolean bl2, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
        Explosion.BlockInteraction blockInteraction = switch (explosionInteraction.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Explosion.BlockInteraction.KEEP;
            case 1 -> this.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case 2 -> {
                if (this.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    yield this.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY);
                }
                yield Explosion.BlockInteraction.KEEP;
            }
            case 3 -> this.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
            case 4 -> Explosion.BlockInteraction.TRIGGER_BLOCK;
        };
        Explosion explosion = new Explosion(this, entity, damageSource, explosionDamageCalculator, d, d2, d3, f, bl, blockInteraction, particleOptions, particleOptions2, holder);
        explosion.explode();
        explosion.finalizeExplosion(bl2);
        return explosion;
    }

    private Explosion.BlockInteraction getDestroyType(GameRules.Key<GameRules.BooleanValue> key) {
        return this.getGameRules().getBoolean(key) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
    }

    public abstract String gatherChunkSourceStats();

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return null;
        }
        if (!this.isClientSide && Thread.currentThread() != this.thread) {
            return null;
        }
        return this.getChunkAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getBlockPos();
        if (this.isOutsideBuildHeight(blockPos)) {
            return;
        }
        this.getChunkAt(blockPos).addAndRegisterBlockEntity(blockEntity);
    }

    public void removeBlockEntity(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return;
        }
        this.getChunkAt(blockPos).removeBlockEntity(blockPos);
    }

    public boolean isLoaded(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        return this.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPos blockPos, Entity entity, Direction direction) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        ChunkAccess chunkAccess = this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), ChunkStatus.FULL, false);
        if (chunkAccess == null) {
            return false;
        }
        return chunkAccess.getBlockState(blockPos).entityCanStandOnFace(this, blockPos, entity, direction);
    }

    public boolean loadedAndEntityCanStandOn(BlockPos blockPos, Entity entity) {
        return this.loadedAndEntityCanStandOnFace(blockPos, entity, Direction.UP);
    }

    public void updateSkyBrightness() {
        double d = 1.0 - (double)(this.getRainLevel(1.0f) * 5.0f) / 16.0;
        double d2 = 1.0 - (double)(this.getThunderLevel(1.0f) * 5.0f) / 16.0;
        double d3 = 0.5 + 2.0 * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0f) * ((float)Math.PI * 2)), -0.25, 0.25);
        this.skyDarken = (int)((1.0 - d3 * d * d2) * 11.0);
    }

    public void setSpawnSettings(boolean bl, boolean bl2) {
        this.getChunkSource().setSpawnSettings(bl, bl2);
    }

    public BlockPos getSharedSpawnPos() {
        BlockPos blockPos = this.levelData.getSpawnPos();
        if (!this.getWorldBorder().isWithinBounds(blockPos)) {
            blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return blockPos;
    }

    public float getSharedSpawnAngle() {
        return this.levelData.getSpawnAngle();
    }

    protected void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0f;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0f;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.getChunkSource().close();
    }

    @Override
    @Nullable
    public BlockGetter getChunkForCollisions(int n, int n2) {
        return this.getChunk(n, n2, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aABB, Predicate<? super Entity> predicate) {
        this.getProfiler().incrementCounter("getEntities");
        ArrayList arrayList = Lists.newArrayList();
        this.getEntities().get(aABB, entity2 -> {
            if (entity2 != entity && predicate.test((Entity)entity2)) {
                arrayList.add(entity2);
            }
            if (entity2 instanceof EnderDragon) {
                for (EnderDragonPart enderDragonPart : ((EnderDragon)entity2).getSubEntities()) {
                    if (entity2 == entity || !predicate.test(enderDragonPart)) continue;
                    arrayList.add(enderDragonPart);
                }
            }
        });
        return arrayList;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate) {
        ArrayList arrayList = Lists.newArrayList();
        this.getEntities(entityTypeTest, aABB, predicate, arrayList);
        return arrayList;
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate, List<? super T> list) {
        this.getEntities(entityTypeTest, aABB, predicate, list, Integer.MAX_VALUE);
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate, List<? super T> list, int n) {
        this.getProfiler().incrementCounter("getEntities");
        this.getEntities().get(entityTypeTest, aABB, entity -> {
            if (predicate.test(entity)) {
                list.add((Object)entity);
                if (list.size() >= n) {
                    return AbortableIterationConsumer.Continuation.ABORT;
                }
            }
            if (entity instanceof EnderDragon) {
                EnderDragon enderDragon = (EnderDragon)entity;
                for (EnderDragonPart enderDragonPart : enderDragon.getSubEntities()) {
                    Entity entity2 = (Entity)entityTypeTest.tryCast(enderDragonPart);
                    if (entity2 == null || !predicate.test(entity2)) continue;
                    list.add((Object)entity2);
                    if (list.size() < n) continue;
                    return AbortableIterationConsumer.Continuation.ABORT;
                }
            }
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }

    @Nullable
    public abstract Entity getEntity(int var1);

    public void blockEntityChanged(BlockPos blockPos) {
        if (this.hasChunkAt(blockPos)) {
            this.getChunkAt(blockPos).setUnsaved(true);
        }
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    public void disconnect() {
    }

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public boolean mayInteract(Player player, BlockPos blockPos) {
        return true;
    }

    public void broadcastEntityEvent(Entity entity, byte by) {
    }

    public void broadcastDamageEvent(Entity entity, DamageSource damageSource) {
    }

    public void blockEvent(BlockPos blockPos, Block block, int n, int n2) {
        this.getBlockState(blockPos).triggerEvent(this, blockPos, n, n2);
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    public GameRules getGameRules() {
        return this.levelData.getGameRules();
    }

    public abstract TickRateManager tickRateManager();

    public float getThunderLevel(float f) {
        return Mth.lerp(f, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(f);
    }

    public void setThunderLevel(float f) {
        float f2;
        this.oThunderLevel = f2 = Mth.clamp(f, 0.0f, 1.0f);
        this.thunderLevel = f2;
    }

    public float getRainLevel(float f) {
        return Mth.lerp(f, this.oRainLevel, this.rainLevel);
    }

    public void setRainLevel(float f) {
        float f2;
        this.oRainLevel = f2 = Mth.clamp(f, 0.0f, 1.0f);
        this.rainLevel = f2;
    }

    public boolean isThundering() {
        if (!this.dimensionType().hasSkyLight() || this.dimensionType().hasCeiling()) {
            return false;
        }
        return (double)this.getThunderLevel(1.0f) > 0.9;
    }

    public boolean isRaining() {
        return (double)this.getRainLevel(1.0f) > 0.2;
    }

    public boolean isRainingAt(BlockPos blockPos) {
        if (!this.isRaining()) {
            return false;
        }
        if (!this.canSeeSky(blockPos)) {
            return false;
        }
        if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > blockPos.getY()) {
            return false;
        }
        Biome biome = this.getBiome(blockPos).value();
        return biome.getPrecipitationAt(blockPos) == Biome.Precipitation.RAIN;
    }

    @Nullable
    public abstract MapItemSavedData getMapData(MapId var1);

    public abstract void setMapData(MapId var1, MapItemSavedData var2);

    public abstract MapId getFreeMapId();

    public void globalLevelEvent(int n, BlockPos blockPos, int n2) {
    }

    public CrashReportCategory fillReportDetails(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Affected level", 1);
        crashReportCategory.setDetail("All players", () -> this.players().size() + " total; " + String.valueOf(this.players()));
        crashReportCategory.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
        crashReportCategory.setDetail("Level dimension", () -> this.dimension().location().toString());
        try {
            this.levelData.fillCrashReportCategory(crashReportCategory, this);
        }
        catch (Throwable throwable) {
            crashReportCategory.setDetailError("Level Data Unobtainable", throwable);
        }
        return crashReportCategory;
    }

    public abstract void destroyBlockProgress(int var1, BlockPos var2, int var3);

    public void createFireworks(double d, double d2, double d3, double d4, double d5, double d6, List<FireworkExplosion> list) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPos blockPos, Block block) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!this.hasChunkAt(blockPos2)) continue;
            BlockState blockState = this.getBlockState(blockPos2);
            if (blockState.is(Blocks.COMPARATOR)) {
                this.neighborChanged(blockState, blockPos2, block, blockPos, false);
                continue;
            }
            if (!blockState.isRedstoneConductor(this, blockPos2) || !(blockState = this.getBlockState(blockPos2 = blockPos2.relative(direction))).is(Blocks.COMPARATOR)) continue;
            this.neighborChanged(blockState, blockPos2, block, blockPos, false);
        }
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        long l = 0L;
        float f = 0.0f;
        if (this.hasChunkAt(blockPos)) {
            f = this.getMoonBrightness();
            l = this.getChunkAt(blockPos).getInhabitedTime();
        }
        return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), l, f);
    }

    @Override
    public int getSkyDarken() {
        return this.skyDarken;
    }

    public void setSkyFlashTime(int n) {
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override
    public DimensionType dimensionType() {
        return this.dimensionTypeRegistration.value();
    }

    public Holder<DimensionType> dimensionTypeRegistration() {
        return this.dimensionTypeRegistration;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(blockPos));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos blockPos, Predicate<FluidState> predicate) {
        return predicate.test(this.getFluidState(blockPos));
    }

    public abstract RecipeManager getRecipeManager();

    public BlockPos getBlockRandomPos(int n, int n2, int n3, int n4) {
        this.randValue = this.randValue * 3 + 1013904223;
        int n5 = this.randValue >> 2;
        return new BlockPos(n + (n5 & 0xF), n2 + (n5 >> 16 & n4), n3 + (n5 >> 8 & 0xF));
    }

    public boolean noSave() {
        return false;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler.get();
    }

    public Supplier<ProfilerFiller> getProfilerSupplier() {
        return this.profiler;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    public final boolean isDebug() {
        return this.isDebug;
    }

    protected abstract LevelEntityGetter<Entity> getEntities();

    @Override
    public long nextSubTickCount() {
        return this.subTickCount++;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }

    public DamageSources damageSources() {
        return this.damageSources;
    }

    public abstract PotionBrewing potionBrewing();

    @Override
    public /* synthetic */ ChunkAccess getChunk(int n, int n2) {
        return this.getChunk(n, n2);
    }

    public static enum ExplosionInteraction implements StringRepresentable
    {
        NONE("none"),
        BLOCK("block"),
        MOB("mob"),
        TNT("tnt"),
        TRIGGER("trigger");

        public static final Codec<ExplosionInteraction> CODEC;
        private final String id;

        private ExplosionInteraction(String string2) {
            this.id = string2;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(ExplosionInteraction::values);
        }
    }
}

