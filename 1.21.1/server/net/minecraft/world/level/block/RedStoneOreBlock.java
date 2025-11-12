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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RedStoneOreBlock
extends Block {
    public static final MapCodec<RedStoneOreBlock> CODEC = RedStoneOreBlock.simpleCodec(RedStoneOreBlock::new);
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public MapCodec<RedStoneOreBlock> codec() {
        return CODEC;
    }

    public RedStoneOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        RedStoneOreBlock.interact(blockState, level, blockPos);
        super.attack(blockState, level, blockPos, player);
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        if (!entity.isSteppingCarefully()) {
            RedStoneOreBlock.interact(blockState, level, blockPos);
        }
        super.stepOn(level, blockPos, blockState, entity);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            RedStoneOreBlock.spawnParticles(level, blockPos);
        } else {
            RedStoneOreBlock.interact(blockState, level, blockPos);
        }
        if (itemStack.getItem() instanceof BlockItem && new BlockPlaceContext(player, interactionHand, itemStack, blockHitResult).canPlace()) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.SUCCESS;
    }

    private static void interact(BlockState blockState, Level level, BlockPos blockPos) {
        RedStoneOreBlock.spawnParticles(level, blockPos);
        if (!blockState.getValue(LIT).booleanValue()) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(LIT, true), 3);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(LIT);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(LIT).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(LIT, false), 3);
        }
    }

    @Override
    protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
        if (bl) {
            this.tryDropExperience(serverLevel, blockPos, itemStack, UniformInt.of(1, 5));
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(LIT).booleanValue()) {
            RedStoneOreBlock.spawnParticles(level, blockPos);
        }
    }

    private static void spawnParticles(Level level, BlockPos blockPos) {
        double d = 0.5625;
        RandomSource randomSource = level.random;
        for (Direction direction : Direction.values()) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (level.getBlockState(blockPos2).isSolidRender(level, blockPos2)) continue;
            Direction.Axis axis = direction.getAxis();
            double d2 = axis == Direction.Axis.X ? 0.5 + 0.5625 * (double)direction.getStepX() : (double)randomSource.nextFloat();
            double d3 = axis == Direction.Axis.Y ? 0.5 + 0.5625 * (double)direction.getStepY() : (double)randomSource.nextFloat();
            double d4 = axis == Direction.Axis.Z ? 0.5 + 0.5625 * (double)direction.getStepZ() : (double)randomSource.nextFloat();
            level.addParticle(DustParticleOptions.REDSTONE, (double)blockPos.getX() + d2, (double)blockPos.getY() + d3, (double)blockPos.getZ() + d4, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}

