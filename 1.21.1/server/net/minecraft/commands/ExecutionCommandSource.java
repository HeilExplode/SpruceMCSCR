/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.ResultConsumer
 *  com.mojang.brigadier.exceptions.CommandExceptionType
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
    public boolean hasPermission(int var1);

    public T withCallback(CommandResultCallback var1);

    public CommandResultCallback callback();

    default public T clearCallbacks() {
        return this.withCallback(CommandResultCallback.EMPTY);
    }

    public CommandDispatcher<T> dispatcher();

    public void handleError(CommandExceptionType var1, Message var2, boolean var3, @Nullable TraceCallbacks var4);

    public boolean isSilent();

    default public void handleError(CommandSyntaxException commandSyntaxException, boolean bl, @Nullable TraceCallbacks traceCallbacks) {
        this.handleError(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), bl, traceCallbacks);
    }

    public static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
        return (commandContext, bl, n) -> ((ExecutionCommandSource)commandContext.getSource()).callback().onResult(bl, n);
    }
}

