/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryFileCodec<E>
implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<E> elementCodec;
    private final boolean allowInline;

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        return RegistryFileCodec.create(resourceKey, codec, true);
    }

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        return new RegistryFileCodec<E>(resourceKey, codec, bl);
    }

    private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        this.registryKey = resourceKey;
        this.elementCodec = codec;
        this.allowInline = bl;
    }

    public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).owner(this.registryKey)).isPresent()) {
            if (!holder.canSerializeIn(optional.get())) {
                return DataResult.error(() -> "Element " + String.valueOf(holder) + " is not valid in current registry set");
            }
            return (DataResult)holder.unwrap().map(resourceKey -> ResourceLocation.CODEC.encode((Object)resourceKey.location(), dynamicOps, t), object2 -> this.elementCodec.encode(object2, dynamicOps, t));
        }
        return this.elementCodec.encode(holder.value(), dynamicOps, t);
    }

    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T t) {
        if (dynamicOps instanceof RegistryOps) {
            RegistryOps registryOps = (RegistryOps)dynamicOps;
            Optional optional = registryOps.getter(this.registryKey);
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Registry does not exist: " + String.valueOf(this.registryKey));
            }
            HolderGetter holderGetter = optional.get();
            DataResult dataResult = ResourceLocation.CODEC.decode(dynamicOps, t);
            if (dataResult.result().isEmpty()) {
                if (!this.allowInline) {
                    return DataResult.error(() -> "Inline definitions not allowed here");
                }
                return this.elementCodec.decode(dynamicOps, t).map(pair -> pair.mapFirst(Holder::direct));
            }
            Pair pair2 = (Pair)dataResult.result().get();
            ResourceKey resourceKey = ResourceKey.create(this.registryKey, (ResourceLocation)pair2.getFirst());
            return holderGetter.get(resourceKey).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + String.valueOf(resourceKey))).map(reference -> Pair.of((Object)reference, (Object)pair2.getSecond())).setLifecycle(Lifecycle.stable());
        }
        return this.elementCodec.decode(dynamicOps, t).map(pair -> pair.mapFirst(Holder::direct));
    }

    public String toString() {
        return "RegistryFileCodec[" + String.valueOf(this.registryKey) + " " + String.valueOf(this.elementCodec) + "]";
    }

    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((Holder)object, dynamicOps, object2);
    }
}

