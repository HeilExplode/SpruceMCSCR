/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public class ContinuationTask<T, P>
implements EntryAction<T> {
    private final TaskProvider<T, P> taskFactory;
    private final List<P> arguments;
    private final CommandQueueEntry<T> selfEntry;
    private int index;

    private ContinuationTask(TaskProvider<T, P> taskProvider, List<P> list, Frame frame) {
        this.taskFactory = taskProvider;
        this.arguments = list;
        this.selfEntry = new CommandQueueEntry(frame, this);
    }

    @Override
    public void execute(ExecutionContext<T> executionContext, Frame frame) {
        P p = this.arguments.get(this.index);
        executionContext.queueNext(this.taskFactory.create(frame, p));
        if (++this.index < this.arguments.size()) {
            executionContext.queueNext(this.selfEntry);
        }
    }

    public static <T, P> void schedule(ExecutionContext<T> executionContext, Frame frame, List<P> list, TaskProvider<T, P> taskProvider) {
        int n = list.size();
        switch (n) {
            case 0: {
                break;
            }
            case 1: {
                executionContext.queueNext(taskProvider.create(frame, list.get(0)));
                break;
            }
            case 2: {
                executionContext.queueNext(taskProvider.create(frame, list.get(0)));
                executionContext.queueNext(taskProvider.create(frame, list.get(1)));
                break;
            }
            default: {
                executionContext.queueNext(new ContinuationTask<T, P>(taskProvider, list, (Frame)frame).selfEntry);
            }
        }
    }

    @FunctionalInterface
    public static interface TaskProvider<T, P> {
        public CommandQueueEntry<T> create(Frame var1, P var2);
    }
}

