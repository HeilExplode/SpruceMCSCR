/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
    public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ConfiguredFeature.CODEC.fieldOf("feature").forGetter(placedFeature -> placedFeature.feature), (App)PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(placedFeature -> placedFeature.placement)).apply((Applicative)instance, PlacedFeature::new));
    public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registries.PLACED_FEATURE, DIRECT_CODEC);
    public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC);
    public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC, true).listOf();

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.empty()), randomSource, blockPos);
    }

    public boolean placeWithBiomeCheck(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.of(this)), randomSource, blockPos);
    }

    private boolean placeWithContext(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos2) {
        PlacementModifier placementModifier2;
        Stream<BlockPos> stream = Stream.of(blockPos2);
        for (PlacementModifier placementModifier2 : this.placement) {
            stream = stream.flatMap(blockPos -> placementModifier2.getPositions(placementContext, randomSource, (BlockPos)blockPos));
        }
        ConfiguredFeature<?, ?> configuredFeature = this.feature.value();
        placementModifier2 = new MutableBoolean();
        stream.forEach(arg_0 -> PlacedFeature.lambda$placeWithContext$4(configuredFeature, placementContext, randomSource, (MutableBoolean)placementModifier2, arg_0));
        return placementModifier2.isTrue();
    }

    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.value().getFeatures();
    }

    @Override
    public String toString() {
        return "Placed " + String.valueOf(this.feature);
    }

    private static /* synthetic */ void lambda$placeWithContext$4(ConfiguredFeature configuredFeature, PlacementContext placementContext, RandomSource randomSource, MutableBoolean mutableBoolean, BlockPos blockPos) {
        if (configuredFeature.place(placementContext.getLevel(), placementContext.generator(), randomSource, blockPos)) {
            mutableBoolean.setTrue();
        }
    }
}

