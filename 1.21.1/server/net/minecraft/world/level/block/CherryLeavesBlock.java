/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CherryLeavesBlock
extends LeavesBlock {
    public static final MapCodec<CherryLeavesBlock> CODEC = CherryLeavesBlock.simpleCodec(CherryLeavesBlock::new);

    public MapCodec<CherryLeavesBlock> codec() {
        return CODEC;
    }

    public CherryLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);
        if (randomSource.nextInt(10) != 0) {
            return;
        }
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (CherryLeavesBlock.isFaceFull(blockState2.getCollisionShape(level, blockPos2), Direction.UP)) {
            return;
        }
        ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, ParticleTypes.CHERRY_LEAVES);
    }
}

