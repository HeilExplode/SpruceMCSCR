/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TypedDataComponent<T>(DataComponentType<T> type, T value) {
    public static final StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>>(){

        @Override
        public TypedDataComponent<?> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            DataComponentType dataComponentType = (DataComponentType)DataComponentType.STREAM_CODEC.decode(registryFriendlyByteBuf);
            return 1.decodeTyped(registryFriendlyByteBuf, dataComponentType);
        }

        private static <T> TypedDataComponent<T> decodeTyped(RegistryFriendlyByteBuf registryFriendlyByteBuf, DataComponentType<T> dataComponentType) {
            return new TypedDataComponent<T>(dataComponentType, dataComponentType.streamCodec().decode(registryFriendlyByteBuf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, TypedDataComponent<?> typedDataComponent) {
            1.encodeCap(registryFriendlyByteBuf, typedDataComponent);
        }

        private static <T> void encodeCap(RegistryFriendlyByteBuf registryFriendlyByteBuf, TypedDataComponent<T> typedDataComponent) {
            DataComponentType.STREAM_CODEC.encode(registryFriendlyByteBuf, typedDataComponent.type());
            typedDataComponent.type().streamCodec().encode(registryFriendlyByteBuf, typedDataComponent.value());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryFriendlyByteBuf)((Object)object), (TypedDataComponent)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryFriendlyByteBuf)((Object)object));
        }
    };

    static TypedDataComponent<?> fromEntryUnchecked(Map.Entry<DataComponentType<?>, Object> entry) {
        return TypedDataComponent.createUnchecked(entry.getKey(), entry.getValue());
    }

    public static <T> TypedDataComponent<T> createUnchecked(DataComponentType<T> dataComponentType, Object object) {
        return new TypedDataComponent<Object>(dataComponentType, object);
    }

    public void applyTo(PatchedDataComponentMap patchedDataComponentMap) {
        patchedDataComponentMap.set(this.type, this.value);
    }

    public <D> DataResult<D> encodeValue(DynamicOps<D> dynamicOps) {
        Codec<T> codec = this.type.codec();
        if (codec == null) {
            return DataResult.error(() -> "Component of type " + String.valueOf(this.type) + " is not encodable");
        }
        return codec.encodeStart(dynamicOps, this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.type) + "=>" + String.valueOf(this.value);
    }
}

