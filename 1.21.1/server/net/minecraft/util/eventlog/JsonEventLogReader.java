/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 *  com.google.gson.stream.JsonReader
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 */
package net.minecraft.util.eventlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import javax.annotation.Nullable;

public interface JsonEventLogReader<T>
extends Closeable {
    public static <T> JsonEventLogReader<T> create(final Codec<T> codec, Reader reader) {
        final JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        return new JsonEventLogReader<T>(){

            @Override
            @Nullable
            public T next() throws IOException {
                try {
                    if (!jsonReader.hasNext()) {
                        return null;
                    }
                    JsonElement jsonElement = JsonParser.parseReader((JsonReader)jsonReader);
                    return codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(IOException::new);
                }
                catch (JsonParseException jsonParseException) {
                    throw new IOException(jsonParseException);
                }
                catch (EOFException eOFException) {
                    return null;
                }
            }

            @Override
            public void close() throws IOException {
                jsonReader.close();
            }
        };
    }

    @Nullable
    public T next() throws IOException;
}

