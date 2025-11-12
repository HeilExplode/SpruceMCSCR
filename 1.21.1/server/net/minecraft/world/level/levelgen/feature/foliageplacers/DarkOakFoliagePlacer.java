/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class DarkOakFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> DarkOakFoliagePlacer.foliagePlacerParts(instance).apply((Applicative)instance, DarkOakFoliagePlacer::new));

    public DarkOakFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        super(intProvider, intProvider2);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        BlockPos blockPos = foliageAttachment.pos().above(n4);
        boolean bl = foliageAttachment.doubleTrunk();
        if (bl) {
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n3 + 2, -1, bl);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n3 + 3, 0, bl);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n3 + 2, 1, bl);
            if (randomSource.nextBoolean()) {
                this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n3, 2, bl);
            }
        } else {
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n3 + 2, -1, bl);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n3 + 1, 0, bl);
        }
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int n, TreeConfiguration treeConfiguration) {
        return 4;
    }

    @Override
    protected boolean shouldSkipLocationSigned(RandomSource randomSource, int n, int n2, int n3, int n4, boolean bl) {
        if (!(n2 != 0 || !bl || n != -n4 && n < n4 || n3 != -n4 && n3 < n4)) {
            return true;
        }
        return super.shouldSkipLocationSigned(randomSource, n, n2, n3, n4, bl);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int n, int n2, int n3, int n4, boolean bl) {
        if (n2 == -1 && !bl) {
            return n == n4 && n3 == n4;
        }
        if (n2 == 1) {
            return n + n3 > n4 * 2 - 2;
        }
        return false;
    }
}

