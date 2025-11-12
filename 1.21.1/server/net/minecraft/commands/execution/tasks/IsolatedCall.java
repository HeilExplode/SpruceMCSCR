/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution.tasks;

import java.util.function.Consumer;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;

public class IsolatedCall<T extends ExecutionCommandSource<T>>
implements EntryAction<T> {
    private final Consumer<ExecutionControl<T>> taskProducer;
    private final CommandResultCallback output;

    public IsolatedCall(Consumer<ExecutionControl<T>> consumer, CommandResultCallback commandResultCallback) {
        this.taskProducer = consumer;
        this.output = commandResultCallback;
    }

    @Override
    public void execute(ExecutionContext<T> executionContext, Frame frame) {
        int n = frame.depth() + 1;
        Frame frame2 = new Frame(n, this.output, executionContext.frameControlForDepth(n));
        this.taskProducer.accept(ExecutionControl.create(executionContext, frame2));
    }
}

