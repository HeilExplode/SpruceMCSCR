/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package net.minecraft.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface DefaultedRegistry<T>
extends Registry<T> {
    @Override
    @Nonnull
    public ResourceLocation getKey(T var1);

    @Override
    @Nonnull
    public T get(@Nullable ResourceLocation var1);

    @Override
    @Nonnull
    public T byId(int var1);

    public ResourceLocation getDefaultKey();
}

