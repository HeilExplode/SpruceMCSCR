/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
    private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51f;
    private static final float OCEAN_CONTINENTALNESS = -0.4f;
    private static final float PLAINS_CONTINENTALNESS = 0.1f;
    private static final float BEACH_CONTINENTALNESS = -0.15f;
    private static final ToFloatFunction<Float> NO_TRANSFORM = ToFloatFunction.IDENTITY;
    private static final ToFloatFunction<Float> AMPLIFIED_OFFSET = ToFloatFunction.createUnlimited(f -> f < 0.0f ? f : f * 2.0f);
    private static final ToFloatFunction<Float> AMPLIFIED_FACTOR = ToFloatFunction.createUnlimited(f -> 1.25f - 6.25f / (f + 5.0f));
    private static final ToFloatFunction<Float> AMPLIFIED_JAGGEDNESS = ToFloatFunction.createUnlimited(f -> f * 2.0f);

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldOffset(I i, I i2, I i3, boolean bl) {
        ToFloatFunction<Float> toFloatFunction = bl ? AMPLIFIED_OFFSET : NO_TRANSFORM;
        CubicSpline<C, I> cubicSpline = TerrainProvider.buildErosionOffsetSpline(i2, i3, -0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, toFloatFunction);
        CubicSpline<C, I> cubicSpline2 = TerrainProvider.buildErosionOffsetSpline(i2, i3, -0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false, toFloatFunction);
        CubicSpline<C, I> cubicSpline3 = TerrainProvider.buildErosionOffsetSpline(i2, i3, -0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, toFloatFunction);
        CubicSpline<C, I> cubicSpline4 = TerrainProvider.buildErosionOffsetSpline(i2, i3, -0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, toFloatFunction);
        return CubicSpline.builder(i, toFloatFunction).addPoint(-1.1f, 0.044f).addPoint(-1.02f, -0.2222f).addPoint(-0.51f, -0.2222f).addPoint(-0.44f, -0.12f).addPoint(-0.18f, -0.12f).addPoint(-0.16f, cubicSpline).addPoint(-0.15f, cubicSpline).addPoint(-0.1f, cubicSpline2).addPoint(0.25f, cubicSpline3).addPoint(1.0f, cubicSpline4).build();
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldFactor(I i, I i2, I i3, I i4, boolean bl) {
        ToFloatFunction<Float> toFloatFunction = bl ? AMPLIFIED_FACTOR : NO_TRANSFORM;
        return CubicSpline.builder(i, NO_TRANSFORM).addPoint(-0.19f, 3.95f).addPoint(-0.15f, TerrainProvider.getErosionFactor(i2, i3, i4, 6.25f, true, NO_TRANSFORM)).addPoint(-0.1f, TerrainProvider.getErosionFactor(i2, i3, i4, 5.47f, true, toFloatFunction)).addPoint(0.03f, TerrainProvider.getErosionFactor(i2, i3, i4, 5.08f, true, toFloatFunction)).addPoint(0.06f, TerrainProvider.getErosionFactor(i2, i3, i4, 4.69f, false, toFloatFunction)).build();
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(I i, I i2, I i3, I i4, boolean bl) {
        ToFloatFunction<Float> toFloatFunction = bl ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
        float f = 0.65f;
        return CubicSpline.builder(i, toFloatFunction).addPoint(-0.11f, 0.0f).addPoint(0.03f, TerrainProvider.buildErosionJaggednessSpline(i2, i3, i4, 1.0f, 0.5f, 0.0f, 0.0f, toFloatFunction)).addPoint(0.65f, TerrainProvider.buildErosionJaggednessSpline(i2, i3, i4, 1.0f, 1.0f, 1.0f, 0.0f, toFloatFunction)).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(I i, I i2, I i3, float f, float f2, float f3, float f4, ToFloatFunction<Float> toFloatFunction) {
        float f5 = -0.5775f;
        CubicSpline<C, I> cubicSpline = TerrainProvider.buildRidgeJaggednessSpline(i2, i3, f, f3, toFloatFunction);
        CubicSpline<C, I> cubicSpline2 = TerrainProvider.buildRidgeJaggednessSpline(i2, i3, f2, f4, toFloatFunction);
        return CubicSpline.builder(i, toFloatFunction).addPoint(-1.0f, cubicSpline).addPoint(-0.78f, cubicSpline2).addPoint(-0.5775f, cubicSpline2).addPoint(-0.375f, 0.0f).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(I i, I i2, float f, float f2, ToFloatFunction<Float> toFloatFunction) {
        float f3 = NoiseRouterData.peaksAndValleys(0.4f);
        float f4 = NoiseRouterData.peaksAndValleys(0.56666666f);
        float f5 = (f3 + f4) / 2.0f;
        CubicSpline.Builder<C, I> builder = CubicSpline.builder(i2, toFloatFunction);
        builder.addPoint(f3, 0.0f);
        if (f2 > 0.0f) {
            builder.addPoint(f5, TerrainProvider.buildWeirdnessJaggednessSpline(i, f2, toFloatFunction));
        } else {
            builder.addPoint(f5, 0.0f);
        }
        if (f > 0.0f) {
            builder.addPoint(1.0f, TerrainProvider.buildWeirdnessJaggednessSpline(i, f, toFloatFunction));
        } else {
            builder.addPoint(1.0f, 0.0f);
        }
        return builder.build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(I i, float f, ToFloatFunction<Float> toFloatFunction) {
        float f2 = 0.63f * f;
        float f3 = 0.3f * f;
        return CubicSpline.builder(i, toFloatFunction).addPoint(-0.01f, f2).addPoint(0.01f, f3).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> getErosionFactor(I i, I i2, I i3, float f, boolean bl, ToFloatFunction<Float> toFloatFunction) {
        CubicSpline cubicSpline = CubicSpline.builder(i2, toFloatFunction).addPoint(-0.2f, 6.3f).addPoint(0.2f, f).build();
        CubicSpline.Builder builder = CubicSpline.builder(i, toFloatFunction).addPoint(-0.6f, cubicSpline).addPoint(-0.5f, CubicSpline.builder(i2, toFloatFunction).addPoint(-0.05f, 6.3f).addPoint(0.05f, 2.67f).build()).addPoint(-0.35f, cubicSpline).addPoint(-0.25f, cubicSpline).addPoint(-0.1f, CubicSpline.builder(i2, toFloatFunction).addPoint(-0.05f, 2.67f).addPoint(0.05f, 6.3f).build()).addPoint(0.03f, cubicSpline);
        if (bl) {
            CubicSpline cubicSpline2 = CubicSpline.builder(i2, toFloatFunction).addPoint(0.0f, f).addPoint(0.1f, 0.625f).build();
            CubicSpline cubicSpline3 = CubicSpline.builder(i3, toFloatFunction).addPoint(-0.9f, f).addPoint(-0.69f, cubicSpline2).build();
            builder.addPoint(0.35f, f).addPoint(0.45f, cubicSpline3).addPoint(0.55f, cubicSpline3).addPoint(0.62f, f);
        } else {
            CubicSpline cubicSpline4 = CubicSpline.builder(i3, toFloatFunction).addPoint(-0.7f, cubicSpline).addPoint(-0.15f, 1.37f).build();
            CubicSpline cubicSpline5 = CubicSpline.builder(i3, toFloatFunction).addPoint(0.45f, cubicSpline).addPoint(0.7f, 1.56f).build();
            builder.addPoint(0.05f, cubicSpline5).addPoint(0.4f, cubicSpline5).addPoint(0.45f, cubicSpline4).addPoint(0.55f, cubicSpline4).addPoint(0.58f, f);
        }
        return builder.build();
    }

    private static float calculateSlope(float f, float f2, float f3, float f4) {
        return (f2 - f) / (f4 - f3);
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(I i, float f, boolean bl, ToFloatFunction<Float> toFloatFunction) {
        CubicSpline.Builder builder = CubicSpline.builder(i, toFloatFunction);
        float f2 = -0.7f;
        float f3 = -1.0f;
        float f4 = TerrainProvider.mountainContinentalness(-1.0f, f, -0.7f);
        float f5 = 1.0f;
        float f6 = TerrainProvider.mountainContinentalness(1.0f, f, -0.7f);
        float f7 = TerrainProvider.calculateMountainRidgeZeroContinentalnessPoint(f);
        float f8 = -0.65f;
        if (-0.65f < f7 && f7 < 1.0f) {
            float f9 = TerrainProvider.mountainContinentalness(-0.65f, f, -0.7f);
            float f10 = -0.75f;
            float f11 = TerrainProvider.mountainContinentalness(-0.75f, f, -0.7f);
            float f12 = TerrainProvider.calculateSlope(f4, f11, -1.0f, -0.75f);
            builder.addPoint(-1.0f, f4, f12);
            builder.addPoint(-0.75f, f11);
            builder.addPoint(-0.65f, f9);
            float f13 = TerrainProvider.mountainContinentalness(f7, f, -0.7f);
            float f14 = TerrainProvider.calculateSlope(f13, f6, f7, 1.0f);
            float f15 = 0.01f;
            builder.addPoint(f7 - 0.01f, f13);
            builder.addPoint(f7, f13, f14);
            builder.addPoint(1.0f, f6, f14);
        } else {
            float f16 = TerrainProvider.calculateSlope(f4, f6, -1.0f, 1.0f);
            if (bl) {
                builder.addPoint(-1.0f, Math.max(0.2f, f4));
                builder.addPoint(0.0f, Mth.lerp(0.5f, f4, f6), f16);
            } else {
                builder.addPoint(-1.0f, f4, f16);
            }
            builder.addPoint(1.0f, f6, f16);
        }
        return builder.build();
    }

    private static float mountainContinentalness(float f, float f2, float f3) {
        float f4 = 1.17f;
        float f5 = 0.46082947f;
        float f6 = 1.0f - (1.0f - f2) * 0.5f;
        float f7 = 0.5f * (1.0f - f2);
        float f8 = (f + 1.17f) * 0.46082947f;
        float f9 = f8 * f6 - f7;
        if (f < f3) {
            return Math.max(f9, -0.2222f);
        }
        return Math.max(f9, 0.0f);
    }

    private static float calculateMountainRidgeZeroContinentalnessPoint(float f) {
        float f2 = 1.17f;
        float f3 = 0.46082947f;
        float f4 = 1.0f - (1.0f - f) * 0.5f;
        float f5 = 0.5f * (1.0f - f);
        return f5 / (0.46082947f * f4) - 1.17f;
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(I i, I i2, float f, float f2, float f3, float f4, float f5, float f6, boolean bl, boolean bl2, ToFloatFunction<Float> toFloatFunction) {
        float f7 = 0.6f;
        float f8 = 0.5f;
        float f9 = 0.5f;
        CubicSpline<C, I> cubicSpline = TerrainProvider.buildMountainRidgeSplineWithPoints(i2, Mth.lerp(f4, 0.6f, 1.5f), bl2, toFloatFunction);
        CubicSpline<C, I> cubicSpline2 = TerrainProvider.buildMountainRidgeSplineWithPoints(i2, Mth.lerp(f4, 0.6f, 1.0f), bl2, toFloatFunction);
        CubicSpline<C, I> cubicSpline3 = TerrainProvider.buildMountainRidgeSplineWithPoints(i2, f4, bl2, toFloatFunction);
        CubicSpline<C, I> cubicSpline4 = TerrainProvider.ridgeSpline(i2, f - 0.15f, 0.5f * f4, Mth.lerp(0.5f, 0.5f, 0.5f) * f4, 0.5f * f4, 0.6f * f4, 0.5f, toFloatFunction);
        CubicSpline<C, I> cubicSpline5 = TerrainProvider.ridgeSpline(i2, f, f5 * f4, f2 * f4, 0.5f * f4, 0.6f * f4, 0.5f, toFloatFunction);
        CubicSpline<C, I> cubicSpline6 = TerrainProvider.ridgeSpline(i2, f, f5, f5, f2, f3, 0.5f, toFloatFunction);
        CubicSpline<C, I> cubicSpline7 = TerrainProvider.ridgeSpline(i2, f, f5, f5, f2, f3, 0.5f, toFloatFunction);
        CubicSpline cubicSpline8 = CubicSpline.builder(i2, toFloatFunction).addPoint(-1.0f, f).addPoint(-0.4f, cubicSpline6).addPoint(0.0f, f3 + 0.07f).build();
        CubicSpline<C, I> cubicSpline9 = TerrainProvider.ridgeSpline(i2, -0.02f, f6, f6, f2, f3, 0.0f, toFloatFunction);
        CubicSpline.Builder<C, I> builder = CubicSpline.builder(i, toFloatFunction).addPoint(-0.85f, cubicSpline).addPoint(-0.7f, cubicSpline2).addPoint(-0.4f, cubicSpline3).addPoint(-0.35f, cubicSpline4).addPoint(-0.1f, cubicSpline5).addPoint(0.2f, cubicSpline6);
        if (bl) {
            builder.addPoint(0.4f, cubicSpline7).addPoint(0.45f, cubicSpline8).addPoint(0.55f, cubicSpline8).addPoint(0.58f, cubicSpline7);
        }
        builder.addPoint(0.7f, cubicSpline9);
        return builder.build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> ridgeSpline(I i, float f, float f2, float f3, float f4, float f5, float f6, ToFloatFunction<Float> toFloatFunction) {
        float f7 = Math.max(0.5f * (f2 - f), f6);
        float f8 = 5.0f * (f3 - f2);
        return CubicSpline.builder(i, toFloatFunction).addPoint(-1.0f, f, f7).addPoint(-0.4f, f2, Math.min(f7, f8)).addPoint(0.0f, f3, f8).addPoint(0.4f, f4, 2.0f * (f4 - f3)).addPoint(1.0f, f5, 0.7f * (f5 - f4)).build();
    }
}

