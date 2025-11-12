/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public record DimensionPadding(int bottom, int top) {
    private static final Codec<DimensionPadding> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("bottom", (Object)0).forGetter(dimensionPadding -> dimensionPadding.bottom), (App)ExtraCodecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("top", (Object)0).forGetter(dimensionPadding -> dimensionPadding.top)).apply((Applicative)instance, DimensionPadding::new));
    public static final Codec<DimensionPadding> CODEC = Codec.either(ExtraCodecs.NON_NEGATIVE_INT, RECORD_CODEC).xmap(either -> (DimensionPadding)either.map(DimensionPadding::new, Function.identity()), dimensionPadding -> dimensionPadding.hasEqualTopAndBottom() ? Either.left((Object)dimensionPadding.bottom) : Either.right((Object)dimensionPadding));
    public static final DimensionPadding ZERO = new DimensionPadding(0);

    public DimensionPadding(int n) {
        this(n, n);
    }

    public boolean hasEqualTopAndBottom() {
        return this.top == this.bottom;
    }
}

