/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.NetherWorldCarver;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<CaveCarverConfiguration> CAVE = WorldCarver.register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = WorldCarver.register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> CANYON = WorldCarver.register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Fluid> liquids = ImmutableSet.of((Object)Fluids.WATER);
    private final MapCodec<ConfiguredWorldCarver<C>> configuredCodec;

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String string, F f) {
        return (F)Registry.register(BuiltInRegistries.CARVER, string, f);
    }

    public WorldCarver(Codec<C> codec) {
        this.configuredCodec = codec.fieldOf("config").xmap(this::configured, ConfiguredWorldCarver::config);
    }

    public ConfiguredWorldCarver<C> configured(C c) {
        return new ConfiguredWorldCarver<C>(this, c);
    }

    public MapCodec<ConfiguredWorldCarver<C>> configuredCodec() {
        return this.configuredCodec;
    }

    public int getRange() {
        return 4;
    }

    protected boolean carveEllipsoid(CarvingContext carvingContext, C c, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, Aquifer aquifer, double d, double d2, double d3, double d4, double d5, CarvingMask carvingMask, CarveSkipChecker carveSkipChecker) {
        ChunkPos chunkPos = chunkAccess.getPos();
        double d6 = chunkPos.getMiddleBlockX();
        double d7 = chunkPos.getMiddleBlockZ();
        double d8 = 16.0 + d4 * 2.0;
        if (Math.abs(d - d6) > d8 || Math.abs(d3 - d7) > d8) {
            return false;
        }
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        int n3 = Math.max(Mth.floor(d - d4) - n - 1, 0);
        int n4 = Math.min(Mth.floor(d + d4) - n, 15);
        int n5 = Math.max(Mth.floor(d2 - d5) - 1, carvingContext.getMinGenY() + 1);
        int n6 = chunkAccess.isUpgrading() ? 0 : 7;
        int n7 = Math.min(Mth.floor(d2 + d5) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 1 - n6);
        int n8 = Math.max(Mth.floor(d3 - d4) - n2 - 1, 0);
        int n9 = Math.min(Mth.floor(d3 + d4) - n2, 15);
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int i = n3; i <= n4; ++i) {
            int n10 = chunkPos.getBlockX(i);
            double d9 = ((double)n10 + 0.5 - d) / d4;
            for (int j = n8; j <= n9; ++j) {
                int n11 = chunkPos.getBlockZ(j);
                double d10 = ((double)n11 + 0.5 - d3) / d4;
                if (d9 * d9 + d10 * d10 >= 1.0) continue;
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                for (int k = n7; k > n5; --k) {
                    double d11 = ((double)k - 0.5 - d2) / d5;
                    if (carveSkipChecker.shouldSkip(carvingContext, d9, d11, d10, k) || carvingMask.get(i, k, j) && !WorldCarver.isDebugEnabled(c)) continue;
                    carvingMask.set(i, k, j);
                    mutableBlockPos.set(n10, k, n11);
                    bl |= this.carveBlock(carvingContext, c, chunkAccess, function, carvingMask, mutableBlockPos, mutableBlockPos2, aquifer, mutableBoolean);
                }
            }
        }
        return bl;
    }

    protected boolean carveBlock(CarvingContext carvingContext, C c, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, CarvingMask carvingMask, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, Aquifer aquifer, MutableBoolean mutableBoolean) {
        BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos);
        if (blockState2.is(Blocks.GRASS_BLOCK) || blockState2.is(Blocks.MYCELIUM)) {
            mutableBoolean.setTrue();
        }
        if (!this.canReplaceBlock(c, blockState2) && !WorldCarver.isDebugEnabled(c)) {
            return false;
        }
        BlockState blockState3 = this.getCarveState(carvingContext, c, mutableBlockPos, aquifer);
        if (blockState3 == null) {
            return false;
        }
        chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
        if (aquifer.shouldScheduleFluidUpdate() && !blockState3.getFluidState().isEmpty()) {
            chunkAccess.markPosForPostprocessing(mutableBlockPos);
        }
        if (mutableBoolean.isTrue()) {
            mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, Direction.DOWN);
            if (chunkAccess.getBlockState(mutableBlockPos2).is(Blocks.DIRT)) {
                carvingContext.topMaterial(function, chunkAccess, mutableBlockPos2, !blockState3.getFluidState().isEmpty()).ifPresent(blockState -> {
                    chunkAccess.setBlockState(mutableBlockPos2, (BlockState)blockState, false);
                    if (!blockState.getFluidState().isEmpty()) {
                        chunkAccess.markPosForPostprocessing(mutableBlockPos2);
                    }
                });
            }
        }
        return true;
    }

    @Nullable
    private BlockState getCarveState(CarvingContext carvingContext, C c, BlockPos blockPos, Aquifer aquifer) {
        if (blockPos.getY() <= ((CarverConfiguration)c).lavaLevel.resolveY(carvingContext)) {
            return LAVA.createLegacyBlock();
        }
        BlockState blockState = aquifer.computeSubstance(new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 0.0);
        if (blockState == null) {
            return WorldCarver.isDebugEnabled(c) ? ((CarverConfiguration)c).debugSettings.getBarrierState() : null;
        }
        return WorldCarver.isDebugEnabled(c) ? WorldCarver.getDebugState(c, blockState) : blockState;
    }

    private static BlockState getDebugState(CarverConfiguration carverConfiguration, BlockState blockState) {
        if (blockState.is(Blocks.AIR)) {
            return carverConfiguration.debugSettings.getAirState();
        }
        if (blockState.is(Blocks.WATER)) {
            BlockState blockState2 = carverConfiguration.debugSettings.getWaterState();
            if (blockState2.hasProperty(BlockStateProperties.WATERLOGGED)) {
                return (BlockState)blockState2.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            return blockState2;
        }
        if (blockState.is(Blocks.LAVA)) {
            return carverConfiguration.debugSettings.getLavaState();
        }
        return blockState;
    }

    public abstract boolean carve(CarvingContext var1, C var2, ChunkAccess var3, Function<BlockPos, Holder<Biome>> var4, RandomSource var5, Aquifer var6, ChunkPos var7, CarvingMask var8);

    public abstract boolean isStartChunk(C var1, RandomSource var2);

    protected boolean canReplaceBlock(C c, BlockState blockState) {
        return blockState.is(((CarverConfiguration)c).replaceable);
    }

    protected static boolean canReach(ChunkPos chunkPos, double d, double d2, int n, int n2, float f) {
        double d3;
        double d4;
        double d5;
        double d6;
        double d7 = chunkPos.getMiddleBlockX();
        double d8 = d - d7;
        return d8 * d8 + (d6 = d2 - (d5 = (double)chunkPos.getMiddleBlockZ())) * d6 - (d4 = (double)(n2 - n)) * d4 <= (d3 = (double)(f + 2.0f + 16.0f)) * d3;
    }

    private static boolean isDebugEnabled(CarverConfiguration carverConfiguration) {
        return carverConfiguration.debugSettings.isDebugMode();
    }

    public static interface CarveSkipChecker {
        public boolean shouldSkip(CarvingContext var1, double var2, double var4, double var6, int var8);
    }
}

