/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMaps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LightEngine;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> {
    private final LightLayer layer;
    protected final LightChunkGetter chunkSource;
    protected final Long2ByteMap sectionStates = new Long2ByteOpenHashMap();
    private final LongSet columnsWithSources = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize((Long2ObjectMap)new Long2ObjectOpenHashMap());
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasInconsistencies;

    protected LayerLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, M m) {
        this.layer = lightLayer;
        this.chunkSource = lightChunkGetter;
        this.updatingSectionData = m;
        this.visibleSectionData = ((DataLayerStorageMap)m).copy();
        ((DataLayerStorageMap)this.visibleSectionData).disableCache();
        this.sectionStates.defaultReturnValue((byte)0);
    }

    protected boolean storingLightForSection(long l) {
        return this.getDataLayer(l, true) != null;
    }

    @Nullable
    protected DataLayer getDataLayer(long l, boolean bl) {
        return this.getDataLayer(bl ? this.updatingSectionData : this.visibleSectionData, l);
    }

    @Nullable
    protected DataLayer getDataLayer(M m, long l) {
        return ((DataLayerStorageMap)m).getLayer(l);
    }

    @Nullable
    protected DataLayer getDataLayerToWrite(long l) {
        DataLayer dataLayer = ((DataLayerStorageMap)this.updatingSectionData).getLayer(l);
        if (dataLayer == null) {
            return null;
        }
        if (this.changedSections.add(l)) {
            dataLayer = dataLayer.copy();
            ((DataLayerStorageMap)this.updatingSectionData).setLayer(l, dataLayer);
            ((DataLayerStorageMap)this.updatingSectionData).clearCache();
        }
        return dataLayer;
    }

    @Nullable
    public DataLayer getDataLayerData(long l) {
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        return this.getDataLayer(l, false);
    }

    protected abstract int getLightValue(long var1);

    protected int getStoredLevel(long l) {
        long l2 = SectionPos.blockToSection(l);
        DataLayer dataLayer = this.getDataLayer(l2, true);
        return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    protected void setStoredLevel(long l, int n) {
        long l2 = SectionPos.blockToSection(l);
        DataLayer dataLayer = this.changedSections.add(l2) ? ((DataLayerStorageMap)this.updatingSectionData).copyDataLayer(l2) : this.getDataLayer(l2, true);
        dataLayer.set(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)), n);
        SectionPos.aroundAndAtBlockPos(l, arg_0 -> ((LongSet)this.sectionsAffectedByLightUpdates).add(arg_0));
    }

    protected void markSectionAndNeighborsAsAffected(long l) {
        int n = SectionPos.x(l);
        int n2 = SectionPos.y(l);
        int n3 = SectionPos.z(l);
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(n + j, n2 + k, n3 + i));
                }
            }
        }
    }

    protected DataLayer createDataLayer(long l) {
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        return new DataLayer();
    }

    protected boolean hasInconsistencies() {
        return this.hasInconsistencies;
    }

    protected void markNewInconsistencies(LightEngine<M, ?> lightEngine) {
        DataLayer dataLayer;
        long l;
        if (!this.hasInconsistencies) {
            return;
        }
        this.hasInconsistencies = false;
        LongIterator longIterator = this.toRemove.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            DataLayer dataLayer2 = (DataLayer)this.queuedSections.remove(l);
            dataLayer = ((DataLayerStorageMap)this.updatingSectionData).removeLayer(l);
            if (!this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(l))) continue;
            if (dataLayer2 != null) {
                this.queuedSections.put(l, (Object)dataLayer2);
                continue;
            }
            if (dataLayer == null) continue;
            this.queuedSections.put(l, (Object)dataLayer);
        }
        ((DataLayerStorageMap)this.updatingSectionData).clearCache();
        longIterator = this.toRemove.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            this.onNodeRemoved(l);
            this.changedSections.add(l);
        }
        this.toRemove.clear();
        longIterator = Long2ObjectMaps.fastIterator(this.queuedSections);
        while (longIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)longIterator.next();
            long l2 = entry.getLongKey();
            if (!this.storingLightForSection(l2)) continue;
            dataLayer = (DataLayer)entry.getValue();
            if (((DataLayerStorageMap)this.updatingSectionData).getLayer(l2) != dataLayer) {
                ((DataLayerStorageMap)this.updatingSectionData).setLayer(l2, dataLayer);
                this.changedSections.add(l2);
            }
            longIterator.remove();
        }
        ((DataLayerStorageMap)this.updatingSectionData).clearCache();
    }

    protected void onNodeAdded(long l) {
    }

    protected void onNodeRemoved(long l) {
    }

    protected void setLightEnabled(long l, boolean bl) {
        if (bl) {
            this.columnsWithSources.add(l);
        } else {
            this.columnsWithSources.remove(l);
        }
    }

    protected boolean lightOnInSection(long l) {
        long l2 = SectionPos.getZeroNode(l);
        return this.columnsWithSources.contains(l2);
    }

    public void retainData(long l, boolean bl) {
        if (bl) {
            this.columnsToRetainQueuedDataFor.add(l);
        } else {
            this.columnsToRetainQueuedDataFor.remove(l);
        }
    }

    protected void queueSectionData(long l, @Nullable DataLayer dataLayer) {
        if (dataLayer != null) {
            this.queuedSections.put(l, (Object)dataLayer);
            this.hasInconsistencies = true;
        } else {
            this.queuedSections.remove(l);
        }
    }

    protected void updateSectionStatus(long l, boolean bl) {
        byte by;
        byte by2 = this.sectionStates.get(l);
        if (by2 == (by = SectionState.hasData(by2, !bl))) {
            return;
        }
        this.putSectionState(l, by);
        int n = bl ? -1 : 1;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (i == 0 && j == 0 && k == 0) continue;
                    long l2 = SectionPos.offset(l, i, j, k);
                    byte by3 = this.sectionStates.get(l2);
                    this.putSectionState(l2, SectionState.neighborCount(by3, SectionState.neighborCount(by3) + n));
                }
            }
        }
    }

    protected void putSectionState(long l, byte by) {
        if (by != 0) {
            if (this.sectionStates.put(l, by) == 0) {
                this.initializeSection(l);
            }
        } else if (this.sectionStates.remove(l) != 0) {
            this.removeSection(l);
        }
    }

    private void initializeSection(long l) {
        if (!this.toRemove.remove(l)) {
            ((DataLayerStorageMap)this.updatingSectionData).setLayer(l, this.createDataLayer(l));
            this.changedSections.add(l);
            this.onNodeAdded(l);
            this.markSectionAndNeighborsAsAffected(l);
            this.hasInconsistencies = true;
        }
    }

    private void removeSection(long l) {
        this.toRemove.add(l);
        this.hasInconsistencies = true;
    }

    protected void swapSectionMap() {
        Object object;
        if (!this.changedSections.isEmpty()) {
            object = ((DataLayerStorageMap)this.updatingSectionData).copy();
            ((DataLayerStorageMap)object).disableCache();
            this.visibleSectionData = object;
            this.changedSections.clear();
        }
        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            object = this.sectionsAffectedByLightUpdates.iterator();
            while (object.hasNext()) {
                long l = object.nextLong();
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(l));
            }
            this.sectionsAffectedByLightUpdates.clear();
        }
    }

    public SectionType getDebugSectionType(long l) {
        return SectionState.type(this.sectionStates.get(l));
    }

    protected static class SectionState {
        public static final byte EMPTY = 0;
        private static final int MIN_NEIGHBORS = 0;
        private static final int MAX_NEIGHBORS = 26;
        private static final byte HAS_DATA_BIT = 32;
        private static final byte NEIGHBOR_COUNT_BITS = 31;

        protected SectionState() {
        }

        public static byte hasData(byte by, boolean bl) {
            return (byte)(bl ? by | 0x20 : by & 0xFFFFFFDF);
        }

        public static byte neighborCount(byte by, int n) {
            if (n < 0 || n > 26) {
                throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
            }
            return (byte)(by & 0xFFFFFFE0 | n & 0x1F);
        }

        public static boolean hasData(byte by) {
            return (by & 0x20) != 0;
        }

        public static int neighborCount(byte by) {
            return by & 0x1F;
        }

        public static SectionType type(byte by) {
            if (by == 0) {
                return SectionType.EMPTY;
            }
            if (SectionState.hasData(by)) {
                return SectionType.LIGHT_AND_DATA;
            }
            return SectionType.LIGHT_ONLY;
        }
    }

    public static enum SectionType {
        EMPTY("2"),
        LIGHT_ONLY("1"),
        LIGHT_AND_DATA("0");

        private final String display;

        private SectionType(String string2) {
            this.display = string2;
        }

        public String display() {
            return this.display;
        }
    }
}

