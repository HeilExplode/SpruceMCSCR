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
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class NoiseBasedCountPlacement
extends RepeatingPlacement {
    public static final MapCodec<NoiseBasedCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("noise_to_count_ratio").forGetter(noiseBasedCountPlacement -> noiseBasedCountPlacement.noiseToCountRatio), (App)Codec.DOUBLE.fieldOf("noise_factor").forGetter(noiseBasedCountPlacement -> noiseBasedCountPlacement.noiseFactor), (App)Codec.DOUBLE.fieldOf("noise_offset").orElse((Object)0.0).forGetter(noiseBasedCountPlacement -> noiseBasedCountPlacement.noiseOffset)).apply((Applicative)instance, NoiseBasedCountPlacement::new));
    private final int noiseToCountRatio;
    private final double noiseFactor;
    private final double noiseOffset;

    private NoiseBasedCountPlacement(int n, double d, double d2) {
        this.noiseToCountRatio = n;
        this.noiseFactor = d;
        this.noiseOffset = d2;
    }

    public static NoiseBasedCountPlacement of(int n, double d, double d2) {
        return new NoiseBasedCountPlacement(n, d, d2);
    }

    @Override
    protected int count(RandomSource randomSource, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / this.noiseFactor, (double)blockPos.getZ() / this.noiseFactor, false);
        return (int)Math.ceil((d + this.noiseOffset) * (double)this.noiseToCountRatio);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_BASED_COUNT;
    }
}

