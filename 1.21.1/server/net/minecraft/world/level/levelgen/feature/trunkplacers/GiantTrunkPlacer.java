/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class GiantTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> GiantTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, GiantTrunkPlacer::new));

    public GiantTrunkPlacer(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos blockPos2 = blockPos.below();
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2, treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.east(), treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south(), treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south().east(), treeConfiguration);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < n; ++i) {
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 0, i, 0);
            if (i >= n - 1) continue;
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 1, i, 0);
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 1, i, 1);
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 0, i, 1);
        }
        return ImmutableList.of((Object)new FoliagePlacer.FoliageAttachment(blockPos.above(n), 0, true));
    }

    private void placeLogIfFreeWithOffset(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, TreeConfiguration treeConfiguration, BlockPos blockPos, int n, int n2, int n3) {
        mutableBlockPos.setWithOffset(blockPos, n, n2, n3);
        this.placeLogIfFree(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration);
    }
}

