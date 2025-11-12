/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class DripstoneClusterConfiguration
implements FeatureConfiguration {
    public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)1, (int)512).fieldOf("floor_to_ceiling_search_range").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.floorToCeilingSearchRange), (App)IntProvider.codec(1, 128).fieldOf("height").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.height), (App)IntProvider.codec(1, 128).fieldOf("radius").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.radius), (App)Codec.intRange((int)0, (int)64).fieldOf("max_stalagmite_stalactite_height_diff").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff), (App)Codec.intRange((int)1, (int)64).fieldOf("height_deviation").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.heightDeviation), (App)IntProvider.codec(0, 128).fieldOf("dripstone_block_layer_thickness").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.dripstoneBlockLayerThickness), (App)FloatProvider.codec(0.0f, 2.0f).fieldOf("density").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.density), (App)FloatProvider.codec(0.0f, 2.0f).fieldOf("wetness").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.wetness), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_dripstone_column_at_max_distance_from_center").forGetter(dripstoneClusterConfiguration -> Float.valueOf(dripstoneClusterConfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter)), (App)Codec.intRange((int)1, (int)64).fieldOf("max_distance_from_edge_affecting_chance_of_dripstone_column").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn), (App)Codec.intRange((int)1, (int)64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxDistanceFromCenterAffectingHeightBias)).apply((Applicative)instance, DripstoneClusterConfiguration::new));
    public final int floorToCeilingSearchRange;
    public final IntProvider height;
    public final IntProvider radius;
    public final int maxStalagmiteStalactiteHeightDiff;
    public final int heightDeviation;
    public final IntProvider dripstoneBlockLayerThickness;
    public final FloatProvider density;
    public final FloatProvider wetness;
    public final float chanceOfDripstoneColumnAtMaxDistanceFromCenter;
    public final int maxDistanceFromEdgeAffectingChanceOfDripstoneColumn;
    public final int maxDistanceFromCenterAffectingHeightBias;

    public DripstoneClusterConfiguration(int n, IntProvider intProvider, IntProvider intProvider2, int n2, int n3, IntProvider intProvider3, FloatProvider floatProvider, FloatProvider floatProvider2, float f, int n4, int n5) {
        this.floorToCeilingSearchRange = n;
        this.height = intProvider;
        this.radius = intProvider2;
        this.maxStalagmiteStalactiteHeightDiff = n2;
        this.heightDeviation = n3;
        this.dripstoneBlockLayerThickness = intProvider3;
        this.density = floatProvider;
        this.wetness = floatProvider2;
        this.chanceOfDripstoneColumnAtMaxDistanceFromCenter = f;
        this.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn = n4;
        this.maxDistanceFromCenterAffectingHeightBias = n5;
    }
}

