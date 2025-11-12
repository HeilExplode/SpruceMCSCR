/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.util.Mth;

public class ByIdMap {
    private static <T> IntFunction<T> createMap(ToIntFunction<T> toIntFunction, T[] TArray) {
        if (TArray.length == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        Int2ObjectOpenHashMap int2ObjectOpenHashMap = new Int2ObjectOpenHashMap();
        for (T t : TArray) {
            int n = toIntFunction.applyAsInt(t);
            Object object = int2ObjectOpenHashMap.put(n, t);
            if (object == null) continue;
            throw new IllegalArgumentException("Duplicate entry on id " + n + ": current=" + String.valueOf(t) + ", previous=" + String.valueOf(object));
        }
        return int2ObjectOpenHashMap;
    }

    public static <T> IntFunction<T> sparse(ToIntFunction<T> toIntFunction, T[] TArray, T t) {
        IntFunction intFunction = ByIdMap.createMap(toIntFunction, TArray);
        return n -> Objects.requireNonNullElse(intFunction.apply(n), t);
    }

    private static <T> T[] createSortedArray(ToIntFunction<T> toIntFunction, T[] TArray) {
        int n = TArray.length;
        if (n == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        Object[] objectArray = (Object[])TArray.clone();
        Arrays.fill(objectArray, null);
        for (T t : TArray) {
            int n2 = toIntFunction.applyAsInt(t);
            if (n2 < 0 || n2 >= n) {
                throw new IllegalArgumentException("Values are not continous, found index " + n2 + " for value " + String.valueOf(t));
            }
            Object object = objectArray[n2];
            if (object != null) {
                throw new IllegalArgumentException("Duplicate entry on id " + n2 + ": current=" + String.valueOf(t) + ", previous=" + String.valueOf(object));
            }
            objectArray[n2] = t;
        }
        for (int i = 0; i < n; ++i) {
            if (objectArray[i] != null) continue;
            throw new IllegalArgumentException("Missing value at index: " + i);
        }
        return objectArray;
    }

    public static <T> IntFunction<T> continuous(ToIntFunction<T> toIntFunction, T[] TArray, OutOfBoundsStrategy outOfBoundsStrategy) {
        Object[] objectArray = ByIdMap.createSortedArray(toIntFunction, TArray);
        int n = objectArray.length;
        return switch (outOfBoundsStrategy.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Object var5_5 = objectArray[0];
                yield n2 -> n2 >= 0 && n2 < n ? objectArray[n2] : var5_5;
            }
            case 1 -> n2 -> objectArray[Mth.positiveModulo(n2, n)];
            case 2 -> n2 -> objectArray[Mth.clamp(n2, 0, n - 1)];
        };
    }

    public static enum OutOfBoundsStrategy {
        ZERO,
        WRAP,
        CLAMP;

    }
}

