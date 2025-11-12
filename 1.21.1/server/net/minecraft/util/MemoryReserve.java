/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import javax.annotation.Nullable;

public class MemoryReserve {
    @Nullable
    private static byte[] reserve = null;

    public static void allocate() {
        reserve = new byte[0xA00000];
    }

    public static void release() {
        reserve = new byte[0];
    }
}

