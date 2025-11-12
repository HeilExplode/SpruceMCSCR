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

public class NoiseThresholdCountPlacement
extends RepeatingPlacement {
    public static final MapCodec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.DOUBLE.fieldOf("noise_level").forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.noiseLevel), (App)Codec.INT.fieldOf("below_noise").forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.belowNoise), (App)Codec.INT.fieldOf("above_noise").forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.aboveNoise)).apply((Applicative)instance, NoiseThresholdCountPlacement::new));
    private final double noiseLevel;
    private final int belowNoise;
    private final int aboveNoise;

    private NoiseThresholdCountPlacement(double d, int n, int n2) {
        this.noiseLevel = d;
        this.belowNoise = n;
        this.aboveNoise = n2;
    }

    public static NoiseThresholdCountPlacement of(double d, int n, int n2) {
        return new NoiseThresholdCountPlacement(d, n, n2);
    }

    @Override
    protected int count(RandomSource randomSource, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
        return d < this.noiseLevel ? this.belowNoise : this.aboveNoise;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_THRESHOLD_COUNT;
    }
}

