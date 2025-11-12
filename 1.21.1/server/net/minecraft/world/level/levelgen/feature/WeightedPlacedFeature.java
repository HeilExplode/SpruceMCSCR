/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
    public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)PlacedFeature.CODEC.fieldOf("feature").forGetter(weightedPlacedFeature -> weightedPlacedFeature.feature), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance").forGetter(weightedPlacedFeature -> Float.valueOf(weightedPlacedFeature.chance))).apply((Applicative)instance, WeightedPlacedFeature::new));
    public final Holder<PlacedFeature> feature;
    public final float chance;

    public WeightedPlacedFeature(Holder<PlacedFeature> holder, float f) {
        this.feature = holder;
        this.chance = f;
    }

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        return this.feature.value().place(worldGenLevel, chunkGenerator, randomSource, blockPos);
    }
}

