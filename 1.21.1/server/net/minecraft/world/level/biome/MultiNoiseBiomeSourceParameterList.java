/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;

public class MultiNoiseBiomeSourceParameterList {
    public static final Codec<MultiNoiseBiomeSourceParameterList> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Preset.CODEC.fieldOf("preset").forGetter(multiNoiseBiomeSourceParameterList -> multiNoiseBiomeSourceParameterList.preset), RegistryOps.retrieveGetter(Registries.BIOME)).apply((Applicative)instance, MultiNoiseBiomeSourceParameterList::new));
    public static final Codec<Holder<MultiNoiseBiomeSourceParameterList>> CODEC = RegistryFileCodec.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, DIRECT_CODEC);
    private final Preset preset;
    private final Climate.ParameterList<Holder<Biome>> parameters;

    public MultiNoiseBiomeSourceParameterList(Preset preset, HolderGetter<Biome> holderGetter) {
        this.preset = preset;
        this.parameters = preset.provider.apply(holderGetter::getOrThrow);
    }

    public Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    public static Map<Preset, Climate.ParameterList<ResourceKey<Biome>>> knownPresets() {
        return Preset.BY_NAME.values().stream().collect(Collectors.toMap(preset -> preset, preset -> preset.provider().apply(resourceKey -> resourceKey)));
    }

    public static final class Preset
    extends Record {
        private final ResourceLocation id;
        final SourceProvider provider;
        public static final Preset NETHER = new Preset(ResourceLocation.withDefaultNamespace("nether"), new SourceProvider(){

            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
                return new Climate.ParameterList(List.of(Pair.of((Object)Climate.parameters(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(Biomes.NETHER_WASTES)), Pair.of((Object)Climate.parameters(0.0f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(Biomes.SOUL_SAND_VALLEY)), Pair.of((Object)Climate.parameters(0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(Biomes.CRIMSON_FOREST)), Pair.of((Object)Climate.parameters(0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.375f), function.apply(Biomes.WARPED_FOREST)), Pair.of((Object)Climate.parameters(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.175f), function.apply(Biomes.BASALT_DELTAS))));
            }
        });
        public static final Preset OVERWORLD = new Preset(ResourceLocation.withDefaultNamespace("overworld"), new SourceProvider(){

            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
                return Preset.generateOverworldBiomes(function);
            }
        });
        static final Map<ResourceLocation, Preset> BY_NAME = Stream.of(NETHER, OVERWORLD).collect(Collectors.toMap(Preset::id, preset -> preset));
        public static final Codec<Preset> CODEC = ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(BY_NAME.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown preset: " + String.valueOf(resourceLocation))), preset -> DataResult.success((Object)preset.id));

        public Preset(ResourceLocation resourceLocation, SourceProvider sourceProvider) {
            this.id = resourceLocation;
            this.provider = sourceProvider;
        }

        static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> function) {
            ImmutableList.Builder builder = ImmutableList.builder();
            new OverworldBiomeBuilder().addBiomes(pair -> builder.add((Object)pair.mapSecond(function)));
            return new Climate.ParameterList(builder.build());
        }

        public Stream<ResourceKey<Biome>> usedBiomes() {
            return this.provider.apply(resourceKey -> resourceKey).values().stream().map(Pair::getSecond).distinct();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Preset.class, "id;provider", "id", "provider"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Preset.class, "id;provider", "id", "provider"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Preset.class, "id;provider", "id", "provider"}, this, object);
        }

        public ResourceLocation id() {
            return this.id;
        }

        public SourceProvider provider() {
            return this.provider;
        }

        @FunctionalInterface
        static interface SourceProvider {
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> var1);
        }
    }
}

