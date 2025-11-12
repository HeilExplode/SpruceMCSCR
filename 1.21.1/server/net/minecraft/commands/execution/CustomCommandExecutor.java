/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.TraceCallbacks;

public interface CustomCommandExecutor<T> {
    public void run(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4);

    public static abstract class WithErrorHandling<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor<T> {
        @Override
        public final void run(T t, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
            try {
                this.runGuarded(t, contextChain, chainModifiers, executionControl);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                this.onError(commandSyntaxException, t, chainModifiers, executionControl.tracer());
                t.callback().onFailure();
            }
        }

        protected void onError(CommandSyntaxException commandSyntaxException, T t, ChainModifiers chainModifiers, @Nullable TraceCallbacks traceCallbacks) {
            t.handleError(commandSyntaxException, chainModifiers.isForked(), traceCallbacks);
        }

        protected abstract void runGuarded(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4) throws CommandSyntaxException;
    }

    public static interface CommandAdapter<T>
    extends Command<T>,
    CustomCommandExecutor<T> {
        default public int run(CommandContext<T> commandContext) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}

