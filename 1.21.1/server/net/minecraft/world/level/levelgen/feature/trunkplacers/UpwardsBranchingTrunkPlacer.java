/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class UpwardsBranchingTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> UpwardsBranchingTrunkPlacer.trunkPlacerParts(instance).and(instance.group((App)IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.extraBranchSteps), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("place_branch_per_log_probability").forGetter(upwardsBranchingTrunkPlacer -> Float.valueOf(upwardsBranchingTrunkPlacer.placeBranchPerLogProbability)), (App)IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.extraBranchLength), (App)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.canGrowThrough))).apply((Applicative)instance, UpwardsBranchingTrunkPlacer::new));
    private final IntProvider extraBranchSteps;
    private final float placeBranchPerLogProbability;
    private final IntProvider extraBranchLength;
    private final HolderSet<Block> canGrowThrough;

    public UpwardsBranchingTrunkPlacer(int n, int n2, int n3, IntProvider intProvider, float f, IntProvider intProvider2, HolderSet<Block> holderSet) {
        super(n, n2, n3);
        this.extraBranchSteps = intProvider;
        this.placeBranchPerLogProbability = f;
        this.extraBranchLength = intProvider2;
        this.canGrowThrough = holderSet;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        ArrayList arrayList = Lists.newArrayList();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < n; ++i) {
            int n2 = blockPos.getY() + i;
            if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(blockPos.getX(), n2, blockPos.getZ()), treeConfiguration) && i < n - 1 && randomSource.nextFloat() < this.placeBranchPerLogProbability) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
                int n3 = this.extraBranchLength.sample(randomSource);
                int n4 = Math.max(0, n3 - this.extraBranchLength.sample(randomSource) - 1);
                int n5 = this.extraBranchSteps.sample(randomSource);
                this.placeBranch(levelSimulatedReader, biConsumer, randomSource, n, treeConfiguration, arrayList, mutableBlockPos, n2, direction, n4, n5);
            }
            if (i != n - 1) continue;
            arrayList.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.set(blockPos.getX(), n2 + 1, blockPos.getZ()), 0, false));
        }
        return arrayList;
    }

    private void placeBranch(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, TreeConfiguration treeConfiguration, List<FoliagePlacer.FoliageAttachment> list, BlockPos.MutableBlockPos mutableBlockPos, int n2, Direction direction, int n3, int n4) {
        int n5 = n2 + n3;
        int n6 = mutableBlockPos.getX();
        int n7 = mutableBlockPos.getZ();
        for (int i = n3; i < n && n4 > 0; ++i, --n4) {
            if (i < 1) continue;
            int n8 = n2 + i;
            n5 = n8;
            if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(n6 += direction.getStepX(), n8, n7 += direction.getStepZ()), treeConfiguration)) {
                ++n5;
            }
            list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
        }
        if (n5 - n2 > 1) {
            BlockPos blockPos = new BlockPos(n6, n5, n7);
            list.add(new FoliagePlacer.FoliageAttachment(blockPos, 0, false));
            list.add(new FoliagePlacer.FoliageAttachment(blockPos.below(2), 0, false));
        }
    }

    @Override
    protected boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return super.validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.canGrowThrough));
    }
}

