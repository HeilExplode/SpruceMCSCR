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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig
extends BlockPileConfiguration {
    public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.stateProvider), (App)ExtraCodecs.POSITIVE_INT.fieldOf("spread_width").forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.spreadWidth), (App)ExtraCodecs.POSITIVE_INT.fieldOf("spread_height").forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.spreadHeight)).apply((Applicative)instance, NetherForestVegetationConfig::new));
    public final int spreadWidth;
    public final int spreadHeight;

    public NetherForestVegetationConfig(BlockStateProvider blockStateProvider, int n, int n2) {
        super(blockStateProvider);
        this.spreadWidth = n;
        this.spreadHeight = n2;
    }
}

