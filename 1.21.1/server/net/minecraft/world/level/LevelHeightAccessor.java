/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
    public int getHeight();

    public int getMinBuildHeight();

    default public int getMaxBuildHeight() {
        return this.getMinBuildHeight() + this.getHeight();
    }

    default public int getSectionsCount() {
        return this.getMaxSection() - this.getMinSection();
    }

    default public int getMinSection() {
        return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
    }

    default public int getMaxSection() {
        return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
    }

    default public boolean isOutsideBuildHeight(BlockPos blockPos) {
        return this.isOutsideBuildHeight(blockPos.getY());
    }

    default public boolean isOutsideBuildHeight(int n) {
        return n < this.getMinBuildHeight() || n >= this.getMaxBuildHeight();
    }

    default public int getSectionIndex(int n) {
        return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(n));
    }

    default public int getSectionIndexFromSectionY(int n) {
        return n - this.getMinSection();
    }

    default public int getSectionYFromSectionIndex(int n) {
        return n + this.getMinSection();
    }

    public static LevelHeightAccessor create(final int n, final int n2) {
        return new LevelHeightAccessor(){

            @Override
            public int getHeight() {
                return n2;
            }

            @Override
            public int getMinBuildHeight() {
                return n;
            }
        };
    }
}

