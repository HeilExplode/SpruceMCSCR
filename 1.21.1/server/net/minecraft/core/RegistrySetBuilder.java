/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Cloner;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;

public class RegistrySetBuilder {
    private final List<RegistryStub<?>> entries = new ArrayList();

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.RegistryLookup<T> registryLookup) {
        return new EmptyTagLookup<T>(registryLookup){

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
                return registryLookup.get(resourceKey);
            }
        };
    }

    static <T> HolderLookup.RegistryLookup<T> lookupFromMap(final ResourceKey<? extends Registry<? extends T>> resourceKey, final Lifecycle lifecycle, HolderOwner<T> holderOwner, final Map<ResourceKey<T>, Holder.Reference<T>> map) {
        return new EmptyTagRegistryLookup<T>(holderOwner){

            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return resourceKey;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return lifecycle;
            }

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey2) {
                return Optional.ofNullable((Holder.Reference)map.get(resourceKey2));
            }

            @Override
            public Stream<Holder.Reference<T>> listElements() {
                return map.values().stream();
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, RegistryBootstrap<T> registryBootstrap) {
        this.entries.add(new RegistryStub<T>(resourceKey, lifecycle, registryBootstrap));
        return this;
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourceKey, RegistryBootstrap<T> registryBootstrap) {
        return this.add(resourceKey, Lifecycle.stable(), registryBootstrap);
    }

    private BuildState createState(RegistryAccess registryAccess) {
        BuildState buildState = BuildState.create(registryAccess, this.entries.stream().map(RegistryStub::key));
        this.entries.forEach(registryStub -> registryStub.apply(buildState));
        return buildState;
    }

    private static HolderLookup.Provider buildProviderWithContext(UniversalOwner universalOwner, RegistryAccess registryAccess, Stream<HolderLookup.RegistryLookup<?>> stream) {
        record Entry<T>(HolderLookup.RegistryLookup<T> lookup, RegistryOps.RegistryInfo<T> opsInfo) {
            public static <T> Entry<T> createForContextRegistry(HolderLookup.RegistryLookup<T> registryLookup) {
                return new Entry<T>(new EmptyTagLookupWrapper<T>(registryLookup, registryLookup), RegistryOps.RegistryInfo.fromRegistryLookup(registryLookup));
            }

            public static <T> Entry<T> createForNewRegistry(UniversalOwner universalOwner, HolderLookup.RegistryLookup<T> registryLookup) {
                return new Entry(new EmptyTagLookupWrapper(universalOwner.cast(), registryLookup), new RegistryOps.RegistryInfo(universalOwner.cast(), registryLookup, registryLookup.registryLifecycle()));
            }
        }
        final HashMap hashMap = new HashMap();
        registryAccess.registries().forEach(registryEntry -> hashMap.put(registryEntry.key(), Entry.createForContextRegistry(registryEntry.value().asLookup())));
        stream.forEach(registryLookup -> hashMap.put(registryLookup.key(), Entry.createForNewRegistry(universalOwner, registryLookup)));
        return new HolderLookup.Provider(){

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
                return hashMap.keySet().stream();
            }

            <T> Optional<Entry<T>> getEntry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return Optional.ofNullable((Entry)hashMap.get(resourceKey));
            }

            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return this.getEntry(resourceKey).map(Entry::lookup);
            }

            @Override
            public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicOps) {
                return RegistryOps.create(dynamicOps, new RegistryOps.RegistryInfoLookup(){

                    @Override
                    public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                        return this.getEntry(resourceKey).map(Entry::opsInfo);
                    }
                });
            }
        };
    }

    public HolderLookup.Provider build(RegistryAccess registryAccess) {
        BuildState buildState = this.createState(registryAccess);
        Stream<HolderLookup.RegistryLookup<?>> stream = this.entries.stream().map(registryStub -> registryStub.collectRegisteredValues(buildState).buildAsLookup(buildState.owner));
        HolderLookup.Provider provider = RegistrySetBuilder.buildProviderWithContext(buildState.owner, registryAccess, stream);
        buildState.reportNotCollectedHolders();
        buildState.reportUnclaimedRegisteredValues();
        buildState.throwOnError();
        return provider;
    }

    private HolderLookup.Provider createLazyFullPatchedRegistries(RegistryAccess registryAccess, HolderLookup.Provider provider, Cloner.Factory factory, Map<ResourceKey<? extends Registry<?>>, RegistryContents<?>> map, HolderLookup.Provider provider2) {
        UniversalOwner universalOwner = new UniversalOwner();
        MutableObject mutableObject = new MutableObject();
        List list = map.keySet().stream().map(resourceKey -> this.createLazyFullPatchedRegistries(universalOwner, factory, (ResourceKey)resourceKey, provider2, provider, (MutableObject<HolderLookup.Provider>)mutableObject)).collect(Collectors.toUnmodifiableList());
        HolderLookup.Provider provider3 = RegistrySetBuilder.buildProviderWithContext(universalOwner, registryAccess, list.stream());
        mutableObject.setValue((Object)provider3);
        return provider3;
    }

    private <T> HolderLookup.RegistryLookup<T> createLazyFullPatchedRegistries(HolderOwner<T> holderOwner, Cloner.Factory factory, ResourceKey<? extends Registry<? extends T>> resourceKey, HolderLookup.Provider provider, HolderLookup.Provider provider2, MutableObject<HolderLookup.Provider> mutableObject) {
        Cloner cloner = factory.cloner(resourceKey);
        if (cloner == null) {
            throw new NullPointerException("No cloner for " + String.valueOf(resourceKey.location()));
        }
        HashMap hashMap = new HashMap();
        HolderLookup.RegistryLookup registryLookup = provider.lookupOrThrow(resourceKey);
        registryLookup.listElements().forEach(reference -> {
            ResourceKey resourceKey = reference.key();
            LazyHolder lazyHolder = new LazyHolder(holderOwner, resourceKey);
            lazyHolder.supplier = () -> cloner.clone(reference.value(), provider, (HolderLookup.Provider)mutableObject.getValue());
            hashMap.put(resourceKey, lazyHolder);
        });
        HolderLookup.RegistryLookup registryLookup2 = provider2.lookupOrThrow(resourceKey);
        registryLookup2.listElements().forEach(reference -> {
            ResourceKey resourceKey = reference.key();
            hashMap.computeIfAbsent(resourceKey, resourceKey2 -> {
                LazyHolder lazyHolder = new LazyHolder(holderOwner, resourceKey);
                lazyHolder.supplier = () -> cloner.clone(reference.value(), provider2, (HolderLookup.Provider)mutableObject.getValue());
                return lazyHolder;
            });
        });
        Lifecycle lifecycle = registryLookup.registryLifecycle().add(registryLookup2.registryLifecycle());
        return RegistrySetBuilder.lookupFromMap(resourceKey, lifecycle, holderOwner, hashMap);
    }

    public PatchedRegistries buildPatch(RegistryAccess registryAccess, HolderLookup.Provider provider, Cloner.Factory factory) {
        BuildState buildState = this.createState(registryAccess);
        HashMap hashMap = new HashMap();
        this.entries.stream().map(registryStub -> registryStub.collectRegisteredValues(buildState)).forEach(registryContents -> hashMap.put((ResourceKey<Registry<?>>)registryContents.key, (RegistryContents<?>)registryContents));
        Set set = registryAccess.listRegistries().collect(Collectors.toUnmodifiableSet());
        provider.listRegistries().filter(resourceKey -> !set.contains(resourceKey)).forEach(resourceKey -> hashMap.putIfAbsent((ResourceKey<Registry<?>>)resourceKey, new RegistryContents(resourceKey, Lifecycle.stable(), Map.of())));
        Stream<HolderLookup.RegistryLookup<?>> stream = hashMap.values().stream().map(registryContents -> registryContents.buildAsLookup(buildState.owner));
        HolderLookup.Provider provider2 = RegistrySetBuilder.buildProviderWithContext(buildState.owner, registryAccess, stream);
        buildState.reportUnclaimedRegisteredValues();
        buildState.throwOnError();
        HolderLookup.Provider provider3 = this.createLazyFullPatchedRegistries(registryAccess, provider, factory, hashMap, provider2);
        return new PatchedRegistries(provider3, provider2);
    }

    record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistryBootstrap<T> bootstrap) {
        void apply(BuildState buildState) {
            this.bootstrap.run(buildState.bootstrapContext());
        }

        public RegistryContents<T> collectRegisteredValues(BuildState buildState) {
            HashMap hashMap = new HashMap();
            Iterator<Map.Entry<ResourceKey<?>, RegisteredValue<?>>> iterator = buildState.registeredValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceKey<?>, RegisteredValue<?>> entry = iterator.next();
                ResourceKey<?> resourceKey = entry.getKey();
                if (!resourceKey.isFor(this.key)) continue;
                ResourceKey<?> resourceKey2 = resourceKey;
                RegisteredValue<?> registeredValue = entry.getValue();
                Holder.Reference<Object> reference = buildState.lookup.holders.remove(resourceKey);
                hashMap.put(resourceKey2, new ValueAndHolder(registeredValue, Optional.ofNullable(reference)));
                iterator.remove();
            }
            return new RegistryContents(this.key, this.lifecycle, hashMap);
        }
    }

    @FunctionalInterface
    public static interface RegistryBootstrap<T> {
        public void run(BootstrapContext<T> var1);
    }

    static final class BuildState
    extends Record {
        final UniversalOwner owner;
        final UniversalLookup lookup;
        final Map<ResourceLocation, HolderGetter<?>> registries;
        final Map<ResourceKey<?>, RegisteredValue<?>> registeredValues;
        final List<RuntimeException> errors;

        private BuildState(UniversalOwner universalOwner, UniversalLookup universalLookup, Map<ResourceLocation, HolderGetter<?>> map, Map<ResourceKey<?>, RegisteredValue<?>> map2, List<RuntimeException> list) {
            this.owner = universalOwner;
            this.lookup = universalLookup;
            this.registries = map;
            this.registeredValues = map2;
            this.errors = list;
        }

        public static BuildState create(RegistryAccess registryAccess, Stream<ResourceKey<? extends Registry<?>>> stream) {
            UniversalOwner universalOwner = new UniversalOwner();
            ArrayList<RuntimeException> arrayList = new ArrayList<RuntimeException>();
            UniversalLookup universalLookup = new UniversalLookup(universalOwner);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            registryAccess.registries().forEach(registryEntry -> builder.put((Object)registryEntry.key().location(), RegistrySetBuilder.wrapContextLookup(registryEntry.value().asLookup())));
            stream.forEach(resourceKey -> builder.put((Object)resourceKey.location(), (Object)universalLookup));
            return new BuildState(universalOwner, universalLookup, (Map<ResourceLocation, HolderGetter<?>>)builder.build(), new HashMap(), (List<RuntimeException>)arrayList);
        }

        public <T> BootstrapContext<T> bootstrapContext() {
            return new BootstrapContext<T>(){

                @Override
                public Holder.Reference<T> register(ResourceKey<T> resourceKey, T t, Lifecycle lifecycle) {
                    RegisteredValue registeredValue = registeredValues.put(resourceKey, new RegisteredValue(t, lifecycle));
                    if (registeredValue != null) {
                        errors.add(new IllegalStateException("Duplicate registration for " + String.valueOf(resourceKey) + ", new=" + String.valueOf(t) + ", old=" + String.valueOf(registeredValue.value)));
                    }
                    return lookup.getOrCreate(resourceKey);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey) {
                    return registries.getOrDefault(resourceKey.location(), lookup);
                }
            };
        }

        public void reportUnclaimedRegisteredValues() {
            this.registeredValues.forEach((resourceKey, registeredValue) -> this.errors.add(new IllegalStateException("Orpaned value " + String.valueOf(registeredValue.value) + " for key " + String.valueOf(resourceKey))));
        }

        public void reportNotCollectedHolders() {
            for (ResourceKey<Object> resourceKey : this.lookup.holders.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + String.valueOf(resourceKey)));
            }
        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");
                for (RuntimeException runtimeException : this.errors) {
                    illegalStateException.addSuppressed(runtimeException);
                }
                throw illegalStateException;
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BuildState.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BuildState.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BuildState.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this, object);
        }

        public UniversalOwner owner() {
            return this.owner;
        }

        public UniversalLookup lookup() {
            return this.lookup;
        }

        public Map<ResourceLocation, HolderGetter<?>> registries() {
            return this.registries;
        }

        public Map<ResourceKey<?>, RegisteredValue<?>> registeredValues() {
            return this.registeredValues;
        }

        public List<RuntimeException> errors() {
            return this.errors;
        }
    }

    static class UniversalOwner
    implements HolderOwner<Object> {
        UniversalOwner() {
        }

        public <T> HolderOwner<T> cast() {
            return this;
        }
    }

    public record PatchedRegistries(HolderLookup.Provider full, HolderLookup.Provider patches) {
    }

    static final class RegistryContents<T>
    extends Record {
        final ResourceKey<? extends Registry<? extends T>> key;
        private final Lifecycle lifecycle;
        private final Map<ResourceKey<T>, ValueAndHolder<T>> values;

        RegistryContents(ResourceKey<? extends Registry<? extends T>> resourceKey, Lifecycle lifecycle, Map<ResourceKey<T>, ValueAndHolder<T>> map) {
            this.key = resourceKey;
            this.lifecycle = lifecycle;
            this.values = map;
        }

        public HolderLookup.RegistryLookup<T> buildAsLookup(UniversalOwner universalOwner) {
            Map map = this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> {
                ValueAndHolder valueAndHolder = (ValueAndHolder)entry.getValue();
                Holder.Reference reference = valueAndHolder.holder().orElseGet(() -> Holder.Reference.createStandAlone(universalOwner.cast(), (ResourceKey)entry.getKey()));
                reference.bindValue(valueAndHolder.value().value());
                return reference;
            }));
            return RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, universalOwner.cast(), map);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryContents.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryContents.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryContents.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this, object);
        }

        public ResourceKey<? extends Registry<? extends T>> key() {
            return this.key;
        }

        public Lifecycle lifecycle() {
            return this.lifecycle;
        }

        public Map<ResourceKey<T>, ValueAndHolder<T>> values() {
            return this.values;
        }
    }

    static class LazyHolder<T>
    extends Holder.Reference<T> {
        @Nullable
        Supplier<T> supplier;

        protected LazyHolder(HolderOwner<T> holderOwner, @Nullable ResourceKey<T> resourceKey) {
            super(Holder.Reference.Type.STAND_ALONE, holderOwner, resourceKey, null);
        }

        @Override
        protected void bindValue(T t) {
            super.bindValue(t);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.bindValue(this.supplier.get());
            }
            return super.value();
        }
    }

    record ValueAndHolder<T>(RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
    }

    static final class RegisteredValue<T>
    extends Record {
        final T value;
        private final Lifecycle lifecycle;

        RegisteredValue(T t, Lifecycle lifecycle) {
            this.value = t;
            this.lifecycle = lifecycle;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this, object);
        }

        public T value() {
            return this.value;
        }

        public Lifecycle lifecycle() {
            return this.lifecycle;
        }
    }

    static class UniversalLookup
    extends EmptyTagLookup<Object> {
        final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<ResourceKey<Object>, Holder.Reference<Object>>();

        public UniversalLookup(HolderOwner<Object> holderOwner) {
            super(holderOwner);
        }

        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> resourceKey) {
            return Optional.of(this.getOrCreate(resourceKey));
        }

        <T> Holder.Reference<T> getOrCreate(ResourceKey<T> resourceKey2) {
            return this.holders.computeIfAbsent(resourceKey2, resourceKey -> Holder.Reference.createStandAlone(this.owner, resourceKey));
        }
    }

    static class EmptyTagLookupWrapper<T>
    extends EmptyTagRegistryLookup<T>
    implements HolderLookup.RegistryLookup.Delegate<T> {
        private final HolderLookup.RegistryLookup<T> parent;

        EmptyTagLookupWrapper(HolderOwner<T> holderOwner, HolderLookup.RegistryLookup<T> registryLookup) {
            super(holderOwner);
            this.parent = registryLookup;
        }

        @Override
        public HolderLookup.RegistryLookup<T> parent() {
            return this.parent;
        }
    }

    static abstract class EmptyTagRegistryLookup<T>
    extends EmptyTagLookup<T>
    implements HolderLookup.RegistryLookup<T> {
        protected EmptyTagRegistryLookup(HolderOwner<T> holderOwner) {
            super(holderOwner);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            throw new UnsupportedOperationException("Tags are not available in datagen");
        }
    }

    static abstract class EmptyTagLookup<T>
    implements HolderGetter<T> {
        protected final HolderOwner<T> owner;

        protected EmptyTagLookup(HolderOwner<T> holderOwner) {
            this.owner = holderOwner;
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
            return Optional.of(HolderSet.emptyNamed(this.owner, tagKey));
        }
    }
}

