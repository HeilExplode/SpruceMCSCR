/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TintedGlassBlock
extends TransparentBlock {
    public static final MapCodec<TintedGlassBlock> CODEC = TintedGlassBlock.simpleCodec(TintedGlassBlock::new);

    public MapCodec<TintedGlassBlock> codec() {
        return CODEC;
    }

    public TintedGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    protected int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getMaxLightLevel();
    }
}

