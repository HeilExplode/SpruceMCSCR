/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Optionull {
    @Nullable
    public static <T, R> R map(@Nullable T t, Function<T, R> function) {
        return t == null ? null : (R)function.apply(t);
    }

    public static <T, R> R mapOrDefault(@Nullable T t, Function<T, R> function, R r) {
        return t == null ? r : function.apply(t);
    }

    public static <T, R> R mapOrElse(@Nullable T t, Function<T, R> function, Supplier<R> supplier) {
        return t == null ? supplier.get() : function.apply(t);
    }

    @Nullable
    public static <T> T first(Collection<T> collection) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? (T)iterator.next() : null;
    }

    public static <T> T firstOrDefault(Collection<T> collection, T t) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : t;
    }

    public static <T> T firstOrElse(Collection<T> collection, Supplier<T> supplier) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : supplier.get();
    }

    public static <T> boolean isNullOrEmpty(@Nullable T[] TArray) {
        return TArray == null || TArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable boolean[] blArray) {
        return blArray == null || blArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable byte[] byArray) {
        return byArray == null || byArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable char[] cArray) {
        return cArray == null || cArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable short[] sArray) {
        return sArray == null || sArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable int[] nArray) {
        return nArray == null || nArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable long[] lArray) {
        return lArray == null || lArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable float[] fArray) {
        return fArray == null || fArray.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable double[] dArray) {
        return dArray == null || dArray.length == 0;
    }
}

