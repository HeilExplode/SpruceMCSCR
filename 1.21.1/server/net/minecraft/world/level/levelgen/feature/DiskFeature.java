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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskFeature
extends Feature<DiskConfiguration> {
    public DiskFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
        DiskConfiguration diskConfiguration = featurePlaceContext.config();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        boolean bl = false;
        int n = blockPos.getY();
        int n2 = n + diskConfiguration.halfHeight();
        int n3 = n - diskConfiguration.halfHeight() - 1;
        int n4 = diskConfiguration.radius().sample(randomSource);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-n4, 0, -n4), blockPos.offset(n4, 0, n4))) {
            int n5;
            int n6 = blockPos2.getX() - blockPos.getX();
            if (n6 * n6 + (n5 = blockPos2.getZ() - blockPos.getZ()) * n5 > n4 * n4) continue;
            bl |= this.placeColumn(diskConfiguration, worldGenLevel, randomSource, n2, n3, mutableBlockPos.set(blockPos2));
        }
        return bl;
    }

    protected boolean placeColumn(DiskConfiguration diskConfiguration, WorldGenLevel worldGenLevel, RandomSource randomSource, int n, int n2, BlockPos.MutableBlockPos mutableBlockPos) {
        boolean bl = false;
        for (int i = n; i > n2; --i) {
            mutableBlockPos.setY(i);
            if (!diskConfiguration.target().test(worldGenLevel, mutableBlockPos)) continue;
            BlockState blockState = diskConfiguration.stateProvider().getState(worldGenLevel, randomSource, mutableBlockPos);
            worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
            this.markAboveForPostProcessing(worldGenLevel, mutableBlockPos);
            bl = true;
        }
        return bl;
    }
}

