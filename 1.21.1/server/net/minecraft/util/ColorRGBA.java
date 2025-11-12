/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public record ColorRGBA(int rgba) {
    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<ColorRGBA> CODEC = Codec.STRING.comapFlatMap(string -> {
        if (!string.startsWith(CUSTOM_COLOR_PREFIX)) {
            return DataResult.error(() -> "Not a color code: " + string);
        }
        try {
            int n = (int)Long.parseLong(string.substring(1), 16);
            return DataResult.success((Object)new ColorRGBA(n));
        }
        catch (NumberFormatException numberFormatException) {
            return DataResult.error(() -> "Exception parsing color code: " + numberFormatException.getMessage());
        }
    }, ColorRGBA::formatValue);

    private String formatValue() {
        return String.format(Locale.ROOT, "#%08X", this.rgba);
    }

    @Override
    public String toString() {
        return this.formatValue();
    }
}

