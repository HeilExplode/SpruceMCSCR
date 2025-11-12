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
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LecternBlock
extends BaseEntityBlock {
    public static final MapCodec<LecternBlock> CODEC = LecternBlock.simpleCodec(LecternBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    public static final VoxelShape SHAPE_BASE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    public static final VoxelShape SHAPE_POST = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
    public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
    public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0, 15.0, 0.0, 16.0, 15.0, 16.0);
    public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
    public static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0), Block.box(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0), Block.box(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0), SHAPE_COMMON);
    public static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333), Block.box(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667), Block.box(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0), SHAPE_COMMON);
    public static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(10.666667, 10.0, 0.0, 15.0, 14.0, 16.0), Block.box(6.333333, 12.0, 0.0, 10.666667, 16.0, 16.0), Block.box(2.0, 14.0, 0.0, 6.333333, 18.0, 16.0), SHAPE_COMMON);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(0.0, 10.0, 10.666667, 16.0, 14.0, 15.0), Block.box(0.0, 12.0, 6.333333, 16.0, 16.0, 10.666667), Block.box(0.0, 14.0, 2.0, 16.0, 18.0, 6.333333), SHAPE_COMMON);
    private static final int PAGE_CHANGE_IMPULSE_TICKS = 2;

    public MapCodec<LecternBlock> codec() {
        return CODEC;
    }

    protected LecternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(HAS_BOOK, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPE_COMMON;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        CustomData customData;
        Level level = blockPlaceContext.getLevel();
        ItemStack itemStack = blockPlaceContext.getItemInHand();
        Player player = blockPlaceContext.getPlayer();
        boolean bl = false;
        if (!level.isClientSide && player != null && player.canUseGameMasterBlocks() && (customData = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY)).contains("Book")) {
            bl = true;
        }
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())).setValue(HAS_BOOK, bl);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_COLLISION;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (blockState.getValue(FACING)) {
            case NORTH: {
                return SHAPE_NORTH;
            }
            case SOUTH: {
                return SHAPE_SOUTH;
            }
            case EAST: {
                return SHAPE_EAST;
            }
            case WEST: {
                return SHAPE_WEST;
            }
        }
        return SHAPE_COMMON;
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
        builder.add(FACING, POWERED, HAS_BOOK);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new LecternBlockEntity(blockPos, blockState);
    }

    public static boolean tryPlaceBook(@Nullable LivingEntity livingEntity, Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        if (!blockState.getValue(HAS_BOOK).booleanValue()) {
            if (!level.isClientSide) {
                LecternBlock.placeBook(livingEntity, level, blockPos, blockState, itemStack);
            }
            return true;
        }
        return false;
    }

    private static void placeBook(@Nullable LivingEntity livingEntity, Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
            lecternBlockEntity.setBook(itemStack.consumeAndReturn(1, livingEntity));
            LecternBlock.resetBookState(livingEntity, level, blockPos, blockState, true);
            level.playSound(null, blockPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public static void resetBookState(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        BlockState blockState2 = (BlockState)((BlockState)blockState.setValue(POWERED, false)).setValue(HAS_BOOK, bl);
        level.setBlock(blockPos, blockState2, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
        LecternBlock.updateBelow(level, blockPos, blockState);
    }

    public static void signalPageChange(Level level, BlockPos blockPos, BlockState blockState) {
        LecternBlock.changePowered(level, blockPos, blockState, true);
        level.scheduleTick(blockPos, blockState.getBlock(), 2);
        level.levelEvent(1043, blockPos, 0);
    }

    private static void changePowered(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, bl), 3);
        LecternBlock.updateBelow(level, blockPos, blockState);
    }

    private static void updateBelow(Level level, BlockPos blockPos, BlockState blockState) {
        level.updateNeighborsAt(blockPos.below(), blockState.getBlock());
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        LecternBlock.changePowered(serverLevel, blockPos, blockState, false);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        if (blockState.getValue(HAS_BOOK).booleanValue()) {
            this.popBook(blockState, level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
        if (blockState.getValue(POWERED).booleanValue()) {
            level.updateNeighborsAt(blockPos.below(), this);
        }
    }

    private void popBook(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
            Direction direction = blockState.getValue(FACING);
            ItemStack itemStack = lecternBlockEntity.getBook().copy();
            float f = 0.25f * (float)direction.getStepX();
            float f2 = 0.25f * (float)direction.getStepZ();
            ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + 0.5 + (double)f, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5 + (double)f2, itemStack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
            lecternBlockEntity.clearContent();
        }
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return direction == Direction.UP && blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity;
        if (blockState.getValue(HAS_BOOK).booleanValue() && (blockEntity = level.getBlockEntity(blockPos)) instanceof LecternBlockEntity) {
            return ((LecternBlockEntity)blockEntity).getRedstoneSignal();
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (blockState.getValue(HAS_BOOK).booleanValue()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (itemStack.is(ItemTags.LECTERN_BOOKS)) {
            return LecternBlock.tryPlaceBook(player, level, blockPos, blockState, itemStack) ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (itemStack.isEmpty() && interactionHand == InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (blockState.getValue(HAS_BOOK).booleanValue()) {
            if (!level.isClientSide) {
                this.openScreen(level, blockPos, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        if (!blockState.getValue(HAS_BOOK).booleanValue()) {
            return null;
        }
        return super.getMenuProvider(blockState, level, blockPos);
    }

    private void openScreen(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            player.openMenu((LecternBlockEntity)blockEntity);
            player.awardStat(Stats.INTERACT_WITH_LECTERN);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

