/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

@Deprecated
public class LakeFeature
extends Feature<Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> featurePlaceContext) {
        int n;
        int n2;
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        Configuration configuration = featurePlaceContext.config();
        if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 4) {
            return false;
        }
        blockPos = blockPos.below(4);
        boolean[] blArray = new boolean[2048];
        int n3 = randomSource.nextInt(4) + 4;
        for (int i = 0; i < n3; ++i) {
            double d = randomSource.nextDouble() * 6.0 + 3.0;
            double d2 = randomSource.nextDouble() * 4.0 + 2.0;
            double d3 = randomSource.nextDouble() * 6.0 + 3.0;
            double d4 = randomSource.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
            double d5 = randomSource.nextDouble() * (8.0 - d2 - 4.0) + 2.0 + d2 / 2.0;
            double d6 = randomSource.nextDouble() * (16.0 - d3 - 2.0) + 1.0 + d3 / 2.0;
            for (int j = 1; j < 15; ++j) {
                for (int k = 1; k < 15; ++k) {
                    for (int i2 = 1; i2 < 7; ++i2) {
                        double d7 = ((double)j - d4) / (d / 2.0);
                        double d8 = ((double)i2 - d5) / (d2 / 2.0);
                        double d9 = ((double)k - d6) / (d3 / 2.0);
                        double d10 = d7 * d7 + d8 * d8 + d9 * d9;
                        if (!(d10 < 1.0)) continue;
                        blArray[(j * 16 + k) * 8 + i2] = true;
                    }
                }
            }
        }
        BlockState blockState = configuration.fluid().getState(randomSource, blockPos);
        for (n2 = 0; n2 < 16; ++n2) {
            for (n = 0; n < 16; ++n) {
                for (int i = 0; i < 8; ++i) {
                    boolean bl;
                    boolean bl2 = bl = !blArray[(n2 * 16 + n) * 8 + i] && (n2 < 15 && blArray[((n2 + 1) * 16 + n) * 8 + i] || n2 > 0 && blArray[((n2 - 1) * 16 + n) * 8 + i] || n < 15 && blArray[(n2 * 16 + n + 1) * 8 + i] || n > 0 && blArray[(n2 * 16 + (n - 1)) * 8 + i] || i < 7 && blArray[(n2 * 16 + n) * 8 + i + 1] || i > 0 && blArray[(n2 * 16 + n) * 8 + (i - 1)]);
                    if (!bl) continue;
                    BlockState blockState2 = worldGenLevel.getBlockState(blockPos.offset(n2, i, n));
                    if (i >= 4 && blockState2.liquid()) {
                        return false;
                    }
                    if (i >= 4 || blockState2.isSolid() || worldGenLevel.getBlockState(blockPos.offset(n2, i, n)) == blockState) continue;
                    return false;
                }
            }
        }
        for (n2 = 0; n2 < 16; ++n2) {
            for (n = 0; n < 16; ++n) {
                for (int i = 0; i < 8; ++i) {
                    BlockPos blockPos2;
                    if (!blArray[(n2 * 16 + n) * 8 + i] || !this.canReplaceBlock(worldGenLevel.getBlockState(blockPos2 = blockPos.offset(n2, i, n)))) continue;
                    boolean bl = i >= 4;
                    worldGenLevel.setBlock(blockPos2, bl ? AIR : blockState, 2);
                    if (!bl) continue;
                    worldGenLevel.scheduleTick(blockPos2, AIR.getBlock(), 0);
                    this.markAboveForPostProcessing(worldGenLevel, blockPos2);
                }
            }
        }
        BlockState blockState3 = configuration.barrier().getState(randomSource, blockPos);
        if (!blockState3.isAir()) {
            for (n = 0; n < 16; ++n) {
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 8; ++j) {
                        BlockState blockState4;
                        boolean bl;
                        boolean bl3 = bl = !blArray[(n * 16 + i) * 8 + j] && (n < 15 && blArray[((n + 1) * 16 + i) * 8 + j] || n > 0 && blArray[((n - 1) * 16 + i) * 8 + j] || i < 15 && blArray[(n * 16 + i + 1) * 8 + j] || i > 0 && blArray[(n * 16 + (i - 1)) * 8 + j] || j < 7 && blArray[(n * 16 + i) * 8 + j + 1] || j > 0 && blArray[(n * 16 + i) * 8 + (j - 1)]);
                        if (!bl || j >= 4 && randomSource.nextInt(2) == 0 || !(blockState4 = worldGenLevel.getBlockState(blockPos.offset(n, j, i))).isSolid() || blockState4.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) continue;
                        BlockPos blockPos3 = blockPos.offset(n, j, i);
                        worldGenLevel.setBlock(blockPos3, blockState3, 2);
                        this.markAboveForPostProcessing(worldGenLevel, blockPos3);
                    }
                }
            }
        }
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            for (n = 0; n < 16; ++n) {
                for (int i = 0; i < 16; ++i) {
                    int n4 = 4;
                    BlockPos blockPos4 = blockPos.offset(n, 4, i);
                    if (!worldGenLevel.getBiome(blockPos4).value().shouldFreeze(worldGenLevel, blockPos4, false) || !this.canReplaceBlock(worldGenLevel.getBlockState(blockPos4))) continue;
                    worldGenLevel.setBlock(blockPos4, Blocks.ICE.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }

    private boolean canReplaceBlock(BlockState blockState) {
        return !blockState.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration
    {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.CODEC.fieldOf("fluid").forGetter(Configuration::fluid), (App)BlockStateProvider.CODEC.fieldOf("barrier").forGetter(Configuration::barrier)).apply((Applicative)instance, Configuration::new));
    }
}

