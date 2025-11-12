/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
    public static void spawnParticlesOnBlockFaces(Level level, BlockPos blockPos, ParticleOptions particleOptions, IntProvider intProvider) {
        for (Direction direction : Direction.values()) {
            ParticleUtils.spawnParticlesOnBlockFace(level, blockPos, particleOptions, intProvider, direction, () -> ParticleUtils.getRandomSpeedRanges(level.random), 0.55);
        }
    }

    public static void spawnParticlesOnBlockFace(Level level, BlockPos blockPos, ParticleOptions particleOptions, IntProvider intProvider, Direction direction, Supplier<Vec3> supplier, double d) {
        int n = intProvider.sample(level.random);
        for (int i = 0; i < n; ++i) {
            ParticleUtils.spawnParticleOnFace(level, blockPos, direction, particleOptions, supplier.get(), d);
        }
    }

    private static Vec3 getRandomSpeedRanges(RandomSource randomSource) {
        return new Vec3(Mth.nextDouble(randomSource, -0.5, 0.5), Mth.nextDouble(randomSource, -0.5, 0.5), Mth.nextDouble(randomSource, -0.5, 0.5));
    }

    public static void spawnParticlesAlongAxis(Direction.Axis axis, Level level, BlockPos blockPos, double d, ParticleOptions particleOptions, UniformInt uniformInt) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos);
        boolean bl = axis == Direction.Axis.X;
        boolean bl2 = axis == Direction.Axis.Y;
        boolean bl3 = axis == Direction.Axis.Z;
        int n = uniformInt.sample(level.random);
        for (int i = 0; i < n; ++i) {
            double d2 = vec3.x + Mth.nextDouble(level.random, -1.0, 1.0) * (bl ? 0.5 : d);
            double d3 = vec3.y + Mth.nextDouble(level.random, -1.0, 1.0) * (bl2 ? 0.5 : d);
            double d4 = vec3.z + Mth.nextDouble(level.random, -1.0, 1.0) * (bl3 ? 0.5 : d);
            double d5 = bl ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
            double d6 = bl2 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
            double d7 = bl3 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
            level.addParticle(particleOptions, d2, d3, d4, d5, d6, d7);
        }
    }

    public static void spawnParticleOnFace(Level level, BlockPos blockPos, Direction direction, ParticleOptions particleOptions, Vec3 vec3, double d) {
        Vec3 vec32 = Vec3.atCenterOf(blockPos);
        int n = direction.getStepX();
        int n2 = direction.getStepY();
        int n3 = direction.getStepZ();
        double d2 = vec32.x + (n == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)n * d);
        double d3 = vec32.y + (n2 == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)n2 * d);
        double d4 = vec32.z + (n3 == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)n3 * d);
        double d5 = n == 0 ? vec3.x() : 0.0;
        double d6 = n2 == 0 ? vec3.y() : 0.0;
        double d7 = n3 == 0 ? vec3.z() : 0.0;
        level.addParticle(particleOptions, d2, d3, d4, d5, d6, d7);
    }

    public static void spawnParticleBelow(Level level, BlockPos blockPos, RandomSource randomSource, ParticleOptions particleOptions) {
        double d = (double)blockPos.getX() + randomSource.nextDouble();
        double d2 = (double)blockPos.getY() - 0.05;
        double d3 = (double)blockPos.getZ() + randomSource.nextDouble();
        level.addParticle(particleOptions, d, d2, d3, 0.0, 0.0, 0.0);
    }

    public static void spawnParticleInBlock(LevelAccessor levelAccessor, BlockPos blockPos, int n, ParticleOptions particleOptions) {
        double d = 0.5;
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        double d2 = blockState.isAir() ? 1.0 : blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y);
        ParticleUtils.spawnParticles(levelAccessor, blockPos, n, 0.5, d2, true, particleOptions);
    }

    public static void spawnParticles(LevelAccessor levelAccessor, BlockPos blockPos, int n, double d, double d2, boolean bl, ParticleOptions particleOptions) {
        RandomSource randomSource = levelAccessor.getRandom();
        for (int i = 0; i < n; ++i) {
            double d3 = randomSource.nextGaussian() * 0.02;
            double d4 = randomSource.nextGaussian() * 0.02;
            double d5 = randomSource.nextGaussian() * 0.02;
            double d6 = 0.5 - d;
            double d7 = (double)blockPos.getX() + d6 + randomSource.nextDouble() * d * 2.0;
            double d8 = (double)blockPos.getY() + randomSource.nextDouble() * d2;
            double d9 = (double)blockPos.getZ() + d6 + randomSource.nextDouble() * d * 2.0;
            if (!bl && levelAccessor.getBlockState(BlockPos.containing(d7, d8, d9).below()).isAir()) continue;
            levelAccessor.addParticle(particleOptions, d7, d8, d9, d3, d4, d5);
        }
    }

    public static void spawnSmashAttackParticles(LevelAccessor levelAccessor, BlockPos blockPos, int n) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        Vec3 vec3 = blockPos.getCenter().add(0.0, 0.5, 0.0);
        BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.DUST_PILLAR, levelAccessor.getBlockState(blockPos));
        int n2 = 0;
        while ((float)n2 < (float)n / 3.0f) {
            d6 = vec3.x + levelAccessor.getRandom().nextGaussian() / 2.0;
            d5 = vec3.y;
            d4 = vec3.z + levelAccessor.getRandom().nextGaussian() / 2.0;
            d3 = levelAccessor.getRandom().nextGaussian() * (double)0.2f;
            d2 = levelAccessor.getRandom().nextGaussian() * (double)0.2f;
            d = levelAccessor.getRandom().nextGaussian() * (double)0.2f;
            levelAccessor.addParticle(blockParticleOption, d6, d5, d4, d3, d2, d);
            ++n2;
        }
        n2 = 0;
        while ((float)n2 < (float)n / 1.5f) {
            d6 = vec3.x + 3.5 * Math.cos(n2) + levelAccessor.getRandom().nextGaussian() / 2.0;
            d5 = vec3.y;
            d4 = vec3.z + 3.5 * Math.sin(n2) + levelAccessor.getRandom().nextGaussian() / 2.0;
            d3 = levelAccessor.getRandom().nextGaussian() * (double)0.05f;
            d2 = levelAccessor.getRandom().nextGaussian() * (double)0.05f;
            d = levelAccessor.getRandom().nextGaussian() * (double)0.05f;
            levelAccessor.addParticle(blockParticleOption, d6, d5, d4, d3, d2, d);
            ++n2;
        }
    }
}

