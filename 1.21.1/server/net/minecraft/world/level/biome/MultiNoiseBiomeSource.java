/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource
extends BiomeSource {
    private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
    public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes");
    private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC = MultiNoiseBiomeSourceParameterList.CODEC.fieldOf("preset").withLifecycle(Lifecycle.stable());
    public static final MapCodec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC).xmap(MultiNoiseBiomeSource::new, multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters);
    private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

    private MultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> either) {
        this.parameters = either;
    }

    public static MultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> parameterList) {
        return new MultiNoiseBiomeSource((Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>>)Either.left(parameterList));
    }

    public static MultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> holder) {
        return new MultiNoiseBiomeSource((Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>>)Either.right(holder));
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return (Climate.ParameterList)this.parameters.map(parameterList -> parameterList, holder -> ((MultiNoiseBiomeSourceParameterList)holder.value()).parameters());
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.parameters().values().stream().map(Pair::getSecond);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> resourceKey) {
        Optional optional = this.parameters.right();
        return optional.isPresent() && ((Holder)optional.get()).is(resourceKey);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3, Climate.Sampler sampler) {
        return this.getNoiseBiome(sampler.sample(n, n2, n3));
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.parameters().findValue(targetPoint);
    }

    @Override
    public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
        int n = QuartPos.fromBlock(blockPos.getX());
        int n2 = QuartPos.fromBlock(blockPos.getY());
        int n3 = QuartPos.fromBlock(blockPos.getZ());
        Climate.TargetPoint targetPoint = sampler.sample(n, n2, n3);
        float f = Climate.unquantizeCoord(targetPoint.continentalness());
        float f2 = Climate.unquantizeCoord(targetPoint.erosion());
        float f3 = Climate.unquantizeCoord(targetPoint.temperature());
        float f4 = Climate.unquantizeCoord(targetPoint.humidity());
        float f5 = Climate.unquantizeCoord(targetPoint.weirdness());
        double d = NoiseRouterData.peaksAndValleys(f5);
        OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
        list.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d) + " C: " + overworldBiomeBuilder.getDebugStringForContinentalness(f) + " E: " + overworldBiomeBuilder.getDebugStringForErosion(f2) + " T: " + overworldBiomeBuilder.getDebugStringForTemperature(f3) + " H: " + overworldBiomeBuilder.getDebugStringForHumidity(f4));
    }
}

