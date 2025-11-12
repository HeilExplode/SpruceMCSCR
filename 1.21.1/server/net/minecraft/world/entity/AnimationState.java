/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.util.Mth;

public class AnimationState {
    private static final long STOPPED = Long.MAX_VALUE;
    private long lastTime = Long.MAX_VALUE;
    private long accumulatedTime;

    public void start(int n) {
        this.lastTime = (long)n * 1000L / 20L;
        this.accumulatedTime = 0L;
    }

    public void startIfStopped(int n) {
        if (!this.isStarted()) {
            this.start(n);
        }
    }

    public void animateWhen(boolean bl, int n) {
        if (bl) {
            this.startIfStopped(n);
        } else {
            this.stop();
        }
    }

    public void stop() {
        this.lastTime = Long.MAX_VALUE;
    }

    public void ifStarted(Consumer<AnimationState> consumer) {
        if (this.isStarted()) {
            consumer.accept(this);
        }
    }

    public void updateTime(float f, float f2) {
        if (!this.isStarted()) {
            return;
        }
        long l = Mth.lfloor(f * 1000.0f / 20.0f);
        this.accumulatedTime += (long)((float)(l - this.lastTime) * f2);
        this.lastTime = l;
    }

    public void fastForward(int n, float f) {
        if (!this.isStarted()) {
            return;
        }
        this.accumulatedTime += (long)((float)(n * 1000) * f) / 20L;
    }

    public long getAccumulatedTime() {
        return this.accumulatedTime;
    }

    public boolean isStarted() {
        return this.lastTime != Long.MAX_VALUE;
    }
}

