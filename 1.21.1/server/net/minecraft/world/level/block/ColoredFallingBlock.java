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
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredFallingBlock
extends FallingBlock {
    public static final MapCodec<ColoredFallingBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter(coloredFallingBlock -> coloredFallingBlock.dustColor), ColoredFallingBlock.propertiesCodec()).apply((Applicative)instance, ColoredFallingBlock::new));
    private final ColorRGBA dustColor;

    public MapCodec<ColoredFallingBlock> codec() {
        return CODEC;
    }

    public ColoredFallingBlock(ColorRGBA colorRGBA, BlockBehaviour.Properties properties) {
        super(properties);
        this.dustColor = colorRGBA;
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.dustColor.rgba();
    }
}

