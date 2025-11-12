/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CanyonCarverConfiguration
extends CarverConfiguration {
    public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CarverConfiguration.CODEC.forGetter(canyonCarverConfiguration -> canyonCarverConfiguration), (App)FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.verticalRotation), (App)CanyonShapeConfiguration.CODEC.fieldOf("shape").forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.shape)).apply((Applicative)instance, CanyonCarverConfiguration::new));
    public final FloatProvider verticalRotation;
    public final CanyonShapeConfiguration shape;

    public CanyonCarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, CarverDebugSettings carverDebugSettings, HolderSet<Block> holderSet, FloatProvider floatProvider2, CanyonShapeConfiguration canyonShapeConfiguration) {
        super(f, heightProvider, floatProvider, verticalAnchor, carverDebugSettings, holderSet);
        this.verticalRotation = floatProvider2;
        this.shape = canyonShapeConfiguration;
    }

    public CanyonCarverConfiguration(CarverConfiguration carverConfiguration, FloatProvider floatProvider, CanyonShapeConfiguration canyonShapeConfiguration) {
        this(carverConfiguration.probability, carverConfiguration.y, carverConfiguration.yScale, carverConfiguration.lavaLevel, carverConfiguration.debugSettings, carverConfiguration.replaceable, floatProvider, canyonShapeConfiguration);
    }

    public static class CanyonShapeConfiguration {
        public static final Codec<CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)FloatProvider.CODEC.fieldOf("distance_factor").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.distanceFactor), (App)FloatProvider.CODEC.fieldOf("thickness").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.thickness), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("width_smoothness").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.widthSmoothness), (App)FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.horizontalRadiusFactor), (App)Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(canyonShapeConfiguration -> Float.valueOf(canyonShapeConfiguration.verticalRadiusDefaultFactor)), (App)Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(canyonShapeConfiguration -> Float.valueOf(canyonShapeConfiguration.verticalRadiusCenterFactor))).apply((Applicative)instance, CanyonShapeConfiguration::new));
        public final FloatProvider distanceFactor;
        public final FloatProvider thickness;
        public final int widthSmoothness;
        public final FloatProvider horizontalRadiusFactor;
        public final float verticalRadiusDefaultFactor;
        public final float verticalRadiusCenterFactor;

        public CanyonShapeConfiguration(FloatProvider floatProvider, FloatProvider floatProvider2, int n, FloatProvider floatProvider3, float f, float f2) {
            this.widthSmoothness = n;
            this.horizontalRadiusFactor = floatProvider3;
            this.verticalRadiusDefaultFactor = f;
            this.verticalRadiusCenterFactor = f2;
            this.distanceFactor = floatProvider;
            this.thickness = floatProvider2;
        }
    }
}

