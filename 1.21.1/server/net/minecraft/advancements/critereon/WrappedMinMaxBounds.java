/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public record WrappedMinMaxBounds(@Nullable Float min, @Nullable Float max) {
    public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds(null, null);
    public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.ints"));

    public static WrappedMinMaxBounds exactly(float f) {
        return new WrappedMinMaxBounds(Float.valueOf(f), Float.valueOf(f));
    }

    public static WrappedMinMaxBounds between(float f, float f2) {
        return new WrappedMinMaxBounds(Float.valueOf(f), Float.valueOf(f2));
    }

    public static WrappedMinMaxBounds atLeast(float f) {
        return new WrappedMinMaxBounds(Float.valueOf(f), null);
    }

    public static WrappedMinMaxBounds atMost(float f) {
        return new WrappedMinMaxBounds(null, Float.valueOf(f));
    }

    public boolean matches(float f) {
        if (this.min != null && this.max != null && this.min.floatValue() > this.max.floatValue() && this.min.floatValue() > f && this.max.floatValue() < f) {
            return false;
        }
        if (this.min != null && this.min.floatValue() > f) {
            return false;
        }
        return this.max == null || !(this.max.floatValue() < f);
    }

    public boolean matchesSqr(double d) {
        if (this.min != null && this.max != null && this.min.floatValue() > this.max.floatValue() && (double)(this.min.floatValue() * this.min.floatValue()) > d && (double)(this.max.floatValue() * this.max.floatValue()) < d) {
            return false;
        }
        if (this.min != null && (double)(this.min.floatValue() * this.min.floatValue()) > d) {
            return false;
        }
        return this.max == null || !((double)(this.max.floatValue() * this.max.floatValue()) < d);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        if (this.min != null && this.max != null && this.min.equals(this.max)) {
            return new JsonPrimitive((Number)this.min);
        }
        JsonObject jsonObject = new JsonObject();
        if (this.min != null) {
            jsonObject.addProperty("min", (Number)this.min);
        }
        if (this.max != null) {
            jsonObject.addProperty("max", (Number)this.min);
        }
        return jsonObject;
    }

    public static WrappedMinMaxBounds fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        if (GsonHelper.isNumberValue(jsonElement)) {
            float f = GsonHelper.convertToFloat(jsonElement, "value");
            return new WrappedMinMaxBounds(Float.valueOf(f), Float.valueOf(f));
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
        Float f = jsonObject.has("min") ? Float.valueOf(GsonHelper.getAsFloat(jsonObject, "min")) : null;
        Float f2 = jsonObject.has("max") ? Float.valueOf(GsonHelper.getAsFloat(jsonObject, "max")) : null;
        return new WrappedMinMaxBounds(f, f2);
    }

    public static WrappedMinMaxBounds fromReader(StringReader stringReader, boolean bl) throws CommandSyntaxException {
        return WrappedMinMaxBounds.fromReader(stringReader, bl, f -> f);
    }

    public static WrappedMinMaxBounds fromReader(StringReader stringReader, boolean bl, Function<Float, Float> function) throws CommandSyntaxException {
        Float f;
        if (!stringReader.canRead()) {
            throw MinMaxBounds.ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
        }
        int n = stringReader.getCursor();
        Float f2 = WrappedMinMaxBounds.optionallyFormat(WrappedMinMaxBounds.readNumber(stringReader, bl), function);
        if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
            stringReader.skip();
            stringReader.skip();
            f = WrappedMinMaxBounds.optionallyFormat(WrappedMinMaxBounds.readNumber(stringReader, bl), function);
            if (f2 == null && f == null) {
                stringReader.setCursor(n);
                throw MinMaxBounds.ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
            }
        } else {
            if (!bl && stringReader.canRead() && stringReader.peek() == '.') {
                stringReader.setCursor(n);
                throw ERROR_INTS_ONLY.createWithContext((ImmutableStringReader)stringReader);
            }
            f = f2;
        }
        if (f2 == null && f == null) {
            stringReader.setCursor(n);
            throw MinMaxBounds.ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
        }
        return new WrappedMinMaxBounds(f2, f);
    }

    @Nullable
    private static Float readNumber(StringReader stringReader, boolean bl) throws CommandSyntaxException {
        int n = stringReader.getCursor();
        while (stringReader.canRead() && WrappedMinMaxBounds.isAllowedNumber(stringReader, bl)) {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(n, stringReader.getCursor());
        if (string.isEmpty()) {
            return null;
        }
        try {
            return Float.valueOf(Float.parseFloat(string));
        }
        catch (NumberFormatException numberFormatException) {
            if (bl) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext((ImmutableStringReader)stringReader, (Object)string);
            }
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
    }

    private static boolean isAllowedNumber(StringReader stringReader, boolean bl) {
        char c = stringReader.peek();
        if (c >= '0' && c <= '9' || c == '-') {
            return true;
        }
        if (bl && c == '.') {
            return !stringReader.canRead(2) || stringReader.peek(1) != '.';
        }
        return false;
    }

    @Nullable
    private static Float optionallyFormat(@Nullable Float f, Function<Float, Float> function) {
        return f == null ? null : function.apply(f);
    }
}

