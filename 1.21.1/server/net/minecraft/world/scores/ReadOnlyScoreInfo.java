/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ReadOnlyScoreInfo {
    public int value();

    public boolean isLocked();

    @Nullable
    public NumberFormat numberFormat();

    default public MutableComponent formatValue(NumberFormat numberFormat) {
        return Objects.requireNonNullElse(this.numberFormat(), numberFormat).format(this.value());
    }

    public static MutableComponent safeFormatValue(@Nullable ReadOnlyScoreInfo readOnlyScoreInfo, NumberFormat numberFormat) {
        return readOnlyScoreInfo != null ? readOnlyScoreInfo.formatValue(numberFormat) : numberFormat.format(0);
    }
}

