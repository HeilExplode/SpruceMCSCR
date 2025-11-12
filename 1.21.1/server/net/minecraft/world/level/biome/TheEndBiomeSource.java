/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource
extends BiomeSource {
    public static final MapCodec<TheEndBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RegistryOps.retrieveElement(Biomes.THE_END), RegistryOps.retrieveElement(Biomes.END_HIGHLANDS), RegistryOps.retrieveElement(Biomes.END_MIDLANDS), RegistryOps.retrieveElement(Biomes.SMALL_END_ISLANDS), RegistryOps.retrieveElement(Biomes.END_BARRENS)).apply((Applicative)instance, instance.stable(TheEndBiomeSource::new)));
    private final Holder<Biome> end;
    private final Holder<Biome> highlands;
    private final Holder<Biome> midlands;
    private final Holder<Biome> islands;
    private final Holder<Biome> barrens;

    public static TheEndBiomeSource create(HolderGetter<Biome> holderGetter) {
        return new TheEndBiomeSource(holderGetter.getOrThrow(Biomes.THE_END), holderGetter.getOrThrow(Biomes.END_HIGHLANDS), holderGetter.getOrThrow(Biomes.END_MIDLANDS), holderGetter.getOrThrow(Biomes.SMALL_END_ISLANDS), holderGetter.getOrThrow(Biomes.END_BARRENS));
    }

    private TheEndBiomeSource(Holder<Biome> holder, Holder<Biome> holder2, Holder<Biome> holder3, Holder<Biome> holder4, Holder<Biome> holder5) {
        this.end = holder;
        this.highlands = holder2;
        this.midlands = holder3;
        this.islands = holder4;
        this.barrens = holder5;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(this.end, this.highlands, this.midlands, this.islands, this.barrens);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int n, int n2, int n3, Climate.Sampler sampler) {
        int n4;
        int n5 = QuartPos.toBlock(n);
        int n6 = QuartPos.toBlock(n2);
        int n7 = QuartPos.toBlock(n3);
        int n8 = SectionPos.blockToSectionCoord(n5);
        if ((long)n8 * (long)n8 + (long)(n4 = SectionPos.blockToSectionCoord(n7)) * (long)n4 <= 4096L) {
            return this.end;
        }
        int n9 = (SectionPos.blockToSectionCoord(n5) * 2 + 1) * 8;
        int n10 = (SectionPos.blockToSectionCoord(n7) * 2 + 1) * 8;
        double d = sampler.erosion().compute(new DensityFunction.SinglePointContext(n9, n6, n10));
        if (d > 0.25) {
            return this.highlands;
        }
        if (d >= -0.0625) {
            return this.midlands;
        }
        if (d < -0.21875) {
            return this.islands;
        }
        return this.barrens;
    }
}

