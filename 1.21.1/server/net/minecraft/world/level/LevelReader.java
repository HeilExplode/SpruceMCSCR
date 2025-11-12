/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public interface LevelReader
extends BlockAndTintGetter,
CollisionGetter,
SignalGetter,
BiomeManager.NoiseBiomeSource {
    @Nullable
    public ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    public boolean hasChunk(int var1, int var2);

    public int getHeight(Heightmap.Types var1, int var2, int var3);

    public int getSkyDarken();

    public BiomeManager getBiomeManager();

    default public Holder<Biome> getBiome(BlockPos blockPos) {
        return this.getBiomeManager().getBiome(blockPos);
    }

    default public Stream<BlockState> getBlockStatesIfLoaded(AABB aABB) {
        int n;
        int n2 = Mth.floor(aABB.minX);
        int n3 = Mth.floor(aABB.maxX);
        int n4 = Mth.floor(aABB.minY);
        int n5 = Mth.floor(aABB.maxY);
        int n6 = Mth.floor(aABB.minZ);
        if (this.hasChunksAt(n2, n4, n6, n3, n5, n = Mth.floor(aABB.maxZ))) {
            return this.getBlockStates(aABB);
        }
        return Stream.empty();
    }

    @Override
    default public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return colorResolver.getColor(this.getBiome(blockPos).value(), blockPos.getX(), blockPos.getZ());
    }

    @Override
    default public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        ChunkAccess chunkAccess = this.getChunk(QuartPos.toSection(n), QuartPos.toSection(n3), ChunkStatus.BIOMES, false);
        if (chunkAccess != null) {
            return chunkAccess.getNoiseBiome(n, n2, n3);
        }
        return this.getUncachedNoiseBiome(n, n2, n3);
    }

    public Holder<Biome> getUncachedNoiseBiome(int var1, int var2, int var3);

    public boolean isClientSide();

    @Deprecated
    public int getSeaLevel();

    public DimensionType dimensionType();

    @Override
    default public int getMinBuildHeight() {
        return this.dimensionType().minY();
    }

    @Override
    default public int getHeight() {
        return this.dimensionType().height();
    }

    default public BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
        return new BlockPos(blockPos.getX(), this.getHeight(types, blockPos.getX(), blockPos.getZ()), blockPos.getZ());
    }

    default public boolean isEmptyBlock(BlockPos blockPos) {
        return this.getBlockState(blockPos).isAir();
    }

    default public boolean canSeeSkyFromBelowWater(BlockPos blockPos) {
        if (blockPos.getY() >= this.getSeaLevel()) {
            return this.canSeeSky(blockPos);
        }
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), this.getSeaLevel(), blockPos.getZ());
        if (!this.canSeeSky(blockPos2)) {
            return false;
        }
        blockPos2 = blockPos2.below();
        while (blockPos2.getY() > blockPos.getY()) {
            BlockState blockState = this.getBlockState(blockPos2);
            if (blockState.getLightBlock(this, blockPos2) > 0 && !blockState.liquid()) {
                return false;
            }
            blockPos2 = blockPos2.below();
        }
        return true;
    }

    default public float getPathfindingCostFromLightLevels(BlockPos blockPos) {
        return this.getLightLevelDependentMagicValue(blockPos) - 0.5f;
    }

    @Deprecated
    default public float getLightLevelDependentMagicValue(BlockPos blockPos) {
        float f = (float)this.getMaxLocalRawBrightness(blockPos) / 15.0f;
        float f2 = f / (4.0f - 3.0f * f);
        return Mth.lerp(this.dimensionType().ambientLight(), f2, 1.0f);
    }

    default public ChunkAccess getChunk(BlockPos blockPos) {
        return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    default public ChunkAccess getChunk(int n, int n2) {
        return this.getChunk(n, n2, ChunkStatus.FULL, true);
    }

    default public ChunkAccess getChunk(int n, int n2, ChunkStatus chunkStatus) {
        return this.getChunk(n, n2, chunkStatus, true);
    }

    @Override
    @Nullable
    default public BlockGetter getChunkForCollisions(int n, int n2) {
        return this.getChunk(n, n2, ChunkStatus.EMPTY, false);
    }

    default public boolean isWaterAt(BlockPos blockPos) {
        return this.getFluidState(blockPos).is(FluidTags.WATER);
    }

    default public boolean containsAnyLiquid(AABB aABB) {
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.ceil(aABB.maxX);
        int n3 = Mth.floor(aABB.minY);
        int n4 = Mth.ceil(aABB.maxY);
        int n5 = Mth.floor(aABB.minZ);
        int n6 = Mth.ceil(aABB.maxZ);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    BlockState blockState = this.getBlockState(mutableBlockPos.set(i, j, k));
                    if (blockState.getFluidState().isEmpty()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    default public int getMaxLocalRawBrightness(BlockPos blockPos) {
        return this.getMaxLocalRawBrightness(blockPos, this.getSkyDarken());
    }

    default public int getMaxLocalRawBrightness(BlockPos blockPos, int n) {
        if (blockPos.getX() < -30000000 || blockPos.getZ() < -30000000 || blockPos.getX() >= 30000000 || blockPos.getZ() >= 30000000) {
            return 15;
        }
        return this.getRawBrightness(blockPos, n);
    }

    @Deprecated
    default public boolean hasChunkAt(int n, int n2) {
        return this.hasChunk(SectionPos.blockToSectionCoord(n), SectionPos.blockToSectionCoord(n2));
    }

    @Deprecated
    default public boolean hasChunkAt(BlockPos blockPos) {
        return this.hasChunkAt(blockPos.getX(), blockPos.getZ());
    }

    @Deprecated
    default public boolean hasChunksAt(BlockPos blockPos, BlockPos blockPos2) {
        return this.hasChunksAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
    }

    @Deprecated
    default public boolean hasChunksAt(int n, int n2, int n3, int n4, int n5, int n6) {
        if (n5 < this.getMinBuildHeight() || n2 >= this.getMaxBuildHeight()) {
            return false;
        }
        return this.hasChunksAt(n, n3, n4, n6);
    }

    @Deprecated
    default public boolean hasChunksAt(int n, int n2, int n3, int n4) {
        int n5 = SectionPos.blockToSectionCoord(n);
        int n6 = SectionPos.blockToSectionCoord(n3);
        int n7 = SectionPos.blockToSectionCoord(n2);
        int n8 = SectionPos.blockToSectionCoord(n4);
        for (int i = n5; i <= n6; ++i) {
            for (int j = n7; j <= n8; ++j) {
                if (this.hasChunk(i, j)) continue;
                return false;
            }
        }
        return true;
    }

    public RegistryAccess registryAccess();

    public FeatureFlagSet enabledFeatures();

    default public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
        Registry registry = this.registryAccess().registryOrThrow(resourceKey);
        return registry.asLookup().filterFeatures(this.enabledFeatures());
    }
}

