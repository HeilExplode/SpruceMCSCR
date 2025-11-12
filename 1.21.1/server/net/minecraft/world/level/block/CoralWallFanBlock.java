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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralWallFanBlock
extends BaseCoralWallFanBlock {
    public static final MapCodec<CoralWallFanBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CoralBlock.DEAD_CORAL_FIELD.forGetter(coralWallFanBlock -> coralWallFanBlock.deadBlock), CoralWallFanBlock.propertiesCodec()).apply((Applicative)instance, CoralWallFanBlock::new));
    private final Block deadBlock;

    public MapCodec<CoralWallFanBlock> codec() {
        return CODEC;
    }

    protected CoralWallFanBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.deadBlock = block;
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.tryScheduleDieTick(blockState, level, blockPos);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!CoralWallFanBlock.scanForWater(blockState, serverLevel, blockPos)) {
            serverLevel.setBlock(blockPos, (BlockState)((BlockState)this.deadBlock.defaultBlockState().setValue(WATERLOGGED, false)).setValue(FACING, blockState.getValue(FACING)), 2);
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        this.tryScheduleDieTick(blockState, levelAccessor, blockPos);
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }
}

