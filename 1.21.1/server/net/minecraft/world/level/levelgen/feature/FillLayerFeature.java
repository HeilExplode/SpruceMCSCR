/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature
extends Feature<LayerConfiguration> {
    public FillLayerFeature(Codec<LayerConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LayerConfiguration> featurePlaceContext) {
        BlockPos blockPos = featurePlaceContext.origin();
        LayerConfiguration layerConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int n = blockPos.getX() + i;
                int n2 = blockPos.getZ() + j;
                int n3 = worldGenLevel.getMinBuildHeight() + layerConfiguration.height;
                mutableBlockPos.set(n, n3, n2);
                if (!worldGenLevel.getBlockState(mutableBlockPos).isAir()) continue;
                worldGenLevel.setBlock(mutableBlockPos, layerConfiguration.state, 2);
            }
        }
        return true;
    }
}

