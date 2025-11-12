/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public final class OreVeinifier {
    private static final float VEININESS_THRESHOLD = 0.4f;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2;
    private static final float VEIN_SOLIDNESS = 0.7f;
    private static final float MIN_RICHNESS = 0.1f;
    private static final float MAX_RICHNESS = 0.3f;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6f;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02f;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3f;

    private OreVeinifier() {
    }

    protected static NoiseChunk.BlockStateFiller create(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3, PositionalRandomFactory positionalRandomFactory) {
        BlockState blockState = null;
        return functionContext -> {
            double d = densityFunction.compute(functionContext);
            int n = functionContext.blockY();
            VeinType veinType = d > 0.0 ? VeinType.COPPER : VeinType.IRON;
            double d2 = Math.abs(d);
            int n2 = veinType.maxY - n;
            int n3 = n - veinType.minY;
            if (n3 < 0 || n2 < 0) {
                return blockState;
            }
            int n4 = Math.min(n2, n3);
            double d3 = Mth.clampedMap((double)n4, 0.0, 20.0, -0.2, 0.0);
            if (d2 + d3 < (double)0.4f) {
                return blockState;
            }
            RandomSource randomSource = positionalRandomFactory.at(functionContext.blockX(), n, functionContext.blockZ());
            if (randomSource.nextFloat() > 0.7f) {
                return blockState;
            }
            if (densityFunction2.compute(functionContext) >= 0.0) {
                return blockState;
            }
            double d4 = Mth.clampedMap(d2, (double)0.4f, (double)0.6f, (double)0.1f, (double)0.3f);
            if ((double)randomSource.nextFloat() < d4 && densityFunction3.compute(functionContext) > (double)-0.3f) {
                return randomSource.nextFloat() < 0.02f ? veinType.rawOreBlock : veinType.ore;
            }
            return veinType.filler;
        };
    }

    protected static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        final BlockState ore;
        final BlockState rawOreBlock;
        final BlockState filler;
        protected final int minY;
        protected final int maxY;

        private VeinType(BlockState blockState, BlockState blockState2, BlockState blockState3, int n2, int n3) {
            this.ore = blockState;
            this.rawOreBlock = blockState2;
            this.filler = blockState3;
            this.minY = n2;
            this.maxY = n3;
        }
    }
}

