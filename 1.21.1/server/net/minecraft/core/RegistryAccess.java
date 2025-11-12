/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  org.slf4j.Logger
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess
extends HolderLookup.Provider {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Frozen EMPTY = new ImmutableRegistryAccess(Map.of()).freeze();

    public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> var1);

    @Override
    default public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
        return this.registry(resourceKey).map(Registry::asLookup);
    }

    default public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + String.valueOf(resourceKey)));
    }

    public Stream<RegistryEntry<?>> registries();

    @Override
    default public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
        return this.registries().map(RegistryEntry::key);
    }

    public static Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> registry) {
        return new Frozen(){

            public <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                Registry registry2 = registry;
                return registry2.getOptional(resourceKey);
            }

            @Override
            public Stream<RegistryEntry<?>> registries() {
                return registry.entrySet().stream().map(RegistryEntry::fromMapEntry);
            }

            @Override
            public Frozen freeze() {
                return this;
            }
        };
    }

    default public Frozen freeze() {
        class FrozenAccess
        extends ImmutableRegistryAccess
        implements Frozen {
            protected FrozenAccess(RegistryAccess registryAccess, Stream<RegistryEntry<?>> stream) {
                super(stream);
            }
        }
        return new FrozenAccess(this, this.registries().map(RegistryEntry::freeze));
    }

    default public Lifecycle allRegistriesLifecycle() {
        return this.registries().map(registryEntry -> registryEntry.value.registryLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
    }

    public static final class RegistryEntry<T>
    extends Record {
        private final ResourceKey<? extends Registry<T>> key;
        final Registry<T> value;

        public RegistryEntry(ResourceKey<? extends Registry<T>> resourceKey, Registry<T> registry) {
            this.key = resourceKey;
            this.value = registry;
        }

        private static <T, R extends Registry<? extends T>> RegistryEntry<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends Registry<?>>, R> entry) {
            return RegistryEntry.fromUntyped(entry.getKey(), (Registry)entry.getValue());
        }

        private static <T> RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> resourceKey, Registry<?> registry) {
            return new RegistryEntry(resourceKey, registry);
        }

        private RegistryEntry<T> freeze() {
            return new RegistryEntry<T>(this.key, this.value.freeze());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryEntry.class, "key;value", "key", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryEntry.class, "key;value", "key", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryEntry.class, "key;value", "key", "value"}, this, object);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Registry<T> value() {
            return this.value;
        }
    }

    public static class ImmutableRegistryAccess
    implements RegistryAccess {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableRegistryAccess(List<? extends Registry<?>> list) {
            this.registries = list.stream().collect(Collectors.toUnmodifiableMap(Registry::key, registry -> registry));
        }

        public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> map) {
            this.registries = Map.copyOf(map);
        }

        public ImmutableRegistryAccess(Stream<RegistryEntry<?>> stream) {
            this.registries = (Map)stream.collect(ImmutableMap.toImmutableMap(RegistryEntry::key, RegistryEntry::value));
        }

        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.ofNullable(this.registries.get(resourceKey)).map(registry -> registry);
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return this.registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
        }
    }

    public static interface Frozen
    extends RegistryAccess {
    }
}

