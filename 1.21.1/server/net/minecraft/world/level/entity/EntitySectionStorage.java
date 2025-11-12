/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectFunction
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongAVLTreeSet
 *  it.unimi.dsi.fastutil.longs.LongBidirectionalIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.longs.LongSortedSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;

public class EntitySectionStorage<T extends EntityAccess> {
    private final Class<T> entityClass;
    private final Long2ObjectFunction<Visibility> intialSectionVisibility;
    private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap();
    private final LongSortedSet sectionIds = new LongAVLTreeSet();

    public EntitySectionStorage(Class<T> clazz, Long2ObjectFunction<Visibility> long2ObjectFunction) {
        this.entityClass = clazz;
        this.intialSectionVisibility = long2ObjectFunction;
    }

    public void forEachAccessibleNonEmptySection(AABB aABB, AbortableIterationConsumer<EntitySection<T>> abortableIterationConsumer) {
        int n = 2;
        int n2 = SectionPos.posToSectionCoord(aABB.minX - 2.0);
        int n3 = SectionPos.posToSectionCoord(aABB.minY - 4.0);
        int n4 = SectionPos.posToSectionCoord(aABB.minZ - 2.0);
        int n5 = SectionPos.posToSectionCoord(aABB.maxX + 2.0);
        int n6 = SectionPos.posToSectionCoord(aABB.maxY + 0.0);
        int n7 = SectionPos.posToSectionCoord(aABB.maxZ + 2.0);
        for (int i = n2; i <= n5; ++i) {
            long l = SectionPos.asLong(i, 0, 0);
            long l2 = SectionPos.asLong(i, -1, -1);
            LongBidirectionalIterator longBidirectionalIterator = this.sectionIds.subSet(l, l2 + 1L).iterator();
            while (longBidirectionalIterator.hasNext()) {
                EntitySection entitySection;
                long l3 = longBidirectionalIterator.nextLong();
                int n8 = SectionPos.y(l3);
                int n9 = SectionPos.z(l3);
                if (n8 < n3 || n8 > n6 || n9 < n4 || n9 > n7 || (entitySection = (EntitySection)this.sections.get(l3)) == null || entitySection.isEmpty() || !entitySection.getStatus().isAccessible() || !abortableIterationConsumer.accept(entitySection).shouldAbort()) continue;
                return;
            }
        }
    }

    public LongStream getExistingSectionPositionsInChunk(long l) {
        int n;
        int n2 = ChunkPos.getX(l);
        LongSortedSet longSortedSet = this.getChunkSections(n2, n = ChunkPos.getZ(l));
        if (longSortedSet.isEmpty()) {
            return LongStream.empty();
        }
        LongBidirectionalIterator longBidirectionalIterator = longSortedSet.iterator();
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize((PrimitiveIterator.OfLong)longBidirectionalIterator, 1301), false);
    }

    private LongSortedSet getChunkSections(int n, int n2) {
        long l = SectionPos.asLong(n, 0, n2);
        long l2 = SectionPos.asLong(n, -1, n2);
        return this.sectionIds.subSet(l, l2 + 1L);
    }

    public Stream<EntitySection<T>> getExistingSectionsInChunk(long l) {
        return this.getExistingSectionPositionsInChunk(l).mapToObj(arg_0 -> this.sections.get(arg_0)).filter(Objects::nonNull);
    }

    private static long getChunkKeyFromSectionKey(long l) {
        return ChunkPos.asLong(SectionPos.x(l), SectionPos.z(l));
    }

    public EntitySection<T> getOrCreateSection(long l) {
        return (EntitySection)this.sections.computeIfAbsent(l, this::createSection);
    }

    @Nullable
    public EntitySection<T> getSection(long l) {
        return (EntitySection)this.sections.get(l);
    }

    private EntitySection<T> createSection(long l) {
        long l2 = EntitySectionStorage.getChunkKeyFromSectionKey(l);
        Visibility visibility = (Visibility)((Object)this.intialSectionVisibility.get(l2));
        this.sectionIds.add(l);
        return new EntitySection<T>(this.entityClass, visibility);
    }

    public LongSet getAllChunksWithExistingSections() {
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        this.sections.keySet().forEach(arg_0 -> EntitySectionStorage.lambda$getAllChunksWithExistingSections$0((LongSet)longOpenHashSet, arg_0));
        return longOpenHashSet;
    }

    public void getEntities(AABB aABB, AbortableIterationConsumer<T> abortableIterationConsumer) {
        this.forEachAccessibleNonEmptySection(aABB, entitySection -> entitySection.getEntities(aABB, abortableIterationConsumer));
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<U> abortableIterationConsumer) {
        this.forEachAccessibleNonEmptySection(aABB, entitySection -> entitySection.getEntities(entityTypeTest, aABB, abortableIterationConsumer));
    }

    public void remove(long l) {
        this.sections.remove(l);
        this.sectionIds.remove(l);
    }

    @VisibleForDebug
    public int count() {
        return this.sectionIds.size();
    }

    private static /* synthetic */ void lambda$getAllChunksWithExistingSections$0(LongSet longSet, long l) {
        longSet.add(EntitySectionStorage.getChunkKeyFromSectionKey(l));
    }
}

