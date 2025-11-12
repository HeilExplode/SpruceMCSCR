/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

@Deprecated
public class CountOnEveryLayerPlacement
extends PlacementModifier {
    public static final MapCodec<CountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountOnEveryLayerPlacement::new, countOnEveryLayerPlacement -> countOnEveryLayerPlacement.count);
    private final IntProvider count;

    private CountOnEveryLayerPlacement(IntProvider intProvider) {
        this.count = intProvider;
    }

    public static CountOnEveryLayerPlacement of(IntProvider intProvider) {
        return new CountOnEveryLayerPlacement(intProvider);
    }

    public static CountOnEveryLayerPlacement of(int n) {
        return CountOnEveryLayerPlacement.of(ConstantInt.of(n));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        boolean bl;
        Stream.Builder<BlockPos> builder = Stream.builder();
        int n = 0;
        do {
            bl = false;
            for (int i = 0; i < this.count.sample(randomSource); ++i) {
                int n2;
                int n3;
                int n4 = randomSource.nextInt(16) + blockPos.getX();
                int n5 = CountOnEveryLayerPlacement.findOnGroundYPosition(placementContext, n4, n3 = placementContext.getHeight(Heightmap.Types.MOTION_BLOCKING, n4, n2 = randomSource.nextInt(16) + blockPos.getZ()), n2, n);
                if (n5 == Integer.MAX_VALUE) continue;
                builder.add(new BlockPos(n4, n5, n2));
                bl = true;
            }
            ++n;
        } while (bl);
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.COUNT_ON_EVERY_LAYER;
    }

    private static int findOnGroundYPosition(PlacementContext placementContext, int n, int n2, int n3, int n4) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n, n2, n3);
        int n5 = 0;
        BlockState blockState = placementContext.getBlockState(mutableBlockPos);
        for (int i = n2; i >= placementContext.getMinBuildHeight() + 1; --i) {
            mutableBlockPos.setY(i - 1);
            BlockState blockState2 = placementContext.getBlockState(mutableBlockPos);
            if (!CountOnEveryLayerPlacement.isEmpty(blockState2) && CountOnEveryLayerPlacement.isEmpty(blockState) && !blockState2.is(Blocks.BEDROCK)) {
                if (n5 == n4) {
                    return mutableBlockPos.getY() + 1;
                }
                ++n5;
            }
            blockState = blockState2;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA);
    }
}

