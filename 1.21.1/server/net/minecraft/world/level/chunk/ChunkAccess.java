/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.ShortArrayList
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public abstract class ChunkAccess
implements BlockGetter,
BiomeManager.NoiseBiomeSource,
LightChunk,
StructureAccess {
    public static final int NO_FILLED_SECTION = -1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LongSet EMPTY_REFERENCE_SET = new LongOpenHashSet();
    protected final ShortList[] postProcessing;
    protected volatile boolean unsaved;
    private volatile boolean isLightCorrect;
    protected final ChunkPos chunkPos;
    private long inhabitedTime;
    @Nullable
    @Deprecated
    private BiomeGenerationSettings carverBiomeSettings;
    @Nullable
    protected NoiseChunk noiseChunk;
    protected final UpgradeData upgradeData;
    @Nullable
    protected BlendingData blendingData;
    protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    protected ChunkSkyLightSources skyLightSources;
    private final Map<Structure, StructureStart> structureStarts = Maps.newHashMap();
    private final Map<Structure, LongSet> structuresRefences = Maps.newHashMap();
    protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    protected final Map<BlockPos, BlockEntity> blockEntities = new Object2ObjectOpenHashMap();
    protected final LevelHeightAccessor levelHeightAccessor;
    protected final LevelChunkSection[] sections;

    public ChunkAccess(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSectionArray, @Nullable BlendingData blendingData) {
        this.chunkPos = chunkPos;
        this.upgradeData = upgradeData;
        this.levelHeightAccessor = levelHeightAccessor;
        this.sections = new LevelChunkSection[levelHeightAccessor.getSectionsCount()];
        this.inhabitedTime = l;
        this.postProcessing = new ShortList[levelHeightAccessor.getSectionsCount()];
        this.blendingData = blendingData;
        this.skyLightSources = new ChunkSkyLightSources(levelHeightAccessor);
        if (levelChunkSectionArray != null) {
            if (this.sections.length == levelChunkSectionArray.length) {
                System.arraycopy(levelChunkSectionArray, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", (Object)levelChunkSectionArray.length, (Object)this.sections.length);
            }
        }
        ChunkAccess.replaceMissingSections(registry, this.sections);
    }

    private static void replaceMissingSections(Registry<Biome> registry, LevelChunkSection[] levelChunkSectionArray) {
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            if (levelChunkSectionArray[i] != null) continue;
            levelChunkSectionArray[i] = new LevelChunkSection(registry);
        }
    }

    public GameEventListenerRegistry getListenerRegistry(int n) {
        return GameEventListenerRegistry.NOOP;
    }

    @Nullable
    public abstract BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    public abstract void setBlockEntity(BlockEntity var1);

    public abstract void addEntity(Entity var1);

    public int getHighestFilledSectionIndex() {
        LevelChunkSection[] levelChunkSectionArray = this.getSections();
        for (int i = levelChunkSectionArray.length - 1; i >= 0; --i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            if (levelChunkSection.hasOnlyAir()) continue;
            return i;
        }
        return -1;
    }

    @Deprecated(forRemoval=true)
    public int getHighestSectionPosition() {
        int n = this.getHighestFilledSectionIndex();
        return n == -1 ? this.getMinBuildHeight() : SectionPos.sectionToBlockCoord(this.getSectionYFromSectionIndex(n));
    }

    public Set<BlockPos> getBlockEntitiesPos() {
        HashSet hashSet = Sets.newHashSet(this.pendingBlockEntities.keySet());
        hashSet.addAll(this.blockEntities.keySet());
        return hashSet;
    }

    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    public LevelChunkSection getSection(int n) {
        return this.getSections()[n];
    }

    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public void setHeightmap(Heightmap.Types types, long[] lArray) {
        this.getOrCreateHeightmapUnprimed(types).setRawData(this, types, lArray);
    }

    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types2) {
        return this.heightmaps.computeIfAbsent(types2, types -> new Heightmap(this, (Heightmap.Types)types));
    }

    public boolean hasPrimedHeightmap(Heightmap.Types types) {
        return this.heightmaps.get(types) != null;
    }

    public int getHeight(Heightmap.Types types, int n, int n2) {
        Heightmap heightmap = this.heightmaps.get(types);
        if (heightmap == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE && this instanceof LevelChunk) {
                LOGGER.error("Unprimed heightmap: " + String.valueOf(types) + " " + n + " " + n2);
            }
            Heightmap.primeHeightmaps(this, EnumSet.of(types));
            heightmap = this.heightmaps.get(types);
        }
        return heightmap.getFirstAvailable(n & 0xF, n2 & 0xF) - 1;
    }

    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @Override
    @Nullable
    public StructureStart getStartForStructure(Structure structure) {
        return this.structureStarts.get(structure);
    }

    @Override
    public void setStartForStructure(Structure structure, StructureStart structureStart) {
        this.structureStarts.put(structure, structureStart);
        this.unsaved = true;
    }

    public Map<Structure, StructureStart> getAllStarts() {
        return Collections.unmodifiableMap(this.structureStarts);
    }

    public void setAllStarts(Map<Structure, StructureStart> map) {
        this.structureStarts.clear();
        this.structureStarts.putAll(map);
        this.unsaved = true;
    }

    @Override
    public LongSet getReferencesForStructure(Structure structure) {
        return this.structuresRefences.getOrDefault(structure, EMPTY_REFERENCE_SET);
    }

    @Override
    public void addReferenceForStructure(Structure structure2, long l) {
        this.structuresRefences.computeIfAbsent(structure2, structure -> new LongOpenHashSet()).add(l);
        this.unsaved = true;
    }

    @Override
    public Map<Structure, LongSet> getAllReferences() {
        return Collections.unmodifiableMap(this.structuresRefences);
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> map) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(map);
        this.unsaved = true;
    }

    public boolean isYSpaceEmpty(int n, int n2) {
        if (n < this.getMinBuildHeight()) {
            n = this.getMinBuildHeight();
        }
        if (n2 >= this.getMaxBuildHeight()) {
            n2 = this.getMaxBuildHeight() - 1;
        }
        for (int i = n; i <= n2; i += 16) {
            if (this.getSection(this.getSectionIndex(i)).hasOnlyAir()) continue;
            return false;
        }
        return true;
    }

    public boolean isSectionEmpty(int n) {
        return this.getSection(this.getSectionIndexFromSectionY(n)).hasOnlyAir();
    }

    public void setUnsaved(boolean bl) {
        this.unsaved = bl;
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    public abstract ChunkStatus getPersistedStatus();

    public ChunkStatus getHighestGeneratedStatus() {
        ChunkStatus chunkStatus = this.getPersistedStatus();
        BelowZeroRetrogen belowZeroRetrogen = this.getBelowZeroRetrogen();
        if (belowZeroRetrogen != null) {
            ChunkStatus chunkStatus2 = belowZeroRetrogen.targetStatus();
            return ChunkStatus.max(chunkStatus2, chunkStatus);
        }
        return chunkStatus;
    }

    public abstract void removeBlockEntity(BlockPos var1);

    public void markPosForPostprocessing(BlockPos blockPos) {
        LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)blockPos);
    }

    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void addPackedPostProcess(short s, int n) {
        ChunkAccess.getOrCreateOffsetList(this.getPostProcessing(), n).add(s);
    }

    public void setBlockEntityNbt(CompoundTag compoundTag) {
        this.pendingBlockEntities.put(BlockEntity.getPosFromTag(compoundTag), compoundTag);
    }

    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.pendingBlockEntities.get(blockPos);
    }

    @Nullable
    public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos var1, HolderLookup.Provider var2);

    @Override
    public final void findBlockLightSources(BiConsumer<BlockPos, BlockState> biConsumer) {
        this.findBlocks(blockState -> blockState.getLightEmission() != 0, biConsumer);
    }

    public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> biConsumer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = this.getMinSection(); i < this.getMaxSection(); ++i) {
            LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(i));
            if (!levelChunkSection.maybeHas(predicate)) continue;
            BlockPos blockPos = SectionPos.of(this.chunkPos, i).origin();
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int i2 = 0; i2 < 16; ++i2) {
                        BlockState blockState = levelChunkSection.getBlockState(i2, j, k);
                        if (!predicate.test(blockState)) continue;
                        biConsumer.accept(mutableBlockPos.setWithOffset(blockPos, i2, j, k), blockState);
                    }
                }
            }
        }
    }

    public abstract TickContainerAccess<Block> getBlockTicks();

    public abstract TickContainerAccess<Fluid> getFluidTicks();

    public abstract TicksToSave getTicksForSerialization();

    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    public boolean isOldNoiseGeneration() {
        return this.blendingData != null;
    }

    @Nullable
    public BlendingData getBlendingData() {
        return this.blendingData;
    }

    public void setBlendingData(BlendingData blendingData) {
        this.blendingData = blendingData;
    }

    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    public void incrementInhabitedTime(long l) {
        this.inhabitedTime += l;
    }

    public void setInhabitedTime(long l) {
        this.inhabitedTime = l;
    }

    public static ShortList getOrCreateOffsetList(ShortList[] shortListArray, int n) {
        if (shortListArray[n] == null) {
            shortListArray[n] = new ShortArrayList();
        }
        return shortListArray[n];
    }

    public boolean isLightCorrect() {
        return this.isLightCorrect;
    }

    public void setLightCorrect(boolean bl) {
        this.isLightCorrect = bl;
        this.setUnsaved(true);
    }

    @Override
    public int getMinBuildHeight() {
        return this.levelHeightAccessor.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.levelHeightAccessor.getHeight();
    }

    public NoiseChunk getOrCreateNoiseChunk(Function<ChunkAccess, NoiseChunk> function) {
        if (this.noiseChunk == null) {
            this.noiseChunk = function.apply(this);
        }
        return this.noiseChunk;
    }

    @Deprecated
    public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> supplier) {
        if (this.carverBiomeSettings == null) {
            this.carverBiomeSettings = supplier.get();
        }
        return this.carverBiomeSettings;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        try {
            int n4 = QuartPos.fromBlock(this.getMinBuildHeight());
            int n5 = n4 + QuartPos.fromBlock(this.getHeight()) - 1;
            int n6 = Mth.clamp(n2, n4, n5);
            int n7 = this.getSectionIndex(QuartPos.toBlock(n6));
            return this.sections[n7].getNoiseBiome(n & 3, n6 & 3, n3 & 3);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting biome");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Biome being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, n, n2, n3));
            throw new ReportedException(crashReport);
        }
    }

    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
        ChunkPos chunkPos = this.getPos();
        int n = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int n2 = QuartPos.fromBlock(chunkPos.getMinBlockZ());
        LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();
        for (int i = levelHeightAccessor.getMinSection(); i < levelHeightAccessor.getMaxSection(); ++i) {
            LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndexFromSectionY(i));
            int n3 = QuartPos.fromSection(i);
            levelChunkSection.fillBiomesFromNoise(biomeResolver, sampler, n, n3, n2);
        }
    }

    public boolean hasAnyStructureReferences() {
        return !this.getAllReferences().isEmpty();
    }

    @Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return null;
    }

    public boolean isUpgrading() {
        return this.getBelowZeroRetrogen() != null;
    }

    public LevelHeightAccessor getHeightAccessorForGeneration() {
        return this;
    }

    public void initializeLightSources() {
        this.skyLightSources.fillFrom(this);
    }

    @Override
    public ChunkSkyLightSources getSkyLightSources() {
        return this.skyLightSources;
    }

    public record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids) {
    }
}

