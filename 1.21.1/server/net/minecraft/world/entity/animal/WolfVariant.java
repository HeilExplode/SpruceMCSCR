/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.animal;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class WolfVariant {
    public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("wild_texture").forGetter(wolfVariant -> wolfVariant.wildTexture), (App)ResourceLocation.CODEC.fieldOf("tame_texture").forGetter(wolfVariant -> wolfVariant.tameTexture), (App)ResourceLocation.CODEC.fieldOf("angry_texture").forGetter(wolfVariant -> wolfVariant.angryTexture), (App)RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(WolfVariant::biomes)).apply((Applicative)instance, WolfVariant::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WolfVariant> DIRECT_STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, WolfVariant::wildTexture, ResourceLocation.STREAM_CODEC, WolfVariant::tameTexture, ResourceLocation.STREAM_CODEC, WolfVariant::angryTexture, ByteBufCodecs.holderSet(Registries.BIOME), WolfVariant::biomes, WolfVariant::new);
    public static final Codec<Holder<WolfVariant>> CODEC = RegistryFileCodec.create(Registries.WOLF_VARIANT, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfVariant>> STREAM_CODEC = ByteBufCodecs.holder(Registries.WOLF_VARIANT, DIRECT_STREAM_CODEC);
    private final ResourceLocation wildTexture;
    private final ResourceLocation tameTexture;
    private final ResourceLocation angryTexture;
    private final ResourceLocation wildTextureFull;
    private final ResourceLocation tameTextureFull;
    private final ResourceLocation angryTextureFull;
    private final HolderSet<Biome> biomes;

    public WolfVariant(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, HolderSet<Biome> holderSet) {
        this.wildTexture = resourceLocation;
        this.wildTextureFull = WolfVariant.fullTextureId(resourceLocation);
        this.tameTexture = resourceLocation2;
        this.tameTextureFull = WolfVariant.fullTextureId(resourceLocation2);
        this.angryTexture = resourceLocation3;
        this.angryTextureFull = WolfVariant.fullTextureId(resourceLocation3);
        this.biomes = holderSet;
    }

    private static ResourceLocation fullTextureId(ResourceLocation resourceLocation) {
        return resourceLocation.withPath(string -> "textures/" + string + ".png");
    }

    public ResourceLocation wildTexture() {
        return this.wildTextureFull;
    }

    public ResourceLocation tameTexture() {
        return this.tameTextureFull;
    }

    public ResourceLocation angryTexture() {
        return this.angryTextureFull;
    }

    public HolderSet<Biome> biomes() {
        return this.biomes;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof WolfVariant) {
            WolfVariant wolfVariant = (WolfVariant)object;
            return Objects.equals(this.wildTexture, wolfVariant.wildTexture) && Objects.equals(this.tameTexture, wolfVariant.tameTexture) && Objects.equals(this.angryTexture, wolfVariant.angryTexture) && Objects.equals(this.biomes, wolfVariant.biomes);
        }
        return false;
    }

    public int hashCode() {
        int n = 1;
        n = 31 * n + this.wildTexture.hashCode();
        n = 31 * n + this.tameTexture.hashCode();
        n = 31 * n + this.angryTexture.hashCode();
        n = 31 * n + this.biomes.hashCode();
        return n;
    }
}

