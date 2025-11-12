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
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ColumnFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)IntProvider.codec(0, 3).fieldOf("reach").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.reach), (App)IntProvider.codec(1, 10).fieldOf("height").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.height)).apply((Applicative)instance, ColumnFeatureConfiguration::new));
    private final IntProvider reach;
    private final IntProvider height;

    public ColumnFeatureConfiguration(IntProvider intProvider, IntProvider intProvider2) {
        this.reach = intProvider;
        this.height = intProvider2;
    }

    public IntProvider reach() {
        return this.reach;
    }

    public IntProvider height() {
        return this.height;
    }
}

