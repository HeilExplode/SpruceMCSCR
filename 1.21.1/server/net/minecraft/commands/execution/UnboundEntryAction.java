/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution;

import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

@FunctionalInterface
public interface UnboundEntryAction<T> {
    public void execute(T var1, ExecutionContext<T> var2, Frame var3);

    default public EntryAction<T> bind(T t) {
        return (executionContext, frame) -> this.execute(t, executionContext, frame);
    }
}

