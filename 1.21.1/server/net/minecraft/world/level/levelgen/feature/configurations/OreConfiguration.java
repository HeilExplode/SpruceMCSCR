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
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class OreConfiguration
implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.list(TargetBlockState.CODEC).fieldOf("targets").forGetter(oreConfiguration -> oreConfiguration.targetStates), (App)Codec.intRange((int)0, (int)64).fieldOf("size").forGetter(oreConfiguration -> oreConfiguration.size), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("discard_chance_on_air_exposure").forGetter(oreConfiguration -> Float.valueOf(oreConfiguration.discardChanceOnAirExposure))).apply((Applicative)instance, OreConfiguration::new));
    public final List<TargetBlockState> targetStates;
    public final int size;
    public final float discardChanceOnAirExposure;

    public OreConfiguration(List<TargetBlockState> list, int n, float f) {
        this.size = n;
        this.targetStates = list;
        this.discardChanceOnAirExposure = f;
    }

    public OreConfiguration(List<TargetBlockState> list, int n) {
        this(list, n, 0.0f);
    }

    public OreConfiguration(RuleTest ruleTest, BlockState blockState, int n, float f) {
        this((List<TargetBlockState>)ImmutableList.of((Object)new TargetBlockState(ruleTest, blockState)), n, f);
    }

    public OreConfiguration(RuleTest ruleTest, BlockState blockState, int n) {
        this((List<TargetBlockState>)ImmutableList.of((Object)new TargetBlockState(ruleTest, blockState)), n, 0.0f);
    }

    public static TargetBlockState target(RuleTest ruleTest, BlockState blockState) {
        return new TargetBlockState(ruleTest, blockState);
    }

    public static class TargetBlockState {
        public static final Codec<TargetBlockState> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RuleTest.CODEC.fieldOf("target").forGetter(targetBlockState -> targetBlockState.target), (App)BlockState.CODEC.fieldOf("state").forGetter(targetBlockState -> targetBlockState.state)).apply((Applicative)instance, TargetBlockState::new));
        public final RuleTest target;
        public final BlockState state;

        TargetBlockState(RuleTest ruleTest, BlockState blockState) {
            this.target = ruleTest;
            this.state = blockState;
        }
    }
}

