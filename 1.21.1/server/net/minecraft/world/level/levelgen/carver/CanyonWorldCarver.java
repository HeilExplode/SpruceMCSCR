/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class CanyonWorldCarver
extends WorldCarver<CanyonCarverConfiguration> {
    public CanyonWorldCarver(Codec<CanyonCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CanyonCarverConfiguration canyonCarverConfiguration, RandomSource randomSource) {
        return randomSource.nextFloat() <= canyonCarverConfiguration.probability;
    }

    @Override
    public boolean carve(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, RandomSource randomSource, Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {
        int n = (this.getRange() * 2 - 1) * 16;
        double d = chunkPos.getBlockX(randomSource.nextInt(16));
        int n2 = canyonCarverConfiguration.y.sample(randomSource, carvingContext);
        double d2 = chunkPos.getBlockZ(randomSource.nextInt(16));
        float f = randomSource.nextFloat() * ((float)Math.PI * 2);
        float f2 = canyonCarverConfiguration.verticalRotation.sample(randomSource);
        double d3 = canyonCarverConfiguration.yScale.sample(randomSource);
        float f3 = canyonCarverConfiguration.shape.thickness.sample(randomSource);
        int n3 = (int)((float)n * canyonCarverConfiguration.shape.distanceFactor.sample(randomSource));
        boolean bl = false;
        this.doCarve(carvingContext, canyonCarverConfiguration, chunkAccess, function, randomSource.nextLong(), aquifer, d, n2, d2, f3, f, f2, 0, n3, d3, carvingMask);
        return true;
    }

    private void doCarve(CarvingContext carvingContext2, CanyonCarverConfiguration canyonCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, long l, Aquifer aquifer, double d4, double d5, double d6, float f, float f2, float f3, int n2, int n3, double d7, CarvingMask carvingMask) {
        RandomSource randomSource = RandomSource.create(l);
        float[] fArray = this.initWidthFactors(carvingContext2, canyonCarverConfiguration, randomSource);
        float f4 = 0.0f;
        float f5 = 0.0f;
        for (int i = n2; i < n3; ++i) {
            double d8 = 1.5 + (double)(Mth.sin((float)i * (float)Math.PI / (float)n3) * f);
            double d9 = d8 * d7;
            d8 *= (double)canyonCarverConfiguration.shape.horizontalRadiusFactor.sample(randomSource);
            d9 = this.updateVerticalRadius(canyonCarverConfiguration, randomSource, d9, n3, i);
            float f6 = Mth.cos(f3);
            float f7 = Mth.sin(f3);
            d4 += (double)(Mth.cos(f2) * f6);
            d5 += (double)f7;
            d6 += (double)(Mth.sin(f2) * f6);
            f3 *= 0.7f;
            f3 += f5 * 0.05f;
            f2 += f4 * 0.05f;
            f5 *= 0.8f;
            f4 *= 0.5f;
            f5 += (randomSource.nextFloat() - randomSource.nextFloat()) * randomSource.nextFloat() * 2.0f;
            f4 += (randomSource.nextFloat() - randomSource.nextFloat()) * randomSource.nextFloat() * 4.0f;
            if (randomSource.nextInt(4) == 0) continue;
            if (!CanyonWorldCarver.canReach(chunkAccess.getPos(), d4, d6, i, n3, f)) {
                return;
            }
            this.carveEllipsoid(carvingContext2, canyonCarverConfiguration, chunkAccess, function, aquifer, d4, d5, d6, d8, d9, carvingMask, (carvingContext, d, d2, d3, n) -> this.shouldSkip(carvingContext, fArray, d, d2, d3, n));
        }
    }

    private float[] initWidthFactors(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, RandomSource randomSource) {
        int n = carvingContext.getGenDepth();
        float[] fArray = new float[n];
        float f = 1.0f;
        for (int i = 0; i < n; ++i) {
            if (i == 0 || randomSource.nextInt(canyonCarverConfiguration.shape.widthSmoothness) == 0) {
                f = 1.0f + randomSource.nextFloat() * randomSource.nextFloat();
            }
            fArray[i] = f * f;
        }
        return fArray;
    }

    private double updateVerticalRadius(CanyonCarverConfiguration canyonCarverConfiguration, RandomSource randomSource, double d, float f, float f2) {
        float f3 = 1.0f - Mth.abs(0.5f - f2 / f) * 2.0f;
        float f4 = canyonCarverConfiguration.shape.verticalRadiusDefaultFactor + canyonCarverConfiguration.shape.verticalRadiusCenterFactor * f3;
        return (double)f4 * d * (double)Mth.randomBetween(randomSource, 0.75f, 1.0f);
    }

    private boolean shouldSkip(CarvingContext carvingContext, float[] fArray, double d, double d2, double d3, int n) {
        int n2 = n - carvingContext.getMinGenY();
        return (d * d + d3 * d3) * (double)fArray[n2 - 1] + d2 * d2 / 6.0 >= 1.0;
    }
}

