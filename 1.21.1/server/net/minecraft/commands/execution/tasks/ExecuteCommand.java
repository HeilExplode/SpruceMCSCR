/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;

public class ExecuteCommand<T extends ExecutionCommandSource<T>>
implements UnboundEntryAction<T> {
    private final String commandInput;
    private final ChainModifiers modifiers;
    private final CommandContext<T> executionContext;

    public ExecuteCommand(String string, ChainModifiers chainModifiers, CommandContext<T> commandContext) {
        this.commandInput = string;
        this.modifiers = chainModifiers;
        this.executionContext = commandContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute(T t, ExecutionContext<T> executionContext, Frame frame) {
        executionContext.profiler().push(() -> "execute " + this.commandInput);
        try {
            executionContext.incrementCost();
            int n = ContextChain.runExecutable(this.executionContext, t, ExecutionCommandSource.resultConsumer(), (boolean)this.modifiers.isForked());
            TraceCallbacks traceCallbacks = executionContext.tracer();
            if (traceCallbacks != null) {
                traceCallbacks.onReturn(frame.depth(), this.commandInput, n);
            }
        }
        catch (CommandSyntaxException commandSyntaxException) {
            t.handleError(commandSyntaxException, this.modifiers.isForked(), executionContext.tracer());
        }
        finally {
            executionContext.profiler().pop();
        }
    }

    @Override
    public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
        this.execute((ExecutionCommandSource)object, executionContext, frame);
    }
}

