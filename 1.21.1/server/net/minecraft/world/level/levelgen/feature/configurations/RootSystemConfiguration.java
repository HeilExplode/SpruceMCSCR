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
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration
implements FeatureConfiguration {
    public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)PlacedFeature.CODEC.fieldOf("feature").forGetter(rootSystemConfiguration -> rootSystemConfiguration.treeFeature), (App)Codec.intRange((int)1, (int)64).fieldOf("required_vertical_space_for_tree").forGetter(rootSystemConfiguration -> rootSystemConfiguration.requiredVerticalSpaceForTree), (App)Codec.intRange((int)1, (int)64).fieldOf("root_radius").forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootRadius), (App)TagKey.hashedCodec(Registries.BLOCK).fieldOf("root_replaceable").forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootReplaceable), (App)BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootStateProvider), (App)Codec.intRange((int)1, (int)256).fieldOf("root_placement_attempts").forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootPlacementAttempts), (App)Codec.intRange((int)1, (int)4096).fieldOf("root_column_max_height").forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootColumnMaxHeight), (App)Codec.intRange((int)1, (int)64).fieldOf("hanging_root_radius").forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootRadius), (App)Codec.intRange((int)0, (int)16).fieldOf("hanging_roots_vertical_span").forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootsVerticalSpan), (App)BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootStateProvider), (App)Codec.intRange((int)1, (int)256).fieldOf("hanging_root_placement_attempts").forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootPlacementAttempts), (App)Codec.intRange((int)1, (int)64).fieldOf("allowed_vertical_water_for_tree").forGetter(rootSystemConfiguration -> rootSystemConfiguration.allowedVerticalWaterForTree), (App)BlockPredicate.CODEC.fieldOf("allowed_tree_position").forGetter(rootSystemConfiguration -> rootSystemConfiguration.allowedTreePosition)).apply((Applicative)instance, RootSystemConfiguration::new));
    public final Holder<PlacedFeature> treeFeature;
    public final int requiredVerticalSpaceForTree;
    public final int rootRadius;
    public final TagKey<Block> rootReplaceable;
    public final BlockStateProvider rootStateProvider;
    public final int rootPlacementAttempts;
    public final int rootColumnMaxHeight;
    public final int hangingRootRadius;
    public final int hangingRootsVerticalSpan;
    public final BlockStateProvider hangingRootStateProvider;
    public final int hangingRootPlacementAttempts;
    public final int allowedVerticalWaterForTree;
    public final BlockPredicate allowedTreePosition;

    public RootSystemConfiguration(Holder<PlacedFeature> holder, int n, int n2, TagKey<Block> tagKey, BlockStateProvider blockStateProvider, int n3, int n4, int n5, int n6, BlockStateProvider blockStateProvider2, int n7, int n8, BlockPredicate blockPredicate) {
        this.treeFeature = holder;
        this.requiredVerticalSpaceForTree = n;
        this.rootRadius = n2;
        this.rootReplaceable = tagKey;
        this.rootStateProvider = blockStateProvider;
        this.rootPlacementAttempts = n3;
        this.rootColumnMaxHeight = n4;
        this.hangingRootRadius = n5;
        this.hangingRootsVerticalSpan = n6;
        this.hangingRootStateProvider = blockStateProvider2;
        this.hangingRootPlacementAttempts = n7;
        this.allowedVerticalWaterForTree = n8;
        this.allowedTreePosition = blockPredicate;
    }
}

