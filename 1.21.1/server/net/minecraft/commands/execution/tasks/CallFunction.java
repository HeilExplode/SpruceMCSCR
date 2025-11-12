/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.ContinuationTask;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T extends ExecutionCommandSource<T>>
implements UnboundEntryAction<T> {
    private final InstantiatedFunction<T> function;
    private final CommandResultCallback resultCallback;
    private final boolean returnParentFrame;

    public CallFunction(InstantiatedFunction<T> instantiatedFunction, CommandResultCallback commandResultCallback, boolean bl) {
        this.function = instantiatedFunction;
        this.resultCallback = commandResultCallback;
        this.returnParentFrame = bl;
    }

    @Override
    public void execute(T t, ExecutionContext<T> executionContext, Frame frame2) {
        executionContext.incrementCost();
        List<UnboundEntryAction<T>> list = this.function.entries();
        TraceCallbacks traceCallbacks = executionContext.tracer();
        if (traceCallbacks != null) {
            traceCallbacks.onCall(frame2.depth(), this.function.id(), this.function.entries().size());
        }
        int n = frame2.depth() + 1;
        Frame.FrameControl frameControl = this.returnParentFrame ? frame2.frameControl() : executionContext.frameControlForDepth(n);
        Frame frame3 = new Frame(n, this.resultCallback, frameControl);
        ContinuationTask.schedule(executionContext, frame3, list, (frame, unboundEntryAction) -> new CommandQueueEntry<ExecutionCommandSource>(frame, unboundEntryAction.bind(t)));
    }

    @Override
    public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
        this.execute((ExecutionCommandSource)object, executionContext, frame);
    }
}

