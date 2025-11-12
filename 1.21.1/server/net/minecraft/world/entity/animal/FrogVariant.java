/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<FrogVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FROG_VARIANT);
    public static final ResourceKey<FrogVariant> TEMPERATE = FrogVariant.createKey("temperate");
    public static final ResourceKey<FrogVariant> WARM = FrogVariant.createKey("warm");
    public static final ResourceKey<FrogVariant> COLD = FrogVariant.createKey("cold");

    private static ResourceKey<FrogVariant> createKey(String string) {
        return ResourceKey.create(Registries.FROG_VARIANT, ResourceLocation.withDefaultNamespace(string));
    }

    public static FrogVariant bootstrap(Registry<FrogVariant> registry) {
        FrogVariant.register(registry, TEMPERATE, "textures/entity/frog/temperate_frog.png");
        FrogVariant.register(registry, WARM, "textures/entity/frog/warm_frog.png");
        return FrogVariant.register(registry, COLD, "textures/entity/frog/cold_frog.png");
    }

    private static FrogVariant register(Registry<FrogVariant> registry, ResourceKey<FrogVariant> resourceKey, String string) {
        return Registry.register(registry, resourceKey, new FrogVariant(ResourceLocation.withDefaultNamespace(string)));
    }
}

