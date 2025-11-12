/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.network;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FilterMask;

public record FilteredText(String raw, FilterMask mask) {
    public static final FilteredText EMPTY = FilteredText.passThrough("");

    public static FilteredText passThrough(String string) {
        return new FilteredText(string, FilterMask.PASS_THROUGH);
    }

    public static FilteredText fullyFiltered(String string) {
        return new FilteredText(string, FilterMask.FULLY_FILTERED);
    }

    @Nullable
    public String filtered() {
        return this.mask.apply(this.raw);
    }

    public String filteredOrEmpty() {
        return Objects.requireNonNullElse(this.filtered(), "");
    }

    public boolean isFiltered() {
        return !this.mask.isEmpty();
    }
}

