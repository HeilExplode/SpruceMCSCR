/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;

public abstract class ScalableParticleOptionsBase
implements ParticleOptions {
    public static final float MIN_SCALE = 0.01f;
    public static final float MAX_SCALE = 4.0f;
    protected static final Codec<Float> SCALE = Codec.FLOAT.validate(f -> f.floatValue() >= 0.01f && f.floatValue() <= 4.0f ? DataResult.success((Object)f) : DataResult.error(() -> "Value must be within range [0.01;4.0]: " + f));
    private final float scale;

    public ScalableParticleOptionsBase(float f) {
        this.scale = Mth.clamp(f, 0.01f, 4.0f);
    }

    public float getScale() {
        return this.scale;
    }
}

