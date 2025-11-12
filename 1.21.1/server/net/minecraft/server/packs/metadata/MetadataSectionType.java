/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.server.packs.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public interface MetadataSectionType<T>
extends MetadataSectionSerializer<T> {
    public JsonObject toJson(T var1);

    public static <T> MetadataSectionType<T> fromCodec(final String string, final Codec<T> codec) {
        return new MetadataSectionType<T>(){

            @Override
            public String getMetadataSectionName() {
                return string;
            }

            @Override
            public T fromJson(JsonObject jsonObject) {
                return codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonObject).getOrThrow(JsonParseException::new);
            }

            @Override
            public JsonObject toJson(T t) {
                return ((JsonElement)codec.encodeStart((DynamicOps)JsonOps.INSTANCE, t).getOrThrow(IllegalArgumentException::new)).getAsJsonObject();
            }
        };
    }
}

