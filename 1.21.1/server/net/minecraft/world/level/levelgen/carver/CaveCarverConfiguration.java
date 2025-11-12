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
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration
extends CarverConfiguration {
    public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CarverConfiguration.CODEC.forGetter(caveCarverConfiguration -> caveCarverConfiguration), (App)FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier").forGetter(caveCarverConfiguration -> caveCarverConfiguration.horizontalRadiusMultiplier), (App)FloatProvider.CODEC.fieldOf("vertical_radius_multiplier").forGetter(caveCarverConfiguration -> caveCarverConfiguration.verticalRadiusMultiplier), (App)FloatProvider.codec(-1.0f, 1.0f).fieldOf("floor_level").forGetter(caveCarverConfiguration -> caveCarverConfiguration.floorLevel)).apply((Applicative)instance, CaveCarverConfiguration::new));
    public final FloatProvider horizontalRadiusMultiplier;
    public final FloatProvider verticalRadiusMultiplier;
    final FloatProvider floorLevel;

    public CaveCarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, CarverDebugSettings carverDebugSettings, HolderSet<Block> holderSet, FloatProvider floatProvider2, FloatProvider floatProvider3, FloatProvider floatProvider4) {
        super(f, heightProvider, floatProvider, verticalAnchor, carverDebugSettings, holderSet);
        this.horizontalRadiusMultiplier = floatProvider2;
        this.verticalRadiusMultiplier = floatProvider3;
        this.floorLevel = floatProvider4;
    }

    public CaveCarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, HolderSet<Block> holderSet, FloatProvider floatProvider2, FloatProvider floatProvider3, FloatProvider floatProvider4) {
        this(f, heightProvider, floatProvider, verticalAnchor, CarverDebugSettings.DEFAULT, holderSet, floatProvider2, floatProvider3, floatProvider4);
    }

    public CaveCarverConfiguration(CarverConfiguration carverConfiguration, FloatProvider floatProvider, FloatProvider floatProvider2, FloatProvider floatProvider3) {
        this(carverConfiguration.probability, carverConfiguration.y, carverConfiguration.yScale, carverConfiguration.lavaLevel, carverConfiguration.debugSettings, carverConfiguration.replaceable, floatProvider, floatProvider2, floatProvider3);
    }
}

