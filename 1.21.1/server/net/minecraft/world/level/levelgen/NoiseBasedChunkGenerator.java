/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;

public final class NoiseBasedChunkGenerator
extends ChunkGenerator {
    public static final MapCodec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource), (App)NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)).apply((Applicative)instance, instance.stable(NoiseBasedChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final Holder<NoiseGeneratorSettings> settings;
    private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

    public NoiseBasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
        super(biomeSource);
        this.settings = holder;
        this.globalFluidPicker = Suppliers.memoize(() -> NoiseBasedChunkGenerator.createFluidPicker((NoiseGeneratorSettings)holder.value()));
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings noiseGeneratorSettings) {
        Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int n = noiseGeneratorSettings.seaLevel();
        Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(n, noiseGeneratorSettings.defaultFluid());
        Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        return (n2, n3, n4) -> {
            if (n3 < Math.min(-54, n)) {
                return fluidStatus;
            }
            return fluidStatus2;
        };
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(blender, randomState, structureManager, chunkAccess);
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess2) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunkAccess2);
        chunkAccess2.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(randomState.router(), this.settings.value().spawnTarget()));
    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(chunkAccess, randomState, Beardifier.forStructuresInChunk(structureManager, chunkAccess.getPos()), this.settings.value(), this.globalFluidPicker.get(), blender);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.settings;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return this.settings.is(resourceKey);
    }

    @Override
    public int getBaseHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.iterateNoiseColumn(levelHeightAccessor, randomState, n, n2, null, types.isOpaque()).orElse(levelHeightAccessor.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int n, int n2, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        MutableObject mutableObject = new MutableObject();
        this.iterateNoiseColumn(levelHeightAccessor, randomState, n, n2, (MutableObject<NoiseColumn>)mutableObject, null);
        return (NoiseColumn)mutableObject.getValue();
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        NoiseRouter noiseRouter = randomState.router();
        DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        double d = noiseRouter.ridges().compute(singlePointContext);
        list.add("NoiseRouter T: " + decimalFormat.format(noiseRouter.temperature().compute(singlePointContext)) + " V: " + decimalFormat.format(noiseRouter.vegetation().compute(singlePointContext)) + " C: " + decimalFormat.format(noiseRouter.continents().compute(singlePointContext)) + " E: " + decimalFormat.format(noiseRouter.erosion().compute(singlePointContext)) + " D: " + decimalFormat.format(noiseRouter.depth().compute(singlePointContext)) + " W: " + decimalFormat.format(d) + " PV: " + decimalFormat.format(NoiseRouterData.peaksAndValleys((float)d)) + " AS: " + decimalFormat.format(noiseRouter.initialDensityWithoutJaggedness().compute(singlePointContext)) + " N: " + decimalFormat.format(noiseRouter.finalDensity().compute(singlePointContext)));
    }

    private OptionalInt iterateNoiseColumn(LevelHeightAccessor levelHeightAccessor, RandomState randomState, int n, int n2, @Nullable MutableObject<NoiseColumn> mutableObject, @Nullable Predicate<BlockState> predicate) {
        BlockState[] blockStateArray;
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(levelHeightAccessor);
        int n3 = noiseSettings.getCellHeight();
        int n4 = noiseSettings.minY();
        int n5 = Mth.floorDiv(n4, n3);
        int n6 = Mth.floorDiv(noiseSettings.height(), n3);
        if (n6 <= 0) {
            return OptionalInt.empty();
        }
        if (mutableObject == null) {
            blockStateArray = null;
        } else {
            blockStateArray = new BlockState[noiseSettings.height()];
            mutableObject.setValue((Object)new NoiseColumn(n4, blockStateArray));
        }
        int n7 = noiseSettings.getCellWidth();
        int n8 = Math.floorDiv(n, n7);
        int n9 = Math.floorDiv(n2, n7);
        int n10 = Math.floorMod(n, n7);
        int n11 = Math.floorMod(n2, n7);
        int n12 = n8 * n7;
        int n13 = n9 * n7;
        double d = (double)n10 / (double)n7;
        double d2 = (double)n11 / (double)n7;
        NoiseChunk noiseChunk = new NoiseChunk(1, randomState, n12, n13, noiseSettings, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker.get(), Blender.empty());
        noiseChunk.initializeForFirstCellX();
        noiseChunk.advanceCellX(0);
        for (int i = n6 - 1; i >= 0; --i) {
            noiseChunk.selectCellYZ(i, 0);
            for (int j = n3 - 1; j >= 0; --j) {
                BlockState blockState;
                int n14 = (n5 + i) * n3 + j;
                double d3 = (double)j / (double)n3;
                noiseChunk.updateForY(n14, d3);
                noiseChunk.updateForX(n, d);
                noiseChunk.updateForZ(n2, d2);
                BlockState blockState2 = noiseChunk.getInterpolatedState();
                BlockState blockState3 = blockState = blockState2 == null ? this.settings.value().defaultBlock() : blockState2;
                if (blockStateArray != null) {
                    int n15 = i * n3 + j;
                    blockStateArray[n15] = blockState;
                }
                if (predicate == null || !predicate.test(blockState)) continue;
                noiseChunk.stopInterpolation();
                return OptionalInt.of(n14 + 1);
            }
        }
        noiseChunk.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
        if (SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
            return;
        }
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
        this.buildSurface(chunkAccess, worldGenerationContext, randomState, structureManager, worldGenRegion.getBiomeManager(), worldGenRegion.registryAccess().registryOrThrow(Registries.BIOME), Blender.of(worldGenRegion));
    }

    @VisibleForTesting
    public void buildSurface(ChunkAccess chunkAccess2, WorldGenerationContext worldGenerationContext, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> registry, Blender blender) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
        randomState.surfaceSystem().buildSurface(randomState, biomeManager, registry, noiseGeneratorSettings.useLegacyRandomSource(), worldGenerationContext, chunkAccess2, noiseChunk, noiseGeneratorSettings.surfaceRule());
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess2, GenerationStep.Carving carving) {
        BiomeManager biomeManager2 = biomeManager.withDifferentSource((n, n2, n3) -> this.biomeSource.getNoiseBiome(n, n2, n3, randomState.sampler()));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        int n4 = 8;
        ChunkPos chunkPos = chunkAccess2.getPos();
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, Blender.of(worldGenRegion), randomState));
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext(this, worldGenRegion.registryAccess(), chunkAccess2.getHeightAccessorForGeneration(), noiseChunk, randomState, this.settings.value().surfaceRule());
        CarvingMask carvingMask = ((ProtoChunk)chunkAccess2).getOrCreateCarvingMask(carving);
        for (int i = -8; i <= 8; ++i) {
            for (int j = -8; j <= 8; ++j) {
                ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + i, chunkPos.z + j);
                ChunkAccess chunkAccess3 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
                BiomeGenerationSettings biomeGenerationSettings = chunkAccess3.carverBiome(() -> this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), randomState.sampler())));
                Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers(carving);
                int n5 = 0;
                for (Holder<ConfiguredWorldCarver<?>> holder : iterable) {
                    ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                    worldgenRandom.setLargeFeatureSeed(l + (long)n5, chunkPos2.x, chunkPos2.z);
                    if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                        configuredWorldCarver.carve(carvingContext, chunkAccess2, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
                    }
                    ++n5;
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunkAccess.getHeightAccessorForGeneration());
        int n = noiseSettings.minY();
        int n2 = Mth.floorDiv(n, noiseSettings.getCellHeight());
        int n3 = Mth.floorDiv(noiseSettings.height(), noiseSettings.getCellHeight());
        if (n3 <= 0) {
            return CompletableFuture.completedFuture(chunkAccess);
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {
            int n4 = chunkAccess.getSectionIndex(n3 * noiseSettings.getCellHeight() - 1 + n);
            int n5 = chunkAccess.getSectionIndex(n);
            HashSet hashSet = Sets.newHashSet();
            for (int i = n4; i >= n5; --i) {
                LevelChunkSection levelChunkSection = chunkAccess.getSection(i);
                levelChunkSection.acquire();
                hashSet.add(levelChunkSection);
            }
            try {
                ChunkAccess chunkAccess2 = this.doFill(blender, structureManager, randomState, chunkAccess, n2, n3);
                return chunkAccess2;
            }
            finally {
                for (LevelChunkSection levelChunkSection : hashSet) {
                    levelChunkSection.release();
                }
            }
        }), Util.backgroundExecutor());
    }

    private ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess2, int n, int n2) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        Heightmap heightmap = chunkAccess2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess2.getPos();
        int n3 = chunkPos.getMinBlockX();
        int n4 = chunkPos.getMinBlockZ();
        Aquifer aquifer = noiseChunk.aquifer();
        noiseChunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n5 = noiseChunk.cellWidth();
        int n6 = noiseChunk.cellHeight();
        int n7 = 16 / n5;
        int n8 = 16 / n5;
        for (int i = 0; i < n7; ++i) {
            noiseChunk.advanceCellX(i);
            for (int j = 0; j < n8; ++j) {
                int n9 = chunkAccess2.getSectionsCount() - 1;
                LevelChunkSection levelChunkSection = chunkAccess2.getSection(n9);
                for (int k = n2 - 1; k >= 0; --k) {
                    noiseChunk.selectCellYZ(k, j);
                    for (int i2 = n6 - 1; i2 >= 0; --i2) {
                        int n10 = (n + k) * n6 + i2;
                        int n11 = n10 & 0xF;
                        int n12 = chunkAccess2.getSectionIndex(n10);
                        if (n9 != n12) {
                            n9 = n12;
                            levelChunkSection = chunkAccess2.getSection(n12);
                        }
                        double d = (double)i2 / (double)n6;
                        noiseChunk.updateForY(n10, d);
                        for (int i3 = 0; i3 < n5; ++i3) {
                            int n13 = n3 + i * n5 + i3;
                            int n14 = n13 & 0xF;
                            double d2 = (double)i3 / (double)n5;
                            noiseChunk.updateForX(n13, d2);
                            for (int i4 = 0; i4 < n5; ++i4) {
                                int n15 = n4 + j * n5 + i4;
                                int n16 = n15 & 0xF;
                                double d3 = (double)i4 / (double)n5;
                                noiseChunk.updateForZ(n15, d3);
                                BlockState blockState = noiseChunk.getInterpolatedState();
                                if (blockState == null) {
                                    blockState = this.settings.value().defaultBlock();
                                }
                                if ((blockState = this.debugPreliminarySurfaceLevel(noiseChunk, n13, n10, n15, blockState)) == AIR || SharedConstants.debugVoidTerrain(chunkAccess2.getPos())) continue;
                                levelChunkSection.setBlockState(n14, n11, n16, blockState, false);
                                heightmap.update(n14, n10, n16, blockState);
                                heightmap2.update(n14, n10, n16, blockState);
                                if (!aquifer.shouldScheduleFluidUpdate() || blockState.getFluidState().isEmpty()) continue;
                                mutableBlockPos.set(n13, n10, n15);
                                chunkAccess2.markPosForPostprocessing(mutableBlockPos);
                            }
                        }
                    }
                }
            }
            noiseChunk.swapSlices();
        }
        noiseChunk.stopInterpolation();
        return chunkAccess2;
    }

    private BlockState debugPreliminarySurfaceLevel(NoiseChunk noiseChunk, int n, int n2, int n3, BlockState blockState) {
        return blockState;
    }

    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        if (this.settings.value().disableMobGeneration()) {
            return;
        }
        ChunkPos chunkPos = worldGenRegion.getCenter();
        Holder<Biome> holder = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxBuildHeight() - 1));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, holder, chunkPos, worldgenRandom);
    }
}

