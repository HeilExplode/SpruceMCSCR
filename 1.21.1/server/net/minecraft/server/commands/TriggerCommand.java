/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType((Message)Component.translatable("commands.trigger.failed.unprimed"));
    private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType((Message)Component.translatable("commands.trigger.failed.invalid"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("trigger").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).suggests((commandContext, suggestionsBuilder) -> TriggerCommand.suggestObjectives((CommandSourceStack)commandContext.getSource(), suggestionsBuilder)).executes(commandContext -> TriggerCommand.simpleTrigger((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective")))).then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer()).executes(commandContext -> TriggerCommand.addValue((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"value")))))).then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer()).executes(commandContext -> TriggerCommand.setValue((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"value")))))));
    }

    public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack commandSourceStack, SuggestionsBuilder suggestionsBuilder) {
        Entity entity = commandSourceStack.getEntity();
        ArrayList arrayList = Lists.newArrayList();
        if (entity != null) {
            ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
            for (Objective objective : serverScoreboard.getObjectives()) {
                ReadOnlyScoreInfo readOnlyScoreInfo;
                if (objective.getCriteria() != ObjectiveCriteria.TRIGGER || (readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(entity, objective)) == null || readOnlyScoreInfo.isLocked()) continue;
                arrayList.add(objective.getName());
            }
        }
        return SharedSuggestionProvider.suggest(arrayList, suggestionsBuilder);
    }

    private static int addValue(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Objective objective, int n) throws CommandSyntaxException {
        ScoreAccess scoreAccess = TriggerCommand.getScore(commandSourceStack.getServer().getScoreboard(), serverPlayer, objective);
        int n2 = scoreAccess.add(n);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.trigger.add.success", objective.getFormattedDisplayName(), n), true);
        return n2;
    }

    private static int setValue(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Objective objective, int n) throws CommandSyntaxException {
        ScoreAccess scoreAccess = TriggerCommand.getScore(commandSourceStack.getServer().getScoreboard(), serverPlayer, objective);
        scoreAccess.set(n);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.trigger.set.success", objective.getFormattedDisplayName(), n), true);
        return n;
    }

    private static int simpleTrigger(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Objective objective) throws CommandSyntaxException {
        ScoreAccess scoreAccess = TriggerCommand.getScore(commandSourceStack.getServer().getScoreboard(), serverPlayer, objective);
        int n = scoreAccess.add(1);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.trigger.simple.success", objective.getFormattedDisplayName()), true);
        return n;
    }

    private static ScoreAccess getScore(Scoreboard scoreboard, ScoreHolder scoreHolder, Objective objective) throws CommandSyntaxException {
        if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_INVALID_OBJECTIVE.create();
        }
        ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
        if (readOnlyScoreInfo == null || readOnlyScoreInfo.isLocked()) {
            throw ERROR_NOT_PRIMED.create();
        }
        ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
        scoreAccess.lock();
        return scoreAccess;
    }
}

