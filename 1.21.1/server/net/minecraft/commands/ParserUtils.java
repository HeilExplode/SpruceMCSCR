/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.internal.Streams
 *  com.google.gson.stream.JsonReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import net.minecraft.CharPredicate;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;

public class ParserUtils {
    private static final Field JSON_READER_POS = Util.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
        }
        catch (NoSuchFieldException noSuchFieldException) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", noSuchFieldException);
        }
    });
    private static final Field JSON_READER_LINESTART = Util.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
        }
        catch (NoSuchFieldException noSuchFieldException) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", noSuchFieldException);
        }
    });

    private static int getPos(JsonReader jsonReader) {
        try {
            return JSON_READER_POS.getInt(jsonReader) - JSON_READER_LINESTART.getInt(jsonReader);
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalAccessException);
        }
    }

    public static <T> T parseJson(HolderLookup.Provider provider, com.mojang.brigadier.StringReader stringReader, Codec<T> codec) {
        JsonReader jsonReader = new JsonReader((Reader)new StringReader(stringReader.getRemaining()));
        jsonReader.setLenient(false);
        try {
            JsonElement jsonElement = Streams.parse((JsonReader)jsonReader);
            Object object = codec.parse(provider.createSerializationContext(JsonOps.INSTANCE), (Object)jsonElement).getOrThrow(JsonParseException::new);
            return (T)object;
        }
        catch (StackOverflowError stackOverflowError) {
            throw new JsonParseException((Throwable)stackOverflowError);
        }
        finally {
            stringReader.setCursor(stringReader.getCursor() + ParserUtils.getPos(jsonReader));
        }
    }

    public static String readWhile(com.mojang.brigadier.StringReader stringReader, CharPredicate charPredicate) {
        int n = stringReader.getCursor();
        while (stringReader.canRead() && charPredicate.test(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(n, stringReader.getCursor());
    }
}

