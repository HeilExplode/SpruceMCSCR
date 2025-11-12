/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import net.minecraft.util.thread.BlockableEventLoop;

public abstract class ReentrantBlockableEventLoop<R extends Runnable>
extends BlockableEventLoop<R> {
    private int reentrantCount;

    public ReentrantBlockableEventLoop(String string) {
        super(string);
    }

    @Override
    public boolean scheduleExecutables() {
        return this.runningTask() || super.scheduleExecutables();
    }

    protected boolean runningTask() {
        return this.reentrantCount != 0;
    }

    @Override
    public void doRunTask(R r) {
        ++this.reentrantCount;
        try {
            super.doRunTask(r);
        }
        finally {
            --this.reentrantCount;
        }
    }
}

