/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.context.ContextChain$Stage
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.ContinuationTask;
import net.minecraft.commands.execution.tasks.ExecuteCommand;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.network.chat.Component;

public class BuildContexts<T extends ExecutionCommandSource<T>> {
    @VisibleForTesting
    public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType(object -> Component.translatableEscape("command.forkLimit", object));
    private final String commandInput;
    private final ContextChain<T> command;

    public BuildContexts(String string, ContextChain<T> contextChain) {
        this.commandInput = string;
        this.command = contextChain;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void execute(T t, List<T> list, ExecutionContext<T> executionContext, Frame frame2, ChainModifiers chainModifiers) {
        ExecuteCommand<ExecutionCommandSource> executeCommand;
        Command command;
        ContextChain contextChain = this.command;
        ChainModifiers chainModifiers2 = chainModifiers;
        List<Object> list2 = list;
        if (contextChain.getStage() != ContextChain.Stage.EXECUTE) {
            executionContext.profiler().push(() -> "prepare " + this.commandInput);
            try {
                int n2 = executionContext.forkLimit();
                while (contextChain.getStage() != ContextChain.Stage.EXECUTE) {
                    command = contextChain.getTopContext();
                    if (command.isForked()) {
                        chainModifiers2 = chainModifiers2.setForked();
                    }
                    if ((executeCommand = command.getRedirectModifier()) instanceof CustomModifierExecutor) {
                        CustomModifierExecutor customModifierExecutor = (CustomModifierExecutor)((Object)executeCommand);
                        customModifierExecutor.apply(t, list2, contextChain, chainModifiers2, ExecutionControl.create(executionContext, frame2));
                        return;
                    }
                    if (executeCommand != null) {
                        executionContext.incrementCost();
                        boolean bl2 = chainModifiers2.isForked();
                        ObjectArrayList objectArrayList = new ObjectArrayList();
                        for (ExecutionCommandSource executionCommandSource2 : list2) {
                            Collection collection;
                            block21: {
                                try {
                                    collection = ContextChain.runModifier((CommandContext)command, (Object)executionCommandSource2, (commandContext, bl, n) -> {}, (boolean)bl2);
                                    if (objectArrayList.size() + collection.size() < n2) break block21;
                                    t.handleError(ERROR_FORK_LIMIT_REACHED.create((Object)n2), bl2, executionContext.tracer());
                                    return;
                                }
                                catch (CommandSyntaxException commandSyntaxException) {
                                    executionCommandSource2.handleError(commandSyntaxException, bl2, executionContext.tracer());
                                    if (bl2) continue;
                                    executionContext.profiler().pop();
                                    return;
                                }
                            }
                            objectArrayList.addAll(collection);
                        }
                        list2 = objectArrayList;
                    }
                    contextChain = contextChain.nextStage();
                }
            }
            finally {
                executionContext.profiler().pop();
            }
        }
        if (list2.isEmpty()) {
            if (chainModifiers2.isReturn()) {
                executionContext.queueNext(new CommandQueueEntry(frame2, FallthroughTask.instance()));
            }
            return;
        }
        CommandContext commandContext2 = contextChain.getTopContext();
        command = commandContext2.getCommand();
        if (command instanceof CustomCommandExecutor) {
            executeCommand = (CustomCommandExecutor)command;
            ExecutionControl executionControl = ExecutionControl.create(executionContext, frame2);
            for (Object object : list2) {
                executeCommand.run((ExecutionCommandSource)object, (ContextChain<ExecutionCommandSource>)contextChain, chainModifiers2, executionControl);
            }
        } else {
            if (chainModifiers2.isReturn()) {
                executeCommand = (ExecutionCommandSource)list2.get(0);
                executeCommand = executeCommand.withCallback(CommandResultCallback.chain(executeCommand.callback(), frame2.returnValueConsumer()));
                list2 = List.of(executeCommand);
            }
            executeCommand = new ExecuteCommand(this.commandInput, chainModifiers2, commandContext2);
            ContinuationTask.schedule(executionContext, frame2, list2, (frame, executionCommandSource) -> new CommandQueueEntry<ExecutionCommandSource>(frame, executeCommand.bind((ExecutionCommandSource)executionCommandSource)));
        }
    }

    protected void traceCommandStart(ExecutionContext<T> executionContext, Frame frame) {
        TraceCallbacks traceCallbacks = executionContext.tracer();
        if (traceCallbacks != null) {
            traceCallbacks.onCommand(frame.depth(), this.commandInput);
        }
    }

    public String toString() {
        return this.commandInput;
    }

    public static class TopLevel<T extends ExecutionCommandSource<T>>
    extends BuildContexts<T>
    implements EntryAction<T> {
        private final T source;

        public TopLevel(String string, ContextChain<T> contextChain, T t) {
            super(string, contextChain);
            this.source = t;
        }

        @Override
        public void execute(ExecutionContext<T> executionContext, Frame frame) {
            this.traceCommandStart(executionContext, frame);
            this.execute(this.source, List.of(this.source), executionContext, frame, ChainModifiers.DEFAULT);
        }
    }

    public static class Continuation<T extends ExecutionCommandSource<T>>
    extends BuildContexts<T>
    implements EntryAction<T> {
        private final ChainModifiers modifiers;
        private final T originalSource;
        private final List<T> sources;

        public Continuation(String string, ContextChain<T> contextChain, ChainModifiers chainModifiers, T t, List<T> list) {
            super(string, contextChain);
            this.originalSource = t;
            this.sources = list;
            this.modifiers = chainModifiers;
        }

        @Override
        public void execute(ExecutionContext<T> executionContext, Frame frame) {
            this.execute(this.originalSource, this.sources, executionContext, frame, this.modifiers);
        }
    }

    public static class Unbound<T extends ExecutionCommandSource<T>>
    extends BuildContexts<T>
    implements UnboundEntryAction<T> {
        public Unbound(String string, ContextChain<T> contextChain) {
            super(string, contextChain);
        }

        @Override
        public void execute(T t, ExecutionContext<T> executionContext, Frame frame) {
            this.traceCommandStart(executionContext, frame);
            this.execute(t, List.of(t), executionContext, frame, ChainModifiers.DEFAULT);
        }

        @Override
        public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
            this.execute((ExecutionCommandSource)object, executionContext, frame);
        }
    }
}

