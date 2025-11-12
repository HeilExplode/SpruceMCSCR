/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

public interface HolderOwner<T> {
    default public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return holderOwner == this;
    }
}

