/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.slf4j.Logger;

public class VeryBiasedToBottomHeight
extends HeightProvider {
    public static final MapCodec<VeryBiasedToBottomHeight> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(veryBiasedToBottomHeight -> veryBiasedToBottomHeight.minInclusive), (App)VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(veryBiasedToBottomHeight -> veryBiasedToBottomHeight.maxInclusive), (App)Codec.intRange((int)1, (int)Integer.MAX_VALUE).optionalFieldOf("inner", (Object)1).forGetter(veryBiasedToBottomHeight -> veryBiasedToBottomHeight.inner)).apply((Applicative)instance, VeryBiasedToBottomHeight::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final int inner;

    private VeryBiasedToBottomHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int n) {
        this.minInclusive = verticalAnchor;
        this.maxInclusive = verticalAnchor2;
        this.inner = n;
    }

    public static VeryBiasedToBottomHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int n) {
        return new VeryBiasedToBottomHeight(verticalAnchor, verticalAnchor2, n);
    }

    @Override
    public int sample(RandomSource randomSource, WorldGenerationContext worldGenerationContext) {
        int n = this.minInclusive.resolveY(worldGenerationContext);
        int n2 = this.maxInclusive.resolveY(worldGenerationContext);
        if (n2 - n - this.inner + 1 <= 0) {
            LOGGER.warn("Empty height range: {}", (Object)this);
            return n;
        }
        int n3 = Mth.nextInt(randomSource, n + this.inner, n2);
        int n4 = Mth.nextInt(randomSource, n, n3 - 1);
        return Mth.nextInt(randomSource, n, n4 - 1 + this.inner);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.VERY_BIASED_TO_BOTTOM;
    }

    public String toString() {
        return "biased[" + String.valueOf(this.minInclusive) + "-" + String.valueOf(this.maxInclusive) + " inner: " + this.inner + "]";
    }
}

