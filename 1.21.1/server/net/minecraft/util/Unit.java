/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;

public enum Unit {
    INSTANCE;

    public static final Codec<Unit> CODEC;

    static {
        CODEC = Codec.unit((Object)((Object)INSTANCE));
    }
}

