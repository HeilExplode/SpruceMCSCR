/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock
extends FallingBlock {
    public static final MapCodec<ConcretePowderBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("concrete").forGetter(concretePowderBlock -> concretePowderBlock.concrete), ConcretePowderBlock.propertiesCodec()).apply((Applicative)instance, ConcretePowderBlock::new));
    private final Block concrete;

    public MapCodec<ConcretePowderBlock> codec() {
        return CODEC;
    }

    public ConcretePowderBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.concrete = block;
    }

    @Override
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        if (ConcretePowderBlock.shouldSolidify(level, blockPos, blockState2)) {
            level.setBlock(blockPos, this.concrete.defaultBlockState(), 3);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState;
        BlockPos blockPos;
        Level level = blockPlaceContext.getLevel();
        if (ConcretePowderBlock.shouldSolidify(level, blockPos = blockPlaceContext.getClickedPos(), blockState = level.getBlockState(blockPos))) {
            return this.concrete.defaultBlockState();
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    private static boolean shouldSolidify(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ConcretePowderBlock.canSolidify(blockState) || ConcretePowderBlock.touchesLiquid(blockGetter, blockPos);
    }

    private static boolean touchesLiquid(BlockGetter blockGetter, BlockPos blockPos) {
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : Direction.values()) {
            BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
            if (direction == Direction.DOWN && !ConcretePowderBlock.canSolidify(blockState)) continue;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            blockState = blockGetter.getBlockState(mutableBlockPos);
            if (!ConcretePowderBlock.canSolidify(blockState) || blockState.isFaceSturdy(blockGetter, blockPos, direction.getOpposite())) continue;
            bl = true;
            break;
        }
        return bl;
    }

    private static boolean canSolidify(BlockState blockState) {
        return blockState.getFluidState().is(FluidTags.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (ConcretePowderBlock.touchesLiquid(levelAccessor, blockPos)) {
            return this.concrete.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.getMapColor((BlockGetter)blockGetter, (BlockPos)blockPos).col;
    }
}

