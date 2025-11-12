/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock
extends Block {
    protected static final VoxelShape PRESSED_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    protected static final AABB TOUCH_AABB = new AABB(0.0625, 0.0, 0.0625, 0.9375, 0.25, 0.9375);
    protected final BlockSetType type;

    protected BasePressurePlateBlock(BlockBehaviour.Properties properties, BlockSetType blockSetType) {
        super(properties.sound(blockSetType.soundType()));
        this.type = blockSetType;
    }

    protected abstract MapCodec<? extends BasePressurePlateBlock> codec();

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getSignalForState(blockState) > 0 ? PRESSED_AABB : AABB;
    }

    protected int getPressedTime() {
        return 20;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        return BasePressurePlateBlock.canSupportRigidBlock(levelReader, blockPos2) || BasePressurePlateBlock.canSupportCenter(levelReader, blockPos2, Direction.UP);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int n = this.getSignalForState(blockState);
        if (n > 0) {
            this.checkPressed(null, serverLevel, blockPos, blockState, n);
        }
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        int n = this.getSignalForState(blockState);
        if (n == 0) {
            this.checkPressed(entity, level, blockPos, blockState, n);
        }
    }

    private void checkPressed(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, int n) {
        boolean bl;
        int n2 = this.getSignalStrength(level, blockPos);
        boolean bl2 = n > 0;
        boolean bl3 = bl = n2 > 0;
        if (n != n2) {
            BlockState blockState2 = this.setSignalForState(blockState, n2);
            level.setBlock(blockPos, blockState2, 2);
            this.updateNeighbours(level, blockPos);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (!bl && bl2) {
            level.playSound(null, blockPos, this.type.pressurePlateClickOff(), SoundSource.BLOCKS);
            level.gameEvent(entity, GameEvent.BLOCK_DEACTIVATE, blockPos);
        } else if (bl && !bl2) {
            level.playSound(null, blockPos, this.type.pressurePlateClickOn(), SoundSource.BLOCKS);
            level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, blockPos);
        }
        if (bl) {
            level.scheduleTick(new BlockPos(blockPos), this, this.getPressedTime());
        }
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl || blockState.is(blockState2.getBlock())) {
            return;
        }
        if (this.getSignalForState(blockState) > 0) {
            this.updateNeighbours(level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    protected void updateNeighbours(Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, this);
        level.updateNeighborsAt(blockPos.below(), this);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return this.getSignalForState(blockState);
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.UP) {
            return this.getSignalForState(blockState);
        }
        return 0;
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    protected static int getEntityCount(Level level, AABB aABB, Class<? extends Entity> clazz) {
        return level.getEntitiesOfClass(clazz, aABB, EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers())).size();
    }

    protected abstract int getSignalStrength(Level var1, BlockPos var2);

    protected abstract int getSignalForState(BlockState var1);

    protected abstract BlockState setSignalForState(BlockState var1, int var2);
}

