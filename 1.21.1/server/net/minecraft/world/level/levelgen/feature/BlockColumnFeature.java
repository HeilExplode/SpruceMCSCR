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
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature
extends Feature<BlockColumnConfiguration> {
    public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> featurePlaceContext) {
        int n;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockColumnConfiguration blockColumnConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        int n2 = blockColumnConfiguration.layers().size();
        int[] nArray = new int[n2];
        int n3 = 0;
        for (int i = 0; i < n2; ++i) {
            nArray[i] = blockColumnConfiguration.layers().get(i).height().sample(randomSource);
            n3 += nArray[i];
        }
        if (n3 == 0) {
            return false;
        }
        BlockPos.MutableBlockPos mutableBlockPos = featurePlaceContext.origin().mutable();
        BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable().move(blockColumnConfiguration.direction());
        for (n = 0; n < n3; ++n) {
            if (!blockColumnConfiguration.allowedPlacement().test(worldGenLevel, mutableBlockPos2)) {
                BlockColumnFeature.truncate(nArray, n3, n, blockColumnConfiguration.prioritizeTip());
                break;
            }
            mutableBlockPos2.move(blockColumnConfiguration.direction());
        }
        for (n = 0; n < n2; ++n) {
            int n4 = nArray[n];
            if (n4 == 0) continue;
            BlockColumnConfiguration.Layer layer = blockColumnConfiguration.layers().get(n);
            for (int i = 0; i < n4; ++i) {
                worldGenLevel.setBlock(mutableBlockPos, layer.state().getState(randomSource, mutableBlockPos), 2);
                mutableBlockPos.move(blockColumnConfiguration.direction());
            }
        }
        return true;
    }

    private static void truncate(int[] nArray, int n, int n2, boolean bl) {
        int n3;
        int n4 = n - n2;
        int n5 = bl ? 1 : -1;
        int n6 = bl ? 0 : nArray.length - 1;
        int n7 = bl ? nArray.length : -1;
        for (int i = n6; i != n7 && n4 > 0; n4 -= n3, i += n5) {
            int n8 = nArray[i];
            n3 = Math.min(n8, n4);
            int n9 = i;
            nArray[n9] = nArray[n9] - n3;
        }
    }
}

