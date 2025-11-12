/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class DebugCommand {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.alreadyRunning"));
    static final SimpleCommandExceptionType NO_RECURSIVE_TRACES = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.function.noRecursion"));
    static final SimpleCommandExceptionType NO_RETURN_RUN = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.function.noReturnRun"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debug").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.literal("start").executes(commandContext -> DebugCommand.start((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("stop").executes(commandContext -> DebugCommand.stop((CommandSourceStack)commandContext.getSource())))).then(((LiteralArgumentBuilder)Commands.literal("function").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).executes((Command)new TraceCustomExecutor()))));
    }

    private static int start(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer.isTimeProfilerRunning()) {
            throw ERROR_ALREADY_RUNNING.create();
        }
        minecraftServer.startTimeProfiler();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.debug.started"), true);
        return 0;
    }

    private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (!minecraftServer.isTimeProfilerRunning()) {
            throw ERROR_NOT_RUNNING.create();
        }
        ProfileResults profileResults = minecraftServer.stopTimeProfiler();
        double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
        double d2 = (double)profileResults.getTickDuration() / d;
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), profileResults.getTickDuration(), String.format(Locale.ROOT, "%.2f", d2)), true);
        return (int)d2;
    }

    static class TraceCustomExecutor
    extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
    implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
        TraceCustomExecutor() {
        }

        @Override
        public void runGuarded(CommandSourceStack commandSourceStack, ContextChain<CommandSourceStack> contextChain, ChainModifiers chainModifiers, ExecutionControl<CommandSourceStack> executionControl) throws CommandSyntaxException {
            if (chainModifiers.isReturn()) {
                throw NO_RETURN_RUN.create();
            }
            if (executionControl.tracer() != null) {
                throw NO_RECURSIVE_TRACES.create();
            }
            CommandContext commandContext = contextChain.getTopContext();
            Collection<CommandFunction<CommandSourceStack>> collection = FunctionArgument.getFunctions((CommandContext<CommandSourceStack>)commandContext, "name");
            MinecraftServer minecraftServer = commandSourceStack.getServer();
            String string = "debug-trace-" + Util.getFilenameFormattedDateTime() + ".txt";
            CommandDispatcher<CommandSourceStack> commandDispatcher = commandSourceStack.getServer().getFunctions().getDispatcher();
            int n = 0;
            try {
                Path path = minecraftServer.getFile("debug");
                Files.createDirectories(path, new FileAttribute[0]);
                final PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(path.resolve(string), StandardCharsets.UTF_8, new OpenOption[0]));
                Tracer tracer = new Tracer(printWriter);
                executionControl.tracer(tracer);
                for (final CommandFunction<CommandSourceStack> commandFunction : collection) {
                    try {
                        CommandSourceStack commandSourceStack2 = commandSourceStack.withSource(tracer).withMaximumPermission(2);
                        InstantiatedFunction<CommandSourceStack> instantiatedFunction = commandFunction.instantiate(null, commandDispatcher);
                        executionControl.queueNext(new CallFunction<CommandSourceStack>(this, instantiatedFunction, CommandResultCallback.EMPTY, false){

                            @Override
                            public void execute(CommandSourceStack commandSourceStack, ExecutionContext<CommandSourceStack> executionContext, Frame frame) {
                                printWriter.println(commandFunction.id());
                                super.execute(commandSourceStack, executionContext, frame);
                            }

                            @Override
                            public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
                                this.execute((CommandSourceStack)object, (ExecutionContext<CommandSourceStack>)executionContext, frame);
                            }
                        }.bind(commandSourceStack2));
                        n += instantiatedFunction.entries().size();
                    }
                    catch (FunctionInstantiationException functionInstantiationException) {
                        commandSourceStack.sendFailure(functionInstantiationException.messageComponent());
                    }
                }
            }
            catch (IOException | UncheckedIOException exception) {
                LOGGER.warn("Tracing failed", (Throwable)exception);
                commandSourceStack.sendFailure(Component.translatable("commands.debug.function.traceFailed"));
            }
            int n2 = n;
            executionControl.queueNext((executionContext, frame) -> {
                if (collection.size() == 1) {
                    commandSourceStack.sendSuccess(() -> Component.translatable("commands.debug.function.success.single", n2, Component.translationArg(((CommandFunction)collection.iterator().next()).id()), string), true);
                } else {
                    commandSourceStack.sendSuccess(() -> Component.translatable("commands.debug.function.success.multiple", n2, collection.size(), string), true);
                }
            });
        }

        @Override
        public /* synthetic */ void runGuarded(ExecutionCommandSource executionCommandSource, ContextChain contextChain, ChainModifiers chainModifiers, ExecutionControl executionControl) throws CommandSyntaxException {
            this.runGuarded((CommandSourceStack)executionCommandSource, (ContextChain<CommandSourceStack>)contextChain, chainModifiers, (ExecutionControl<CommandSourceStack>)executionControl);
        }
    }

    static class Tracer
    implements CommandSource,
    TraceCallbacks {
        public static final int INDENT_OFFSET = 1;
        private final PrintWriter output;
        private int lastIndent;
        private boolean waitingForResult;

        Tracer(PrintWriter printWriter) {
            this.output = printWriter;
        }

        private void indentAndSave(int n) {
            this.printIndent(n);
            this.lastIndent = n;
        }

        private void printIndent(int n) {
            for (int i = 0; i < n + 1; ++i) {
                this.output.write("    ");
            }
        }

        private void newLine() {
            if (this.waitingForResult) {
                this.output.println();
                this.waitingForResult = false;
            }
        }

        @Override
        public void onCommand(int n, String string) {
            this.newLine();
            this.indentAndSave(n);
            this.output.print("[C] ");
            this.output.print(string);
            this.waitingForResult = true;
        }

        @Override
        public void onReturn(int n, String string, int n2) {
            if (this.waitingForResult) {
                this.output.print(" -> ");
                this.output.println(n2);
                this.waitingForResult = false;
            } else {
                this.indentAndSave(n);
                this.output.print("[R = ");
                this.output.print(n2);
                this.output.print("] ");
                this.output.println(string);
            }
        }

        @Override
        public void onCall(int n, ResourceLocation resourceLocation, int n2) {
            this.newLine();
            this.indentAndSave(n);
            this.output.print("[F] ");
            this.output.print(resourceLocation);
            this.output.print(" size=");
            this.output.println(n2);
        }

        @Override
        public void onError(String string) {
            this.newLine();
            this.indentAndSave(this.lastIndent + 1);
            this.output.print("[E] ");
            this.output.print(string);
        }

        @Override
        public void sendSystemMessage(Component component) {
            this.newLine();
            this.printIndent(this.lastIndent + 1);
            this.output.print("[M] ");
            this.output.println(component.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public boolean alwaysAccepts() {
            return true;
        }

        @Override
        public void close() {
            IOUtils.closeQuietly((Writer)this.output);
        }
    }
}

