/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CandleBlock
extends AbstractCandleBlock
implements SimpleWaterloggedBlock {
    public static final MapCodec<CandleBlock> CODEC = CandleBlock.simpleCodec(CandleBlock::new);
    public static final int MIN_CANDLES = 1;
    public static final int MAX_CANDLES = 4;
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = blockState -> blockState.getValue(LIT) != false ? 3 * blockState.getValue(CANDLES) : 0;
    private static final Int2ObjectMap<List<Vec3>> PARTICLE_OFFSETS = Util.make(() -> {
        Int2ObjectOpenHashMap int2ObjectOpenHashMap = new Int2ObjectOpenHashMap();
        int2ObjectOpenHashMap.defaultReturnValue((Object)ImmutableList.of());
        int2ObjectOpenHashMap.put(1, (Object)ImmutableList.of((Object)new Vec3(0.5, 0.5, 0.5)));
        int2ObjectOpenHashMap.put(2, (Object)ImmutableList.of((Object)new Vec3(0.375, 0.44, 0.5), (Object)new Vec3(0.625, 0.5, 0.44)));
        int2ObjectOpenHashMap.put(3, (Object)ImmutableList.of((Object)new Vec3(0.5, 0.313, 0.625), (Object)new Vec3(0.375, 0.44, 0.5), (Object)new Vec3(0.56, 0.5, 0.44)));
        int2ObjectOpenHashMap.put(4, (Object)ImmutableList.of((Object)new Vec3(0.44, 0.313, 0.56), (Object)new Vec3(0.625, 0.44, 0.56), (Object)new Vec3(0.375, 0.44, 0.375), (Object)new Vec3(0.56, 0.5, 0.375)));
        return Int2ObjectMaps.unmodifiable((Int2ObjectMap)int2ObjectOpenHashMap);
    });
    private static final VoxelShape ONE_AABB = Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
    private static final VoxelShape TWO_AABB = Block.box(5.0, 0.0, 6.0, 11.0, 6.0, 9.0);
    private static final VoxelShape THREE_AABB = Block.box(5.0, 0.0, 6.0, 10.0, 6.0, 11.0);
    private static final VoxelShape FOUR_AABB = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 10.0);

    public MapCodec<CandleBlock> codec() {
        return CODEC;
    }

    public CandleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(CANDLES, 1)).setValue(LIT, false)).setValue(WATERLOGGED, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.isEmpty() && player.getAbilities().mayBuild && blockState.getValue(LIT).booleanValue()) {
            CandleBlock.extinguish(player, blockState, level, blockPos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().getItem() == this.asItem() && blockState.getValue(CANDLES) < 4) {
            return true;
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return (BlockState)blockState.cycle(CANDLES);
        }
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.getType() == Fluids.WATER;
        return (BlockState)super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, bl);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (blockState.getValue(CANDLES)) {
            default: {
                return ONE_AABB;
            }
            case 2: {
                return TWO_AABB;
            }
            case 3: {
                return THREE_AABB;
            }
            case 4: 
        }
        return FOUR_AABB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CANDLES, LIT, WATERLOGGED);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.getValue(WATERLOGGED).booleanValue() || fluidState.getType() != Fluids.WATER) {
            return false;
        }
        BlockState blockState2 = (BlockState)blockState.setValue(WATERLOGGED, true);
        if (blockState.getValue(LIT).booleanValue()) {
            CandleBlock.extinguish(null, blockState2, levelAccessor, blockPos);
        } else {
            levelAccessor.setBlock(blockPos, blockState2, 3);
        }
        levelAccessor.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
        return true;
    }

    public static boolean canLight(BlockState blockState) {
        return blockState.is(BlockTags.CANDLES, blockStateBase -> blockStateBase.hasProperty(LIT) && blockStateBase.hasProperty(WATERLOGGED)) && blockState.getValue(LIT) == false && blockState.getValue(WATERLOGGED) == false;
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState blockState) {
        return (Iterable)PARTICLE_OFFSETS.get(blockState.getValue(CANDLES).intValue());
    }

    @Override
    protected boolean canBeLit(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) == false && super.canBeLit(blockState);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return Block.canSupportCenter(levelReader, blockPos.below(), Direction.UP);
    }
}

