/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution;

import net.minecraft.commands.CommandResultCallback;

public record Frame(int depth, CommandResultCallback returnValueConsumer, FrameControl frameControl) {
    public void returnSuccess(int n) {
        this.returnValueConsumer.onSuccess(n);
    }

    public void returnFailure() {
        this.returnValueConsumer.onFailure();
    }

    public void discard() {
        this.frameControl.discard();
    }

    @FunctionalInterface
    public static interface FrameControl {
        public void discard();
    }
}

