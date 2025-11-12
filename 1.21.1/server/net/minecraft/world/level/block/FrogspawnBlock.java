/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock
extends Block {
    public static final MapCodec<FrogspawnBlock> CODEC = FrogspawnBlock.simpleCodec(FrogspawnBlock::new);
    private static final int MIN_TADPOLES_SPAWN = 2;
    private static final int MAX_TADPOLES_SPAWN = 5;
    private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
    private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);
    private static int minHatchTickDelay = 3600;
    private static int maxHatchTickDelay = 12000;

    public MapCodec<FrogspawnBlock> codec() {
        return CODEC;
    }

    public FrogspawnBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return FrogspawnBlock.mayPlaceOn(levelReader, blockPos.below());
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        level.scheduleTick(blockPos, this, FrogspawnBlock.getFrogspawnHatchDelay(level.getRandom()));
    }

    private static int getFrogspawnHatchDelay(RandomSource randomSource) {
        return randomSource.nextInt(minHatchTickDelay, maxHatchTickDelay);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!this.canSurvive(blockState, levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!this.canSurvive(blockState, serverLevel, blockPos)) {
            this.destroyBlock(serverLevel, blockPos);
            return;
        }
        this.hatchFrogspawn(serverLevel, blockPos, randomSource);
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (entity.getType().equals(EntityType.FALLING_BLOCK)) {
            this.destroyBlock(level, blockPos);
        }
    }

    private static boolean mayPlaceOn(BlockGetter blockGetter, BlockPos blockPos) {
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        FluidState fluidState2 = blockGetter.getFluidState(blockPos.above());
        return fluidState.getType() == Fluids.WATER && fluidState2.getType() == Fluids.EMPTY;
    }

    private void hatchFrogspawn(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        this.destroyBlock(serverLevel, blockPos);
        serverLevel.playSound(null, blockPos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0f, 1.0f);
        this.spawnTadpoles(serverLevel, blockPos, randomSource);
    }

    private void destroyBlock(Level level, BlockPos blockPos) {
        level.destroyBlock(blockPos, false);
    }

    private void spawnTadpoles(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int n = randomSource.nextInt(2, 6);
        for (int i = 1; i <= n; ++i) {
            Tadpole tadpole = EntityType.TADPOLE.create(serverLevel);
            if (tadpole == null) continue;
            double d = (double)blockPos.getX() + this.getRandomTadpolePositionOffset(randomSource);
            double d2 = (double)blockPos.getZ() + this.getRandomTadpolePositionOffset(randomSource);
            int n2 = randomSource.nextInt(1, 361);
            tadpole.moveTo(d, (double)blockPos.getY() - 0.5, d2, n2, 0.0f);
            tadpole.setPersistenceRequired();
            serverLevel.addFreshEntity(tadpole);
        }
    }

    private double getRandomTadpolePositionOffset(RandomSource randomSource) {
        double d = 0.2f;
        return Mth.clamp(randomSource.nextDouble(), (double)0.2f, 0.7999999970197678);
    }

    @VisibleForTesting
    public static void setHatchDelay(int n, int n2) {
        minHatchTickDelay = n;
        maxHatchTickDelay = n2;
    }

    @VisibleForTesting
    public static void setDefaultHatchDelay() {
        minHatchTickDelay = 3600;
        maxHatchTickDelay = 12000;
    }
}

