/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireHookBlock
extends Block {
    public static final MapCodec<TripWireHookBlock> CODEC = TripWireHookBlock.simpleCodec(TripWireHookBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final int WIRE_DIST_MIN = 1;
    protected static final int WIRE_DIST_MAX = 42;
    private static final int RECHECK_PERIOD = 10;
    protected static final int AABB_OFFSET = 3;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
    protected static final VoxelShape WEST_AABB = Block.box(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);

    public MapCodec<TripWireHookBlock> codec() {
        return CODEC;
    }

    public TripWireHookBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(ATTACHED, false));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (blockState.getValue(FACING)) {
            default: {
                return EAST_AABB;
            }
            case WEST: {
                return WEST_AABB;
            }
            case SOUTH: {
                return SOUTH_AABB;
            }
            case NORTH: 
        }
        return NORTH_AABB;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        return direction.getAxis().isHorizontal() && blockState2.isFaceSturdy(levelReader, blockPos2, direction);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction[] directionArray;
        BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(POWERED, false)).setValue(ATTACHED, false);
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        for (Direction direction : directionArray = blockPlaceContext.getNearestLookingDirections()) {
            Direction direction2;
            if (!direction.getAxis().isHorizontal() || !(blockState = (BlockState)blockState.setValue(FACING, direction2 = direction.getOpposite())).canSurvive(level, blockPos)) continue;
            return blockState;
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        TripWireHookBlock.calculateState(level, blockPos, blockState, false, false, -1, null);
    }

    public static void calculateState(Level level, BlockPos blockPos, BlockState blockState, boolean bl, boolean bl2, int n, @Nullable BlockState blockState2) {
        Object object;
        BlockPos blockPos2;
        Optional<Direction> optional = blockState.getOptionalValue(FACING);
        if (!optional.isPresent()) {
            return;
        }
        Direction direction = optional.get();
        boolean bl3 = blockState.getOptionalValue(ATTACHED).orElse(false);
        boolean bl4 = blockState.getOptionalValue(POWERED).orElse(false);
        Block block = blockState.getBlock();
        boolean bl5 = !bl;
        boolean bl6 = false;
        int n2 = 0;
        BlockState[] blockStateArray = new BlockState[42];
        for (int i = 1; i < 42; ++i) {
            blockPos2 = blockPos.relative(direction, i);
            object = level.getBlockState(blockPos2);
            if (((BlockBehaviour.BlockStateBase)object).is(Blocks.TRIPWIRE_HOOK)) {
                if (((StateHolder)object).getValue(FACING) != direction.getOpposite()) break;
                n2 = i;
                break;
            }
            if (((BlockBehaviour.BlockStateBase)object).is(Blocks.TRIPWIRE) || i == n) {
                if (i == n) {
                    object = (BlockState)MoreObjects.firstNonNull((Object)blockState2, (Object)object);
                }
                boolean bl7 = ((StateHolder)object).getValue(TripWireBlock.DISARMED) == false;
                boolean bl8 = ((StateHolder)object).getValue(TripWireBlock.POWERED);
                bl6 |= bl7 && bl8;
                blockStateArray[i] = object;
                if (i != n) continue;
                level.scheduleTick(blockPos, block, 10);
                bl5 &= bl7;
                continue;
            }
            blockStateArray[i] = null;
            bl5 = false;
        }
        BlockState blockState3 = (BlockState)((BlockState)block.defaultBlockState().trySetValue(ATTACHED, bl5)).trySetValue(POWERED, bl6 &= (bl5 &= n2 > 1));
        if (n2 > 0) {
            blockPos2 = blockPos.relative(direction, n2);
            object = direction.getOpposite();
            level.setBlock(blockPos2, (BlockState)blockState3.setValue(FACING, object), 3);
            TripWireHookBlock.notifyNeighbors(block, level, blockPos2, (Direction)object);
            TripWireHookBlock.emitState(level, blockPos2, bl5, bl6, bl3, bl4);
        }
        TripWireHookBlock.emitState(level, blockPos, bl5, bl6, bl3, bl4);
        if (!bl) {
            level.setBlock(blockPos, (BlockState)blockState3.setValue(FACING, direction), 3);
            if (bl2) {
                TripWireHookBlock.notifyNeighbors(block, level, blockPos, direction);
            }
        }
        if (bl3 != bl5) {
            for (int i = 1; i < n2; ++i) {
                object = blockPos.relative(direction, i);
                BlockState blockState4 = blockStateArray[i];
                if (blockState4 == null) continue;
                level.setBlock((BlockPos)object, (BlockState)blockState4.trySetValue(ATTACHED, bl5), 3);
                if (level.getBlockState((BlockPos)object).isAir()) continue;
            }
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        TripWireHookBlock.calculateState(serverLevel, blockPos, blockState, false, true, -1, null);
    }

    private static void emitState(Level level, BlockPos blockPos, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        if (bl2 && !bl4) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4f, 0.6f);
            level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, blockPos);
        } else if (!bl2 && bl4) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4f, 0.5f);
            level.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, blockPos);
        } else if (bl && !bl3) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4f, 0.7f);
            level.gameEvent(null, GameEvent.BLOCK_ATTACH, blockPos);
        } else if (!bl && bl3) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4f, 1.2f / (level.random.nextFloat() * 0.2f + 0.9f));
            level.gameEvent(null, GameEvent.BLOCK_DETACH, blockPos);
        }
    }

    private static void notifyNeighbors(Block block, Level level, BlockPos blockPos, Direction direction) {
        level.updateNeighborsAt(blockPos, block);
        level.updateNeighborsAt(blockPos.relative(direction.getOpposite()), block);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl || blockState.is(blockState2.getBlock())) {
            return;
        }
        boolean bl2 = blockState.getValue(ATTACHED);
        boolean bl3 = blockState.getValue(POWERED);
        if (bl2 || bl3) {
            TripWireHookBlock.calculateState(level, blockPos, blockState, true, false, -1, null);
        }
        if (bl3) {
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.relative(blockState.getValue(FACING).getOpposite()), this);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return 0;
        }
        if (blockState.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, ATTACHED);
    }
}

