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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class LayerConfiguration
implements FeatureConfiguration {
    public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)DimensionType.Y_SIZE).fieldOf("height").forGetter(layerConfiguration -> layerConfiguration.height), (App)BlockState.CODEC.fieldOf("state").forGetter(layerConfiguration -> layerConfiguration.state)).apply((Applicative)instance, LayerConfiguration::new));
    public final int height;
    public final BlockState state;

    public LayerConfiguration(int n, BlockState blockState) {
        this.height = n;
        this.state = blockState;
    }
}

