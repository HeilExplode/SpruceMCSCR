/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MangrovePropaguleBlock
extends SaplingBlock
implements SimpleWaterloggedBlock {
    public static final MapCodec<MangrovePropaguleBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TreeGrower.CODEC.fieldOf("tree").forGetter(mangrovePropaguleBlock -> mangrovePropaguleBlock.treeGrower), MangrovePropaguleBlock.propertiesCodec()).apply((Applicative)instance, MangrovePropaguleBlock::new));
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final int MAX_AGE = 4;
    private static final VoxelShape[] SHAPE_PER_AGE = new VoxelShape[]{Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0), Block.box(7.0, 10.0, 7.0, 9.0, 16.0, 9.0), Block.box(7.0, 7.0, 7.0, 9.0, 16.0, 9.0), Block.box(7.0, 3.0, 7.0, 9.0, 16.0, 9.0), Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)};
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    public MapCodec<MangrovePropaguleBlock> codec() {
        return CODEC;
    }

    public MangrovePropaguleBlock(TreeGrower treeGrower, BlockBehaviour.Properties properties) {
        super(treeGrower, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(STAGE, 0)).setValue(AGE, 0)).setValue(WATERLOGGED, false)).setValue(HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return super.mayPlaceOn(blockState, blockGetter, blockPos) || blockState.is(Blocks.CLAY);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.getType() == Fluids.WATER;
        return (BlockState)((BlockState)super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, bl)).setValue(AGE, 4);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
        VoxelShape voxelShape = blockState.getValue(HANGING) == false ? SHAPE_PER_AGE[4] : SHAPE_PER_AGE[blockState.getValue(AGE)];
        return voxelShape.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (MangrovePropaguleBlock.isHanging(blockState)) {
            return levelReader.getBlockState(blockPos.above()).is(Blocks.MANGROVE_LEAVES);
        }
        return super.canSurvive(blockState, levelReader, blockPos);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction == Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
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
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!MangrovePropaguleBlock.isHanging(blockState)) {
            if (randomSource.nextInt(7) == 0) {
                this.advanceTree(serverLevel, blockPos, blockState, randomSource);
            }
            return;
        }
        if (!MangrovePropaguleBlock.isFullyGrown(blockState)) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.cycle(AGE), 2);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return !MangrovePropaguleBlock.isHanging(blockState) || !MangrovePropaguleBlock.isFullyGrown(blockState);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return MangrovePropaguleBlock.isHanging(blockState) ? !MangrovePropaguleBlock.isFullyGrown(blockState) : super.isBonemealSuccess(level, randomSource, blockPos, blockState);
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        if (MangrovePropaguleBlock.isHanging(blockState) && !MangrovePropaguleBlock.isFullyGrown(blockState)) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.cycle(AGE), 2);
        } else {
            super.performBonemeal(serverLevel, randomSource, blockPos, blockState);
        }
    }

    private static boolean isHanging(BlockState blockState) {
        return blockState.getValue(HANGING);
    }

    private static boolean isFullyGrown(BlockState blockState) {
        return blockState.getValue(AGE) == 4;
    }

    public static BlockState createNewHangingPropagule() {
        return MangrovePropaguleBlock.createNewHangingPropagule(0);
    }

    public static BlockState createNewHangingPropagule(int n) {
        return (BlockState)((BlockState)Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(HANGING, true)).setValue(AGE, n);
    }
}

