/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.LightEventListener;
import net.minecraft.world.level.lighting.SkyLightEngine;

public class LevelLightEngine
implements LightEventListener {
    public static final int LIGHT_SECTION_PADDING = 1;
    protected final LevelHeightAccessor levelHeightAccessor;
    @Nullable
    private final LightEngine<?, ?> blockEngine;
    @Nullable
    private final LightEngine<?, ?> skyEngine;

    public LevelLightEngine(LightChunkGetter lightChunkGetter, boolean bl, boolean bl2) {
        this.levelHeightAccessor = lightChunkGetter.getLevel();
        this.blockEngine = bl ? new BlockLightEngine(lightChunkGetter) : null;
        this.skyEngine = bl2 ? new SkyLightEngine(lightChunkGetter) : null;
    }

    @Override
    public void checkBlock(BlockPos blockPos) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(blockPos);
        }
        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(blockPos);
        }
    }

    @Override
    public boolean hasLightWork() {
        if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
            return true;
        }
        return this.blockEngine != null && this.blockEngine.hasLightWork();
    }

    @Override
    public int runLightUpdates() {
        int n = 0;
        if (this.blockEngine != null) {
            n += this.blockEngine.runLightUpdates();
        }
        if (this.skyEngine != null) {
            n += this.skyEngine.runLightUpdates();
        }
        return n;
    }

    @Override
    public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus(sectionPos, bl);
        }
        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus(sectionPos, bl);
        }
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
        if (this.blockEngine != null) {
            this.blockEngine.setLightEnabled(chunkPos, bl);
        }
        if (this.skyEngine != null) {
            this.skyEngine.setLightEnabled(chunkPos, bl);
        }
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        if (this.blockEngine != null) {
            this.blockEngine.propagateLightSources(chunkPos);
        }
        if (this.skyEngine != null) {
            this.skyEngine.propagateLightSources(chunkPos);
        }
    }

    public LayerLightEventListener getLayerListener(LightLayer lightLayer) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine == null) {
                return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
            }
            return this.blockEngine;
        }
        if (this.skyEngine == null) {
            return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
        }
        return this.skyEngine;
    }

    public String getDebugData(LightLayer lightLayer, SectionPos sectionPos) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugData(sectionPos.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugData(sectionPos.asLong());
        }
        return "n/a";
    }

    public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer lightLayer, SectionPos sectionPos) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugSectionType(sectionPos.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugSectionType(sectionPos.asLong());
        }
        return LayerLightSectionStorage.SectionType.EMPTY;
    }

    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(sectionPos.asLong(), dataLayer);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(sectionPos.asLong(), dataLayer);
        }
    }

    public void retainData(ChunkPos chunkPos, boolean bl) {
        if (this.blockEngine != null) {
            this.blockEngine.retainData(chunkPos, bl);
        }
        if (this.skyEngine != null) {
            this.skyEngine.retainData(chunkPos, bl);
        }
    }

    public int getRawBrightness(BlockPos blockPos, int n) {
        int n2 = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(blockPos) - n;
        int n3 = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(blockPos);
        return Math.max(n3, n2);
    }

    public boolean lightOnInSection(SectionPos sectionPos) {
        long l = sectionPos.asLong();
        return this.blockEngine == null || ((LayerLightSectionStorage)this.blockEngine.storage).lightOnInSection(l) && (this.skyEngine == null || ((LayerLightSectionStorage)this.skyEngine.storage).lightOnInSection(l));
    }

    public int getLightSectionCount() {
        return this.levelHeightAccessor.getSectionsCount() + 2;
    }

    public int getMinLightSection() {
        return this.levelHeightAccessor.getMinSection() - 1;
    }

    public int getMaxLightSection() {
        return this.getMinLightSection() + this.getLightSectionCount();
    }
}

