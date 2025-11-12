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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class EndRodBlock
extends RodBlock {
    public static final MapCodec<EndRodBlock> CODEC = EndRodBlock.simpleCodec(EndRodBlock::new);

    public MapCodec<EndRodBlock> codec() {
        return CODEC;
    }

    protected EndRodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction.getOpposite()));
        if (blockState.is(this) && blockState.getValue(FACING) == direction) {
            return (BlockState)this.defaultBlockState().setValue(FACING, direction.getOpposite());
        }
        return (BlockState)this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        Direction direction = blockState.getValue(FACING);
        double d = (double)blockPos.getX() + 0.55 - (double)(randomSource.nextFloat() * 0.1f);
        double d2 = (double)blockPos.getY() + 0.55 - (double)(randomSource.nextFloat() * 0.1f);
        double d3 = (double)blockPos.getZ() + 0.55 - (double)(randomSource.nextFloat() * 0.1f);
        double d4 = 0.4f - (randomSource.nextFloat() + randomSource.nextFloat()) * 0.4f;
        if (randomSource.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD, d + (double)direction.getStepX() * d4, d2 + (double)direction.getStepY() * d4, d3 + (double)direction.getStepZ() * d4, randomSource.nextGaussian() * 0.005, randomSource.nextGaussian() * 0.005, randomSource.nextGaussian() * 0.005);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}

