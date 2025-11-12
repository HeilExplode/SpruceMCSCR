/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.math.Fraction
 *  org.apache.commons.lang3.math.NumberUtils
 *  org.joml.Math
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.minecraft.util;

import java.util.Locale;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.Fraction;
import org.apache.commons.lang3.math.NumberUtils;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Mth {
    private static final long UUID_VERSION = 61440L;
    private static final long UUID_VERSION_TYPE_4 = 16384L;
    private static final long UUID_VARIANT = -4611686018427387904L;
    private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
    public static final float PI = (float)java.lang.Math.PI;
    public static final float HALF_PI = 1.5707964f;
    public static final float TWO_PI = (float)java.lang.Math.PI * 2;
    public static final float DEG_TO_RAD = (float)java.lang.Math.PI / 180;
    public static final float RAD_TO_DEG = 57.295776f;
    public static final float EPSILON = 1.0E-5f;
    public static final float SQRT_OF_TWO = Mth.sqrt(2.0f);
    private static final float SIN_SCALE = 10430.378f;
    public static final Vector3f Y_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);
    public static final Vector3f X_AXIS = new Vector3f(1.0f, 0.0f, 0.0f);
    public static final Vector3f Z_AXIS = new Vector3f(0.0f, 0.0f, 1.0f);
    private static final float[] SIN = Util.make(new float[65536], fArray -> {
        for (int i = 0; i < ((float[])fArray).length; ++i) {
            fArray[i] = (float)java.lang.Math.sin((double)i * java.lang.Math.PI * 2.0 / 65536.0);
        }
    });
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double ONE_SIXTH = 0.16666666666666666;
    private static final int FRAC_EXP = 8;
    private static final int LUT_SIZE = 257;
    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];

    public static float sin(float f) {
        return SIN[(int)(f * 10430.378f) & 0xFFFF];
    }

    public static float cos(float f) {
        return SIN[(int)(f * 10430.378f + 16384.0f) & 0xFFFF];
    }

    public static float sqrt(float f) {
        return (float)java.lang.Math.sqrt(f);
    }

    public static int floor(float f) {
        int n = (int)f;
        return f < (float)n ? n - 1 : n;
    }

    public static int floor(double d) {
        int n = (int)d;
        return d < (double)n ? n - 1 : n;
    }

    public static long lfloor(double d) {
        long l = (long)d;
        return d < (double)l ? l - 1L : l;
    }

    public static float abs(float f) {
        return java.lang.Math.abs(f);
    }

    public static int abs(int n) {
        return java.lang.Math.abs(n);
    }

    public static int ceil(float f) {
        int n = (int)f;
        return f > (float)n ? n + 1 : n;
    }

    public static int ceil(double d) {
        int n = (int)d;
        return d > (double)n ? n + 1 : n;
    }

    public static int clamp(int n, int n2, int n3) {
        return java.lang.Math.min(java.lang.Math.max(n, n2), n3);
    }

    public static long clamp(long l, long l2, long l3) {
        return java.lang.Math.min(java.lang.Math.max(l, l2), l3);
    }

    public static float clamp(float f, float f2, float f3) {
        if (f < f2) {
            return f2;
        }
        return java.lang.Math.min(f, f3);
    }

    public static double clamp(double d, double d2, double d3) {
        if (d < d2) {
            return d2;
        }
        return java.lang.Math.min(d, d3);
    }

    public static double clampedLerp(double d, double d2, double d3) {
        if (d3 < 0.0) {
            return d;
        }
        if (d3 > 1.0) {
            return d2;
        }
        return Mth.lerp(d3, d, d2);
    }

    public static float clampedLerp(float f, float f2, float f3) {
        if (f3 < 0.0f) {
            return f;
        }
        if (f3 > 1.0f) {
            return f2;
        }
        return Mth.lerp(f3, f, f2);
    }

    public static double absMax(double d, double d2) {
        if (d < 0.0) {
            d = -d;
        }
        if (d2 < 0.0) {
            d2 = -d2;
        }
        return java.lang.Math.max(d, d2);
    }

    public static int floorDiv(int n, int n2) {
        return java.lang.Math.floorDiv(n, n2);
    }

    public static int nextInt(RandomSource randomSource, int n, int n2) {
        if (n >= n2) {
            return n;
        }
        return randomSource.nextInt(n2 - n + 1) + n;
    }

    public static float nextFloat(RandomSource randomSource, float f, float f2) {
        if (f >= f2) {
            return f;
        }
        return randomSource.nextFloat() * (f2 - f) + f;
    }

    public static double nextDouble(RandomSource randomSource, double d, double d2) {
        if (d >= d2) {
            return d;
        }
        return randomSource.nextDouble() * (d2 - d) + d;
    }

    public static boolean equal(float f, float f2) {
        return java.lang.Math.abs(f2 - f) < 1.0E-5f;
    }

    public static boolean equal(double d, double d2) {
        return java.lang.Math.abs(d2 - d) < (double)1.0E-5f;
    }

    public static int positiveModulo(int n, int n2) {
        return java.lang.Math.floorMod(n, n2);
    }

    public static float positiveModulo(float f, float f2) {
        return (f % f2 + f2) % f2;
    }

    public static double positiveModulo(double d, double d2) {
        return (d % d2 + d2) % d2;
    }

    public static boolean isMultipleOf(int n, int n2) {
        return n % n2 == 0;
    }

    public static int wrapDegrees(int n) {
        int n2 = n % 360;
        if (n2 >= 180) {
            n2 -= 360;
        }
        if (n2 < -180) {
            n2 += 360;
        }
        return n2;
    }

    public static float wrapDegrees(float f) {
        float f2 = f % 360.0f;
        if (f2 >= 180.0f) {
            f2 -= 360.0f;
        }
        if (f2 < -180.0f) {
            f2 += 360.0f;
        }
        return f2;
    }

    public static double wrapDegrees(double d) {
        double d2 = d % 360.0;
        if (d2 >= 180.0) {
            d2 -= 360.0;
        }
        if (d2 < -180.0) {
            d2 += 360.0;
        }
        return d2;
    }

    public static float degreesDifference(float f, float f2) {
        return Mth.wrapDegrees(f2 - f);
    }

    public static float degreesDifferenceAbs(float f, float f2) {
        return Mth.abs(Mth.degreesDifference(f, f2));
    }

    public static float rotateIfNecessary(float f, float f2, float f3) {
        float f4 = Mth.degreesDifference(f, f2);
        float f5 = Mth.clamp(f4, -f3, f3);
        return f2 - f5;
    }

    public static float approach(float f, float f2, float f3) {
        f3 = Mth.abs(f3);
        if (f < f2) {
            return Mth.clamp(f + f3, f, f2);
        }
        return Mth.clamp(f - f3, f2, f);
    }

    public static float approachDegrees(float f, float f2, float f3) {
        float f4 = Mth.degreesDifference(f, f2);
        return Mth.approach(f, f + f4, f3);
    }

    public static int getInt(String string, int n) {
        return NumberUtils.toInt((String)string, (int)n);
    }

    public static int smallestEncompassingPowerOfTwo(int n) {
        int n2 = n - 1;
        n2 |= n2 >> 1;
        n2 |= n2 >> 2;
        n2 |= n2 >> 4;
        n2 |= n2 >> 8;
        n2 |= n2 >> 16;
        return n2 + 1;
    }

    public static boolean isPowerOfTwo(int n) {
        return n != 0 && (n & n - 1) == 0;
    }

    public static int ceillog2(int n) {
        n = Mth.isPowerOfTwo(n) ? n : Mth.smallestEncompassingPowerOfTwo(n);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)n * 125613361L >> 27) & 0x1F];
    }

    public static int log2(int n) {
        return Mth.ceillog2(n) - (Mth.isPowerOfTwo(n) ? 0 : 1);
    }

    public static int color(float f, float f2, float f3) {
        return FastColor.ARGB32.color(0, Mth.floor(f * 255.0f), Mth.floor(f2 * 255.0f), Mth.floor(f3 * 255.0f));
    }

    public static float frac(float f) {
        return f - (float)Mth.floor(f);
    }

    public static double frac(double d) {
        return d - (double)Mth.lfloor(d);
    }

    @Deprecated
    public static long getSeed(Vec3i vec3i) {
        return Mth.getSeed(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    @Deprecated
    public static long getSeed(int n, int n2, int n3) {
        long l = (long)(n * 3129871) ^ (long)n3 * 116129781L ^ (long)n2;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static UUID createInsecureUUID(RandomSource randomSource) {
        long l = randomSource.nextLong() & 0xFFFFFFFFFFFF0FFFL | 0x4000L;
        long l2 = randomSource.nextLong() & 0x3FFFFFFFFFFFFFFFL | Long.MIN_VALUE;
        return new UUID(l, l2);
    }

    public static UUID createInsecureUUID() {
        return Mth.createInsecureUUID(RANDOM);
    }

    public static double inverseLerp(double d, double d2, double d3) {
        return (d - d2) / (d3 - d2);
    }

    public static float inverseLerp(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    public static boolean rayIntersectsAABB(Vec3 vec3, Vec3 vec32, AABB aABB) {
        double d = (aABB.minX + aABB.maxX) * 0.5;
        double d2 = (aABB.maxX - aABB.minX) * 0.5;
        double d3 = vec3.x - d;
        if (java.lang.Math.abs(d3) > d2 && d3 * vec32.x >= 0.0) {
            return false;
        }
        double d4 = (aABB.minY + aABB.maxY) * 0.5;
        double d5 = (aABB.maxY - aABB.minY) * 0.5;
        double d6 = vec3.y - d4;
        if (java.lang.Math.abs(d6) > d5 && d6 * vec32.y >= 0.0) {
            return false;
        }
        double d7 = (aABB.minZ + aABB.maxZ) * 0.5;
        double d8 = (aABB.maxZ - aABB.minZ) * 0.5;
        double d9 = vec3.z - d7;
        if (java.lang.Math.abs(d9) > d8 && d9 * vec32.z >= 0.0) {
            return false;
        }
        double d10 = java.lang.Math.abs(vec32.x);
        double d11 = java.lang.Math.abs(vec32.y);
        double d12 = java.lang.Math.abs(vec32.z);
        double d13 = vec32.y * d9 - vec32.z * d6;
        if (java.lang.Math.abs(d13) > d5 * d12 + d8 * d11) {
            return false;
        }
        d13 = vec32.z * d3 - vec32.x * d9;
        if (java.lang.Math.abs(d13) > d2 * d12 + d8 * d10) {
            return false;
        }
        d13 = vec32.x * d6 - vec32.y * d3;
        return java.lang.Math.abs(d13) < d2 * d11 + d5 * d10;
    }

    public static double atan2(double d, double d2) {
        double d3;
        boolean bl;
        boolean bl2;
        boolean bl3;
        double d4 = d2 * d2 + d * d;
        if (Double.isNaN(d4)) {
            return Double.NaN;
        }
        boolean bl4 = bl3 = d < 0.0;
        if (bl3) {
            d = -d;
        }
        boolean bl5 = bl2 = d2 < 0.0;
        if (bl2) {
            d2 = -d2;
        }
        boolean bl6 = bl = d > d2;
        if (bl) {
            d3 = d2;
            d2 = d;
            d = d3;
        }
        d3 = Mth.fastInvSqrt(d4);
        d2 *= d3;
        double d5 = FRAC_BIAS + (d *= d3);
        int n = (int)Double.doubleToRawLongBits(d5);
        double d6 = ASIN_TAB[n];
        double d7 = COS_TAB[n];
        double d8 = d5 - FRAC_BIAS;
        double d9 = d * d7 - d2 * d8;
        double d10 = (6.0 + d9 * d9) * d9 * 0.16666666666666666;
        double d11 = d6 + d10;
        if (bl) {
            d11 = 1.5707963267948966 - d11;
        }
        if (bl2) {
            d11 = java.lang.Math.PI - d11;
        }
        if (bl3) {
            d11 = -d11;
        }
        return d11;
    }

    public static float invSqrt(float f) {
        return Math.invsqrt((float)f);
    }

    public static double invSqrt(double d) {
        return Math.invsqrt((double)d);
    }

    @Deprecated
    public static double fastInvSqrt(double d) {
        double d2 = 0.5 * d;
        long l = Double.doubleToRawLongBits(d);
        l = 6910469410427058090L - (l >> 1);
        d = Double.longBitsToDouble(l);
        d *= 1.5 - d2 * d * d;
        return d;
    }

    public static float fastInvCubeRoot(float f) {
        int n = Float.floatToIntBits(f);
        n = 1419967116 - n / 3;
        float f2 = Float.intBitsToFloat(n);
        f2 = 0.6666667f * f2 + 1.0f / (3.0f * f2 * f2 * f);
        f2 = 0.6666667f * f2 + 1.0f / (3.0f * f2 * f2 * f);
        return f2;
    }

    public static int hsvToRgb(float f, float f2, float f3) {
        return Mth.hsvToArgb(f, f2, f3, 0);
    }

    public static int hsvToArgb(float f, float f2, float f3, int n) {
        float f4;
        float f5;
        int n2 = (int)(f * 6.0f) % 6;
        float f6 = f * 6.0f - (float)n2;
        float f7 = f3 * (1.0f - f2);
        float f8 = f3 * (1.0f - f6 * f2);
        float f9 = f3 * (1.0f - (1.0f - f6) * f2);
        return FastColor.ARGB32.color(n, Mth.clamp((int)(f5 * 255.0f), 0, 255), Mth.clamp((int)(f4 * 255.0f), 0, 255), Mth.clamp((int)((switch (n2) {
            case 0 -> {
                f5 = f3;
                f4 = f9;
                yield f7;
            }
            case 1 -> {
                f5 = f8;
                f4 = f3;
                yield f7;
            }
            case 2 -> {
                f5 = f7;
                f4 = f3;
                yield f9;
            }
            case 3 -> {
                f5 = f7;
                f4 = f8;
                yield f3;
            }
            case 4 -> {
                f5 = f9;
                f4 = f7;
                yield f3;
            }
            case 5 -> {
                f5 = f3;
                f4 = f7;
                yield f8;
            }
            default -> throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + f + ", " + f2 + ", " + f3);
        }) * 255.0f), 0, 255));
    }

    public static int murmurHash3Mixer(int n) {
        n ^= n >>> 16;
        n *= -2048144789;
        n ^= n >>> 13;
        n *= -1028477387;
        n ^= n >>> 16;
        return n;
    }

    public static int binarySearch(int n, int n2, IntPredicate intPredicate) {
        int n3 = n2 - n;
        while (n3 > 0) {
            int n4 = n3 / 2;
            int n5 = n + n4;
            if (intPredicate.test(n5)) {
                n3 = n4;
                continue;
            }
            n = n5 + 1;
            n3 -= n4 + 1;
        }
        return n;
    }

    public static int lerpInt(float f, int n, int n2) {
        return n + Mth.floor(f * (float)(n2 - n));
    }

    public static int lerpDiscrete(float f, int n, int n2) {
        int n3 = n2 - n;
        return n + Mth.floor(f * (float)(n3 - 1)) + (f > 0.0f ? 1 : 0);
    }

    public static float lerp(float f, float f2, float f3) {
        return f2 + f * (f3 - f2);
    }

    public static double lerp(double d, double d2, double d3) {
        return d2 + d * (d3 - d2);
    }

    public static double lerp2(double d, double d2, double d3, double d4, double d5, double d6) {
        return Mth.lerp(d2, Mth.lerp(d, d3, d4), Mth.lerp(d, d5, d6));
    }

    public static double lerp3(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11) {
        return Mth.lerp(d3, Mth.lerp2(d, d2, d4, d5, d6, d7), Mth.lerp2(d, d2, d8, d9, d10, d11));
    }

    public static float catmullrom(float f, float f2, float f3, float f4, float f5) {
        return 0.5f * (2.0f * f3 + (f4 - f2) * f + (2.0f * f2 - 5.0f * f3 + 4.0f * f4 - f5) * f * f + (3.0f * f3 - f2 - 3.0f * f4 + f5) * f * f * f);
    }

    public static double smoothstep(double d) {
        return d * d * d * (d * (d * 6.0 - 15.0) + 10.0);
    }

    public static double smoothstepDerivative(double d) {
        return 30.0 * d * d * (d - 1.0) * (d - 1.0);
    }

    public static int sign(double d) {
        if (d == 0.0) {
            return 0;
        }
        return d > 0.0 ? 1 : -1;
    }

    public static float rotLerp(float f, float f2, float f3) {
        return f2 + f * Mth.wrapDegrees(f3 - f2);
    }

    public static double rotLerp(double d, double d2, double d3) {
        return d2 + d * Mth.wrapDegrees(d3 - d2);
    }

    public static float triangleWave(float f, float f2) {
        return (java.lang.Math.abs(f % f2 - f2 * 0.5f) - f2 * 0.25f) / (f2 * 0.25f);
    }

    public static float square(float f) {
        return f * f;
    }

    public static double square(double d) {
        return d * d;
    }

    public static int square(int n) {
        return n * n;
    }

    public static long square(long l) {
        return l * l;
    }

    public static double clampedMap(double d, double d2, double d3, double d4, double d5) {
        return Mth.clampedLerp(d4, d5, Mth.inverseLerp(d, d2, d3));
    }

    public static float clampedMap(float f, float f2, float f3, float f4, float f5) {
        return Mth.clampedLerp(f4, f5, Mth.inverseLerp(f, f2, f3));
    }

    public static double map(double d, double d2, double d3, double d4, double d5) {
        return Mth.lerp(Mth.inverseLerp(d, d2, d3), d4, d5);
    }

    public static float map(float f, float f2, float f3, float f4, float f5) {
        return Mth.lerp(Mth.inverseLerp(f, f2, f3), f4, f5);
    }

    public static double wobble(double d) {
        return d + (2.0 * RandomSource.create(Mth.floor(d * 3000.0)).nextDouble() - 1.0) * 1.0E-7 / 2.0;
    }

    public static int roundToward(int n, int n2) {
        return Mth.positiveCeilDiv(n, n2) * n2;
    }

    public static int positiveCeilDiv(int n, int n2) {
        return -java.lang.Math.floorDiv(-n, n2);
    }

    public static int randomBetweenInclusive(RandomSource randomSource, int n, int n2) {
        return randomSource.nextInt(n2 - n + 1) + n;
    }

    public static float randomBetween(RandomSource randomSource, float f, float f2) {
        return randomSource.nextFloat() * (f2 - f) + f;
    }

    public static float normal(RandomSource randomSource, float f, float f2) {
        return f + (float)randomSource.nextGaussian() * f2;
    }

    public static double lengthSquared(double d, double d2) {
        return d * d + d2 * d2;
    }

    public static double length(double d, double d2) {
        return java.lang.Math.sqrt(Mth.lengthSquared(d, d2));
    }

    public static double lengthSquared(double d, double d2, double d3) {
        return d * d + d2 * d2 + d3 * d3;
    }

    public static double length(double d, double d2, double d3) {
        return java.lang.Math.sqrt(Mth.lengthSquared(d, d2, d3));
    }

    public static float lengthSquared(float f, float f2, float f3) {
        return f * f + f2 * f2 + f3 * f3;
    }

    public static int quantize(double d, int n) {
        return Mth.floor(d / (double)n) * n;
    }

    public static IntStream outFromOrigin(int n, int n2, int n3) {
        return Mth.outFromOrigin(n, n2, n3, 1);
    }

    public static IntStream outFromOrigin(int n, int n2, int n3, int n6) {
        if (n2 > n3) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "upperbound %d expected to be > lowerBound %d", n3, n2));
        }
        if (n6 < 1) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "steps expected to be >= 1, was %d", n6));
        }
        if (n < n2 || n > n3) {
            return IntStream.empty();
        }
        return IntStream.iterate(n, n4 -> {
            int n5 = java.lang.Math.abs(n - n4);
            return n - n5 >= n2 || n + n5 <= n3;
        }, n5 -> {
            int n6;
            boolean bl;
            boolean bl2 = n5 <= n;
            int n7 = java.lang.Math.abs(n - n5);
            boolean bl3 = bl = n + n7 + n6 <= n3;
            if (!(bl2 && bl || (n6 = n - n7 - (bl2 ? n6 : 0)) < n2)) {
                return n6;
            }
            return n + n7 + n6;
        });
    }

    public static Quaternionf rotationAroundAxis(Vector3f vector3f, Quaternionf quaternionf, Quaternionf quaternionf2) {
        float f = vector3f.dot(quaternionf.x, quaternionf.y, quaternionf.z);
        return quaternionf2.set(vector3f.x * f, vector3f.y * f, vector3f.z * f, quaternionf.w).normalize();
    }

    public static int mulAndTruncate(Fraction fraction, int n) {
        return fraction.getNumerator() * n / fraction.getDenominator();
    }

    static {
        for (int i = 0; i < 257; ++i) {
            double d = (double)i / 256.0;
            double d2 = java.lang.Math.asin(d);
            Mth.COS_TAB[i] = java.lang.Math.cos(d2);
            Mth.ASIN_TAB[i] = d2;
        }
    }
}

