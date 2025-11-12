/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.GsonHelper;

public class ComponentDataFixUtils {
    private static final String EMPTY_CONTENTS = ComponentDataFixUtils.createTextComponentJson("");

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> dynamicOps, String string) {
        String string2 = ComponentDataFixUtils.createTextComponentJson(string);
        return new Dynamic(dynamicOps, dynamicOps.createString(string2));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> dynamicOps) {
        return new Dynamic(dynamicOps, dynamicOps.createString(EMPTY_CONTENTS));
    }

    private static String createTextComponentJson(String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", string);
        return GsonHelper.toStableString((JsonElement)jsonObject);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> dynamicOps, String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("translate", string);
        return new Dynamic(dynamicOps, dynamicOps.createString(GsonHelper.toStableString((JsonElement)jsonObject)));
    }

    public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> dynamic) {
        return (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(string -> ComponentDataFixUtils.createPlainTextComponent(dynamic.getOps(), string)).result(), dynamic);
    }

    public static Dynamic<?> rewriteFromLenient(Dynamic<?> dynamic) {
        Optional optional = dynamic.asString().result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        String string = (String)optional.get();
        if (string.isEmpty() || string.equals("null")) {
            return ComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
        }
        char c = string.charAt(0);
        char c2 = string.charAt(string.length() - 1);
        if (c == '\"' && c2 == '\"' || c == '{' && c2 == '}' || c == '[' && c2 == ']') {
            try {
                JsonElement jsonElement = JsonParser.parseString((String)string);
                if (jsonElement.isJsonPrimitive()) {
                    return ComponentDataFixUtils.createPlainTextComponent(dynamic.getOps(), jsonElement.getAsString());
                }
                return dynamic.createString(GsonHelper.toStableString(jsonElement));
            }
            catch (JsonParseException jsonParseException) {
                // empty catch block
            }
        }
        return ComponentDataFixUtils.createPlainTextComponent(dynamic.getOps(), string);
    }

    public static Optional<String> extractTranslationString(String string) {
        try {
            JsonObject jsonObject;
            JsonElement jsonElement;
            JsonElement jsonElement2 = JsonParser.parseString((String)string);
            if (jsonElement2.isJsonObject() && (jsonElement = (jsonObject = jsonElement2.getAsJsonObject()).get("translate")) != null && jsonElement.isJsonPrimitive()) {
                return Optional.of(jsonElement.getAsString());
            }
        }
        catch (JsonParseException jsonParseException) {
            // empty catch block
        }
        return Optional.empty();
    }
}

