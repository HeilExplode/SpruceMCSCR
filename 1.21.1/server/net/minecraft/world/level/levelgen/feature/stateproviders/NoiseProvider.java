/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P4
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider
extends NoiseBasedStateProvider {
    public static final MapCodec<NoiseProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> NoiseProvider.noiseProviderCodec(instance).apply((Applicative)instance, NoiseProvider::new));
    protected final List<BlockState> states;

    protected static <P extends NoiseProvider> Products.P4<RecordCodecBuilder.Mu<P>, Long, NormalNoise.NoiseParameters, Float, List<BlockState>> noiseProviderCodec(RecordCodecBuilder.Instance<P> instance) {
        return NoiseProvider.noiseCodec(instance).and((App)Codec.list(BlockState.CODEC).fieldOf("states").forGetter(noiseProvider -> noiseProvider.states));
    }

    public NoiseProvider(long l, NormalNoise.NoiseParameters noiseParameters, float f, List<BlockState> list) {
        super(l, noiseParameters, f);
        this.states = list;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.NOISE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
        return this.getRandomState(this.states, blockPos, this.scale);
    }

    protected BlockState getRandomState(List<BlockState> list, BlockPos blockPos, double d) {
        double d2 = this.getNoiseValue(blockPos, d);
        return this.getRandomState(list, d2);
    }

    protected BlockState getRandomState(List<BlockState> list, double d) {
        double d2 = Mth.clamp((1.0 + d) / 2.0, 0.0, 0.9999);
        return list.get((int)(d2 * (double)list.size()));
    }
}

