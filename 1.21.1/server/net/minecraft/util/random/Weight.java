/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  org.slf4j.Logger
 */
package net.minecraft.util.random;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.slf4j.Logger;

public class Weight {
    public static final Codec<Weight> CODEC = Codec.INT.xmap(Weight::of, Weight::asInt);
    private static final Weight ONE = new Weight(1);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int value;

    private Weight(int n) {
        this.value = n;
    }

    public static Weight of(int n) {
        if (n == 1) {
            return ONE;
        }
        Weight.validateWeight(n);
        return new Weight(n);
    }

    public int asInt() {
        return this.value;
    }

    private static void validateWeight(int n) {
        if (n < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
        }
        if (n == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.warn("Found 0 weight, make sure this is intentional!");
        }
    }

    public String toString() {
        return Integer.toString(this.value);
    }

    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof Weight && this.value == ((Weight)object).value;
    }
}

