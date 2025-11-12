/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
    public final BlockStateProvider fillingProvider;
    public final BlockStateProvider innerLayerProvider;
    public final BlockStateProvider alternateInnerLayerProvider;
    public final BlockStateProvider middleLayerProvider;
    public final BlockStateProvider outerLayerProvider;
    public final List<BlockState> innerPlacements;
    public final TagKey<Block> cannotReplace;
    public final TagKey<Block> invalidBlocks;
    public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.CODEC.fieldOf("filling_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.fillingProvider), (App)BlockStateProvider.CODEC.fieldOf("inner_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.innerLayerProvider), (App)BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.alternateInnerLayerProvider), (App)BlockStateProvider.CODEC.fieldOf("middle_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.middleLayerProvider), (App)BlockStateProvider.CODEC.fieldOf("outer_layer_provider").forGetter(geodeBlockSettings -> geodeBlockSettings.outerLayerProvider), (App)ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(geodeBlockSettings -> geodeBlockSettings.innerPlacements), (App)TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace").forGetter(geodeBlockSettings -> geodeBlockSettings.cannotReplace), (App)TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks").forGetter(geodeBlockSettings -> geodeBlockSettings.invalidBlocks)).apply((Applicative)instance, GeodeBlockSettings::new));

    public GeodeBlockSettings(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, BlockStateProvider blockStateProvider3, BlockStateProvider blockStateProvider4, BlockStateProvider blockStateProvider5, List<BlockState> list, TagKey<Block> tagKey, TagKey<Block> tagKey2) {
        this.fillingProvider = blockStateProvider;
        this.innerLayerProvider = blockStateProvider2;
        this.alternateInnerLayerProvider = blockStateProvider3;
        this.middleLayerProvider = blockStateProvider4;
        this.outerLayerProvider = blockStateProvider5;
        this.innerPlacements = list;
        this.cannotReplace = tagKey;
        this.invalidBlocks = tagKey2;
    }
}

