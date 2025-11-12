/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.item.component;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public record ResolvableProfile(Optional<String> name, Optional<UUID> id, PropertyMap properties, GameProfile gameProfile) {
    private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ResolvableProfile::name), (App)UUIDUtil.CODEC.optionalFieldOf("id").forGetter(ResolvableProfile::id), (App)ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", (Object)new PropertyMap()).forGetter(ResolvableProfile::properties)).apply((Applicative)instance, ResolvableProfile::new));
    public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(FULL_CODEC, ExtraCodecs.PLAYER_NAME, string -> new ResolvableProfile(Optional.of(string), Optional.empty(), new PropertyMap()));
    public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(16).apply(ByteBufCodecs::optional), ResolvableProfile::name, UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), ResolvableProfile::id, ByteBufCodecs.GAME_PROFILE_PROPERTIES, ResolvableProfile::properties, ResolvableProfile::new);

    public ResolvableProfile(Optional<String> optional, Optional<UUID> optional2, PropertyMap propertyMap) {
        this(optional, optional2, propertyMap, ResolvableProfile.createProfile(optional, optional2, propertyMap));
    }

    public ResolvableProfile(GameProfile gameProfile) {
        this(Optional.of(gameProfile.getName()), Optional.of(gameProfile.getId()), gameProfile.getProperties(), gameProfile);
    }

    public CompletableFuture<ResolvableProfile> resolve() {
        if (this.isResolved()) {
            return CompletableFuture.completedFuture(this);
        }
        if (this.id.isPresent()) {
            return SkullBlockEntity.fetchGameProfile(this.id.get()).thenApply(optional -> {
                GameProfile gameProfile = optional.orElseGet(() -> new GameProfile(this.id.get(), this.name.orElse("")));
                return new ResolvableProfile(gameProfile);
            });
        }
        return SkullBlockEntity.fetchGameProfile(this.name.orElseThrow()).thenApply(optional -> {
            GameProfile gameProfile = optional.orElseGet(() -> new GameProfile(Util.NIL_UUID, this.name.get()));
            return new ResolvableProfile(gameProfile);
        });
    }

    private static GameProfile createProfile(Optional<String> optional, Optional<UUID> optional2, PropertyMap propertyMap) {
        GameProfile gameProfile = new GameProfile(optional2.orElse(Util.NIL_UUID), optional.orElse(""));
        gameProfile.getProperties().putAll((Multimap)propertyMap);
        return gameProfile;
    }

    public boolean isResolved() {
        if (!this.properties.isEmpty()) {
            return true;
        }
        return this.id.isPresent() == this.name.isPresent();
    }
}

