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

public final class RegistryFixedCodec<E>
implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public static <E> RegistryFixedCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey) {
        return new RegistryFixedCodec<E>(resourceKey);
    }

    private RegistryFixedCodec(ResourceKey<? extends Registry<E>> resourceKey) {
        this.registryKey = resourceKey;
    }

    public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).owner(this.registryKey)).isPresent()) {
            if (!holder.canSerializeIn(optional.get())) {
                return DataResult.error(() -> "Element " + String.valueOf(holder) + " is not valid in current registry set");
            }
            return (DataResult)holder.unwrap().map(resourceKey -> ResourceLocation.CODEC.encode((Object)resourceKey.location(), dynamicOps, t), object -> DataResult.error(() -> "Elements from registry " + String.valueOf(this.registryKey) + " can't be serialized to a value"));
        }
        return DataResult.error(() -> "Can't access registry " + String.valueOf(this.registryKey));
    }

    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T t) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).getter(this.registryKey)).isPresent()) {
            return ResourceLocation.CODEC.decode(dynamicOps, t).flatMap(pair -> {
                ResourceLocation resourceLocation = (ResourceLocation)pair.getFirst();
                return ((HolderGetter)optional.get()).get(ResourceKey.create(this.registryKey, resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + String.valueOf(resourceLocation))).map(reference -> Pair.of((Object)reference, (Object)pair.getSecond())).setLifecycle(Lifecycle.stable());
            });
        }
        return DataResult.error(() -> "Can't access registry " + String.valueOf(this.registryKey));
    }

    public String toString() {
        return "RegistryFixedCodec[" + String.valueOf(this.registryKey) + "]";
    }

    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((Holder)object, dynamicOps, object2);
    }
}

