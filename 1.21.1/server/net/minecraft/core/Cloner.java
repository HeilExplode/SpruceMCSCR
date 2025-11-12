/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.JavaOps
 *  javax.annotation.Nullable
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public class Cloner<T> {
    private final Codec<T> directCodec;

    Cloner(Codec<T> codec) {
        this.directCodec = codec;
    }

    public T clone(T t, HolderLookup.Provider provider, HolderLookup.Provider provider2) {
        RegistryOps registryOps = provider.createSerializationContext(JavaOps.INSTANCE);
        RegistryOps registryOps2 = provider2.createSerializationContext(JavaOps.INSTANCE);
        Object object = this.directCodec.encodeStart(registryOps, t).getOrThrow(string -> new IllegalStateException("Failed to encode: " + string));
        return (T)this.directCodec.parse(registryOps2, object).getOrThrow(string -> new IllegalStateException("Failed to decode: " + string));
    }

    public static class Factory {
        private final Map<ResourceKey<? extends Registry<?>>, Cloner<?>> codecs = new HashMap();

        public <T> Factory addCodec(ResourceKey<? extends Registry<? extends T>> resourceKey, Codec<T> codec) {
            this.codecs.put(resourceKey, new Cloner<T>(codec));
            return this;
        }

        @Nullable
        public <T> Cloner<T> cloner(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.codecs.get(resourceKey);
        }
    }
}

