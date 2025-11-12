/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
    public static final Codec<InclusiveRange<Integer>> INT = InclusiveRange.codec(Codec.INT);

    public InclusiveRange {
        if (t.compareTo(t2) > 0) {
            throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
        }
    }

    public InclusiveRange(T t) {
        this(t, t);
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec) {
        return ExtraCodecs.intervalCodec(codec, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive);
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec, T t, T t2) {
        return InclusiveRange.codec(codec).validate(inclusiveRange -> {
            if (inclusiveRange.minInclusive().compareTo(t) < 0) {
                return DataResult.error(() -> "Range limit too low, expected at least " + String.valueOf(t) + " [" + String.valueOf(inclusiveRange.minInclusive()) + "-" + String.valueOf(inclusiveRange.maxInclusive()) + "]");
            }
            if (inclusiveRange.maxInclusive().compareTo(t2) > 0) {
                return DataResult.error(() -> "Range limit too high, expected at most " + String.valueOf(t2) + " [" + String.valueOf(inclusiveRange.minInclusive()) + "-" + String.valueOf(inclusiveRange.maxInclusive()) + "]");
            }
            return DataResult.success((Object)inclusiveRange);
        });
    }

    public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T t, T t2) {
        if (t.compareTo(t2) <= 0) {
            return DataResult.success(new InclusiveRange<T>(t, t2));
        }
        return DataResult.error(() -> "min_inclusive must be less than or equal to max_inclusive");
    }

    public boolean isValueInRange(T t) {
        return t.compareTo(this.minInclusive) >= 0 && t.compareTo(this.maxInclusive) <= 0;
    }

    public boolean contains(InclusiveRange<T> inclusiveRange) {
        return inclusiveRange.minInclusive().compareTo(this.minInclusive) >= 0 && inclusiveRange.maxInclusive.compareTo(this.maxInclusive) <= 0;
    }

    @Override
    public String toString() {
        return "[" + String.valueOf(this.minInclusive) + ", " + String.valueOf(this.maxInclusive) + "]";
    }
}

