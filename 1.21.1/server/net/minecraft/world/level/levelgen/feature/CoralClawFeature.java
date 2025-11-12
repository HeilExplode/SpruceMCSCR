/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.CoralFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralClawFeature
extends CoralFeature {
    public CoralClawFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean placeFeature(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        if (!this.placeCoralBlock(levelAccessor, randomSource, blockPos, blockState)) {
            return false;
        }
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        int n = randomSource.nextInt(2) + 2;
        List<Direction> list = Util.toShuffledList(Stream.of(direction, direction.getClockWise(), direction.getCounterClockWise()), randomSource);
        List<Direction> list2 = list.subList(0, n);
        block0: for (Direction direction2 : list2) {
            int n2;
            int n3;
            Direction direction3;
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
            int n4 = randomSource.nextInt(2) + 1;
            mutableBlockPos.move(direction2);
            if (direction2 == direction) {
                direction3 = direction;
                n3 = randomSource.nextInt(3) + 2;
            } else {
                mutableBlockPos.move(Direction.UP);
                Direction[] directionArray = new Direction[]{direction2, Direction.UP};
                direction3 = Util.getRandom(directionArray, randomSource);
                n3 = randomSource.nextInt(3) + 3;
            }
            for (n2 = 0; n2 < n4 && this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState); ++n2) {
                mutableBlockPos.move(direction3);
            }
            mutableBlockPos.move(direction3.getOpposite());
            mutableBlockPos.move(Direction.UP);
            for (n2 = 0; n2 < n3; ++n2) {
                mutableBlockPos.move(direction);
                if (!this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState)) continue block0;
                if (!(randomSource.nextFloat() < 0.25f)) continue;
                mutableBlockPos.move(Direction.UP);
            }
        }
        return true;
    }
}

