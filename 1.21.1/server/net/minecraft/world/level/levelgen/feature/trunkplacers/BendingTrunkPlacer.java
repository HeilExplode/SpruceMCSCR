/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class BendingTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> BendingTrunkPlacer.trunkPlacerParts(instance).and(instance.group((App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", (Object)1).forGetter(bendingTrunkPlacer -> bendingTrunkPlacer.minHeightForLeaves), (App)IntProvider.codec(1, 64).fieldOf("bend_length").forGetter(bendingTrunkPlacer -> bendingTrunkPlacer.bendLength))).apply((Applicative)instance, BendingTrunkPlacer::new));
    private final int minHeightForLeaves;
    private final IntProvider bendLength;

    public BendingTrunkPlacer(int n, int n2, int n3, int n4, IntProvider intProvider) {
        super(n, n2, n3);
        this.minHeightForLeaves = n4;
        this.bendLength = intProvider;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.BENDING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        int n2;
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        int n3 = n - 1;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        Vec3i vec3i = mutableBlockPos.below();
        BendingTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, (BlockPos)vec3i, treeConfiguration);
        ArrayList arrayList = Lists.newArrayList();
        for (n2 = 0; n2 <= n3; ++n2) {
            if (n2 + 1 >= n3 + randomSource.nextInt(2)) {
                mutableBlockPos.move(direction);
            }
            if (TreeFeature.validTreePos(levelSimulatedReader, mutableBlockPos)) {
                this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration);
            }
            if (n2 >= this.minHeightForLeaves) {
                arrayList.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
            }
            mutableBlockPos.move(Direction.UP);
        }
        n2 = this.bendLength.sample(randomSource);
        for (int i = 0; i <= n2; ++i) {
            if (TreeFeature.validTreePos(levelSimulatedReader, mutableBlockPos)) {
                this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration);
            }
            arrayList.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
            mutableBlockPos.move(direction);
        }
        return arrayList;
    }
}

