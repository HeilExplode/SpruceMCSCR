/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.item.ItemStack;

public class Crackiness {
    public static final Crackiness GOLEM = new Crackiness(0.75f, 0.5f, 0.25f);
    public static final Crackiness WOLF_ARMOR = new Crackiness(0.95f, 0.69f, 0.32f);
    private final float fractionLow;
    private final float fractionMedium;
    private final float fractionHigh;

    private Crackiness(float f, float f2, float f3) {
        this.fractionLow = f;
        this.fractionMedium = f2;
        this.fractionHigh = f3;
    }

    public Level byFraction(float f) {
        if (f < this.fractionHigh) {
            return Level.HIGH;
        }
        if (f < this.fractionMedium) {
            return Level.MEDIUM;
        }
        if (f < this.fractionLow) {
            return Level.LOW;
        }
        return Level.NONE;
    }

    public Level byDamage(ItemStack itemStack) {
        if (!itemStack.isDamageableItem()) {
            return Level.NONE;
        }
        return this.byDamage(itemStack.getDamageValue(), itemStack.getMaxDamage());
    }

    public Level byDamage(int n, int n2) {
        return this.byFraction((float)(n2 - n) / (float)n2);
    }

    public static enum Level {
        NONE,
        LOW,
        MEDIUM,
        HIGH;

    }
}

