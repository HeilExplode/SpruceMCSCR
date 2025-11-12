/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature
extends Feature<TreeConfiguration> {
    private static final int BLOCK_UPDATE_FLAGS = 19;

    public TreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    private static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.VINE));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
    }

    private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.REPLACEABLE_BY_TREES));
    }

    private boolean doPlace(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos, BiConsumer<BlockPos, BlockState> biConsumer, BiConsumer<BlockPos, BlockState> biConsumer2, FoliagePlacer.FoliageSetter foliageSetter, TreeConfiguration treeConfiguration) {
        int n = treeConfiguration.trunkPlacer.getTreeHeight(randomSource);
        int n2 = treeConfiguration.foliagePlacer.foliageHeight(randomSource, n, treeConfiguration);
        int n3 = n - n2;
        int n4 = treeConfiguration.foliagePlacer.foliageRadius(randomSource, n3);
        BlockPos blockPos2 = treeConfiguration.rootPlacer.map(rootPlacer -> rootPlacer.getTrunkOrigin(blockPos, randomSource)).orElse(blockPos);
        int n5 = Math.min(blockPos.getY(), blockPos2.getY());
        int n6 = Math.max(blockPos.getY(), blockPos2.getY()) + n + 1;
        if (n5 < worldGenLevel.getMinBuildHeight() + 1 || n6 > worldGenLevel.getMaxBuildHeight()) {
            return false;
        }
        OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
        int n7 = this.getMaxFreeTreeHeight(worldGenLevel, n, blockPos2, treeConfiguration);
        if (n7 < n && (optionalInt.isEmpty() || n7 < optionalInt.getAsInt())) {
            return false;
        }
        if (treeConfiguration.rootPlacer.isPresent() && !treeConfiguration.rootPlacer.get().placeRoots(worldGenLevel, biConsumer, randomSource, blockPos, blockPos2, treeConfiguration)) {
            return false;
        }
        List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer.placeTrunk(worldGenLevel, biConsumer2, randomSource, n7, blockPos2, treeConfiguration);
        list.forEach(foliageAttachment -> treeConfiguration.foliagePlacer.createFoliage(worldGenLevel, foliageSetter, randomSource, treeConfiguration, n7, (FoliagePlacer.FoliageAttachment)foliageAttachment, n2, n4));
        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= n + 1; ++i) {
            int n2 = treeConfiguration.minimumSize.getSizeAtHeight(n, i);
            for (int j = -n2; j <= n2; ++j) {
                for (int k = -n2; k <= n2; ++k) {
                    mutableBlockPos.setWithOffset(blockPos, j, i, k);
                    if (treeConfiguration.trunkPlacer.isFree(levelSimulatedReader, mutableBlockPos) && (treeConfiguration.ignoreVines || !TreeFeature.isVine(levelSimulatedReader, mutableBlockPos))) continue;
                    return i - 2;
                }
            }
        }
        return n;
    }

    @Override
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        TreeFeature.setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    @Override
    public final boolean place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext) {
        final WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();
        TreeConfiguration treeConfiguration = featurePlaceContext.config();
        HashSet hashSet = Sets.newHashSet();
        HashSet hashSet2 = Sets.newHashSet();
        final HashSet hashSet3 = Sets.newHashSet();
        HashSet hashSet4 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biConsumer = (blockPos, blockState) -> {
            hashSet.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPos, blockState) -> {
            hashSet2.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        FoliagePlacer.FoliageSetter foliageSetter = new FoliagePlacer.FoliageSetter(){

            @Override
            public void set(BlockPos blockPos, BlockState blockState) {
                hashSet3.add(blockPos.immutable());
                worldGenLevel.setBlock(blockPos, blockState, 19);
            }

            @Override
            public boolean isSet(BlockPos blockPos) {
                return hashSet3.contains(blockPos);
            }
        };
        BiConsumer<BlockPos, BlockState> biConsumer3 = (blockPos, blockState) -> {
            hashSet4.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        boolean bl = this.doPlace(worldGenLevel, randomSource, blockPos2, biConsumer, biConsumer2, foliageSetter, treeConfiguration);
        if (!bl || hashSet2.isEmpty() && hashSet3.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            TreeDecorator.Context context = new TreeDecorator.Context(worldGenLevel, biConsumer3, randomSource, hashSet2, hashSet3, hashSet);
            treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(context));
        }
        return BoundingBox.encapsulatingPositions(Iterables.concat((Iterable)hashSet, (Iterable)hashSet2, (Iterable)hashSet3, (Iterable)hashSet4)).map(boundingBox -> {
            DiscreteVoxelShape discreteVoxelShape = TreeFeature.updateLeaves(worldGenLevel, boundingBox, hashSet2, hashSet4, hashSet);
            StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            return true;
        }).orElse(false);
    }

    /*
     * Unable to fully structure code
     */
    private static DiscreteVoxelShape updateLeaves(LevelAccessor var0, BoundingBox var1_1, Set<BlockPos> var2_2, Set<BlockPos> var3_3, Set<BlockPos> var4_4) {
        var5_5 = new BitSetDiscreteVoxelShape(var1_1.getXSpan(), var1_1.getYSpan(), var1_1.getZSpan());
        var6_6 = 7;
        var7_7 = Lists.newArrayList();
        for (var8_8 = 0; var8_8 < 7; ++var8_8) {
            var7_7.add(Sets.newHashSet());
        }
        for (BlockPos var9_10 : Lists.newArrayList((Iterable)Sets.union(var3_3, var4_4))) {
            if (!var1_1.isInside(var9_10)) continue;
            var5_5.fill(var9_10.getX() - var1_1.minX(), var9_10.getY() - var1_1.minY(), var9_10.getZ() - var1_1.minZ());
        }
        var8_9 = new BlockPos.MutableBlockPos();
        var9_11 = 0;
        ((Set)var7_7.get(0)).addAll(var2_2);
        block2: while (true) {
            if (var9_11 < 7 && ((Set)var7_7.get(var9_11)).isEmpty()) {
                ++var9_11;
                continue;
            }
            if (var9_11 >= 7) break;
            var10_12 = ((Set)var7_7.get(var9_11)).iterator();
            var11_13 = (BlockPos)var10_12.next();
            var10_12.remove();
            if (!var1_1.isInside(var11_13)) continue;
            if (var9_11 != 0) {
                var12_14 = var0.getBlockState(var11_13);
                TreeFeature.setBlockKnownShape(var0, var11_13, (BlockState)var12_14.setValue(BlockStateProperties.DISTANCE, var9_11));
            }
            var5_5.fill(var11_13.getX() - var1_1.minX(), var11_13.getY() - var1_1.minY(), var11_13.getZ() - var1_1.minZ());
            var12_14 = Direction.values();
            var13_15 = var12_14.length;
            var14_16 = 0;
            while (true) {
                if (var14_16 < var13_15) ** break;
                continue block2;
                var15_17 = var12_14[var14_16];
                var8_9.setWithOffset((Vec3i)var11_13, var15_17);
                if (var1_1.isInside(var8_9) && !var5_5.isFull(var16_18 = var8_9.getX() - var1_1.minX(), var17_19 = var8_9.getY() - var1_1.minY(), var18_20 = var8_9.getZ() - var1_1.minZ()) && !(var20_22 = LeavesBlock.getOptionalDistanceAt(var19_21 = var0.getBlockState(var8_9))).isEmpty() && (var21_23 = Math.min(var20_22.getAsInt(), var9_11 + 1)) < 7) {
                    ((Set)var7_7.get(var21_23)).add(var8_9.immutable());
                    var9_11 = Math.min(var9_11, var21_23);
                }
                ++var14_16;
            }
            break;
        }
        return var5_5;
    }
}

