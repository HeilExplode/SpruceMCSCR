/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.commands.execution;

import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionControl<T> {
    public void queueNext(EntryAction<T> var1);

    public void tracer(@Nullable TraceCallbacks var1);

    @Nullable
    public TraceCallbacks tracer();

    public Frame currentFrame();

    public static <T extends ExecutionCommandSource<T>> ExecutionControl<T> create(final ExecutionContext<T> executionContext, final Frame frame) {
        return new ExecutionControl<T>(){

            @Override
            public void queueNext(EntryAction<T> entryAction) {
                executionContext.queueNext(new CommandQueueEntry(frame, entryAction));
            }

            @Override
            public void tracer(@Nullable TraceCallbacks traceCallbacks) {
                executionContext.tracer(traceCallbacks);
            }

            @Override
            @Nullable
            public TraceCallbacks tracer() {
                return executionContext.tracer();
            }

            @Override
            public Frame currentFrame() {
                return frame;
            }
        };
    }
}

