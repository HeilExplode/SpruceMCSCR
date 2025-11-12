/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.sounds;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("sound_id").forGetter(SoundEvent::getLocation), (App)Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEvent::fixedRange)).apply((Applicative)instance, SoundEvent::create));
    public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
    public static final StreamCodec<ByteBuf, SoundEvent> DIRECT_STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, SoundEvent::getLocation, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), SoundEvent::fixedRange, SoundEvent::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEvent>> STREAM_CODEC = ByteBufCodecs.holder(Registries.SOUND_EVENT, DIRECT_STREAM_CODEC);
    private static final float DEFAULT_RANGE = 16.0f;
    private final ResourceLocation location;
    private final float range;
    private final boolean newSystem;

    private static SoundEvent create(ResourceLocation resourceLocation, Optional<Float> optional) {
        return optional.map(f -> SoundEvent.createFixedRangeEvent(resourceLocation, f.floatValue())).orElseGet(() -> SoundEvent.createVariableRangeEvent(resourceLocation));
    }

    public static SoundEvent createVariableRangeEvent(ResourceLocation resourceLocation) {
        return new SoundEvent(resourceLocation, 16.0f, false);
    }

    public static SoundEvent createFixedRangeEvent(ResourceLocation resourceLocation, float f) {
        return new SoundEvent(resourceLocation, f, true);
    }

    private SoundEvent(ResourceLocation resourceLocation, float f, boolean bl) {
        this.location = resourceLocation;
        this.range = f;
        this.newSystem = bl;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public float getRange(float f) {
        if (this.newSystem) {
            return this.range;
        }
        return f > 1.0f ? 16.0f * f : 16.0f;
    }

    private Optional<Float> fixedRange() {
        return this.newSystem ? Optional.of(Float.valueOf(this.range)) : Optional.empty();
    }
}

