/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;

public class SkyLightSectionStorage
extends LayerLightSectionStorage<SkyDataLayerStorageMap> {
    protected SkyLightSectionStorage(LightChunkGetter lightChunkGetter) {
        super(LightLayer.SKY, lightChunkGetter, new SkyDataLayerStorageMap((Long2ObjectOpenHashMap<DataLayer>)new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
    }

    @Override
    protected int getLightValue(long l) {
        return this.getLightValue(l, false);
    }

    protected int getLightValue(long l, boolean bl) {
        long l2 = SectionPos.blockToSection(l);
        int n = SectionPos.y(l2);
        SkyDataLayerStorageMap skyDataLayerStorageMap = bl ? (SkyDataLayerStorageMap)this.updatingSectionData : (SkyDataLayerStorageMap)this.visibleSectionData;
        int n2 = skyDataLayerStorageMap.topSections.get(SectionPos.getZeroNode(l2));
        if (n2 == skyDataLayerStorageMap.currentLowestY || n >= n2) {
            if (bl && !this.lightOnInSection(l2)) {
                return 0;
            }
            return 15;
        }
        DataLayer dataLayer = this.getDataLayer(skyDataLayerStorageMap, l2);
        if (dataLayer == null) {
            l = BlockPos.getFlatIndex(l);
            while (dataLayer == null) {
                if (++n >= n2) {
                    return 15;
                }
                l2 = SectionPos.offset(l2, Direction.UP);
                dataLayer = this.getDataLayer(skyDataLayerStorageMap, l2);
            }
        }
        return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    @Override
    protected void onNodeAdded(long l) {
        long l2;
        int n;
        int n2 = SectionPos.y(l);
        if (((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY > n2) {
            ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY = n2;
            ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.defaultReturnValue(((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY);
        }
        if ((n = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(l2 = SectionPos.getZeroNode(l))) < n2 + 1) {
            ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.put(l2, n2 + 1);
        }
    }

    @Override
    protected void onNodeRemoved(long l) {
        long l2 = SectionPos.getZeroNode(l);
        int n = SectionPos.y(l);
        if (((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(l2) == n + 1) {
            long l3 = l;
            while (!this.storingLightForSection(l3) && this.hasLightDataAtOrBelow(n)) {
                --n;
                l3 = SectionPos.offset(l3, Direction.DOWN);
            }
            if (this.storingLightForSection(l3)) {
                ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.put(l2, n + 1);
            } else {
                ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.remove(l2);
            }
        }
    }

    @Override
    protected DataLayer createDataLayer(long l) {
        DataLayer dataLayer;
        DataLayer dataLayer2 = (DataLayer)this.queuedSections.get(l);
        if (dataLayer2 != null) {
            return dataLayer2;
        }
        int n = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(SectionPos.getZeroNode(l));
        if (n == ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY || SectionPos.y(l) >= n) {
            if (this.lightOnInSection(l)) {
                return new DataLayer(15);
            }
            return new DataLayer();
        }
        long l2 = SectionPos.offset(l, Direction.UP);
        while ((dataLayer = this.getDataLayer(l2, true)) == null) {
            l2 = SectionPos.offset(l2, Direction.UP);
        }
        return SkyLightSectionStorage.repeatFirstLayer(dataLayer);
    }

    private static DataLayer repeatFirstLayer(DataLayer dataLayer) {
        if (dataLayer.isDefinitelyHomogenous()) {
            return dataLayer.copy();
        }
        byte[] byArray = dataLayer.getData();
        byte[] byArray2 = new byte[2048];
        for (int i = 0; i < 16; ++i) {
            System.arraycopy(byArray, 0, byArray2, i * 128, 128);
        }
        return new DataLayer(byArray2);
    }

    protected boolean hasLightDataAtOrBelow(int n) {
        return n >= ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY;
    }

    protected boolean isAboveData(long l) {
        long l2 = SectionPos.getZeroNode(l);
        int n = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(l2);
        return n == ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY || SectionPos.y(l) >= n;
    }

    protected int getTopSectionY(long l) {
        return ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(l);
    }

    protected int getBottomSectionY() {
        return ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY;
    }

    protected static final class SkyDataLayerStorageMap
    extends DataLayerStorageMap<SkyDataLayerStorageMap> {
        int currentLowestY;
        final Long2IntOpenHashMap topSections;

        public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap, Long2IntOpenHashMap long2IntOpenHashMap, int n) {
            super(long2ObjectOpenHashMap);
            this.topSections = long2IntOpenHashMap;
            long2IntOpenHashMap.defaultReturnValue(n);
            this.currentLowestY = n;
        }

        @Override
        public SkyDataLayerStorageMap copy() {
            return new SkyDataLayerStorageMap((Long2ObjectOpenHashMap<DataLayer>)this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }

        @Override
        public /* synthetic */ DataLayerStorageMap copy() {
            return this.copy();
        }
    }
}

