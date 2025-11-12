/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.CoralFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralTreeFeature
extends CoralFeature {
    public CoralTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean placeFeature(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        int n = randomSource.nextInt(3) + 1;
        for (int i = 0; i < n; ++i) {
            if (!this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState)) {
                return true;
            }
            mutableBlockPos.move(Direction.UP);
        }
        BlockPos blockPos2 = mutableBlockPos.immutable();
        int n2 = randomSource.nextInt(3) + 2;
        List<Direction> list = Direction.Plane.HORIZONTAL.shuffledCopy(randomSource);
        List<Direction> list2 = list.subList(0, n2);
        for (Direction direction : list2) {
            mutableBlockPos.set(blockPos2);
            mutableBlockPos.move(direction);
            int n3 = randomSource.nextInt(5) + 2;
            int n4 = 0;
            for (int i = 0; i < n3 && this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState); ++i) {
                mutableBlockPos.move(Direction.UP);
                if (i != 0 && (++n4 < 2 || !(randomSource.nextFloat() < 0.25f))) continue;
                mutableBlockPos.move(direction);
                n4 = 0;
            }
        }
        return true;
    }
}

