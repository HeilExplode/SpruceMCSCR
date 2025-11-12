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
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class UnderwaterMagmaConfiguration
implements FeatureConfiguration {
    public static final Codec<UnderwaterMagmaConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)512).fieldOf("floor_search_range").forGetter(underwaterMagmaConfiguration -> underwaterMagmaConfiguration.floorSearchRange), (App)Codec.intRange((int)0, (int)64).fieldOf("placement_radius_around_floor").forGetter(underwaterMagmaConfiguration -> underwaterMagmaConfiguration.placementRadiusAroundFloor), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("placement_probability_per_valid_position").forGetter(underwaterMagmaConfiguration -> Float.valueOf(underwaterMagmaConfiguration.placementProbabilityPerValidPosition))).apply((Applicative)instance, UnderwaterMagmaConfiguration::new));
    public final int floorSearchRange;
    public final int placementRadiusAroundFloor;
    public final float placementProbabilityPerValidPosition;

    public UnderwaterMagmaConfiguration(int n, int n2, float f) {
        this.floorSearchRange = n;
        this.placementRadiusAroundFloor = n2;
        this.placementProbabilityPerValidPosition = f;
    }
}

