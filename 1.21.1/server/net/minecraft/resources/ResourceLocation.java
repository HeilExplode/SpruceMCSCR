/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonPrimitive
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;

public final class ResourceLocation
implements Comparable<ResourceLocation> {
    public static final Codec<ResourceLocation> CODEC = Codec.STRING.comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
    public static final StreamCodec<ByteBuf, ResourceLocation> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ResourceLocation::parse, ResourceLocation::toString);
    public static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType((Message)Component.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    private ResourceLocation(String string, String string2) {
        assert (ResourceLocation.isValidNamespace(string));
        assert (ResourceLocation.isValidPath(string2));
        this.namespace = string;
        this.path = string2;
    }

    private static ResourceLocation createUntrusted(String string, String string2) {
        return new ResourceLocation(ResourceLocation.assertValidNamespace(string, string2), ResourceLocation.assertValidPath(string, string2));
    }

    public static ResourceLocation fromNamespaceAndPath(String string, String string2) {
        return ResourceLocation.createUntrusted(string, string2);
    }

    public static ResourceLocation parse(String string) {
        return ResourceLocation.bySeparator(string, ':');
    }

    public static ResourceLocation withDefaultNamespace(String string) {
        return new ResourceLocation(DEFAULT_NAMESPACE, ResourceLocation.assertValidPath(DEFAULT_NAMESPACE, string));
    }

    @Nullable
    public static ResourceLocation tryParse(String string) {
        return ResourceLocation.tryBySeparator(string, ':');
    }

    @Nullable
    public static ResourceLocation tryBuild(String string, String string2) {
        if (ResourceLocation.isValidNamespace(string) && ResourceLocation.isValidPath(string2)) {
            return new ResourceLocation(string, string2);
        }
        return null;
    }

    public static ResourceLocation bySeparator(String string, char c) {
        int n = string.indexOf(c);
        if (n >= 0) {
            String string2 = string.substring(n + 1);
            if (n != 0) {
                String string3 = string.substring(0, n);
                return ResourceLocation.createUntrusted(string3, string2);
            }
            return ResourceLocation.withDefaultNamespace(string2);
        }
        return ResourceLocation.withDefaultNamespace(string);
    }

    @Nullable
    public static ResourceLocation tryBySeparator(String string, char c) {
        int n = string.indexOf(c);
        if (n >= 0) {
            String string2 = string.substring(n + 1);
            if (!ResourceLocation.isValidPath(string2)) {
                return null;
            }
            if (n != 0) {
                String string3 = string.substring(0, n);
                return ResourceLocation.isValidNamespace(string3) ? new ResourceLocation(string3, string2) : null;
            }
            return new ResourceLocation(DEFAULT_NAMESPACE, string2);
        }
        return ResourceLocation.isValidPath(string) ? new ResourceLocation(DEFAULT_NAMESPACE, string) : null;
    }

    public static DataResult<ResourceLocation> read(String string) {
        try {
            return DataResult.success((Object)ResourceLocation.parse(string));
        }
        catch (ResourceLocationException resourceLocationException) {
            return DataResult.error(() -> "Not a valid resource location: " + string + " " + resourceLocationException.getMessage());
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public ResourceLocation withPath(String string) {
        return new ResourceLocation(this.namespace, ResourceLocation.assertValidPath(this.namespace, string));
    }

    public ResourceLocation withPath(UnaryOperator<String> unaryOperator) {
        return this.withPath((String)unaryOperator.apply(this.path));
    }

    public ResourceLocation withPrefix(String string) {
        return this.withPath(string + this.path);
    }

    public ResourceLocation withSuffix(String string) {
        return this.withPath(this.path + string);
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ResourceLocation) {
            ResourceLocation resourceLocation = (ResourceLocation)object;
            return this.namespace.equals(resourceLocation.namespace) && this.path.equals(resourceLocation.path);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    @Override
    public int compareTo(ResourceLocation resourceLocation) {
        int n = this.path.compareTo(resourceLocation.path);
        if (n == 0) {
            n = this.namespace.compareTo(resourceLocation.namespace);
        }
        return n;
    }

    public String toDebugFileName() {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public String toLanguageKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortLanguageKey() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toLanguageKey();
    }

    public String toLanguageKey(String string) {
        return string + "." + this.toLanguageKey();
    }

    public String toLanguageKey(String string, String string2) {
        return string + "." + this.toLanguageKey() + "." + string2;
    }

    private static String readGreedy(StringReader stringReader) {
        int n = stringReader.getCursor();
        while (stringReader.canRead() && ResourceLocation.isAllowedInResourceLocation(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(n, stringReader.getCursor());
    }

    public static ResourceLocation read(StringReader stringReader) throws CommandSyntaxException {
        int n = stringReader.getCursor();
        String string = ResourceLocation.readGreedy(stringReader);
        try {
            return ResourceLocation.parse(string);
        }
        catch (ResourceLocationException resourceLocationException) {
            stringReader.setCursor(n);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)stringReader);
        }
    }

    public static ResourceLocation readNonEmpty(StringReader stringReader) throws CommandSyntaxException {
        int n = stringReader.getCursor();
        String string = ResourceLocation.readGreedy(stringReader);
        if (string.isEmpty()) {
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)stringReader);
        }
        try {
            return ResourceLocation.parse(string);
        }
        catch (ResourceLocationException resourceLocationException) {
            stringReader.setCursor(n);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)stringReader);
        }
    }

    public static boolean isAllowedInResourceLocation(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    public static boolean isValidPath(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (ResourceLocation.validPathChar(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isValidNamespace(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (ResourceLocation.validNamespaceChar(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static String assertValidNamespace(String string, String string2) {
        if (!ResourceLocation.isValidNamespace(string)) {
            throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + string + ":" + string2);
        }
        return string;
    }

    public static boolean validPathChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.';
    }

    private static boolean validNamespaceChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    private static String assertValidPath(String string, String string2) {
        if (!ResourceLocation.isValidPath(string2)) {
            throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + string + ":" + string2);
        }
        return string2;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((ResourceLocation)object);
    }

    public static class Serializer
    implements JsonDeserializer<ResourceLocation>,
    JsonSerializer<ResourceLocation> {
        public ResourceLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return ResourceLocation.parse(GsonHelper.convertToString(jsonElement, "location"));
        }

        public JsonElement serialize(ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(resourceLocation.toString());
        }

        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((ResourceLocation)object, type, jsonSerializationContext);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

