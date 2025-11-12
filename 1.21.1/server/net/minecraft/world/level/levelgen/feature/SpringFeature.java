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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

public class SpringFeature
extends Feature<SpringConfiguration> {
    public SpringFeature(Codec<SpringConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpringConfiguration> featurePlaceContext) {
        BlockPos blockPos;
        SpringConfiguration springConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        if (!worldGenLevel.getBlockState((blockPos = featurePlaceContext.origin()).above()).is(springConfiguration.validBlocks)) {
            return false;
        }
        if (springConfiguration.requiresBlockBelow && !worldGenLevel.getBlockState(blockPos.below()).is(springConfiguration.validBlocks)) {
            return false;
        }
        BlockState blockState = worldGenLevel.getBlockState(blockPos);
        if (!blockState.isAir() && !blockState.is(springConfiguration.validBlocks)) {
            return false;
        }
        int n = 0;
        int n2 = 0;
        if (worldGenLevel.getBlockState(blockPos.west()).is(springConfiguration.validBlocks)) {
            ++n2;
        }
        if (worldGenLevel.getBlockState(blockPos.east()).is(springConfiguration.validBlocks)) {
            ++n2;
        }
        if (worldGenLevel.getBlockState(blockPos.north()).is(springConfiguration.validBlocks)) {
            ++n2;
        }
        if (worldGenLevel.getBlockState(blockPos.south()).is(springConfiguration.validBlocks)) {
            ++n2;
        }
        if (worldGenLevel.getBlockState(blockPos.below()).is(springConfiguration.validBlocks)) {
            ++n2;
        }
        int n3 = 0;
        if (worldGenLevel.isEmptyBlock(blockPos.west())) {
            ++n3;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.east())) {
            ++n3;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.north())) {
            ++n3;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.south())) {
            ++n3;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.below())) {
            ++n3;
        }
        if (n2 == springConfiguration.rockCount && n3 == springConfiguration.holeCount) {
            worldGenLevel.setBlock(blockPos, springConfiguration.state.createLegacyBlock(), 2);
            worldGenLevel.scheduleTick(blockPos, springConfiguration.state.getType(), 0);
            ++n;
        }
        return n > 0;
    }
}

