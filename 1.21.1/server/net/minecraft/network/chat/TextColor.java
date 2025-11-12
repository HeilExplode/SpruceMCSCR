/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;

public final class TextColor {
    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(TextColor::parseColor, TextColor::serialize);
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = (Map)Stream.of(ChatFormatting.values()).filter(ChatFormatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), chatFormatting -> new TextColor(chatFormatting.getColor(), chatFormatting.getName())));
    private static final Map<String, TextColor> NAMED_COLORS = (Map)LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap(textColor -> textColor.name, Function.identity()));
    private final int value;
    @Nullable
    private final String name;

    private TextColor(int n, String string) {
        this.value = n & 0xFFFFFF;
        this.name = string;
    }

    private TextColor(int n) {
        this.value = n & 0xFFFFFF;
        this.name = null;
    }

    public int getValue() {
        return this.value;
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    private String formatValue() {
        return String.format(Locale.ROOT, "#%06X", this.value);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        TextColor textColor = (TextColor)object;
        return this.value == textColor.value;
    }

    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    public String toString() {
        return this.serialize();
    }

    @Nullable
    public static TextColor fromLegacyFormat(ChatFormatting chatFormatting) {
        return LEGACY_FORMAT_TO_COLOR.get(chatFormatting);
    }

    public static TextColor fromRgb(int n) {
        return new TextColor(n);
    }

    public static DataResult<TextColor> parseColor(String string) {
        if (string.startsWith(CUSTOM_COLOR_PREFIX)) {
            try {
                int n = Integer.parseInt(string.substring(1), 16);
                if (n < 0 || n > 0xFFFFFF) {
                    return DataResult.error(() -> "Color value out of range: " + string);
                }
                return DataResult.success((Object)TextColor.fromRgb(n), (Lifecycle)Lifecycle.stable());
            }
            catch (NumberFormatException numberFormatException) {
                return DataResult.error(() -> "Invalid color value: " + string);
            }
        }
        TextColor textColor = NAMED_COLORS.get(string);
        if (textColor == null) {
            return DataResult.error(() -> "Invalid color name: " + string);
        }
        return DataResult.success((Object)textColor, (Lifecycle)Lifecycle.stable());
    }
}

