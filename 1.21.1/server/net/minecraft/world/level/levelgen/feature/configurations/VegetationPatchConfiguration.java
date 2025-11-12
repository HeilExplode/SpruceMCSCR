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
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration
implements FeatureConfiguration {
    public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.replaceable), (App)BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.groundState), (App)PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.vegetationFeature), (App)CaveSurface.CODEC.fieldOf("surface").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.surface), (App)IntProvider.codec(1, 128).fieldOf("depth").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.depth), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("extra_bottom_block_chance").forGetter(vegetationPatchConfiguration -> Float.valueOf(vegetationPatchConfiguration.extraBottomBlockChance)), (App)Codec.intRange((int)1, (int)256).fieldOf("vertical_range").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.verticalRange), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("vegetation_chance").forGetter(vegetationPatchConfiguration -> Float.valueOf(vegetationPatchConfiguration.vegetationChance)), (App)IntProvider.CODEC.fieldOf("xz_radius").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.xzRadius), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("extra_edge_column_chance").forGetter(vegetationPatchConfiguration -> Float.valueOf(vegetationPatchConfiguration.extraEdgeColumnChance))).apply((Applicative)instance, VegetationPatchConfiguration::new));
    public final TagKey<Block> replaceable;
    public final BlockStateProvider groundState;
    public final Holder<PlacedFeature> vegetationFeature;
    public final CaveSurface surface;
    public final IntProvider depth;
    public final float extraBottomBlockChance;
    public final int verticalRange;
    public final float vegetationChance;
    public final IntProvider xzRadius;
    public final float extraEdgeColumnChance;

    public VegetationPatchConfiguration(TagKey<Block> tagKey, BlockStateProvider blockStateProvider, Holder<PlacedFeature> holder, CaveSurface caveSurface, IntProvider intProvider, float f, int n, float f2, IntProvider intProvider2, float f3) {
        this.replaceable = tagKey;
        this.groundState = blockStateProvider;
        this.vegetationFeature = holder;
        this.surface = caveSurface;
        this.depth = intProvider;
        this.extraBottomBlockChance = f;
        this.verticalRange = n;
        this.vegetationChance = f2;
        this.xzRadius = intProvider2;
        this.extraEdgeColumnChance = f3;
    }
}

