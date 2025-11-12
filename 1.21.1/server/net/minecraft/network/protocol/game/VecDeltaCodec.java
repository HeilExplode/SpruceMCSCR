/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.network.protocol.game;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.VisibleForTesting;

public class VecDeltaCodec {
    private static final double TRUNCATION_STEPS = 4096.0;
    private Vec3 base = Vec3.ZERO;

    @VisibleForTesting
    static long encode(double d) {
        return Math.round(d * 4096.0);
    }

    @VisibleForTesting
    static double decode(long l) {
        return (double)l / 4096.0;
    }

    public Vec3 decode(long l, long l2, long l3) {
        if (l == 0L && l2 == 0L && l3 == 0L) {
            return this.base;
        }
        double d = l == 0L ? this.base.x : VecDeltaCodec.decode(VecDeltaCodec.encode(this.base.x) + l);
        double d2 = l2 == 0L ? this.base.y : VecDeltaCodec.decode(VecDeltaCodec.encode(this.base.y) + l2);
        double d3 = l3 == 0L ? this.base.z : VecDeltaCodec.decode(VecDeltaCodec.encode(this.base.z) + l3);
        return new Vec3(d, d2, d3);
    }

    public long encodeX(Vec3 vec3) {
        return VecDeltaCodec.encode(vec3.x) - VecDeltaCodec.encode(this.base.x);
    }

    public long encodeY(Vec3 vec3) {
        return VecDeltaCodec.encode(vec3.y) - VecDeltaCodec.encode(this.base.y);
    }

    public long encodeZ(Vec3 vec3) {
        return VecDeltaCodec.encode(vec3.z) - VecDeltaCodec.encode(this.base.z);
    }

    public Vec3 delta(Vec3 vec3) {
        return vec3.subtract(this.base);
    }

    public void setBase(Vec3 vec3) {
        this.base = vec3;
    }

    public Vec3 getBase() {
        return this.base;
    }
}

