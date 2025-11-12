/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  javax.annotation.Nullable
 */
package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentType<T> {
    public static final Codec<DataComponentType<?>> CODEC = Codec.lazyInitialized(() -> BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentType<?>> STREAM_CODEC = StreamCodec.recursive(streamCodec -> ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE));
    public static final Codec<DataComponentType<?>> PERSISTENT_CODEC = CODEC.validate(dataComponentType -> dataComponentType.isTransient() ? DataResult.error(() -> "Encountered transient component " + String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey((DataComponentType<?>)dataComponentType))) : DataResult.success((Object)dataComponentType));
    public static final Codec<Map<DataComponentType<?>, Object>> VALUE_MAP_CODEC = Codec.dispatchedMap(PERSISTENT_CODEC, DataComponentType::codecOrThrow);

    public static <T> Builder<T> builder() {
        return new Builder();
    }

    @Nullable
    public Codec<T> codec();

    default public Codec<T> codecOrThrow() {
        Codec<T> codec = this.codec();
        if (codec == null) {
            throw new IllegalStateException(String.valueOf(this) + " is not a persistent component");
        }
        return codec;
    }

    default public boolean isTransient() {
        return this.codec() == null;
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    public static class Builder<T> {
        @Nullable
        private Codec<T> codec;
        @Nullable
        private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
        private boolean cacheEncoding;

        public Builder<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            this.streamCodec = streamCodec;
            return this;
        }

        public Builder<T> cacheEncoding() {
            this.cacheEncoding = true;
            return this;
        }

        public DataComponentType<T> build() {
            StreamCodec streamCodec = Objects.requireNonNullElseGet(this.streamCodec, () -> ByteBufCodecs.fromCodecWithRegistries(Objects.requireNonNull(this.codec, "Missing Codec for component")));
            Codec<T> codec = this.cacheEncoding && this.codec != null ? DataComponents.ENCODER_CACHE.wrap(this.codec) : this.codec;
            return new SimpleType<T>(codec, streamCodec);
        }

        static class SimpleType<T>
        implements DataComponentType<T> {
            @Nullable
            private final Codec<T> codec;
            private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

            SimpleType(@Nullable Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
                this.codec = codec;
                this.streamCodec = streamCodec;
            }

            @Override
            @Nullable
            public Codec<T> codec() {
                return this.codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return this.streamCodec;
            }

            public String toString() {
                return Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, this);
            }
        }
    }
}

