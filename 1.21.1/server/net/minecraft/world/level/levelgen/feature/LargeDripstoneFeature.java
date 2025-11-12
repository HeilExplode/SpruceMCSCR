/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature
extends Feature<LargeDripstoneConfiguration> {
    public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        LargeDripstoneConfiguration largeDripstoneConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos)) {
            return false;
        }
        Optional<Column> optional = Column.scan(worldGenLevel, blockPos, largeDripstoneConfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava);
        if (optional.isEmpty() || !(optional.get() instanceof Column.Range)) {
            return false;
        }
        Column.Range range = (Column.Range)optional.get();
        if (range.height() < 4) {
            return false;
        }
        int n = (int)((float)range.height() * largeDripstoneConfiguration.maxColumnRadiusToCaveHeightRatio);
        int n2 = Mth.clamp(n, largeDripstoneConfiguration.columnRadius.getMinValue(), largeDripstoneConfiguration.columnRadius.getMaxValue());
        int n3 = Mth.randomBetweenInclusive(randomSource, largeDripstoneConfiguration.columnRadius.getMinValue(), n2);
        LargeDripstone largeDripstone = LargeDripstoneFeature.makeDripstone(blockPos.atY(range.ceiling() - 1), false, randomSource, n3, largeDripstoneConfiguration.stalactiteBluntness, largeDripstoneConfiguration.heightScale);
        LargeDripstone largeDripstone2 = LargeDripstoneFeature.makeDripstone(blockPos.atY(range.floor() + 1), true, randomSource, n3, largeDripstoneConfiguration.stalagmiteBluntness, largeDripstoneConfiguration.heightScale);
        WindOffsetter windOffsetter = largeDripstone.isSuitableForWind(largeDripstoneConfiguration) && largeDripstone2.isSuitableForWind(largeDripstoneConfiguration) ? new WindOffsetter(blockPos.getY(), randomSource, largeDripstoneConfiguration.windSpeed) : WindOffsetter.noWind();
        boolean bl = largeDripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldGenLevel, windOffsetter);
        boolean bl2 = largeDripstone2.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldGenLevel, windOffsetter);
        if (bl) {
            largeDripstone.placeBlocks(worldGenLevel, randomSource, windOffsetter);
        }
        if (bl2) {
            largeDripstone2.placeBlocks(worldGenLevel, randomSource, windOffsetter);
        }
        return true;
    }

    private static LargeDripstone makeDripstone(BlockPos blockPos, boolean bl, RandomSource randomSource, int n, FloatProvider floatProvider, FloatProvider floatProvider2) {
        return new LargeDripstone(blockPos, bl, n, floatProvider.sample(randomSource), floatProvider2.sample(randomSource));
    }

    private void placeDebugMarkers(WorldGenLevel worldGenLevel, BlockPos blockPos, Column.Range range, WindOffsetter windOffsetter) {
        worldGenLevel.setBlock(windOffsetter.offset(blockPos.atY(range.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        worldGenLevel.setBlock(windOffsetter.offset(blockPos.atY(range.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.atY(range.floor() + 2).mutable();
        while (mutableBlockPos.getY() < range.ceiling() - 1) {
            BlockPos blockPos2 = windOffsetter.offset(mutableBlockPos);
            if (DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos2) || worldGenLevel.getBlockState(blockPos2).is(Blocks.DRIPSTONE_BLOCK)) {
                worldGenLevel.setBlock(blockPos2, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
            }
            mutableBlockPos.move(Direction.UP);
        }
    }

    static final class LargeDripstone {
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        LargeDripstone(BlockPos blockPos, boolean bl, int n, double d, double d2) {
            this.root = blockPos;
            this.pointingUp = bl;
            this.radius = n;
            this.bluntness = d;
            this.scale = d2;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0f);
        }

        private int getMinY() {
            if (this.pointingUp) {
                return this.root.getY();
            }
            return this.root.getY() - this.getHeight();
        }

        private int getMaxY() {
            if (!this.pointingUp) {
                return this.root.getY();
            }
            return this.root.getY() + this.getHeight();
        }

        boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel worldGenLevel, WindOffsetter windOffsetter) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos mutableBlockPos = this.root.mutable();
                int n = Math.min(10, this.getHeight());
                for (int i = 0; i < n; ++i) {
                    if (worldGenLevel.getBlockState(mutableBlockPos).is(Blocks.LAVA)) {
                        return false;
                    }
                    if (DripstoneUtils.isCircleMostlyEmbeddedInStone(worldGenLevel, windOffsetter.offset(mutableBlockPos), this.radius)) {
                        this.root = mutableBlockPos;
                        return true;
                    }
                    mutableBlockPos.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }
                this.radius /= 2;
            }
            return false;
        }

        private int getHeightAtRadius(float f) {
            return (int)DripstoneUtils.getDripstoneHeight(f, this.radius, this.scale, this.bluntness);
        }

        void placeBlocks(WorldGenLevel worldGenLevel, RandomSource randomSource, WindOffsetter windOffsetter) {
            for (int i = -this.radius; i <= this.radius; ++i) {
                block1: for (int j = -this.radius; j <= this.radius; ++j) {
                    int n;
                    float f = Mth.sqrt(i * i + j * j);
                    if (f > (float)this.radius || (n = this.getHeightAtRadius(f)) <= 0) continue;
                    if ((double)randomSource.nextFloat() < 0.2) {
                        n = (int)((float)n * Mth.randomBetween(randomSource, 0.8f, 1.0f));
                    }
                    BlockPos.MutableBlockPos mutableBlockPos = this.root.offset(i, 0, j).mutable();
                    boolean bl = false;
                    int n2 = this.pointingUp ? worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mutableBlockPos.getX(), mutableBlockPos.getZ()) : Integer.MAX_VALUE;
                    for (int k = 0; k < n && mutableBlockPos.getY() < n2; ++k) {
                        BlockPos blockPos = windOffsetter.offset(mutableBlockPos);
                        if (DripstoneUtils.isEmptyOrWaterOrLava(worldGenLevel, blockPos)) {
                            bl = true;
                            Block block = Blocks.DRIPSTONE_BLOCK;
                            worldGenLevel.setBlock(blockPos, block.defaultBlockState(), 2);
                        } else if (bl && worldGenLevel.getBlockState(blockPos).is(BlockTags.BASE_STONE_OVERWORLD)) continue block1;
                        mutableBlockPos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                    }
                }
            }
        }

        boolean isSuitableForWind(LargeDripstoneConfiguration largeDripstoneConfiguration) {
            return this.radius >= largeDripstoneConfiguration.minRadiusForWind && this.bluntness >= (double)largeDripstoneConfiguration.minBluntnessForWind;
        }
    }

    static final class WindOffsetter {
        private final int originY;
        @Nullable
        private final Vec3 windSpeed;

        WindOffsetter(int n, RandomSource randomSource, FloatProvider floatProvider) {
            this.originY = n;
            float f = floatProvider.sample(randomSource);
            float f2 = Mth.randomBetween(randomSource, 0.0f, (float)Math.PI);
            this.windSpeed = new Vec3(Mth.cos(f2) * f, 0.0, Mth.sin(f2) * f);
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        static WindOffsetter noWind() {
            return new WindOffsetter();
        }

        BlockPos offset(BlockPos blockPos) {
            if (this.windSpeed == null) {
                return blockPos;
            }
            int n = this.originY - blockPos.getY();
            Vec3 vec3 = this.windSpeed.scale(n);
            return blockPos.offset(Mth.floor(vec3.x), 0, Mth.floor(vec3.z));
        }
    }
}

