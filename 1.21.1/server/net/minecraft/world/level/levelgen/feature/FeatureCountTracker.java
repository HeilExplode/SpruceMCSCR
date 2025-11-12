/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<ServerLevel, LevelData> data = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5L, TimeUnit.MINUTES).build((CacheLoader)new CacheLoader<ServerLevel, LevelData>(){

        public LevelData load(ServerLevel serverLevel) {
            return new LevelData((Object2IntMap<FeatureData>)Object2IntMaps.synchronize((Object2IntMap)new Object2IntOpenHashMap()), new MutableInt(0));
        }

        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((ServerLevel)object);
        }
    });

    public static void chunkDecorated(ServerLevel serverLevel) {
        try {
            ((LevelData)data.get((Object)serverLevel)).chunksWithFeatures().increment();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to increment chunk count", (Throwable)exception);
        }
    }

    public static void featurePlaced(ServerLevel serverLevel, ConfiguredFeature<?, ?> configuredFeature, Optional<PlacedFeature> optional) {
        try {
            ((LevelData)data.get((Object)serverLevel)).featureData().computeInt((Object)new FeatureData(configuredFeature, optional), (featureData, n) -> n == null ? 1 : n + 1);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to increment feature count", (Throwable)exception);
        }
    }

    public static void clearCounts() {
        data.invalidateAll();
        LOGGER.debug("Cleared feature counts");
    }

    public static void logCounts() {
        LOGGER.debug("Logging feature counts:");
        data.asMap().forEach((serverLevel, levelData) -> {
            String string = serverLevel.dimension().location().toString();
            boolean bl = serverLevel.getServer().isRunning();
            Registry<PlacedFeature> registry = serverLevel.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            String string2 = (bl ? "running" : "dead") + " " + string;
            Integer n = levelData.chunksWithFeatures().getValue();
            LOGGER.debug(string2 + " total_chunks: " + n);
            levelData.featureData().forEach((featureData, n2) -> LOGGER.debug(string2 + " " + String.format(Locale.ROOT, "%10d ", n2) + String.format(Locale.ROOT, "%10f ", (double)n2.intValue() / (double)n.intValue()) + String.valueOf(featureData.topFeature().flatMap(registry::getResourceKey).map(ResourceKey::location)) + " " + String.valueOf(featureData.feature().feature()) + " " + String.valueOf(featureData.feature())));
        });
    }

    record LevelData(Object2IntMap<FeatureData> featureData, MutableInt chunksWithFeatures) {
    }

    record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
    }
}

