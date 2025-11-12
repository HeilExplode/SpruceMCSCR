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
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer
extends RootPlacer {
    public static final int ROOT_WIDTH_LIMIT = 8;
    public static final int ROOT_LENGTH_LIMIT = 15;
    public static final MapCodec<MangroveRootPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> MangroveRootPlacer.rootPlacerParts(instance).and((App)MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter(mangroveRootPlacer -> mangroveRootPlacer.mangroveRootPlacement)).apply((Applicative)instance, MangroveRootPlacer::new));
    private final MangroveRootPlacement mangroveRootPlacement;

    public MangroveRootPlacer(IntProvider intProvider, BlockStateProvider blockStateProvider, Optional<AboveRootPlacement> optional, MangroveRootPlacement mangroveRootPlacement) {
        super(intProvider, blockStateProvider, optional);
        this.mangroveRootPlacement = mangroveRootPlacement;
    }

    @Override
    public boolean placeRoots(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, BlockPos blockPos2, TreeConfiguration treeConfiguration) {
        ArrayList arrayList = Lists.newArrayList();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        while (mutableBlockPos.getY() < blockPos2.getY()) {
            if (!this.canPlaceRoot(levelSimulatedReader, mutableBlockPos)) {
                return false;
            }
            mutableBlockPos.move(Direction.UP);
        }
        arrayList.add(blockPos2.below());
        for (Object object : Direction.Plane.HORIZONTAL) {
            ArrayList arrayList2;
            BlockPos blockPos3 = blockPos2.relative((Direction)object);
            if (!this.simulateRoots(levelSimulatedReader, randomSource, blockPos3, (Direction)object, blockPos2, arrayList2 = Lists.newArrayList(), 0)) {
                return false;
            }
            arrayList.addAll(arrayList2);
            arrayList.add(blockPos2.relative((Direction)object));
        }
        for (Object object : arrayList) {
            this.placeRoot(levelSimulatedReader, biConsumer, randomSource, (BlockPos)object, treeConfiguration);
        }
        return true;
    }

    private boolean simulateRoots(LevelSimulatedReader levelSimulatedReader, RandomSource randomSource, BlockPos blockPos, Direction direction, BlockPos blockPos2, List<BlockPos> list, int n) {
        int n2 = this.mangroveRootPlacement.maxRootLength();
        if (n == n2 || list.size() > n2) {
            return false;
        }
        List<BlockPos> list2 = this.potentialRootPositions(blockPos, direction, randomSource, blockPos2);
        for (BlockPos blockPos3 : list2) {
            if (!this.canPlaceRoot(levelSimulatedReader, blockPos3)) continue;
            list.add(blockPos3);
            if (this.simulateRoots(levelSimulatedReader, randomSource, blockPos3, direction, blockPos2, list, n + 1)) continue;
            return false;
        }
        return true;
    }

    protected List<BlockPos> potentialRootPositions(BlockPos blockPos, Direction direction, RandomSource randomSource, BlockPos blockPos2) {
        BlockPos blockPos3 = blockPos.below();
        BlockPos blockPos4 = blockPos.relative(direction);
        int n = blockPos.distManhattan(blockPos2);
        int n2 = this.mangroveRootPlacement.maxRootWidth();
        float f = this.mangroveRootPlacement.randomSkewChance();
        if (n > n2 - 3 && n <= n2) {
            return randomSource.nextFloat() < f ? List.of(blockPos3, blockPos4.below()) : List.of(blockPos3);
        }
        if (n > n2) {
            return List.of(blockPos3);
        }
        if (randomSource.nextFloat() < f) {
            return List.of(blockPos3);
        }
        return randomSource.nextBoolean() ? List.of(blockPos4) : List.of(blockPos3);
    }

    @Override
    protected boolean canPlaceRoot(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return super.canPlaceRoot(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.mangroveRootPlacement.canGrowThrough()));
    }

    @Override
    protected void placeRoot(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        if (levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.mangroveRootPlacement.muddyRootsIn()))) {
            BlockState blockState2 = this.mangroveRootPlacement.muddyRootsProvider().getState(randomSource, blockPos);
            biConsumer.accept(blockPos, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos, blockState2));
        } else {
            super.placeRoot(levelSimulatedReader, biConsumer, randomSource, blockPos, treeConfiguration);
        }
    }

    @Override
    protected RootPlacerType<?> type() {
        return RootPlacerType.MANGROVE_ROOT_PLACER;
    }
}

