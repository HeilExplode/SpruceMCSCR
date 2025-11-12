/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.valueproviders;

import java.util.Arrays;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.SampledFloat;

public class MultipliedFloats
implements SampledFloat {
    private final SampledFloat[] values;

    public MultipliedFloats(SampledFloat ... sampledFloatArray) {
        this.values = sampledFloatArray;
    }

    @Override
    public float sample(RandomSource randomSource) {
        float f = 1.0f;
        for (SampledFloat sampledFloat : this.values) {
            f *= sampledFloat.sample(randomSource);
        }
        return f;
    }

    public String toString() {
        return "MultipliedFloats" + Arrays.toString(this.values);
    }
}

