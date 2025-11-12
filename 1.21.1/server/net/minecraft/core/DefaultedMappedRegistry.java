/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class DefaultedMappedRegistry<T>
extends MappedRegistry<T>
implements DefaultedRegistry<T> {
    private final ResourceLocation defaultKey;
    private Holder.Reference<T> defaultValue;

    public DefaultedMappedRegistry(String string, ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, boolean bl) {
        super(resourceKey, lifecycle, bl);
        this.defaultKey = ResourceLocation.parse(string);
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> resourceKey, T t, RegistrationInfo registrationInfo) {
        Holder.Reference<T> reference = super.register(resourceKey, t, registrationInfo);
        if (this.defaultKey.equals(resourceKey.location())) {
            this.defaultValue = reference;
        }
        return reference;
    }

    @Override
    public int getId(@Nullable T t) {
        int n = super.getId(t);
        return n == -1 ? super.getId(this.defaultValue.value()) : n;
    }

    @Override
    @Nonnull
    public ResourceLocation getKey(T t) {
        ResourceLocation resourceLocation = super.getKey(t);
        return resourceLocation == null ? this.defaultKey : resourceLocation;
    }

    @Override
    @Nonnull
    public T get(@Nullable ResourceLocation resourceLocation) {
        Object t = super.get(resourceLocation);
        return t == null ? this.defaultValue.value() : t;
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(super.get(resourceLocation));
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return Optional.ofNullable(this.defaultValue);
    }

    @Override
    @Nonnull
    public T byId(int n) {
        Object t = super.byId(n);
        return t == null ? this.defaultValue.value() : t;
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource randomSource) {
        return super.getRandom(randomSource).or(() -> Optional.of(this.defaultValue));
    }

    @Override
    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }
}

