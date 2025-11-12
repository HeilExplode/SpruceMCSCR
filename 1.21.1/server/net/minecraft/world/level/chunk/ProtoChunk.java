/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunk
extends ChunkAccess {
    @Nullable
    private volatile LevelLightEngine lightEngine;
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private final List<CompoundTag> entities = Lists.newArrayList();
    private final Map<GenerationStep.Carving, CarvingMask> carvingMasks = new Object2ObjectArrayMap();
    @Nullable
    private BelowZeroRetrogen belowZeroRetrogen;
    private final ProtoChunkTicks<Block> blockTicks;
    private final ProtoChunkTicks<Fluid> fluidTicks;

    public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, @Nullable BlendingData blendingData) {
        this(chunkPos, upgradeData, null, new ProtoChunkTicks<Block>(), new ProtoChunkTicks<Fluid>(), levelHeightAccessor, registry, blendingData);
    }

    public ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, @Nullable LevelChunkSection[] levelChunkSectionArray, ProtoChunkTicks<Block> protoChunkTicks, ProtoChunkTicks<Fluid> protoChunkTicks2, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, 0L, levelChunkSectionArray, blendingData);
        this.blockTicks = protoChunkTicks;
        this.fluidTicks = protoChunkTicks2;
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public ChunkAccess.TicksToSave getTicksForSerialization() {
        return new ChunkAccess.TicksToSave(this.blockTicks, this.fluidTicks);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int n = blockPos.getY();
        if (this.isOutsideBuildHeight(n)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(n));
        if (levelChunkSection.hasOnlyAir()) {
            return Blocks.AIR.defaultBlockState();
        }
        return levelChunkSection.getBlockState(blockPos.getX() & 0xF, n & 0xF, blockPos.getZ() & 0xF);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        int n = blockPos.getY();
        if (this.isOutsideBuildHeight(n)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(n));
        if (levelChunkSection.hasOnlyAir()) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return levelChunkSection.getFluidState(blockPos.getX() & 0xF, n & 0xF, blockPos.getZ() & 0xF);
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        if (n2 < this.getMinBuildHeight() || n2 >= this.getMaxBuildHeight()) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        int n4 = this.getSectionIndex(n2);
        LevelChunkSection levelChunkSection = this.getSection(n4);
        boolean bl2 = levelChunkSection.hasOnlyAir();
        if (bl2 && blockState.is(Blocks.AIR)) {
            return blockState;
        }
        int n5 = SectionPos.sectionRelative(n);
        int n6 = SectionPos.sectionRelative(n2);
        int n7 = SectionPos.sectionRelative(n3);
        BlockState blockState2 = levelChunkSection.setBlockState(n5, n6, n7, blockState);
        if (this.status.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
            boolean bl3 = levelChunkSection.hasOnlyAir();
            if (bl3 != bl2) {
                this.lightEngine.updateSectionStatus(blockPos, bl3);
            }
            if (LightEngine.hasDifferentLightProperties(this, blockPos, blockState2, blockState)) {
                this.skyLightSources.update(this, n5, n2, n7);
                this.lightEngine.checkBlock(blockPos);
            }
        }
        EnumSet<Heightmap.Types> enumSet = this.getPersistedStatus().heightmapsAfter();
        EnumSet<Heightmap.Types> enumSet2 = null;
        for (Heightmap.Types types : enumSet) {
            Heightmap heightmap = (Heightmap)this.heightmaps.get(types);
            if (heightmap != null) continue;
            if (enumSet2 == null) {
                enumSet2 = EnumSet.noneOf(Heightmap.Types.class);
            }
            enumSet2.add(types);
        }
        if (enumSet2 != null) {
            Heightmap.primeHeightmaps(this, enumSet2);
        }
        for (Heightmap.Types types : enumSet) {
            ((Heightmap)this.heightmaps.get(types)).update(n5, n2, n7, blockState);
        }
        return blockState2;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        this.blockEntities.put(blockEntity.getBlockPos(), blockEntity);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return (BlockEntity)this.blockEntities.get(blockPos);
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(CompoundTag compoundTag) {
        this.entities.add(compoundTag);
    }

    @Override
    public void addEntity(Entity entity) {
        if (entity.isPassenger()) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        entity.save(compoundTag);
        this.addEntity(compoundTag);
    }

    @Override
    public void setStartForStructure(Structure structure, StructureStart structureStart) {
        BelowZeroRetrogen belowZeroRetrogen = this.getBelowZeroRetrogen();
        if (belowZeroRetrogen != null && structureStart.isValid()) {
            BoundingBox boundingBox = structureStart.getBoundingBox();
            LevelHeightAccessor levelHeightAccessor = this.getHeightAccessorForGeneration();
            if (boundingBox.minY() < levelHeightAccessor.getMinBuildHeight() || boundingBox.maxY() >= levelHeightAccessor.getMaxBuildHeight()) {
                return;
            }
        }
        super.setStartForStructure(structure, structureStart);
    }

    public List<CompoundTag> getEntities() {
        return this.entities;
    }

    @Override
    public ChunkStatus getPersistedStatus() {
        return this.status;
    }

    public void setPersistedStatus(ChunkStatus chunkStatus) {
        this.status = chunkStatus;
        if (this.belowZeroRetrogen != null && chunkStatus.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
            this.setBelowZeroRetrogen(null);
        }
        this.setUnsaved(true);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        if (this.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
            return super.getNoiseBiome(n, n2, n3);
        }
        throw new IllegalStateException("Asking for biomes before we have biomes");
    }

    public static short packOffsetCoordinates(BlockPos blockPos) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        int n4 = n & 0xF;
        int n5 = n2 & 0xF;
        int n6 = n3 & 0xF;
        return (short)(n4 | n5 << 4 | n6 << 8);
    }

    public static BlockPos unpackOffsetCoordinates(short s, int n, ChunkPos chunkPos) {
        int n2 = SectionPos.sectionToBlockCoord(chunkPos.x, s & 0xF);
        int n3 = SectionPos.sectionToBlockCoord(n, s >>> 4 & 0xF);
        int n4 = SectionPos.sectionToBlockCoord(chunkPos.z, s >>> 8 & 0xF);
        return new BlockPos(n2, n3, n4);
    }

    @Override
    public void markPosForPostprocessing(BlockPos blockPos) {
        if (!this.isOutsideBuildHeight(blockPos)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(blockPos.getY())).add(ProtoChunk.packOffsetCoordinates(blockPos));
        }
    }

    @Override
    public void addPackedPostProcess(short s, int n) {
        ChunkAccess.getOrCreateOffsetList(this.postProcessing, n).add(s);
    }

    public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
        return Collections.unmodifiableMap(this.pendingBlockEntities);
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity != null) {
            return blockEntity.saveWithFullMetadata(provider);
        }
        return (CompoundTag)this.pendingBlockEntities.get(blockPos);
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
        this.blockEntities.remove(blockPos);
        this.pendingBlockEntities.remove(blockPos);
    }

    @Nullable
    public CarvingMask getCarvingMask(GenerationStep.Carving carving) {
        return this.carvingMasks.get(carving);
    }

    public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving carving2) {
        return this.carvingMasks.computeIfAbsent(carving2, carving -> new CarvingMask(this.getHeight(), this.getMinBuildHeight()));
    }

    public void setCarvingMask(GenerationStep.Carving carving, CarvingMask carvingMask) {
        this.carvingMasks.put(carving, carvingMask);
    }

    public void setLightEngine(LevelLightEngine levelLightEngine) {
        this.lightEngine = levelLightEngine;
    }

    public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen) {
        this.belowZeroRetrogen = belowZeroRetrogen;
    }

    @Override
    @Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return this.belowZeroRetrogen;
    }

    private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> protoChunkTicks) {
        return new LevelChunkTicks<T>(protoChunkTicks.scheduledTicks());
    }

    public LevelChunkTicks<Block> unpackBlockTicks() {
        return ProtoChunk.unpackTicks(this.blockTicks);
    }

    public LevelChunkTicks<Fluid> unpackFluidTicks() {
        return ProtoChunk.unpackTicks(this.fluidTicks);
    }

    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration() {
        if (this.isUpgrading()) {
            return BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR;
        }
        return this;
    }
}

