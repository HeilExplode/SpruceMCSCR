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
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class DarkOakTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> DarkOakTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, DarkOakTrunkPlacer::new));

    public DarkOakTrunkPlacer(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        int n2;
        int n3;
        ArrayList arrayList = Lists.newArrayList();
        BlockPos blockPos2 = blockPos.below();
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2, treeConfiguration);
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.east(), treeConfiguration);
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south(), treeConfiguration);
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south().east(), treeConfiguration);
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        int n4 = n - randomSource.nextInt(4);
        int n5 = 2 - randomSource.nextInt(3);
        int n6 = blockPos.getX();
        int n7 = blockPos.getY();
        int n8 = blockPos.getZ();
        int n9 = n6;
        int n10 = n8;
        int n11 = n7 + n - 1;
        for (n3 = 0; n3 < n; ++n3) {
            BlockPos blockPos3;
            if (n3 >= n4 && n5 > 0) {
                n9 += direction.getStepX();
                n10 += direction.getStepZ();
                --n5;
            }
            if (!TreeFeature.isAirOrLeaves(levelSimulatedReader, blockPos3 = new BlockPos(n9, n2 = n7 + n3, n10))) continue;
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3, treeConfiguration);
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3.east(), treeConfiguration);
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3.south(), treeConfiguration);
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3.east().south(), treeConfiguration);
        }
        arrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(n9, n11, n10), 0, true));
        for (n3 = -1; n3 <= 2; ++n3) {
            for (n2 = -1; n2 <= 2; ++n2) {
                if (n3 >= 0 && n3 <= 1 && n2 >= 0 && n2 <= 1 || randomSource.nextInt(3) > 0) continue;
                int n12 = randomSource.nextInt(3) + 2;
                for (int i = 0; i < n12; ++i) {
                    this.placeLog(levelSimulatedReader, biConsumer, randomSource, new BlockPos(n6 + n3, n11 - i - 1, n8 + n2), treeConfiguration);
                }
                arrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(n9 + n3, n11, n10 + n2), 0, false));
            }
        }
        return arrayList;
    }
}

