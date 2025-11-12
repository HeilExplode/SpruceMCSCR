/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class MappedRegistry<T>
implements WritableRegistry<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ResourceKey<? extends Registry<T>> key;
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList(256);
    private final Reference2IntMap<T> toId = (Reference2IntMap)Util.make(new Reference2IntOpenHashMap(), reference2IntOpenHashMap -> reference2IntOpenHashMap.defaultReturnValue(-1));
    private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<ResourceLocation, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<ResourceKey<T>, Holder.Reference<T>>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<T, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos = new IdentityHashMap<ResourceKey<T>, RegistrationInfo>();
    private Lifecycle registryLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>();
    private boolean frozen;
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    private final HolderLookup.RegistryLookup<T> lookup = new HolderLookup.RegistryLookup<T>(){

        @Override
        public ResourceKey<? extends Registry<? extends T>> key() {
            return MappedRegistry.this.key;
        }

        @Override
        public Lifecycle registryLifecycle() {
            return MappedRegistry.this.registryLifecycle();
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
            return MappedRegistry.this.getHolder(resourceKey);
        }

        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return MappedRegistry.this.holders();
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
            return MappedRegistry.this.getTag(tagKey);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return MappedRegistry.this.getTags().map(Pair::getSecond);
        }
    };
    private final Object tagAdditionLock = new Object();

    public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
        this(resourceKey, lifecycle, false);
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, boolean bl) {
        this.key = resourceKey;
        this.registryLifecycle = lifecycle;
        if (bl) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap<T, Holder.Reference<T>>();
        }
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    public String toString() {
        return "Registry[" + String.valueOf(this.key) + " (" + String.valueOf(this.registryLifecycle) + ")]";
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> resourceKey) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + String.valueOf(resourceKey) + ")");
        }
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> resourceKey2, T t, RegistrationInfo registrationInfo) {
        Holder.Reference reference;
        this.validateWrite(resourceKey2);
        Objects.requireNonNull(resourceKey2);
        Objects.requireNonNull(t);
        if (this.byLocation.containsKey(resourceKey2.location())) {
            Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + String.valueOf(resourceKey2) + "' to registry"));
        }
        if (this.byValue.containsKey(t)) {
            Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + String.valueOf(t) + "' to registry"));
        }
        if (this.unregisteredIntrusiveHolders != null) {
            reference = this.unregisteredIntrusiveHolders.remove(t);
            if (reference == null) {
                throw new AssertionError((Object)("Missing intrusive holder for " + String.valueOf(resourceKey2) + ":" + String.valueOf(t)));
            }
            reference.bindKey(resourceKey2);
        } else {
            reference = this.byKey.computeIfAbsent(resourceKey2, resourceKey -> Holder.Reference.createStandAlone(this.holderOwner(), resourceKey));
        }
        this.byKey.put(resourceKey2, reference);
        this.byLocation.put(resourceKey2.location(), reference);
        this.byValue.put(t, reference);
        int n = this.byId.size();
        this.byId.add((Object)reference);
        this.toId.put(t, n);
        this.registrationInfos.put(resourceKey2, registrationInfo);
        this.registryLifecycle = this.registryLifecycle.add(registrationInfo.lifecycle());
        return reference;
    }

    @Override
    @Nullable
    public ResourceLocation getKey(T t) {
        Holder.Reference<T> reference = this.byValue.get(t);
        return reference != null ? reference.key().location() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T t) {
        return Optional.ofNullable(this.byValue.get(t)).map(Holder.Reference::key);
    }

    @Override
    public int getId(@Nullable T t) {
        return this.toId.getInt(t);
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceKey<T> resourceKey) {
        return MappedRegistry.getValueFromNullable(this.byKey.get(resourceKey));
    }

    @Override
    @Nullable
    public T byId(int n) {
        if (n < 0 || n >= this.byId.size()) {
            return null;
        }
        return ((Holder.Reference)this.byId.get(n)).value();
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(int n) {
        if (n < 0 || n >= this.byId.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable((Holder.Reference)this.byId.get(n));
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.byLocation.get(resourceLocation));
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.byKey.get(resourceKey));
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return this.byId.isEmpty() ? Optional.empty() : Optional.of((Holder.Reference)this.byId.getFirst());
    }

    @Override
    public Holder<T> wrapAsHolder(T t) {
        Holder.Reference<T> reference = this.byValue.get(t);
        return reference != null ? reference : Holder.direct(t);
    }

    Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> resourceKey2) {
        return this.byKey.computeIfAbsent(resourceKey2, resourceKey -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            this.validateWrite((ResourceKey<T>)resourceKey);
            return Holder.Reference.createStandAlone(this.holderOwner(), resourceKey);
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.registrationInfos.get(resourceKey));
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform((Iterator)this.byId.iterator(), Holder::value);
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceLocation resourceLocation) {
        Holder.Reference<T> reference = this.byLocation.get(resourceLocation);
        return MappedRegistry.getValueFromNullable(reference);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> reference) {
        return reference != null ? (T)reference.value() : null;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> holders() {
        return this.byId.stream();
    }

    @Override
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags.entrySet().stream().map(entry -> Pair.of((Object)((TagKey)entry.getKey()), (Object)((HolderSet.Named)entry.getValue())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagKey) {
        HolderSet.Named<T> named = this.tags.get(tagKey);
        if (named != null) {
            return named;
        }
        Object object = this.tagAdditionLock;
        synchronized (object) {
            named = this.tags.get(tagKey);
            if (named != null) {
                return named;
            }
            named = this.createTag(tagKey);
            IdentityHashMap<TagKey<T>, HolderSet.Named<T>> identityHashMap = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>(this.tags);
            identityHashMap.put(tagKey, named);
            this.tags = identityHashMap;
        }
        return named;
    }

    private HolderSet.Named<T> createTag(TagKey<T> tagKey) {
        return new HolderSet.Named<T>(this.holderOwner(), tagKey);
    }

    @Override
    public Stream<TagKey<T>> getTagNames() {
        return this.tags.keySet().stream();
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource randomSource) {
        return Util.getRandomSafe(this.byId, randomSource);
    }

    @Override
    public boolean containsKey(ResourceLocation resourceLocation) {
        return this.byLocation.containsKey(resourceLocation);
    }

    @Override
    public boolean containsKey(ResourceKey<T> resourceKey) {
        return this.byKey.containsKey(resourceKey);
    }

    @Override
    public Registry<T> freeze() {
        if (this.frozen) {
            return this;
        }
        this.frozen = true;
        this.byValue.forEach((? super K object, ? super V reference) -> reference.bindValue(object));
        List<ResourceLocation> list = this.byKey.entrySet().stream().filter(entry -> !((Holder.Reference)entry.getValue()).isBound()).map(entry -> ((ResourceKey)entry.getKey()).location()).sorted().toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + String.valueOf(this.key()) + ": " + String.valueOf(list));
        }
        if (this.unregisteredIntrusiveHolders != null) {
            if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                throw new IllegalStateException("Some intrusive holders were not registered: " + String.valueOf(this.unregisteredIntrusiveHolders.values()));
            }
            this.unregisteredIntrusiveHolders = null;
        }
        return this;
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T t) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        this.validateWrite();
        return this.unregisteredIntrusiveHolders.computeIfAbsent(t, object -> Holder.Reference.createIntrusive(this.asLookup(), object));
    }

    @Override
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagKey) {
        return Optional.ofNullable(this.tags.get(tagKey));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> map) {
        IdentityHashMap<Holder.Reference, List> identityHashMap = new IdentityHashMap<Holder.Reference, List>();
        this.byKey.values().forEach(reference -> identityHashMap.put((Holder.Reference)reference, new ArrayList()));
        map.forEach((? super K tagKey, ? super V list) -> {
            for (Holder holder : list) {
                if (!holder.canSerializeIn(this.asLookup())) {
                    throw new IllegalStateException("Can't create named set " + String.valueOf(tagKey) + " containing value " + String.valueOf(holder) + " from outside registry " + String.valueOf(this));
                }
                if (holder instanceof Holder.Reference) {
                    Holder.Reference reference = (Holder.Reference)holder;
                    ((List)identityHashMap.get(reference)).add(tagKey);
                    continue;
                }
                throw new IllegalStateException("Found direct holder " + String.valueOf(holder) + " value in tag " + String.valueOf(tagKey));
            }
        });
        Sets.SetView setView = Sets.difference(this.tags.keySet(), map.keySet());
        if (!setView.isEmpty()) {
            LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.key(), (Object)setView.stream().map(tagKey -> tagKey.location().toString()).sorted().collect(Collectors.joining(", ")));
        }
        Object object = this.tagAdditionLock;
        synchronized (object) {
            IdentityHashMap<TagKey<T>, HolderSet.Named<T>> identityHashMap2 = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>(this.tags);
            map.forEach((? super K tagKey, ? super V list) -> identityHashMap2.computeIfAbsent((TagKey<T>)tagKey, this::createTag).bind(list));
            identityHashMap.forEach(Holder.Reference::bindTags);
            this.tags = identityHashMap2;
        }
    }

    @Override
    public void resetTags() {
        this.tags.values().forEach(named -> named.bind(List.of()));
        this.byKey.values().forEach(reference -> reference.bindTags(Set.of()));
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>(){

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
                return Optional.of(this.getOrThrow(resourceKey));
            }

            @Override
            public Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
                return MappedRegistry.this.getOrCreateHolderOrThrow(resourceKey);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                return Optional.of(this.getOrThrow(tagKey));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
                return MappedRegistry.this.getOrCreateTag(tagKey);
            }
        };
    }

    @Override
    public HolderOwner<T> holderOwner() {
        return this.lookup;
    }

    @Override
    public HolderLookup.RegistryLookup<T> asLookup() {
        return this.lookup;
    }
}

