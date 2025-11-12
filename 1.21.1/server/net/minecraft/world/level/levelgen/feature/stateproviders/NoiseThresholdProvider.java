/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider
extends NoiseBasedStateProvider {
    public static final MapCodec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> NoiseThresholdProvider.noiseCodec(instance).and(instance.group((App)Codec.floatRange((float)-1.0f, (float)1.0f).fieldOf("threshold").forGetter(noiseThresholdProvider -> Float.valueOf(noiseThresholdProvider.threshold)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("high_chance").forGetter(noiseThresholdProvider -> Float.valueOf(noiseThresholdProvider.highChance)), (App)BlockState.CODEC.fieldOf("default_state").forGetter(noiseThresholdProvider -> noiseThresholdProvider.defaultState), (App)Codec.list(BlockState.CODEC).fieldOf("low_states").forGetter(noiseThresholdProvider -> noiseThresholdProvider.lowStates), (App)Codec.list(BlockState.CODEC).fieldOf("high_states").forGetter(noiseThresholdProvider -> noiseThresholdProvider.highStates))).apply((Applicative)instance, NoiseThresholdProvider::new));
    private final float threshold;
    private final float highChance;
    private final BlockState defaultState;
    private final List<BlockState> lowStates;
    private final List<BlockState> highStates;

    public NoiseThresholdProvider(long l, NormalNoise.NoiseParameters noiseParameters, float f, float f2, float f3, BlockState blockState, List<BlockState> list, List<BlockState> list2) {
        super(l, noiseParameters, f);
        this.threshold = f2;
        this.highChance = f3;
        this.defaultState = blockState;
        this.lowStates = list;
        this.highStates = list2;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
        double d = this.getNoiseValue(blockPos, this.scale);
        if (d < (double)this.threshold) {
            return Util.getRandom(this.lowStates, randomSource);
        }
        if (randomSource.nextFloat() < this.highChance) {
            return Util.getRandom(this.highStates, randomSource);
        }
        return this.defaultState;
    }
}

