/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPlatformFeature
extends Feature<NoneFeatureConfiguration> {
    public EndPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        EndPlatformFeature.createEndPlatform(featurePlaceContext.level(), featurePlaceContext.origin(), false);
        return true;
    }

    public static void createEndPlatform(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -1; k < 3; ++k) {
                    Block block;
                    BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.set(blockPos).move(j, k, i);
                    Block block2 = block = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
                    if (serverLevelAccessor.getBlockState(mutableBlockPos2).is(block)) continue;
                    if (bl) {
                        serverLevelAccessor.destroyBlock(mutableBlockPos2, true, null);
                    }
                    serverLevelAccessor.setBlock(mutableBlockPos2, block.defaultBlockState(), 3);
                }
            }
        }
    }
}

