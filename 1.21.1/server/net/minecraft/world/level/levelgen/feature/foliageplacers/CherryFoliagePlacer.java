/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class CherryFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<CherryFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> CherryFoliagePlacer.foliagePlacerParts(instance).and(instance.group((App)IntProvider.codec(4, 16).fieldOf("height").forGetter(cherryFoliagePlacer -> cherryFoliagePlacer.height), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("wide_bottom_layer_hole_chance").forGetter(cherryFoliagePlacer -> Float.valueOf(cherryFoliagePlacer.wideBottomLayerHoleChance)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("corner_hole_chance").forGetter(cherryFoliagePlacer -> Float.valueOf(cherryFoliagePlacer.wideBottomLayerHoleChance)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("hanging_leaves_chance").forGetter(cherryFoliagePlacer -> Float.valueOf(cherryFoliagePlacer.hangingLeavesChance)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("hanging_leaves_extension_chance").forGetter(cherryFoliagePlacer -> Float.valueOf(cherryFoliagePlacer.hangingLeavesExtensionChance)))).apply((Applicative)instance, CherryFoliagePlacer::new));
    private final IntProvider height;
    private final float wideBottomLayerHoleChance;
    private final float cornerHoleChance;
    private final float hangingLeavesChance;
    private final float hangingLeavesExtensionChance;

    public CherryFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, float f, float f2, float f3, float f4) {
        super(intProvider, intProvider2);
        this.height = intProvider3;
        this.wideBottomLayerHoleChance = f;
        this.cornerHoleChance = f2;
        this.hangingLeavesChance = f3;
        this.hangingLeavesExtensionChance = f4;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        boolean bl = foliageAttachment.doubleTrunk();
        BlockPos blockPos = foliageAttachment.pos().above(n4);
        int n5 = n3 + foliageAttachment.radiusOffset() - 1;
        this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n5 - 2, n2 - 3, bl);
        this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n5 - 1, n2 - 4, bl);
        for (int i = n2 - 5; i >= 0; --i) {
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n5, i, bl);
        }
        this.placeLeavesRowWithHangingLeavesBelow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n5, -1, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
        this.placeLeavesRowWithHangingLeavesBelow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n5 - 1, -2, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int n, TreeConfiguration treeConfiguration) {
        return this.height.sample(randomSource);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int n, int n2, int n3, int n4, boolean bl) {
        boolean bl2;
        if (n2 == -1 && (n == n4 || n3 == n4) && randomSource.nextFloat() < this.wideBottomLayerHoleChance) {
            return true;
        }
        boolean bl3 = n == n4 && n3 == n4;
        boolean bl4 = bl2 = n4 > 2;
        if (bl2) {
            return bl3 || n + n3 > n4 * 2 - 2 && randomSource.nextFloat() < this.cornerHoleChance;
        }
        return bl3 && randomSource.nextFloat() < this.cornerHoleChance;
    }
}

