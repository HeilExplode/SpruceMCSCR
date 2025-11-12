/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CeilingHangingSignBlock
extends SignBlock {
    public static final MapCodec<CeilingHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), CeilingHangingSignBlock.propertiesCodec()).apply((Applicative)instance, CeilingHangingSignBlock::new));
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final float AABB_OFFSET = 5.0f;
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final Map<Integer, VoxelShape> AABBS = Maps.newHashMap((Map)ImmutableMap.of((Object)0, (Object)Block.box(1.0, 0.0, 7.0, 15.0, 10.0, 9.0), (Object)4, (Object)Block.box(7.0, 0.0, 1.0, 9.0, 10.0, 15.0), (Object)8, (Object)Block.box(1.0, 0.0, 7.0, 15.0, 10.0, 9.0), (Object)12, (Object)Block.box(7.0, 0.0, 1.0, 9.0, 10.0, 15.0)));

    public MapCodec<CeilingHangingSignBlock> codec() {
        return CODEC;
    }

    public CeilingHangingSignBlock(WoodType woodType, BlockBehaviour.Properties properties) {
        super(woodType, properties.sound(woodType.hangingSignSoundType()));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(ROTATION, 0)).setValue(ATTACHED, false)).setValue(WATERLOGGED, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        SignBlockEntity signBlockEntity;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SignBlockEntity && this.shouldTryToChainAnotherHangingSign(player, blockHitResult, signBlockEntity = (SignBlockEntity)blockEntity, itemStack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    private boolean shouldTryToChainAnotherHangingSign(Player player, BlockHitResult blockHitResult, SignBlockEntity signBlockEntity, ItemStack itemStack) {
        return !signBlockEntity.canExecuteClickCommands(signBlockEntity.isFacingFrontText(player), player) && itemStack.getItem() instanceof HangingSignItem && blockHitResult.getDirection().equals(Direction.DOWN);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.above()).isFaceSturdy(levelReader, blockPos.above(), Direction.DOWN, SupportType.CENTER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        boolean bl;
        Level level = blockPlaceContext.getLevel();
        FluidState fluidState = level.getFluidState(blockPlaceContext.getClickedPos());
        BlockPos blockPos = blockPlaceContext.getClickedPos().above();
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl2 = blockState.is(BlockTags.ALL_HANGING_SIGNS);
        Direction direction = Direction.fromYRot(blockPlaceContext.getRotation());
        boolean bl3 = bl = !Block.isFaceFull(blockState.getCollisionShape(level, blockPos), Direction.DOWN) || blockPlaceContext.isSecondaryUseActive();
        if (bl2 && !blockPlaceContext.isSecondaryUseActive()) {
            Object object;
            if (blockState.hasProperty(WallHangingSignBlock.FACING)) {
                object = blockState.getValue(WallHangingSignBlock.FACING);
                if (((Direction)object).getAxis().test(direction)) {
                    bl = false;
                }
            } else if (blockState.hasProperty(ROTATION) && ((Optional)(object = RotationSegment.convertToDirection(blockState.getValue(ROTATION)))).isPresent() && ((Direction)((Optional)object).get()).getAxis().test(direction)) {
                bl = false;
            }
        }
        int n = !bl ? RotationSegment.convertToSegment(direction.getOpposite()) : RotationSegment.convertToSegment(blockPlaceContext.getRotation() + 180.0f);
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(ATTACHED, bl)).setValue(ROTATION, n)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = AABBS.get(blockState.getValue(ROTATION));
        return voxelShape == null ? SHAPE : voxelShape;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.getShape(blockState, blockGetter, blockPos, CollisionContext.empty());
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.UP && !this.canSurvive(blockState, levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public float getYRotationDegrees(BlockState blockState) {
        return RotationSegment.convertToDegrees(blockState.getValue(ROTATION));
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(ROTATION, rotation.rotate(blockState.getValue(ROTATION), 16));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState)blockState.setValue(ROTATION, mirror.mirror(blockState.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, ATTACHED, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HangingSignBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return CeilingHangingSignBlock.createTickerHelper(blockEntityType, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
    }
}

