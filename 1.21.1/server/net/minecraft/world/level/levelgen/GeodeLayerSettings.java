/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GeodeLayerSettings {
    private static final Codec<Double> LAYER_RANGE = Codec.doubleRange((double)0.01, (double)50.0);
    public static final Codec<GeodeLayerSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LAYER_RANGE.fieldOf("filling").orElse((Object)1.7).forGetter(geodeLayerSettings -> geodeLayerSettings.filling), (App)LAYER_RANGE.fieldOf("inner_layer").orElse((Object)2.2).forGetter(geodeLayerSettings -> geodeLayerSettings.innerLayer), (App)LAYER_RANGE.fieldOf("middle_layer").orElse((Object)3.2).forGetter(geodeLayerSettings -> geodeLayerSettings.middleLayer), (App)LAYER_RANGE.fieldOf("outer_layer").orElse((Object)4.2).forGetter(geodeLayerSettings -> geodeLayerSettings.outerLayer)).apply((Applicative)instance, GeodeLayerSettings::new));
    public final double filling;
    public final double innerLayer;
    public final double middleLayer;
    public final double outerLayer;

    public GeodeLayerSettings(double d, double d2, double d3, double d4) {
        this.filling = d;
        this.innerLayer = d2;
        this.middleLayer = d3;
        this.outerLayer = d4;
    }
}

