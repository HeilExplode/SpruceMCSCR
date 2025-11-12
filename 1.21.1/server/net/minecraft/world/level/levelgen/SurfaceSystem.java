/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceSystem {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private final BlockState defaultBlock;
    private final int seaLevel;
    private final BlockState[] clayBands;
    private final NormalNoise clayBandsOffsetNoise;
    private final NormalNoise badlandsPillarNoise;
    private final NormalNoise badlandsPillarRoofNoise;
    private final NormalNoise badlandsSurfaceNoise;
    private final NormalNoise icebergPillarNoise;
    private final NormalNoise icebergPillarRoofNoise;
    private final NormalNoise icebergSurfaceNoise;
    private final PositionalRandomFactory noiseRandom;
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;

    public SurfaceSystem(RandomState randomState, BlockState blockState, int n, PositionalRandomFactory positionalRandomFactory) {
        this.defaultBlock = blockState;
        this.seaLevel = n;
        this.noiseRandom = positionalRandomFactory;
        this.clayBandsOffsetNoise = randomState.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
        this.clayBands = SurfaceSystem.generateBands(positionalRandomFactory.fromHashOf(ResourceLocation.withDefaultNamespace("clay_bands")));
        this.surfaceNoise = randomState.getOrCreateNoise(Noises.SURFACE);
        this.surfaceSecondaryNoise = randomState.getOrCreateNoise(Noises.SURFACE_SECONDARY);
        this.badlandsPillarNoise = randomState.getOrCreateNoise(Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = randomState.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = randomState.getOrCreateNoise(Noises.BADLANDS_SURFACE);
        this.icebergPillarNoise = randomState.getOrCreateNoise(Noises.ICEBERG_PILLAR);
        this.icebergPillarRoofNoise = randomState.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
        this.icebergSurfaceNoise = randomState.getOrCreateNoise(Noises.ICEBERG_SURFACE);
    }

    public void buildSurface(RandomState randomState, BiomeManager biomeManager, Registry<Biome> registry, boolean bl, WorldGenerationContext worldGenerationContext, final ChunkAccess chunkAccess, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource) {
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = chunkAccess.getPos();
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        BlockColumn blockColumn = new BlockColumn(){

            @Override
            public BlockState getBlock(int n) {
                return chunkAccess.getBlockState(mutableBlockPos.setY(n));
            }

            @Override
            public void setBlock(int n, BlockState blockState) {
                LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
                if (n >= levelHeightAccessor.getMinBuildHeight() && n < levelHeightAccessor.getMaxBuildHeight()) {
                    chunkAccess.setBlockState(mutableBlockPos.setY(n), blockState, false);
                    if (!blockState.getFluidState().isEmpty()) {
                        chunkAccess.markPosForPostprocessing(mutableBlockPos);
                    }
                }
            }

            public String toString() {
                return "ChunkBlockColumn " + String.valueOf(chunkPos);
            }
        };
        SurfaceRules.Context context = new SurfaceRules.Context(this, randomState, chunkAccess, noiseChunk, biomeManager::getBiome, registry, worldGenerationContext);
        SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int n3 = n + i;
                int n4 = n2 + j;
                int n5 = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, j) + 1;
                mutableBlockPos.setX(n3).setZ(n4);
                Holder<Biome> holder = biomeManager.getBiome(mutableBlockPos2.set(n3, bl ? 0 : n5, n4));
                if (holder.is(Biomes.ERODED_BADLANDS)) {
                    this.erodedBadlandsExtension(blockColumn, n3, n4, n5, chunkAccess);
                }
                int n6 = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, j) + 1;
                context.updateXZ(n3, n4);
                int n7 = 0;
                int n8 = Integer.MIN_VALUE;
                int n9 = Integer.MAX_VALUE;
                int n10 = chunkAccess.getMinBuildHeight();
                for (int k = n6; k >= n10; --k) {
                    BlockState blockState;
                    int n11;
                    BlockState blockState2 = blockColumn.getBlock(k);
                    if (blockState2.isAir()) {
                        n7 = 0;
                        n8 = Integer.MIN_VALUE;
                        continue;
                    }
                    if (!blockState2.getFluidState().isEmpty()) {
                        if (n8 != Integer.MIN_VALUE) continue;
                        n8 = k + 1;
                        continue;
                    }
                    if (n9 >= k) {
                        n9 = DimensionType.WAY_BELOW_MIN_Y;
                        for (n11 = k - 1; n11 >= n10 - 1; --n11) {
                            blockState = blockColumn.getBlock(n11);
                            if (this.isStone(blockState)) continue;
                            n9 = n11 + 1;
                            break;
                        }
                    }
                    n11 = k - n9 + 1;
                    context.updateY(++n7, n11, n8, n3, k, n4);
                    if (blockState2 != this.defaultBlock || (blockState = surfaceRule.tryApply(n3, k, n4)) == null) continue;
                    blockColumn.setBlock(k, blockState);
                }
                if (!holder.is(Biomes.FROZEN_OCEAN) && !holder.is(Biomes.DEEP_FROZEN_OCEAN)) continue;
                this.frozenOceanExtension(context.getMinSurfaceLevel(), holder.value(), blockColumn, mutableBlockPos2, n3, n4, n5);
            }
        }
    }

    protected int getSurfaceDepth(int n, int n2) {
        double d = this.surfaceNoise.getValue(n, 0.0, n2);
        return (int)(d * 2.75 + 3.0 + this.noiseRandom.at(n, 0, n2).nextDouble() * 0.25);
    }

    protected double getSurfaceSecondary(int n, int n2) {
        return this.surfaceSecondaryNoise.getValue(n, 0.0, n2);
    }

    private boolean isStone(BlockState blockState) {
        return !blockState.isAir() && blockState.getFluidState().isEmpty();
    }

    @Deprecated
    public Optional<BlockState> topMaterial(SurfaceRules.RuleSource ruleSource, CarvingContext carvingContext, Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkAccess, NoiseChunk noiseChunk, BlockPos blockPos, boolean bl) {
        SurfaceRules.Context context = new SurfaceRules.Context(this, carvingContext.randomState(), chunkAccess, noiseChunk, function, carvingContext.registryAccess().registryOrThrow(Registries.BIOME), carvingContext);
        SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        context.updateXZ(n, n3);
        context.updateY(1, 1, bl ? n2 + 1 : Integer.MIN_VALUE, n, n2, n3);
        BlockState blockState = surfaceRule.tryApply(n, n2, n3);
        return Optional.ofNullable(blockState);
    }

    private void erodedBadlandsExtension(BlockColumn blockColumn, int n, int n2, int n3, LevelHeightAccessor levelHeightAccessor) {
        BlockState blockState;
        int n4;
        double d = 0.2;
        double d2 = Math.min(Math.abs(this.badlandsSurfaceNoise.getValue(n, 0.0, n2) * 8.25), this.badlandsPillarNoise.getValue((double)n * 0.2, 0.0, (double)n2 * 0.2) * 15.0);
        if (d2 <= 0.0) {
            return;
        }
        double d3 = 0.75;
        double d4 = 1.5;
        double d5 = Math.abs(this.badlandsPillarRoofNoise.getValue((double)n * 0.75, 0.0, (double)n2 * 0.75) * 1.5);
        double d6 = 64.0 + Math.min(d2 * d2 * 2.5, Math.ceil(d5 * 50.0) + 24.0);
        int n5 = Mth.floor(d6);
        if (n3 > n5) {
            return;
        }
        for (n4 = n5; n4 >= levelHeightAccessor.getMinBuildHeight() && !(blockState = blockColumn.getBlock(n4)).is(this.defaultBlock.getBlock()); --n4) {
            if (!blockState.is(Blocks.WATER)) continue;
            return;
        }
        for (n4 = n5; n4 >= levelHeightAccessor.getMinBuildHeight() && blockColumn.getBlock(n4).isAir(); --n4) {
            blockColumn.setBlock(n4, this.defaultBlock);
        }
    }

    private void frozenOceanExtension(int n, Biome biome, BlockColumn blockColumn, BlockPos.MutableBlockPos mutableBlockPos, int n2, int n3, int n4) {
        double d;
        double d2 = 1.28;
        double d3 = Math.min(Math.abs(this.icebergSurfaceNoise.getValue(n2, 0.0, n3) * 8.25), this.icebergPillarNoise.getValue((double)n2 * 1.28, 0.0, (double)n3 * 1.28) * 15.0);
        if (d3 <= 1.8) {
            return;
        }
        double d4 = 1.17;
        double d5 = 1.5;
        double d6 = Math.abs(this.icebergPillarRoofNoise.getValue((double)n2 * 1.17, 0.0, (double)n3 * 1.17) * 1.5);
        double d7 = Math.min(d3 * d3 * 1.2, Math.ceil(d6 * 40.0) + 14.0);
        if (biome.shouldMeltFrozenOceanIcebergSlightly(mutableBlockPos.set(n2, 63, n3))) {
            d7 -= 2.0;
        }
        if (d7 > 2.0) {
            d = (double)this.seaLevel - d7 - 7.0;
            d7 += (double)this.seaLevel;
        } else {
            d7 = 0.0;
            d = 0.0;
        }
        double d8 = d7;
        RandomSource randomSource = this.noiseRandom.at(n2, 0, n3);
        int n5 = 2 + randomSource.nextInt(4);
        int n6 = this.seaLevel + 18 + randomSource.nextInt(10);
        int n7 = 0;
        for (int i = Math.max(n4, (int)d8 + 1); i >= n; --i) {
            if (!(blockColumn.getBlock(i).isAir() && i < (int)d8 && randomSource.nextDouble() > 0.01) && (!blockColumn.getBlock(i).is(Blocks.WATER) || i <= (int)d || i >= this.seaLevel || d == 0.0 || !(randomSource.nextDouble() > 0.15))) continue;
            if (n7 <= n5 && i > n6) {
                blockColumn.setBlock(i, SNOW_BLOCK);
                ++n7;
                continue;
            }
            blockColumn.setBlock(i, PACKED_ICE);
        }
    }

    private static BlockState[] generateBands(RandomSource randomSource) {
        int n;
        Object[] objectArray = new BlockState[192];
        Arrays.fill(objectArray, TERRACOTTA);
        for (n = 0; n < objectArray.length; ++n) {
            if ((n += randomSource.nextInt(5) + 1) >= objectArray.length) continue;
            objectArray[n] = ORANGE_TERRACOTTA;
        }
        SurfaceSystem.makeBands(randomSource, (BlockState[])objectArray, 1, YELLOW_TERRACOTTA);
        SurfaceSystem.makeBands(randomSource, (BlockState[])objectArray, 2, BROWN_TERRACOTTA);
        SurfaceSystem.makeBands(randomSource, (BlockState[])objectArray, 1, RED_TERRACOTTA);
        n = randomSource.nextIntBetweenInclusive(9, 15);
        int n2 = 0;
        for (int i = 0; n2 < n && i < objectArray.length; ++n2, i += randomSource.nextInt(16) + 4) {
            objectArray[i] = WHITE_TERRACOTTA;
            if (i - 1 > 0 && randomSource.nextBoolean()) {
                objectArray[i - 1] = LIGHT_GRAY_TERRACOTTA;
            }
            if (i + 1 >= objectArray.length || !randomSource.nextBoolean()) continue;
            objectArray[i + 1] = LIGHT_GRAY_TERRACOTTA;
        }
        return objectArray;
    }

    private static void makeBands(RandomSource randomSource, BlockState[] blockStateArray, int n, BlockState blockState) {
        int n2 = randomSource.nextIntBetweenInclusive(6, 15);
        for (int i = 0; i < n2; ++i) {
            int n3 = n + randomSource.nextInt(3);
            int n4 = randomSource.nextInt(blockStateArray.length);
            for (int j = 0; n4 + j < blockStateArray.length && j < n3; ++j) {
                blockStateArray[n4 + j] = blockState;
            }
        }
    }

    protected BlockState getBand(int n, int n2, int n3) {
        int n4 = (int)Math.round(this.clayBandsOffsetNoise.getValue(n, 0.0, n3) * 4.0);
        return this.clayBands[(n2 + n4 + this.clayBands.length) % this.clayBands.length];
    }
}

