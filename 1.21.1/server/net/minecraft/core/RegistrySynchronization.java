/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {
    public static final Set<ResourceKey<? extends Registry<?>>> NETWORKABLE_REGISTRIES = RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream().map(RegistryDataLoader.RegistryData::key).collect(Collectors.toUnmodifiableSet());

    public static void packRegistries(DynamicOps<Tag> dynamicOps, RegistryAccess registryAccess, Set<KnownPack> set, BiConsumer<ResourceKey<? extends Registry<?>>, List<PackedRegistryEntry>> biConsumer) {
        RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach(registryData -> RegistrySynchronization.packRegistry(dynamicOps, registryData, registryAccess, set, biConsumer));
    }

    private static <T> void packRegistry(DynamicOps<Tag> dynamicOps, RegistryDataLoader.RegistryData<T> registryData, RegistryAccess registryAccess, Set<KnownPack> set, BiConsumer<ResourceKey<? extends Registry<?>>, List<PackedRegistryEntry>> biConsumer) {
        registryAccess.registry(registryData.key()).ifPresent(registry -> {
            ArrayList arrayList = new ArrayList(registry.size());
            registry.holders().forEach(reference -> {
                Optional<Tag> optional;
                boolean bl = registry.registrationInfo(reference.key()).flatMap(RegistrationInfo::knownPackInfo).filter(set::contains).isPresent();
                if (bl) {
                    optional = Optional.empty();
                } else {
                    Tag tag = (Tag)registryData.elementCodec().encodeStart(dynamicOps, reference.value()).getOrThrow(string -> new IllegalArgumentException("Failed to serialize " + String.valueOf(reference.key()) + ": " + string));
                    optional = Optional.of(tag);
                }
                arrayList.add(new PackedRegistryEntry(reference.key().location(), optional));
            });
            biConsumer.accept(registry.key(), arrayList);
        });
    }

    private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess registryAccess) {
        return registryAccess.registries().filter(registryEntry -> NETWORKABLE_REGISTRIES.contains(registryEntry.key()));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        return RegistrySynchronization.ownedNetworkableRegistries(layeredRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        Stream<RegistryAccess.RegistryEntry<?>> stream = layeredRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
        Stream<RegistryAccess.RegistryEntry<?>> stream2 = RegistrySynchronization.networkedRegistries(layeredRegistryAccess);
        return Stream.concat(stream2, stream);
    }

    public record PackedRegistryEntry(ResourceLocation id, Optional<Tag> data) {
        public static final StreamCodec<ByteBuf, PackedRegistryEntry> STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, PackedRegistryEntry::id, ByteBufCodecs.TAG.apply(ByteBufCodecs::optional), PackedRegistryEntry::data, PackedRegistryEntry::new);
    }
}

