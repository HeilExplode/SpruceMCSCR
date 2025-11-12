/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;

public record WorldDimensions(Map<ResourceKey<LevelStem>, LevelStem> dimensions) {
    public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter(WorldDimensions::dimensions)).apply((Applicative)instance, instance.stable(WorldDimensions::new)));
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);
    private static final int VANILLA_DIMENSION_COUNT = BUILTIN_ORDER.size();

    public WorldDimensions {
        LevelStem levelStem = map.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    public WorldDimensions(Registry<LevelStem> registry) {
        this(registry.holders().collect(Collectors.toMap(Holder.Reference::key, Holder.Reference::value)));
    }

    public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> stream) {
        return Stream.concat(BUILTIN_ORDER.stream(), stream.filter(resourceKey -> !BUILTIN_ORDER.contains(resourceKey)));
    }

    public WorldDimensions replaceOverworldGenerator(RegistryAccess registryAccess, ChunkGenerator chunkGenerator) {
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);
        Map<ResourceKey<LevelStem>, LevelStem> map = WorldDimensions.withOverworld(registry, this.dimensions, chunkGenerator);
        return new WorldDimensions(map);
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> withOverworld(Registry<DimensionType> registry, Map<ResourceKey<LevelStem>, LevelStem> map, ChunkGenerator chunkGenerator) {
        LevelStem levelStem = map.get(LevelStem.OVERWORLD);
        Holder<DimensionType> holder = levelStem == null ? registry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : levelStem.type();
        return WorldDimensions.withOverworld(map, holder, chunkGenerator);
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> withOverworld(Map<ResourceKey<LevelStem>, LevelStem> map, Holder<DimensionType> holder, ChunkGenerator chunkGenerator) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.putAll(map);
        builder.put(LevelStem.OVERWORLD, (Object)new LevelStem(holder, chunkGenerator));
        return builder.buildKeepingLast();
    }

    public ChunkGenerator overworld() {
        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
        return levelStem.generator();
    }

    public Optional<LevelStem> get(ResourceKey<LevelStem> resourceKey) {
        return Optional.ofNullable(this.dimensions.get(resourceKey));
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return (ImmutableSet)this.dimensions().keySet().stream().map(Registries::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(Registry<LevelStem> registry) {
        return registry.getOptional(LevelStem.OVERWORLD).map(levelStem -> {
            ChunkGenerator chunkGenerator = levelStem.generator();
            if (chunkGenerator instanceof DebugLevelSource) {
                return PrimaryLevelData.SpecialWorldProperty.DEBUG;
            }
            if (chunkGenerator instanceof FlatLevelSource) {
                return PrimaryLevelData.SpecialWorldProperty.FLAT;
            }
            return PrimaryLevelData.SpecialWorldProperty.NONE;
        }).orElse(PrimaryLevelData.SpecialWorldProperty.NONE);
    }

    static Lifecycle checkStability(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
        return WorldDimensions.isVanillaLike(resourceKey, levelStem) ? Lifecycle.stable() : Lifecycle.experimental();
    }

    private static boolean isVanillaLike(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
        if (resourceKey == LevelStem.OVERWORLD) {
            return WorldDimensions.isStableOverworld(levelStem);
        }
        if (resourceKey == LevelStem.NETHER) {
            return WorldDimensions.isStableNether(levelStem);
        }
        if (resourceKey == LevelStem.END) {
            return WorldDimensions.isStableEnd(levelStem);
        }
        return false;
    }

    private static boolean isStableOverworld(LevelStem levelStem) {
        MultiNoiseBiomeSource multiNoiseBiomeSource;
        Holder<DimensionType> holder = levelStem.type();
        if (!holder.is(BuiltinDimensionTypes.OVERWORLD) && !holder.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            return false;
        }
        BiomeSource biomeSource = levelStem.generator().getBiomeSource();
        return !(biomeSource instanceof MultiNoiseBiomeSource) || (multiNoiseBiomeSource = (MultiNoiseBiomeSource)biomeSource).stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
    }

    private static boolean isStableNether(LevelStem levelStem) {
        MultiNoiseBiomeSource multiNoiseBiomeSource;
        NoiseBasedChunkGenerator noiseBasedChunkGenerator;
        Object object;
        return levelStem.type().is(BuiltinDimensionTypes.NETHER) && (object = levelStem.generator()) instanceof NoiseBasedChunkGenerator && (noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)object).stable(NoiseGeneratorSettings.NETHER) && (object = noiseBasedChunkGenerator.getBiomeSource()) instanceof MultiNoiseBiomeSource && (multiNoiseBiomeSource = (MultiNoiseBiomeSource)object).stable(MultiNoiseBiomeSourceParameterLists.NETHER);
    }

    private static boolean isStableEnd(LevelStem levelStem) {
        NoiseBasedChunkGenerator noiseBasedChunkGenerator;
        ChunkGenerator chunkGenerator;
        return levelStem.type().is(BuiltinDimensionTypes.END) && (chunkGenerator = levelStem.generator()) instanceof NoiseBasedChunkGenerator && (noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)chunkGenerator).stable(NoiseGeneratorSettings.END) && noiseBasedChunkGenerator.getBiomeSource() instanceof TheEndBiomeSource;
    }

    public Complete bake(Registry<LevelStem> registry) {
        final class Entry
        extends Record {
            final ResourceKey<LevelStem> key;
            final LevelStem value;

            Entry(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
                this.key = resourceKey;
                this.value = levelStem;
            }

            RegistrationInfo registrationInfo() {
                return new RegistrationInfo(Optional.empty(), WorldDimensions.checkStability(this.key, this.value));
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this, object);
            }

            public ResourceKey<LevelStem> key() {
                return this.key;
            }

            public LevelStem value() {
                return this.value;
            }
        }
        Stream<ResourceKey<LevelStem>> stream = Stream.concat(registry.registryKeySet().stream(), this.dimensions.keySet().stream()).distinct();
        ArrayList arrayList = new ArrayList();
        WorldDimensions.keysInOrder(stream).forEach(resourceKey -> registry.getOptional((ResourceKey<LevelStem>)resourceKey).or(() -> Optional.ofNullable(this.dimensions.get(resourceKey))).ifPresent(levelStem -> arrayList.add(new Entry((ResourceKey<LevelStem>)resourceKey, (LevelStem)levelStem))));
        Lifecycle lifecycle = arrayList.size() == VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
        MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, lifecycle);
        arrayList.forEach(entry -> mappedRegistry.register(entry.key, entry.value, entry.registrationInfo()));
        Registry<LevelStem> registry2 = mappedRegistry.freeze();
        PrimaryLevelData.SpecialWorldProperty specialWorldProperty = WorldDimensions.specialWorldProperty(registry2);
        return new Complete(registry2.freeze(), specialWorldProperty);
    }

    public record Complete(Registry<LevelStem> dimensions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty) {
        public Lifecycle lifecycle() {
            return this.dimensions.registryLifecycle();
        }

        public RegistryAccess.Frozen dimensionsRegistryAccess() {
            return new RegistryAccess.ImmutableRegistryAccess(List.of(this.dimensions)).freeze();
        }
    }
}

