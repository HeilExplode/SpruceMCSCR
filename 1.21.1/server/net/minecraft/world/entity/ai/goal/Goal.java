/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.Mth;

public abstract class Goal {
    private final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

    public abstract boolean canUse();

    public boolean canContinueToUse() {
        return this.canUse();
    }

    public boolean isInterruptable() {
        return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean requiresUpdateEveryTick() {
        return false;
    }

    public void tick() {
    }

    public void setFlags(EnumSet<Flag> enumSet) {
        this.flags.clear();
        this.flags.addAll(enumSet);
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public EnumSet<Flag> getFlags() {
        return this.flags;
    }

    protected int adjustedTickDelay(int n) {
        return this.requiresUpdateEveryTick() ? n : Goal.reducedTickDelay(n);
    }

    protected static int reducedTickDelay(int n) {
        return Mth.positiveCeilDiv(n, 2);
    }

    public static enum Flag {
        MOVE,
        LOOK,
        JUMP,
        TARGET;

    }
}

