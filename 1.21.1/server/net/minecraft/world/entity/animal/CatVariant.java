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

public record CatVariant(ResourceLocation texture) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<CatVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.CAT_VARIANT);
    public static final ResourceKey<CatVariant> TABBY = CatVariant.createKey("tabby");
    public static final ResourceKey<CatVariant> BLACK = CatVariant.createKey("black");
    public static final ResourceKey<CatVariant> RED = CatVariant.createKey("red");
    public static final ResourceKey<CatVariant> SIAMESE = CatVariant.createKey("siamese");
    public static final ResourceKey<CatVariant> BRITISH_SHORTHAIR = CatVariant.createKey("british_shorthair");
    public static final ResourceKey<CatVariant> CALICO = CatVariant.createKey("calico");
    public static final ResourceKey<CatVariant> PERSIAN = CatVariant.createKey("persian");
    public static final ResourceKey<CatVariant> RAGDOLL = CatVariant.createKey("ragdoll");
    public static final ResourceKey<CatVariant> WHITE = CatVariant.createKey("white");
    public static final ResourceKey<CatVariant> JELLIE = CatVariant.createKey("jellie");
    public static final ResourceKey<CatVariant> ALL_BLACK = CatVariant.createKey("all_black");

    private static ResourceKey<CatVariant> createKey(String string) {
        return ResourceKey.create(Registries.CAT_VARIANT, ResourceLocation.withDefaultNamespace(string));
    }

    public static CatVariant bootstrap(Registry<CatVariant> registry) {
        CatVariant.register(registry, TABBY, "textures/entity/cat/tabby.png");
        CatVariant.register(registry, BLACK, "textures/entity/cat/black.png");
        CatVariant.register(registry, RED, "textures/entity/cat/red.png");
        CatVariant.register(registry, SIAMESE, "textures/entity/cat/siamese.png");
        CatVariant.register(registry, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
        CatVariant.register(registry, CALICO, "textures/entity/cat/calico.png");
        CatVariant.register(registry, PERSIAN, "textures/entity/cat/persian.png");
        CatVariant.register(registry, RAGDOLL, "textures/entity/cat/ragdoll.png");
        CatVariant.register(registry, WHITE, "textures/entity/cat/white.png");
        CatVariant.register(registry, JELLIE, "textures/entity/cat/jellie.png");
        return CatVariant.register(registry, ALL_BLACK, "textures/entity/cat/all_black.png");
    }

    private static CatVariant register(Registry<CatVariant> registry, ResourceKey<CatVariant> resourceKey, String string) {
        return Registry.register(registry, resourceKey, new CatVariant(ResourceLocation.withDefaultNamespace(string)));
    }
}

