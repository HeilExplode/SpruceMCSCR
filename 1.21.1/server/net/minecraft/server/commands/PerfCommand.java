/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.FileUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileZipper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class PerfCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType((Message)Component.translatable("commands.perf.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType((Message)Component.translatable("commands.perf.alreadyRunning"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("perf").requires(commandSourceStack -> commandSourceStack.hasPermission(4))).then(Commands.literal("start").executes(commandContext -> PerfCommand.startProfilingDedicatedServer((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("stop").executes(commandContext -> PerfCommand.stopProfilingDedicatedServer((CommandSourceStack)commandContext.getSource()))));
    }

    private static int startProfilingDedicatedServer(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer.isRecordingMetrics()) {
            throw ERROR_ALREADY_RUNNING.create();
        }
        Consumer<ProfileResults> consumer = profileResults -> PerfCommand.whenStopped(commandSourceStack, profileResults);
        Consumer<Path> consumer2 = path -> PerfCommand.saveResults(commandSourceStack, path, minecraftServer);
        minecraftServer.startRecordingMetrics(consumer, consumer2);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.perf.started"), false);
        return 0;
    }

    private static int stopProfilingDedicatedServer(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (!minecraftServer.isRecordingMetrics()) {
            throw ERROR_NOT_RUNNING.create();
        }
        minecraftServer.finishRecordingMetrics();
        return 0;
    }

    private static void saveResults(CommandSourceStack commandSourceStack, Path path, MinecraftServer minecraftServer) {
        String string;
        String string2 = String.format(Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), minecraftServer.getWorldData().getLevelName(), SharedConstants.getCurrentVersion().getId());
        try {
            string = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, string2, ".zip");
        }
        catch (IOException iOException) {
            commandSourceStack.sendFailure(Component.translatable("commands.perf.reportFailed"));
            LOGGER.error("Failed to create report name", (Throwable)iOException);
            return;
        }
        try (FileZipper fileZipper = new FileZipper(MetricsPersister.PROFILING_RESULTS_DIR.resolve(string));){
            fileZipper.add(Paths.get("system.txt", new String[0]), minecraftServer.fillSystemReport(new SystemReport()).toLineSeparatedString());
            fileZipper.add(path);
        }
        try {
            FileUtils.forceDelete((File)path.toFile());
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to delete temporary profiling file {}", (Object)path, (Object)iOException);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.perf.reportSaved", string), false);
    }

    private static void whenStopped(CommandSourceStack commandSourceStack, ProfileResults profileResults) {
        if (profileResults == EmptyProfileResults.EMPTY) {
            return;
        }
        int n = profileResults.getTickDuration();
        double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.perf.stopped", String.format(Locale.ROOT, "%.2f", d), n, String.format(Locale.ROOT, "%.2f", (double)n / d)), false);
    }
}

