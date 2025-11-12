/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature
extends Feature<OreConfiguration> {
    public OreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> featurePlaceContext) {
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        OreConfiguration oreConfiguration = featurePlaceContext.config();
        float f = randomSource.nextFloat() * (float)Math.PI;
        float f2 = (float)oreConfiguration.size / 8.0f;
        int n = Mth.ceil(((float)oreConfiguration.size / 16.0f * 2.0f + 1.0f) / 2.0f);
        double d = (double)blockPos.getX() + Math.sin(f) * (double)f2;
        double d2 = (double)blockPos.getX() - Math.sin(f) * (double)f2;
        double d3 = (double)blockPos.getZ() + Math.cos(f) * (double)f2;
        double d4 = (double)blockPos.getZ() - Math.cos(f) * (double)f2;
        int n2 = 2;
        double d5 = blockPos.getY() + randomSource.nextInt(3) - 2;
        double d6 = blockPos.getY() + randomSource.nextInt(3) - 2;
        int n3 = blockPos.getX() - Mth.ceil(f2) - n;
        int n4 = blockPos.getY() - 2 - n;
        int n5 = blockPos.getZ() - Mth.ceil(f2) - n;
        int n6 = 2 * (Mth.ceil(f2) + n);
        int n7 = 2 * (2 + n);
        for (int i = n3; i <= n3 + n6; ++i) {
            for (int j = n5; j <= n5 + n6; ++j) {
                if (n4 > worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, i, j)) continue;
                return this.doPlace(worldGenLevel, randomSource, oreConfiguration, d, d2, d3, d4, d5, d6, n3, n4, n5, n6, n7);
            }
        }
        return false;
    }

    protected boolean doPlace(WorldGenLevel worldGenLevel, RandomSource randomSource, OreConfiguration oreConfiguration, double d, double d2, double d3, double d4, double d5, double d6, int n, int n2, int n3, int n4, int n5) {
        double d7;
        double d8;
        double d9;
        double d10;
        int n6;
        int n7 = 0;
        BitSet bitSet = new BitSet(n4 * n5 * n4);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n8 = oreConfiguration.size;
        double[] dArray = new double[n8 * 4];
        for (n6 = 0; n6 < n8; ++n6) {
            float f = (float)n6 / (float)n8;
            d10 = Mth.lerp((double)f, d, d2);
            d9 = Mth.lerp((double)f, d5, d6);
            d8 = Mth.lerp((double)f, d3, d4);
            d7 = randomSource.nextDouble() * (double)n8 / 16.0;
            double d11 = ((double)(Mth.sin((float)Math.PI * f) + 1.0f) * d7 + 1.0) / 2.0;
            dArray[n6 * 4 + 0] = d10;
            dArray[n6 * 4 + 1] = d9;
            dArray[n6 * 4 + 2] = d8;
            dArray[n6 * 4 + 3] = d11;
        }
        for (n6 = 0; n6 < n8 - 1; ++n6) {
            if (dArray[n6 * 4 + 3] <= 0.0) continue;
            for (int i = n6 + 1; i < n8; ++i) {
                if (dArray[i * 4 + 3] <= 0.0 || !((d7 = dArray[n6 * 4 + 3] - dArray[i * 4 + 3]) * d7 > (d10 = dArray[n6 * 4 + 0] - dArray[i * 4 + 0]) * d10 + (d9 = dArray[n6 * 4 + 1] - dArray[i * 4 + 1]) * d9 + (d8 = dArray[n6 * 4 + 2] - dArray[i * 4 + 2]) * d8)) continue;
                if (d7 > 0.0) {
                    dArray[i * 4 + 3] = -1.0;
                    continue;
                }
                dArray[n6 * 4 + 3] = -1.0;
            }
        }
        try (BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(worldGenLevel);){
            for (int i = 0; i < n8; ++i) {
                d10 = dArray[i * 4 + 3];
                if (d10 < 0.0) continue;
                d9 = dArray[i * 4 + 0];
                d8 = dArray[i * 4 + 1];
                d7 = dArray[i * 4 + 2];
                int n9 = Math.max(Mth.floor(d9 - d10), n);
                int n10 = Math.max(Mth.floor(d8 - d10), n2);
                int n11 = Math.max(Mth.floor(d7 - d10), n3);
                int n12 = Math.max(Mth.floor(d9 + d10), n9);
                int n13 = Math.max(Mth.floor(d8 + d10), n10);
                int n14 = Math.max(Mth.floor(d7 + d10), n11);
                for (int j = n9; j <= n12; ++j) {
                    double d12 = ((double)j + 0.5 - d9) / d10;
                    if (!(d12 * d12 < 1.0)) continue;
                    for (int k = n10; k <= n13; ++k) {
                        double d13 = ((double)k + 0.5 - d8) / d10;
                        if (!(d12 * d12 + d13 * d13 < 1.0)) continue;
                        block11: for (int i2 = n11; i2 <= n14; ++i2) {
                            LevelChunkSection levelChunkSection;
                            int n15;
                            double d14 = ((double)i2 + 0.5 - d7) / d10;
                            if (!(d12 * d12 + d13 * d13 + d14 * d14 < 1.0) || worldGenLevel.isOutsideBuildHeight(k) || bitSet.get(n15 = j - n + (k - n2) * n4 + (i2 - n3) * n4 * n5)) continue;
                            bitSet.set(n15);
                            mutableBlockPos.set(j, k, i2);
                            if (!worldGenLevel.ensureCanWrite(mutableBlockPos) || (levelChunkSection = bulkSectionAccess.getSection(mutableBlockPos)) == null) continue;
                            int n16 = SectionPos.sectionRelative(j);
                            int n17 = SectionPos.sectionRelative(k);
                            int n18 = SectionPos.sectionRelative(i2);
                            BlockState blockState = levelChunkSection.getBlockState(n16, n17, n18);
                            for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
                                if (!OreFeature.canPlaceOre(blockState, bulkSectionAccess::getBlockState, randomSource, oreConfiguration, targetBlockState, mutableBlockPos)) continue;
                                levelChunkSection.setBlockState(n16, n17, n18, targetBlockState.state, false);
                                ++n7;
                                continue block11;
                            }
                        }
                    }
                }
            }
        }
        return n7 > 0;
    }

    public static boolean canPlaceOre(BlockState blockState, Function<BlockPos, BlockState> function, RandomSource randomSource, OreConfiguration oreConfiguration, OreConfiguration.TargetBlockState targetBlockState, BlockPos.MutableBlockPos mutableBlockPos) {
        if (!targetBlockState.target.test(blockState, randomSource)) {
            return false;
        }
        if (OreFeature.shouldSkipAirCheck(randomSource, oreConfiguration.discardChanceOnAirExposure)) {
            return true;
        }
        return !OreFeature.isAdjacentToAir(function, mutableBlockPos);
    }

    protected static boolean shouldSkipAirCheck(RandomSource randomSource, float f) {
        if (f <= 0.0f) {
            return true;
        }
        if (f >= 1.0f) {
            return false;
        }
        return randomSource.nextFloat() >= f;
    }
}

