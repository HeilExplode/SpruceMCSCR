/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.MapMaker
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class ResourceKey<T> {
    private static final ConcurrentMap<InternKey, ResourceKey<?>> VALUES = new MapMaker().weakValues().makeMap();
    private final ResourceLocation registryName;
    private final ResourceLocation location;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
        return ResourceLocation.CODEC.xmap(resourceLocation -> ResourceKey.create(resourceKey, resourceLocation), ResourceKey::location);
    }

    public static <T> StreamCodec<ByteBuf, ResourceKey<T>> streamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
        return ResourceLocation.STREAM_CODEC.map(resourceLocation -> ResourceKey.create(resourceKey, resourceLocation), ResourceKey::location);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation resourceLocation) {
        return ResourceKey.create(resourceKey.location, resourceLocation);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation resourceLocation) {
        return ResourceKey.create(Registries.ROOT_REGISTRY_NAME, resourceLocation);
    }

    private static <T> ResourceKey<T> create(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return VALUES.computeIfAbsent(new InternKey(resourceLocation, resourceLocation2), internKey -> new ResourceKey(internKey.registry, internKey.location));
    }

    private ResourceKey(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        this.registryName = resourceLocation;
        this.location = resourceLocation2;
    }

    public String toString() {
        return "ResourceKey[" + String.valueOf(this.registryName) + " / " + String.valueOf(this.location) + "]";
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> resourceKey) {
        return this.registryName.equals(resourceKey.location());
    }

    public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
        return this.isFor(resourceKey) ? Optional.of(this) : Optional.empty();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public ResourceLocation registry() {
        return this.registryName;
    }

    public ResourceKey<Registry<T>> registryKey() {
        return ResourceKey.createRegistryKey(this.registryName);
    }

    static final class InternKey
    extends Record {
        final ResourceLocation registry;
        final ResourceLocation location;

        InternKey(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
            this.registry = resourceLocation;
            this.location = resourceLocation2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InternKey.class, "registry;location", "registry", "location"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InternKey.class, "registry;location", "registry", "location"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InternKey.class, "registry;location", "registry", "location"}, this, object);
        }

        public ResourceLocation registry() {
            return this.registry;
        }

        public ResourceLocation location() {
            return this.location;
        }
    }
}

