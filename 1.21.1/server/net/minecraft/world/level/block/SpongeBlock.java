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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SpongeBlock
extends Block {
    public static final MapCodec<SpongeBlock> CODEC = SpongeBlock.simpleCodec(SpongeBlock::new);
    public static final int MAX_DEPTH = 6;
    public static final int MAX_COUNT = 64;
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    public MapCodec<SpongeBlock> codec() {
        return CODEC;
    }

    protected SpongeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        this.tryAbsorbWater(level, blockPos);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        this.tryAbsorbWater(level, blockPos);
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
    }

    protected void tryAbsorbWater(Level level, BlockPos blockPos) {
        if (this.removeWaterBreadthFirstSearch(level, blockPos)) {
            level.setBlock(blockPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
            level.playSound(null, blockPos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    private boolean removeWaterBreadthFirstSearch(Level level, BlockPos blockPos3) {
        return BlockPos.breadthFirstTraversal(blockPos3, 6, 65, (blockPos, consumer) -> {
            for (Direction direction : ALL_DIRECTIONS) {
                consumer.accept(blockPos.relative(direction));
            }
        }, blockPos2 -> {
            BucketPickup bucketPickup;
            if (blockPos2.equals(blockPos3)) {
                return true;
            }
            BlockState blockState = level.getBlockState((BlockPos)blockPos2);
            FluidState fluidState = level.getFluidState((BlockPos)blockPos2);
            if (!fluidState.is(FluidTags.WATER)) {
                return false;
            }
            Block block = blockState.getBlock();
            if (block instanceof BucketPickup && !(bucketPickup = (BucketPickup)((Object)block)).pickupBlock(null, level, (BlockPos)blockPos2, blockState).isEmpty()) {
                return true;
            }
            if (blockState.getBlock() instanceof LiquidBlock) {
                level.setBlock((BlockPos)blockPos2, Blocks.AIR.defaultBlockState(), 3);
            } else if (blockState.is(Blocks.KELP) || blockState.is(Blocks.KELP_PLANT) || blockState.is(Blocks.SEAGRASS) || blockState.is(Blocks.TALL_SEAGRASS)) {
                bucketPickup = blockState.hasBlockEntity() ? level.getBlockEntity((BlockPos)blockPos2) : null;
                SpongeBlock.dropResources(blockState, level, blockPos2, (BlockEntity)((Object)bucketPickup));
                level.setBlock((BlockPos)blockPos2, Blocks.AIR.defaultBlockState(), 3);
            } else {
                return false;
            }
            return true;
        }) > 1;
    }
}

