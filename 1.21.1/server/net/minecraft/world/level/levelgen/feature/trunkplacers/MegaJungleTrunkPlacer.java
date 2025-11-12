/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class MegaJungleTrunkPlacer
extends GiantTrunkPlacer {
    public static final MapCodec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> MegaJungleTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, MegaJungleTrunkPlacer::new));

    public MegaJungleTrunkPlacer(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        ArrayList arrayList = Lists.newArrayList();
        arrayList.addAll(super.placeTrunk(levelSimulatedReader, biConsumer, randomSource, n, blockPos, treeConfiguration));
        for (int i = n - 2 - randomSource.nextInt(4); i > n / 2; i -= 2 + randomSource.nextInt(4)) {
            float f = randomSource.nextFloat() * ((float)Math.PI * 2);
            int n2 = 0;
            int n3 = 0;
            for (int j = 0; j < 5; ++j) {
                n2 = (int)(1.5f + Mth.cos(f) * (float)j);
                n3 = (int)(1.5f + Mth.sin(f) * (float)j);
                BlockPos blockPos2 = blockPos.offset(n2, i - 3 + j / 2, n3);
                this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos2, treeConfiguration);
            }
            arrayList.add(new FoliagePlacer.FoliageAttachment(blockPos.offset(n2, i, n3), -2, false));
        }
        return arrayList;
    }
}

