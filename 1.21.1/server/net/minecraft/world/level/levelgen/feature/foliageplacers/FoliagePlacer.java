/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P2
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final IntProvider radius;
    protected final IntProvider offset;

    protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group((App)IntProvider.codec(0, 16).fieldOf("radius").forGetter(foliagePlacer -> foliagePlacer.radius), (App)IntProvider.codec(0, 16).fieldOf("offset").forGetter(foliagePlacer -> foliagePlacer.offset));
    }

    public FoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        this.radius = intProvider;
        this.offset = intProvider2;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int n, FoliageAttachment foliageAttachment, int n2, int n3) {
        this.createFoliage(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, n, foliageAttachment, n2, n3, this.offset(randomSource));
    }

    protected abstract void createFoliage(LevelSimulatedReader var1, FoliageSetter var2, RandomSource var3, TreeConfiguration var4, int var5, FoliageAttachment var6, int var7, int var8, int var9);

    public abstract int foliageHeight(RandomSource var1, int var2, TreeConfiguration var3);

    public int foliageRadius(RandomSource randomSource, int n) {
        return this.radius.sample(randomSource);
    }

    private int offset(RandomSource randomSource) {
        return this.offset.sample(randomSource);
    }

    protected abstract boolean shouldSkipLocation(RandomSource var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(RandomSource randomSource, int n, int n2, int n3, int n4, boolean bl) {
        int n5;
        int n6;
        if (bl) {
            n6 = Math.min(Math.abs(n), Math.abs(n - 1));
            n5 = Math.min(Math.abs(n3), Math.abs(n3 - 1));
        } else {
            n6 = Math.abs(n);
            n5 = Math.abs(n3);
        }
        return this.shouldSkipLocation(randomSource, n6, n2, n5, n4, bl);
    }

    protected void placeLeavesRow(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos, int n, int n2, boolean bl) {
        int n3 = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = -n; i <= n + n3; ++i) {
            for (int j = -n; j <= n + n3; ++j) {
                if (this.shouldSkipLocationSigned(randomSource, i, n2, j, n, bl)) continue;
                mutableBlockPos.setWithOffset(blockPos, i, n2, j);
                FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos);
            }
        }
    }

    protected final void placeLeavesRowWithHangingLeavesBelow(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos, int n, int n2, boolean bl, float f, float f2) {
        this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, n, n2, bl);
        int n3 = bl ? 1 : 0;
        BlockPos blockPos2 = blockPos.below();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction direction2 = direction.getClockWise();
            int n4 = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? n + n3 : n;
            mutableBlockPos.setWithOffset(blockPos, 0, n2 - 1, 0).move(direction2, n4).move(direction, -n);
            for (int i = -n; i < n + n3; ++i) {
                boolean bl2 = foliageSetter.isSet(mutableBlockPos.move(Direction.UP));
                mutableBlockPos.move(Direction.DOWN);
                if (bl2 && FoliagePlacer.tryPlaceExtension(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, f, blockPos2, mutableBlockPos)) {
                    mutableBlockPos.move(Direction.DOWN);
                    FoliagePlacer.tryPlaceExtension(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, f2, blockPos2, mutableBlockPos);
                    mutableBlockPos.move(Direction.UP);
                }
                mutableBlockPos.move(direction);
            }
        }
    }

    private static boolean tryPlaceExtension(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, float f, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos) {
        if (mutableBlockPos.distManhattan(blockPos) >= 7) {
            return false;
        }
        if (randomSource.nextFloat() > f) {
            return false;
        }
        return FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos);
    }

    protected static boolean tryPlaceLeaf(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos) {
        if (!TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
            return false;
        }
        BlockState blockState = treeConfiguration.foliageProvider.getState(randomSource, blockPos);
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.isSourceOfType(Fluids.WATER)));
        }
        foliageSetter.set(blockPos, blockState);
        return true;
    }

    public static interface FoliageSetter {
        public void set(BlockPos var1, BlockState var2);

        public boolean isSet(BlockPos var1);
    }

    public static final class FoliageAttachment {
        private final BlockPos pos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos blockPos, int n, boolean bl) {
            this.pos = blockPos;
            this.radiusOffset = n;
            this.doubleTrunk = bl;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }
}

