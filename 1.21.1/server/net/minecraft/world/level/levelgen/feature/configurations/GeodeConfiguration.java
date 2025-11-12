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
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class GeodeConfiguration
implements FeatureConfiguration {
    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange((double)0.0, (double)1.0);
    public static final Codec<GeodeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter(geodeConfiguration -> geodeConfiguration.geodeBlockSettings), (App)GeodeLayerSettings.CODEC.fieldOf("layers").forGetter(geodeConfiguration -> geodeConfiguration.geodeLayerSettings), (App)GeodeCrackSettings.CODEC.fieldOf("crack").forGetter(geodeConfiguration -> geodeConfiguration.geodeCrackSettings), (App)CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse((Object)0.35).forGetter(geodeConfiguration -> geodeConfiguration.usePotentialPlacementsChance), (App)CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse((Object)0.0).forGetter(geodeConfiguration -> geodeConfiguration.useAlternateLayer0Chance), (App)Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse((Object)true).forGetter(geodeConfiguration -> geodeConfiguration.placementsRequireLayer0Alternate), (App)IntProvider.codec(1, 20).fieldOf("outer_wall_distance").orElse((Object)UniformInt.of(4, 5)).forGetter(geodeConfiguration -> geodeConfiguration.outerWallDistance), (App)IntProvider.codec(1, 20).fieldOf("distribution_points").orElse((Object)UniformInt.of(3, 4)).forGetter(geodeConfiguration -> geodeConfiguration.distributionPoints), (App)IntProvider.codec(0, 10).fieldOf("point_offset").orElse((Object)UniformInt.of(1, 2)).forGetter(geodeConfiguration -> geodeConfiguration.pointOffset), (App)Codec.INT.fieldOf("min_gen_offset").orElse((Object)-16).forGetter(geodeConfiguration -> geodeConfiguration.minGenOffset), (App)Codec.INT.fieldOf("max_gen_offset").orElse((Object)16).forGetter(geodeConfiguration -> geodeConfiguration.maxGenOffset), (App)CHANCE_RANGE.fieldOf("noise_multiplier").orElse((Object)0.05).forGetter(geodeConfiguration -> geodeConfiguration.noiseMultiplier), (App)Codec.INT.fieldOf("invalid_blocks_threshold").forGetter(geodeConfiguration -> geodeConfiguration.invalidBlocksThreshold)).apply((Applicative)instance, GeodeConfiguration::new));
    public final GeodeBlockSettings geodeBlockSettings;
    public final GeodeLayerSettings geodeLayerSettings;
    public final GeodeCrackSettings geodeCrackSettings;
    public final double usePotentialPlacementsChance;
    public final double useAlternateLayer0Chance;
    public final boolean placementsRequireLayer0Alternate;
    public final IntProvider outerWallDistance;
    public final IntProvider distributionPoints;
    public final IntProvider pointOffset;
    public final int minGenOffset;
    public final int maxGenOffset;
    public final double noiseMultiplier;
    public final int invalidBlocksThreshold;

    public GeodeConfiguration(GeodeBlockSettings geodeBlockSettings, GeodeLayerSettings geodeLayerSettings, GeodeCrackSettings geodeCrackSettings, double d, double d2, boolean bl, IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, int n, int n2, double d3, int n3) {
        this.geodeBlockSettings = geodeBlockSettings;
        this.geodeLayerSettings = geodeLayerSettings;
        this.geodeCrackSettings = geodeCrackSettings;
        this.usePotentialPlacementsChance = d;
        this.useAlternateLayer0Chance = d2;
        this.placementsRequireLayer0Alternate = bl;
        this.outerWallDistance = intProvider;
        this.distributionPoints = intProvider2;
        this.pointOffset = intProvider3;
        this.minGenOffset = n;
        this.maxGenOffset = n2;
        this.noiseMultiplier = d3;
        this.invalidBlocksThreshold = n3;
    }
}

