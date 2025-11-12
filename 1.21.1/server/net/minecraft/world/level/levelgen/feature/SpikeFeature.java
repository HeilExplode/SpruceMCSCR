/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature
extends Feature<SpikeConfiguration> {
    public static final int NUMBER_OF_SPIKES = 10;
    private static final int SPIKE_DISTANCE = 42;
    private static final LoadingCache<Long, List<EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build((CacheLoader)new SpikeCacheLoader());

    public SpikeFeature(Codec<SpikeConfiguration> codec) {
        super(codec);
    }

    public static List<EndSpike> getSpikesForLevel(WorldGenLevel worldGenLevel) {
        RandomSource randomSource = RandomSource.create(worldGenLevel.getSeed());
        long l = randomSource.nextLong() & 0xFFFFL;
        return (List)SPIKE_CACHE.getUnchecked((Object)l);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpikeConfiguration> featurePlaceContext) {
        SpikeConfiguration spikeConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        List<EndSpike> list = spikeConfiguration.getSpikes();
        if (list.isEmpty()) {
            list = SpikeFeature.getSpikesForLevel(worldGenLevel);
        }
        for (EndSpike endSpike : list) {
            if (!endSpike.isCenterWithinChunk(blockPos)) continue;
            this.placeSpike(worldGenLevel, randomSource, spikeConfiguration, endSpike);
        }
        return true;
    }

    private void placeSpike(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, SpikeConfiguration spikeConfiguration, EndSpike endSpike) {
        int n = endSpike.getRadius();
        Object object = BlockPos.betweenClosed(new BlockPos(endSpike.getCenterX() - n, serverLevelAccessor.getMinBuildHeight(), endSpike.getCenterZ() - n), new BlockPos(endSpike.getCenterX() + n, endSpike.getHeight() + 10, endSpike.getCenterZ() + n)).iterator();
        while (object.hasNext()) {
            BlockPos blockPos = object.next();
            if (blockPos.distToLowCornerSqr(endSpike.getCenterX(), blockPos.getY(), endSpike.getCenterZ()) <= (double)(n * n + 1) && blockPos.getY() < endSpike.getHeight()) {
                this.setBlock(serverLevelAccessor, blockPos, Blocks.OBSIDIAN.defaultBlockState());
                continue;
            }
            if (blockPos.getY() <= 65) continue;
            this.setBlock(serverLevelAccessor, blockPos, Blocks.AIR.defaultBlockState());
        }
        if (endSpike.isGuarded()) {
            int n2 = -2;
            int n3 = 2;
            int n4 = 3;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int i = -2; i <= 2; ++i) {
                for (int j = -2; j <= 2; ++j) {
                    for (int k = 0; k <= 3; ++k) {
                        boolean bl;
                        boolean bl2 = Mth.abs(i) == 2;
                        boolean bl3 = Mth.abs(j) == 2;
                        boolean bl4 = bl = k == 3;
                        if (!bl2 && !bl3 && !bl) continue;
                        boolean bl5 = i == -2 || i == 2 || bl;
                        boolean bl6 = j == -2 || j == 2 || bl;
                        BlockState blockState = (BlockState)((BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, bl5 && j != -2)).setValue(IronBarsBlock.SOUTH, bl5 && j != 2)).setValue(IronBarsBlock.WEST, bl6 && i != -2)).setValue(IronBarsBlock.EAST, bl6 && i != 2);
                        this.setBlock(serverLevelAccessor, mutableBlockPos.set(endSpike.getCenterX() + i, endSpike.getHeight() + k, endSpike.getCenterZ() + j), blockState);
                    }
                }
            }
        }
        if ((object = EntityType.END_CRYSTAL.create(serverLevelAccessor.getLevel())) != null) {
            ((EndCrystal)object).setBeamTarget(spikeConfiguration.getCrystalBeamTarget());
            ((Entity)object).setInvulnerable(spikeConfiguration.isCrystalInvulnerable());
            ((Entity)object).moveTo((double)endSpike.getCenterX() + 0.5, endSpike.getHeight() + 1, (double)endSpike.getCenterZ() + 0.5, randomSource.nextFloat() * 360.0f, 0.0f);
            serverLevelAccessor.addFreshEntity((Entity)object);
            BlockPos blockPos = ((Entity)object).blockPosition();
            this.setBlock(serverLevelAccessor, blockPos.below(), Blocks.BEDROCK.defaultBlockState());
            this.setBlock(serverLevelAccessor, blockPos, FireBlock.getState(serverLevelAccessor, blockPos));
        }
    }

    public static class EndSpike {
        public static final Codec<EndSpike> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("centerX").orElse((Object)0).forGetter(endSpike -> endSpike.centerX), (App)Codec.INT.fieldOf("centerZ").orElse((Object)0).forGetter(endSpike -> endSpike.centerZ), (App)Codec.INT.fieldOf("radius").orElse((Object)0).forGetter(endSpike -> endSpike.radius), (App)Codec.INT.fieldOf("height").orElse((Object)0).forGetter(endSpike -> endSpike.height), (App)Codec.BOOL.fieldOf("guarded").orElse((Object)false).forGetter(endSpike -> endSpike.guarded)).apply((Applicative)instance, EndSpike::new));
        private final int centerX;
        private final int centerZ;
        private final int radius;
        private final int height;
        private final boolean guarded;
        private final AABB topBoundingBox;

        public EndSpike(int n, int n2, int n3, int n4, boolean bl) {
            this.centerX = n;
            this.centerZ = n2;
            this.radius = n3;
            this.height = n4;
            this.guarded = bl;
            this.topBoundingBox = new AABB(n - n3, DimensionType.MIN_Y, n2 - n3, n + n3, DimensionType.MAX_Y, n2 + n3);
        }

        public boolean isCenterWithinChunk(BlockPos blockPos) {
            return SectionPos.blockToSectionCoord(blockPos.getX()) == SectionPos.blockToSectionCoord(this.centerX) && SectionPos.blockToSectionCoord(blockPos.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean isGuarded() {
            return this.guarded;
        }

        public AABB getTopBoundingBox() {
            return this.topBoundingBox;
        }
    }

    static class SpikeCacheLoader
    extends CacheLoader<Long, List<EndSpike>> {
        SpikeCacheLoader() {
        }

        public List<EndSpike> load(Long l) {
            IntArrayList intArrayList = Util.toShuffledList(IntStream.range(0, 10), RandomSource.create(l));
            ArrayList arrayList = Lists.newArrayList();
            for (int i = 0; i < 10; ++i) {
                int n = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int n2 = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int n3 = intArrayList.get(i);
                int n4 = 2 + n3 / 3;
                int n5 = 76 + n3 * 3;
                boolean bl = n3 == 1 || n3 == 2;
                arrayList.add(new EndSpike(n, n2, n4, n5, bl));
            }
            return arrayList;
        }

        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((Long)object);
        }
    }
}

