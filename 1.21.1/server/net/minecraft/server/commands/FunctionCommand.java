/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;

public class FunctionCommand {
    private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.function.error.argument_not_compound", object));
    static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.function.scheduled.no_functions", object));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.function.instantiationFailure", object, object2));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (commandContext, suggestionsBuilder) -> {
        ServerFunctionManager serverFunctionManager = ((CommandSourceStack)commandContext.getSource()).getServer().getFunctions();
        SharedSuggestionProvider.suggestResource(serverFunctionManager.getTagNames(), suggestionsBuilder, "#");
        return SharedSuggestionProvider.suggestResource(serverFunctionManager.getFunctionNames(), suggestionsBuilder);
    };
    static final Callbacks<CommandSourceStack> FULL_CONTEXT_CALLBACKS = new Callbacks<CommandSourceStack>(){

        @Override
        public void signalResult(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, int n) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.result", Component.translationArg(resourceLocation), n), true);
        }
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("with");
        for (final DataCommands.DataProvider dataProvider : DataCommands.SOURCE_PROVIDERS) {
            dataProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)literalArgumentBuilder, argumentBuilder -> argumentBuilder.executes((Command)new FunctionCustomExecutor(){

                @Override
                protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return dataProvider.access(commandContext).getData();
                }
            }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((Command)new FunctionCustomExecutor(){

                @Override
                protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return FunctionCommand.getArgumentTag(NbtPathArgument.getPath(commandContext, "path"), dataProvider.access(commandContext));
                }
            })));
        }
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("function").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes((Command)new FunctionCustomExecutor(){

            @Override
            @Nullable
            protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) {
                return null;
            }
        })).then(Commands.argument("arguments", CompoundTagArgument.compoundTag()).executes((Command)new FunctionCustomExecutor(){

            @Override
            protected CompoundTag arguments(CommandContext<CommandSourceStack> commandContext) {
                return CompoundTagArgument.getCompoundTag(commandContext, "arguments");
            }
        }))).then(literalArgumentBuilder)));
    }

    static CompoundTag getArgumentTag(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
        Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return compoundTag;
        }
        throw ERROR_ARGUMENT_NOT_COMPOUND.create((Object)tag.getType().getName());
    }

    public static CommandSourceStack modifySenderForExecution(CommandSourceStack commandSourceStack) {
        return commandSourceStack.withSuppressedOutput().withMaximumPermission(2);
    }

    public static <T extends ExecutionCommandSource<T>> void queueFunctions(Collection<CommandFunction<T>> collection, @Nullable CompoundTag compoundTag, T t, T t2, ExecutionControl<T> executionControl, Callbacks<T> callbacks, ChainModifiers chainModifiers) throws CommandSyntaxException {
        if (chainModifiers.isReturn()) {
            FunctionCommand.queueFunctionsAsReturn(collection, compoundTag, t, t2, executionControl, callbacks);
        } else {
            FunctionCommand.queueFunctionsNoReturn(collection, compoundTag, t, t2, executionControl, callbacks);
        }
    }

    private static <T extends ExecutionCommandSource<T>> void instantiateAndQueueFunctions(@Nullable CompoundTag compoundTag, ExecutionControl<T> executionControl, CommandDispatcher<T> commandDispatcher, T t, CommandFunction<T> commandFunction, ResourceLocation resourceLocation, CommandResultCallback commandResultCallback, boolean bl) throws CommandSyntaxException {
        try {
            InstantiatedFunction<T> instantiatedFunction = commandFunction.instantiate(compoundTag, commandDispatcher);
            executionControl.queueNext(new CallFunction<T>(instantiatedFunction, commandResultCallback, bl).bind(t));
        }
        catch (FunctionInstantiationException functionInstantiationException) {
            throw ERROR_FUNCTION_INSTANTATION_FAILURE.create((Object)resourceLocation, (Object)functionInstantiationException.messageComponent());
        }
    }

    private static <T extends ExecutionCommandSource<T>> CommandResultCallback decorateOutputIfNeeded(T t, Callbacks<T> callbacks, ResourceLocation resourceLocation, CommandResultCallback commandResultCallback) {
        if (t.isSilent()) {
            return commandResultCallback;
        }
        return (bl, n) -> {
            callbacks.signalResult(t, resourceLocation, n);
            commandResultCallback.onResult(bl, n);
        };
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsAsReturn(Collection<CommandFunction<T>> collection, @Nullable CompoundTag compoundTag, T t, T t2, ExecutionControl<T> executionControl, Callbacks<T> callbacks) throws CommandSyntaxException {
        CommandDispatcher<T> commandDispatcher = t.dispatcher();
        T t3 = t2.clearCallbacks();
        CommandResultCallback commandResultCallback = CommandResultCallback.chain(t.callback(), executionControl.currentFrame().returnValueConsumer());
        for (CommandFunction<T> commandFunction : collection) {
            ResourceLocation resourceLocation = commandFunction.id();
            CommandResultCallback commandResultCallback2 = FunctionCommand.decorateOutputIfNeeded(t, callbacks, resourceLocation, commandResultCallback);
            FunctionCommand.instantiateAndQueueFunctions(compoundTag, executionControl, commandDispatcher, t3, commandFunction, resourceLocation, commandResultCallback2, true);
        }
        executionControl.queueNext(FallthroughTask.instance());
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsNoReturn(Collection<CommandFunction<T>> collection, @Nullable CompoundTag compoundTag, T t, T t2, ExecutionControl<T> executionControl, Callbacks<T> callbacks) throws CommandSyntaxException {
        CommandDispatcher<T> commandDispatcher = t.dispatcher();
        T t3 = t2.clearCallbacks();
        CommandResultCallback commandResultCallback = t.callback();
        if (collection.isEmpty()) {
            return;
        }
        if (collection.size() == 1) {
            CommandFunction<T> commandFunction = collection.iterator().next();
            ResourceLocation resourceLocation = commandFunction.id();
            CommandResultCallback commandResultCallback2 = FunctionCommand.decorateOutputIfNeeded(t, callbacks, resourceLocation, commandResultCallback);
            FunctionCommand.instantiateAndQueueFunctions(compoundTag, executionControl, commandDispatcher, t3, commandFunction, resourceLocation, commandResultCallback2, false);
        } else if (commandResultCallback == CommandResultCallback.EMPTY) {
            for (CommandFunction<T> commandFunction : collection) {
                ResourceLocation resourceLocation = commandFunction.id();
                CommandResultCallback commandResultCallback3 = FunctionCommand.decorateOutputIfNeeded(t, callbacks, resourceLocation, commandResultCallback);
                FunctionCommand.instantiateAndQueueFunctions(compoundTag, executionControl, commandDispatcher, t3, commandFunction, resourceLocation, commandResultCallback3, false);
            }
        } else {
            class Accumulator {
                boolean anyResult;
                int sum;

                Accumulator() {
                }

                public void add(int n) {
                    this.anyResult = true;
                    this.sum += n;
                }
            }
            Accumulator accumulator = new Accumulator();
            CommandResultCallback commandResultCallback4 = (bl, n) -> accumulator.add(n);
            for (CommandFunction<T> commandFunction : collection) {
                ResourceLocation resourceLocation = commandFunction.id();
                CommandResultCallback commandResultCallback5 = FunctionCommand.decorateOutputIfNeeded(t, callbacks, resourceLocation, commandResultCallback4);
                FunctionCommand.instantiateAndQueueFunctions(compoundTag, executionControl, commandDispatcher, t3, commandFunction, resourceLocation, commandResultCallback5, false);
            }
            executionControl.queueNext((executionContext, frame) -> {
                if (accumulator.anyResult) {
                    commandResultCallback.onSuccess(accumulator.sum);
                }
            });
        }
    }

    public static interface Callbacks<T> {
        public void signalResult(T var1, ResourceLocation var2, int var3);
    }

    static abstract class FunctionCustomExecutor
    extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
    implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
        FunctionCustomExecutor() {
        }

        @Nullable
        protected abstract CompoundTag arguments(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        @Override
        public void runGuarded(CommandSourceStack commandSourceStack, ContextChain<CommandSourceStack> contextChain, ChainModifiers chainModifiers, ExecutionControl<CommandSourceStack> executionControl) throws CommandSyntaxException {
            CommandContext commandContext = contextChain.getTopContext().copyFor((Object)commandSourceStack);
            Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> pair = FunctionArgument.getFunctionCollection((CommandContext<CommandSourceStack>)commandContext, "name");
            Collection collection = (Collection)pair.getSecond();
            if (collection.isEmpty()) {
                throw ERROR_NO_FUNCTIONS.create((Object)Component.translationArg((ResourceLocation)pair.getFirst()));
            }
            CompoundTag compoundTag = this.arguments((CommandContext<CommandSourceStack>)commandContext);
            CommandSourceStack commandSourceStack2 = FunctionCommand.modifySenderForExecution(commandSourceStack);
            if (collection.size() == 1) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.scheduled.single", Component.translationArg(((CommandFunction)collection.iterator().next()).id())), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.scheduled.multiple", ComponentUtils.formatList(collection.stream().map(CommandFunction::id).toList(), Component::translationArg)), true);
            }
            FunctionCommand.queueFunctions(collection, compoundTag, commandSourceStack, commandSourceStack2, executionControl, FULL_CONTEXT_CALLBACKS, chainModifiers);
        }

        @Override
        public /* synthetic */ void runGuarded(ExecutionCommandSource executionCommandSource, ContextChain contextChain, ChainModifiers chainModifiers, ExecutionControl executionControl) throws CommandSyntaxException {
            this.runGuarded((CommandSourceStack)executionCommandSource, (ContextChain<CommandSourceStack>)contextChain, chainModifiers, (ExecutionControl<CommandSourceStack>)executionControl);
        }
    }
}

