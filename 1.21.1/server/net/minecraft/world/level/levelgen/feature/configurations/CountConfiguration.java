/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CountConfiguration
implements FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountConfiguration::new, CountConfiguration::count).codec();
    private final IntProvider count;

    public CountConfiguration(int n) {
        this.count = ConstantInt.of(n);
    }

    public CountConfiguration(IntProvider intProvider) {
        this.count = intProvider;
    }

    public IntProvider count() {
        return this.count;
    }
}

