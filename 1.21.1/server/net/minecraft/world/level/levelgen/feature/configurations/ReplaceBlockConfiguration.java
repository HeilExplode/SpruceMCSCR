/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

public class ReplaceBlockConfiguration
implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(replaceBlockConfiguration -> replaceBlockConfiguration.targetStates)).apply((Applicative)instance, ReplaceBlockConfiguration::new));
    public final List<OreConfiguration.TargetBlockState> targetStates;

    public ReplaceBlockConfiguration(BlockState blockState, BlockState blockState2) {
        this((List<OreConfiguration.TargetBlockState>)ImmutableList.of((Object)OreConfiguration.target(new BlockStateMatchTest(blockState), blockState2)));
    }

    public ReplaceBlockConfiguration(List<OreConfiguration.TargetBlockState> list) {
        this.targetStates = list;
    }
}

