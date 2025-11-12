/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T>
extends DelegatingOps<T> {
    private final RegistryInfoLookup lookupProvider;

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, HolderLookup.Provider provider) {
        return RegistryOps.create(dynamicOps, new HolderLookupAdapter(provider));
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryInfoLookup registryInfoLookup) {
        return new RegistryOps<T>(dynamicOps, registryInfoLookup);
    }

    public static <T> Dynamic<T> injectRegistryContext(Dynamic<T> dynamic, HolderLookup.Provider provider) {
        return new Dynamic(provider.createSerializationContext(dynamic.getOps()), dynamic.getValue());
    }

    private RegistryOps(DynamicOps<T> dynamicOps, RegistryInfoLookup registryInfoLookup) {
        super(dynamicOps);
        this.lookupProvider = registryInfoLookup;
    }

    public <U> RegistryOps<U> withParent(DynamicOps<U> dynamicOps) {
        if (dynamicOps == this.delegate) {
            return this;
        }
        return new RegistryOps<U>(dynamicOps, this.lookupProvider);
    }

    public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.lookupProvider.lookup(resourceKey).map(RegistryInfo::owner);
    }

    public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.lookupProvider.lookup(resourceKey).map(RegistryInfo::getter);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        RegistryOps registryOps = (RegistryOps)object;
        return this.delegate.equals((Object)registryOps.delegate) && this.lookupProvider.equals(registryOps.lookupProvider);
    }

    public int hashCode() {
        return this.delegate.hashCode() * 31 + this.lookupProvider.hashCode();
    }

    public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return ExtraCodecs.retrieveContext(dynamicOps -> {
            if (dynamicOps instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)dynamicOps;
                return registryOps.lookupProvider.lookup(resourceKey).map(registryInfo -> DataResult.success(registryInfo.getter(), (Lifecycle)registryInfo.elementsLifecycle())).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + String.valueOf(resourceKey)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter(object -> null);
    }

    public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> resourceKey) {
        ResourceKey resourceKey2 = ResourceKey.createRegistryKey(resourceKey.registry());
        return ExtraCodecs.retrieveContext(dynamicOps -> {
            if (dynamicOps instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)dynamicOps;
                return registryOps.lookupProvider.lookup(resourceKey2).flatMap(registryInfo -> registryInfo.getter().get(resourceKey)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Can't find value: " + String.valueOf(resourceKey)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter(object -> null);
    }

    static final class HolderLookupAdapter
    implements RegistryInfoLookup {
        private final HolderLookup.Provider lookupProvider;
        private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryInfo<?>>> lookups = new ConcurrentHashMap();

        public HolderLookupAdapter(HolderLookup.Provider provider) {
            this.lookupProvider = provider;
        }

        public <E> Optional<RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return this.lookups.computeIfAbsent(resourceKey, this::createLookup);
        }

        private Optional<RegistryInfo<Object>> createLookup(ResourceKey<? extends Registry<?>> resourceKey) {
            return this.lookupProvider.lookup(resourceKey).map(RegistryInfo::fromRegistryLookup);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof HolderLookupAdapter)) return false;
            HolderLookupAdapter holderLookupAdapter = (HolderLookupAdapter)object;
            if (!this.lookupProvider.equals(holderLookupAdapter.lookupProvider)) return false;
            return true;
        }

        public int hashCode() {
            return this.lookupProvider.hashCode();
        }
    }

    public static interface RegistryInfoLookup {
        public <T> Optional<RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);
    }

    public record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
        public static <T> RegistryInfo<T> fromRegistryLookup(HolderLookup.RegistryLookup<T> registryLookup) {
            return new RegistryInfo<T>(registryLookup, registryLookup, registryLookup.registryLifecycle());
        }
    }
}

