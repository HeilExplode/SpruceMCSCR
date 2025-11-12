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
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RandomPatchConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration
{
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse((Object)128).forGetter(RandomPatchConfiguration::tries), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse((Object)7).forGetter(RandomPatchConfiguration::xzSpread), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse((Object)3).forGetter(RandomPatchConfiguration::ySpread), (App)PlacedFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfiguration::feature)).apply((Applicative)instance, RandomPatchConfiguration::new));
}

