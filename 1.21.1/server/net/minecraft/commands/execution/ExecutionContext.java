/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.commands.execution;

import com.google.common.collect.Queues;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ExecutionContext<T>
implements AutoCloseable {
    private static final int MAX_QUEUE_DEPTH = 10000000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int commandLimit;
    private final int forkLimit;
    private final ProfilerFiller profiler;
    @Nullable
    private TraceCallbacks tracer;
    private int commandQuota;
    private boolean queueOverflow;
    private final Deque<CommandQueueEntry<T>> commandQueue = Queues.newArrayDeque();
    private final List<CommandQueueEntry<T>> newTopCommands = new ObjectArrayList();
    private int currentFrameDepth;

    public ExecutionContext(int n, int n2, ProfilerFiller profilerFiller) {
        this.commandLimit = n;
        this.forkLimit = n2;
        this.profiler = profilerFiller;
        this.commandQuota = n;
    }

    private static <T extends ExecutionCommandSource<T>> Frame createTopFrame(ExecutionContext<T> executionContext, CommandResultCallback commandResultCallback) {
        if (executionContext.currentFrameDepth == 0) {
            return new Frame(0, commandResultCallback, executionContext.commandQueue::clear);
        }
        int n = executionContext.currentFrameDepth + 1;
        return new Frame(n, commandResultCallback, executionContext.frameControlForDepth(n));
    }

    public static <T extends ExecutionCommandSource<T>> void queueInitialFunctionCall(ExecutionContext<T> executionContext, InstantiatedFunction<T> instantiatedFunction, T t, CommandResultCallback commandResultCallback) {
        executionContext.queueNext(new CommandQueueEntry<T>(ExecutionContext.createTopFrame(executionContext, commandResultCallback), new CallFunction<T>(instantiatedFunction, t.callback(), false).bind(t)));
    }

    public static <T extends ExecutionCommandSource<T>> void queueInitialCommandExecution(ExecutionContext<T> executionContext, String string, ContextChain<T> contextChain, T t, CommandResultCallback commandResultCallback) {
        executionContext.queueNext(new CommandQueueEntry<T>(ExecutionContext.createTopFrame(executionContext, commandResultCallback), new BuildContexts.TopLevel<T>(string, contextChain, t)));
    }

    private void handleQueueOverflow() {
        this.queueOverflow = true;
        this.newTopCommands.clear();
        this.commandQueue.clear();
    }

    public void queueNext(CommandQueueEntry<T> commandQueueEntry) {
        if (this.newTopCommands.size() + this.commandQueue.size() > 10000000) {
            this.handleQueueOverflow();
        }
        if (!this.queueOverflow) {
            this.newTopCommands.add(commandQueueEntry);
        }
    }

    public void discardAtDepthOrHigher(int n) {
        while (!this.commandQueue.isEmpty() && this.commandQueue.peek().frame().depth() >= n) {
            this.commandQueue.removeFirst();
        }
    }

    public Frame.FrameControl frameControlForDepth(int n) {
        return () -> this.discardAtDepthOrHigher(n);
    }

    public void runCommandQueue() {
        this.pushNewCommands();
        while (true) {
            if (this.commandQuota <= 0) {
                LOGGER.info("Command execution stopped due to limit (executed {} commands)", (Object)this.commandLimit);
                break;
            }
            CommandQueueEntry<T> commandQueueEntry = this.commandQueue.pollFirst();
            if (commandQueueEntry == null) {
                return;
            }
            this.currentFrameDepth = commandQueueEntry.frame().depth();
            commandQueueEntry.execute(this);
            if (this.queueOverflow) {
                LOGGER.error("Command execution stopped due to command queue overflow (max {})", (Object)10000000);
                break;
            }
            this.pushNewCommands();
        }
        this.currentFrameDepth = 0;
    }

    private void pushNewCommands() {
        for (int i = this.newTopCommands.size() - 1; i >= 0; --i) {
            this.commandQueue.addFirst(this.newTopCommands.get(i));
        }
        this.newTopCommands.clear();
    }

    public void tracer(@Nullable TraceCallbacks traceCallbacks) {
        this.tracer = traceCallbacks;
    }

    @Nullable
    public TraceCallbacks tracer() {
        return this.tracer;
    }

    public ProfilerFiller profiler() {
        return this.profiler;
    }

    public int forkLimit() {
        return this.forkLimit;
    }

    public void incrementCost() {
        --this.commandQuota;
    }

    @Override
    public void close() {
        if (this.tracer != null) {
            this.tracer.close();
        }
    }
}

