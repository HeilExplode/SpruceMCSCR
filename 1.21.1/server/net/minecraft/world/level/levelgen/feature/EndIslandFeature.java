/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndIslandFeature
extends Feature<NoneFeatureConfiguration> {
    public EndIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        float f = (float)randomSource.nextInt(3) + 4.0f;
        int n = 0;
        while (f > 0.5f) {
            for (int i = Mth.floor(-f); i <= Mth.ceil(f); ++i) {
                for (int j = Mth.floor(-f); j <= Mth.ceil(f); ++j) {
                    if (!((float)(i * i + j * j) <= (f + 1.0f) * (f + 1.0f))) continue;
                    this.setBlock(worldGenLevel, blockPos.offset(i, n, j), Blocks.END_STONE.defaultBlockState());
                }
            }
            f -= (float)randomSource.nextInt(2) + 0.5f;
            --n;
        }
        return true;
    }
}

