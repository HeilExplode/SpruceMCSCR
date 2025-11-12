/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ai.gossip.GossipType;
import org.slf4j.Logger;

public class GossipContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DISCARD_THRESHOLD = 2;
    private final Map<UUID, EntityGossips> gossips = Maps.newHashMap();

    @VisibleForDebug
    public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
        HashMap hashMap = Maps.newHashMap();
        this.gossips.keySet().forEach(uUID -> {
            EntityGossips entityGossips = this.gossips.get(uUID);
            hashMap.put(uUID, entityGossips.entries);
        });
        return hashMap;
    }

    public void decay() {
        Iterator<EntityGossips> iterator = this.gossips.values().iterator();
        while (iterator.hasNext()) {
            EntityGossips entityGossips = iterator.next();
            entityGossips.decay();
            if (!entityGossips.isEmpty()) continue;
            iterator.remove();
        }
    }

    private Stream<GossipEntry> unpack() {
        return this.gossips.entrySet().stream().flatMap(entry -> ((EntityGossips)entry.getValue()).unpack((UUID)entry.getKey()));
    }

    private Collection<GossipEntry> selectGossipsForTransfer(RandomSource randomSource, int n) {
        List<GossipEntry> list = this.unpack().toList();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] nArray = new int[list.size()];
        int n2 = 0;
        for (int i = 0; i < list.size(); ++i) {
            GossipEntry gossipEntry = list.get(i);
            nArray[i] = (n2 += Math.abs(gossipEntry.weightedValue())) - 1;
        }
        Set set = Sets.newIdentityHashSet();
        for (int i = 0; i < n; ++i) {
            int n3 = randomSource.nextInt(n2);
            int n4 = Arrays.binarySearch(nArray, n3);
            set.add(list.get(n4 < 0 ? -n4 - 1 : n4));
        }
        return set;
    }

    private EntityGossips getOrCreate(UUID uUID2) {
        return this.gossips.computeIfAbsent(uUID2, uUID -> new EntityGossips());
    }

    public void transferFrom(GossipContainer gossipContainer, RandomSource randomSource, int n) {
        Collection<GossipEntry> collection = gossipContainer.selectGossipsForTransfer(randomSource, n);
        collection.forEach(gossipEntry -> {
            int n = gossipEntry.value - gossipEntry.type.decayPerTransfer;
            if (n >= 2) {
                this.getOrCreate((UUID)gossipEntry.target).entries.mergeInt((Object)gossipEntry.type, n, GossipContainer::mergeValuesForTransfer);
            }
        });
    }

    public int getReputation(UUID uUID, Predicate<GossipType> predicate) {
        EntityGossips entityGossips = this.gossips.get(uUID);
        return entityGossips != null ? entityGossips.weightedValue(predicate) : 0;
    }

    public long getCountForType(GossipType gossipType, DoublePredicate doublePredicate) {
        return this.gossips.values().stream().filter(entityGossips -> doublePredicate.test(entityGossips.entries.getOrDefault((Object)gossipType, 0) * gossipType.weight)).count();
    }

    public void add(UUID uUID, GossipType gossipType, int n3) {
        EntityGossips entityGossips = this.getOrCreate(uUID);
        entityGossips.entries.mergeInt((Object)gossipType, n3, (n, n2) -> this.mergeValuesForAddition(gossipType, n, n2));
        entityGossips.makeSureValueIsntTooLowOrTooHigh(gossipType);
        if (entityGossips.isEmpty()) {
            this.gossips.remove(uUID);
        }
    }

    public void remove(UUID uUID, GossipType gossipType, int n) {
        this.add(uUID, gossipType, -n);
    }

    public void remove(UUID uUID, GossipType gossipType) {
        EntityGossips entityGossips = this.gossips.get(uUID);
        if (entityGossips != null) {
            entityGossips.remove(gossipType);
            if (entityGossips.isEmpty()) {
                this.gossips.remove(uUID);
            }
        }
    }

    public void remove(GossipType gossipType) {
        Iterator<EntityGossips> iterator = this.gossips.values().iterator();
        while (iterator.hasNext()) {
            EntityGossips entityGossips = iterator.next();
            entityGossips.remove(gossipType);
            if (!entityGossips.isEmpty()) continue;
            iterator.remove();
        }
    }

    public <T> T store(DynamicOps<T> dynamicOps) {
        return (T)GossipEntry.LIST_CODEC.encodeStart(dynamicOps, this.unpack().toList()).resultOrPartial(string -> LOGGER.warn("Failed to serialize gossips: {}", string)).orElseGet(() -> dynamicOps.emptyList());
    }

    public void update(Dynamic<?> dynamic) {
        GossipEntry.LIST_CODEC.decode(dynamic).resultOrPartial(string -> LOGGER.warn("Failed to deserialize gossips: {}", string)).stream().flatMap(pair -> ((List)pair.getFirst()).stream()).forEach(gossipEntry -> this.getOrCreate((UUID)gossipEntry.target).entries.put((Object)gossipEntry.type, gossipEntry.value));
    }

    private static int mergeValuesForTransfer(int n, int n2) {
        return Math.max(n, n2);
    }

    private int mergeValuesForAddition(GossipType gossipType, int n, int n2) {
        int n3 = n + n2;
        return n3 > gossipType.max ? Math.max(gossipType.max, n) : n3;
    }

    static class EntityGossips {
        final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap();

        EntityGossips() {
        }

        public int weightedValue(Predicate<GossipType> predicate) {
            return this.entries.object2IntEntrySet().stream().filter(entry -> predicate.test((GossipType)entry.getKey())).mapToInt(entry -> entry.getIntValue() * ((GossipType)entry.getKey()).weight).sum();
        }

        public Stream<GossipEntry> unpack(UUID uUID) {
            return this.entries.object2IntEntrySet().stream().map(entry -> new GossipEntry(uUID, (GossipType)entry.getKey(), entry.getIntValue()));
        }

        public void decay() {
            ObjectIterator objectIterator = this.entries.object2IntEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
                int n = entry.getIntValue() - ((GossipType)entry.getKey()).decayPerDay;
                if (n < 2) {
                    objectIterator.remove();
                    continue;
                }
                entry.setValue(n);
            }
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public void makeSureValueIsntTooLowOrTooHigh(GossipType gossipType) {
            int n = this.entries.getInt((Object)gossipType);
            if (n > gossipType.max) {
                this.entries.put((Object)gossipType, gossipType.max);
            }
            if (n < 2) {
                this.remove(gossipType);
            }
        }

        public void remove(GossipType gossipType) {
            this.entries.removeInt((Object)gossipType);
        }
    }

    static final class GossipEntry
    extends Record {
        final UUID target;
        final GossipType type;
        final int value;
        public static final Codec<GossipEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)UUIDUtil.CODEC.fieldOf("Target").forGetter(GossipEntry::target), (App)GossipType.CODEC.fieldOf("Type").forGetter(GossipEntry::type), (App)ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipEntry::value)).apply((Applicative)instance, GossipEntry::new));
        public static final Codec<List<GossipEntry>> LIST_CODEC = CODEC.listOf();

        GossipEntry(UUID uUID, GossipType gossipType, int n) {
            this.target = uUID;
            this.type = gossipType;
            this.value = n;
        }

        public int weightedValue() {
            return this.value * this.type.weight;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this, object);
        }

        public UUID target() {
            return this.target;
        }

        public GossipType type() {
            return this.type;
        }

        public int value() {
            return this.value;
        }
    }
}

