/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.particles;

public class ParticleGroup {
    private final int limit;
    public static final ParticleGroup SPORE_BLOSSOM = new ParticleGroup(1000);

    public ParticleGroup(int n) {
        this.limit = n;
    }

    public int getLimit() {
        return this.limit;
    }
}

