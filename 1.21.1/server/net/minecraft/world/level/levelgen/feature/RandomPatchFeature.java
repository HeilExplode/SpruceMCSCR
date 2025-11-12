/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature
extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfiguration> featurePlaceContext) {
        RandomPatchConfiguration randomPatchConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        int n = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n2 = randomPatchConfiguration.xzSpread() + 1;
        int n3 = randomPatchConfiguration.ySpread() + 1;
        for (int i = 0; i < randomPatchConfiguration.tries(); ++i) {
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(n2) - randomSource.nextInt(n2), randomSource.nextInt(n3) - randomSource.nextInt(n3), randomSource.nextInt(n2) - randomSource.nextInt(n2));
            if (!randomPatchConfiguration.feature().value().place(worldGenLevel, featurePlaceContext.chunkGenerator(), randomSource, mutableBlockPos)) continue;
            ++n;
        }
        return n > 0;
    }
}

