/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.player.Player;

public class AngerManagement {
    @VisibleForTesting
    protected static final int CONVERSION_DELAY = 2;
    @VisibleForTesting
    protected static final int MAX_ANGER = 150;
    private static final int DEFAULT_ANGER_DECREASE = 1;
    private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 2);
    int highestAnger;
    private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create(instance -> instance.group((App)UUIDUtil.CODEC.fieldOf("uuid").forGetter(Pair::getFirst), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond)).apply((Applicative)instance, Pair::of));
    private final Predicate<Entity> filter;
    @VisibleForTesting
    protected final ArrayList<Entity> suspects;
    private final Sorter suspectSorter;
    @VisibleForTesting
    protected final Object2IntMap<Entity> angerBySuspect;
    @VisibleForTesting
    protected final Object2IntMap<UUID> angerByUuid;

    public static Codec<AngerManagement> codec(Predicate<Entity> predicate) {
        return RecordCodecBuilder.create(instance -> instance.group((App)SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs)).apply((Applicative)instance, list -> new AngerManagement(predicate, (List<Pair<UUID, Integer>>)list)));
    }

    public AngerManagement(Predicate<Entity> predicate, List<Pair<UUID, Integer>> list) {
        this.filter = predicate;
        this.suspects = new ArrayList();
        this.suspectSorter = new Sorter(this);
        this.angerBySuspect = new Object2IntOpenHashMap();
        this.angerByUuid = new Object2IntOpenHashMap(list.size());
        list.forEach(pair -> this.angerByUuid.put((Object)((UUID)pair.getFirst()), (Integer)pair.getSecond()));
    }

    private List<Pair<UUID, Integer>> createUuidAngerPairs() {
        return Streams.concat((Stream[])new Stream[]{this.suspects.stream().map(entity -> Pair.of((Object)entity.getUUID(), (Object)this.angerBySuspect.getInt(entity))), this.angerByUuid.object2IntEntrySet().stream().map(entry -> Pair.of((Object)((UUID)entry.getKey()), (Object)entry.getIntValue()))}).collect(Collectors.toList());
    }

    public void tick(ServerLevel serverLevel, Predicate<Entity> predicate) {
        Object2IntMap.Entry entry;
        --this.conversionDelay;
        if (this.conversionDelay <= 0) {
            this.convertFromUuids(serverLevel);
            this.conversionDelay = 2;
        }
        ObjectIterator objectIterator = this.angerByUuid.object2IntEntrySet().iterator();
        while (objectIterator.hasNext()) {
            entry = (Object2IntMap.Entry)objectIterator.next();
            int n = entry.getIntValue();
            if (n <= 1) {
                objectIterator.remove();
                continue;
            }
            entry.setValue(n - 1);
        }
        entry = this.angerBySuspect.object2IntEntrySet().iterator();
        while (entry.hasNext()) {
            Object2IntMap.Entry entry2 = (Object2IntMap.Entry)entry.next();
            int n = entry2.getIntValue();
            Entity entity = (Entity)entry2.getKey();
            Entity.RemovalReason removalReason = entity.getRemovalReason();
            if (n <= 1 || !predicate.test(entity) || removalReason != null) {
                this.suspects.remove(entity);
                entry.remove();
                if (n <= 1 || removalReason == null) continue;
                switch (removalReason) {
                    case CHANGED_DIMENSION: 
                    case UNLOADED_TO_CHUNK: 
                    case UNLOADED_WITH_PLAYER: {
                        this.angerByUuid.put((Object)entity.getUUID(), n - 1);
                    }
                }
                continue;
            }
            entry2.setValue(n - 1);
        }
        this.sortAndUpdateHighestAnger();
    }

    private void sortAndUpdateHighestAnger() {
        this.highestAnger = 0;
        this.suspects.sort(this.suspectSorter);
        if (this.suspects.size() == 1) {
            this.highestAnger = this.angerBySuspect.getInt((Object)this.suspects.get(0));
        }
    }

    private void convertFromUuids(ServerLevel serverLevel) {
        ObjectIterator objectIterator = this.angerByUuid.object2IntEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            int n = entry.getIntValue();
            Entity entity = serverLevel.getEntity((UUID)entry.getKey());
            if (entity == null) continue;
            this.angerBySuspect.put((Object)entity, n);
            this.suspects.add(entity);
            objectIterator.remove();
        }
    }

    public int increaseAnger(Entity entity2, int n) {
        boolean bl = !this.angerBySuspect.containsKey((Object)entity2);
        int n3 = this.angerBySuspect.computeInt((Object)entity2, (entity, n2) -> Math.min(150, (n2 == null ? 0 : n2) + n));
        if (bl) {
            int n4 = this.angerByUuid.removeInt((Object)entity2.getUUID());
            this.angerBySuspect.put((Object)entity2, n3 += n4);
            this.suspects.add(entity2);
        }
        this.sortAndUpdateHighestAnger();
        return n3;
    }

    public void clearAnger(Entity entity) {
        this.angerBySuspect.removeInt((Object)entity);
        this.suspects.remove(entity);
        this.sortAndUpdateHighestAnger();
    }

    @Nullable
    private Entity getTopSuspect() {
        return this.suspects.stream().filter(this.filter).findFirst().orElse(null);
    }

    public int getActiveAnger(@Nullable Entity entity) {
        return entity == null ? this.highestAnger : this.angerBySuspect.getInt((Object)entity);
    }

    public Optional<LivingEntity> getActiveEntity() {
        return Optional.ofNullable(this.getTopSuspect()).filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity)entity);
    }

    @VisibleForTesting
    protected record Sorter(AngerManagement angerManagement) implements Comparator<Entity>
    {
        @Override
        public int compare(Entity entity, Entity entity2) {
            boolean bl;
            if (entity.equals(entity2)) {
                return 0;
            }
            int n = this.angerManagement.angerBySuspect.getOrDefault((Object)entity, 0);
            int n2 = this.angerManagement.angerBySuspect.getOrDefault((Object)entity2, 0);
            this.angerManagement.highestAnger = Math.max(this.angerManagement.highestAnger, Math.max(n, n2));
            boolean bl2 = AngerLevel.byAnger(n).isAngry();
            if (bl2 != (bl = AngerLevel.byAnger(n2).isAngry())) {
                return bl2 ? -1 : 1;
            }
            boolean bl3 = entity instanceof Player;
            boolean bl4 = entity2 instanceof Player;
            if (bl3 != bl4) {
                return bl3 ? -1 : 1;
            }
            return Integer.compare(n2, n);
        }

        @Override
        public /* synthetic */ int compare(Object object, Object object2) {
            return this.compare((Entity)object, (Entity)object2);
        }
    }
}

