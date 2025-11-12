/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardCommand {
    private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.scoreboard.objectives.add.duplicate"));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType((Message)Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty"));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType((Message)Component.translatable("commands.scoreboard.objectives.display.alreadySet"));
    private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType((Message)Component.translatable("commands.scoreboard.players.enable.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType((Message)Component.translatable("commands.scoreboard.players.enable.invalid"));
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.scoreboard.players.get.null", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("scoreboard").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("objectives").then(Commands.literal("list").executes(commandContext -> ScoreboardCommand.listObjectives((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("add").then(Commands.argument("objective", StringArgumentType.word()).then(((RequiredArgumentBuilder)Commands.argument("criteria", ObjectiveCriteriaArgument.criteria()).executes(commandContext -> ScoreboardCommand.addObjective((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"objective"), ObjectiveCriteriaArgument.getCriteria((CommandContext<CommandSourceStack>)commandContext, "criteria"), Component.literal(StringArgumentType.getString((CommandContext)commandContext, (String)"objective"))))).then(Commands.argument("displayName", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> ScoreboardCommand.addObjective((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"objective"), ObjectiveCriteriaArgument.getCriteria((CommandContext<CommandSourceStack>)commandContext, "criteria"), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "displayName")))))))).then(Commands.literal("modify").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.literal("displayname").then(Commands.argument("displayName", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> ScoreboardCommand.setDisplayName((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "displayName")))))).then(ScoreboardCommand.createRenderTypeModify())).then(Commands.literal("displayautoupdate").then(Commands.argument("value", BoolArgumentType.bool()).executes(commandContext -> ScoreboardCommand.setDisplayAutoUpdate((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"value")))))).then(ScoreboardCommand.addNumberFormats(commandBuildContext, Commands.literal("numberformat"), (commandContext, numberFormat) -> ScoreboardCommand.setObjectiveFormat((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), numberFormat)))))).then(Commands.literal("remove").then(Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.removeObjective((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective")))))).then(Commands.literal("setdisplay").then(((RequiredArgumentBuilder)Commands.argument("slot", ScoreboardSlotArgument.displaySlot()).executes(commandContext -> ScoreboardCommand.clearDisplaySlot((CommandSourceStack)commandContext.getSource(), ScoreboardSlotArgument.getDisplaySlot((CommandContext<CommandSourceStack>)commandContext, "slot")))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.setDisplaySlot((CommandSourceStack)commandContext.getSource(), ScoreboardSlotArgument.getDisplaySlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective")))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("players").then(((LiteralArgumentBuilder)Commands.literal("list").executes(commandContext -> ScoreboardCommand.listTrackedPlayers((CommandSourceStack)commandContext.getSource()))).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(commandContext -> ScoreboardCommand.listTrackedPlayerScores((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getName((CommandContext<CommandSourceStack>)commandContext, "target")))))).then(Commands.literal("set").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer()).executes(commandContext -> ScoreboardCommand.setScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getWritableObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"score")))))))).then(Commands.literal("get").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.getScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getName((CommandContext<CommandSourceStack>)commandContext, "target"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"))))))).then(Commands.literal("add").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer((int)0)).executes(commandContext -> ScoreboardCommand.addScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getWritableObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"score")))))))).then(Commands.literal("remove").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer((int)0)).executes(commandContext -> ScoreboardCommand.removeScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getWritableObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"score")))))))).then(Commands.literal("reset").then(((RequiredArgumentBuilder)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(commandContext -> ScoreboardCommand.resetScores((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets")))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.resetScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"))))))).then(Commands.literal("enable").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((commandContext, suggestionsBuilder) -> ScoreboardCommand.suggestTriggers((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), suggestionsBuilder)).executes(commandContext -> ScoreboardCommand.enableTrigger((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"))))))).then(((LiteralArgumentBuilder)Commands.literal("display").then(Commands.literal("name").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("name", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> ScoreboardCommand.setScoreDisplay((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "name"))))).executes(commandContext -> ScoreboardCommand.setScoreDisplay((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), null)))))).then(Commands.literal("numberformat").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ScoreboardCommand.addNumberFormats(commandBuildContext, Commands.argument("objective", ObjectiveArgument.objective()), (commandContext, numberFormat) -> ScoreboardCommand.setScoreNumberFormat((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), numberFormat))))))).then(Commands.literal("operation").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.argument("operation", OperationArgument.operation()).then(Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.performOperation((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "targets"), ObjectiveArgument.getWritableObjective((CommandContext<CommandSourceStack>)commandContext, "targetObjective"), OperationArgument.getOperation((CommandContext<CommandSourceStack>)commandContext, "operation"), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)commandContext, "source"), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "sourceObjective")))))))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addNumberFormats(CommandBuildContext commandBuildContext, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, NumberFormatCommandExecutor numberFormatCommandExecutor) {
        return argumentBuilder.then(Commands.literal("blank").executes(commandContext -> numberFormatCommandExecutor.run((CommandContext<CommandSourceStack>)commandContext, BlankFormat.INSTANCE))).then(Commands.literal("fixed").then(Commands.argument("contents", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> {
            Component component = ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "contents");
            return numberFormatCommandExecutor.run((CommandContext<CommandSourceStack>)commandContext, new FixedFormat(component));
        }))).then(Commands.literal("styled").then(Commands.argument("style", StyleArgument.style(commandBuildContext)).executes(commandContext -> {
            Style style = StyleArgument.getStyle((CommandContext<CommandSourceStack>)commandContext, "style");
            return numberFormatCommandExecutor.run((CommandContext<CommandSourceStack>)commandContext, new StyledFormat(style));
        }))).executes(commandContext -> numberFormatCommandExecutor.run((CommandContext<CommandSourceStack>)commandContext, null));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("rendertype");
        for (ObjectiveCriteria.RenderType renderType : ObjectiveCriteria.RenderType.values()) {
            literalArgumentBuilder.then(Commands.literal(renderType.getId()).executes(commandContext -> ScoreboardCommand.setRenderType((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective((CommandContext<CommandSourceStack>)commandContext, "objective"), renderType)));
        }
        return literalArgumentBuilder;
    }

    private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, SuggestionsBuilder suggestionsBuilder) {
        ArrayList arrayList = Lists.newArrayList();
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        for (Objective objective : serverScoreboard.getObjectives()) {
            if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) continue;
            boolean bl = false;
            for (ScoreHolder scoreHolder : collection) {
                ReadOnlyScoreInfo readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective);
                if (readOnlyScoreInfo != null && !readOnlyScoreInfo.isLocked()) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            arrayList.add(objective.getName());
        }
        return SharedSuggestionProvider.suggest(arrayList, suggestionsBuilder);
    }

    private static int getScore(CommandSourceStack commandSourceStack, ScoreHolder scoreHolder, Objective objective) throws CommandSyntaxException {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        ReadOnlyScoreInfo readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective);
        if (readOnlyScoreInfo == null) {
            throw ERROR_NO_VALUE.create((Object)objective.getName(), (Object)scoreHolder.getFeedbackDisplayName());
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.get.success", scoreHolder.getFeedbackDisplayName(), readOnlyScoreInfo.value(), objective.getFormattedDisplayName()), false);
        return readOnlyScoreInfo.value();
    }

    private static Component getFirstTargetName(Collection<ScoreHolder> collection) {
        return collection.iterator().next().getFeedbackDisplayName();
    }

    private static int performOperation(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, OperationArgument.Operation operation, Collection<ScoreHolder> collection2, Objective objective2) throws CommandSyntaxException {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        int n = 0;
        for (ScoreHolder scoreHolder : collection) {
            ScoreAccess scoreAccess = serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective);
            for (ScoreHolder scoreHolder2 : collection2) {
                ScoreAccess scoreAccess2 = serverScoreboard.getOrCreatePlayerScore(scoreHolder2, objective2);
                operation.apply(scoreAccess, scoreAccess2);
            }
            n += scoreAccess.get();
        }
        if (collection.size() == 1) {
            int n2 = n;
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(collection), n2), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.multiple", objective.getFormattedDisplayName(), collection.size()), true);
        }
        return n;
    }

    private static int enableTrigger(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective) throws CommandSyntaxException {
        if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_NOT_TRIGGER.create();
        }
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        int n = 0;
        for (ScoreHolder scoreHolder : collection) {
            ScoreAccess scoreAccess = serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective);
            if (!scoreAccess.locked()) continue;
            scoreAccess.unlock();
            ++n;
        }
        if (n == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.enable.success.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(collection)), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.enable.success.multiple", objective.getFormattedDisplayName(), collection.size()), true);
        }
        return n;
    }

    private static int resetScores(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        for (ScoreHolder scoreHolder : collection) {
            serverScoreboard.resetAllPlayerScores(scoreHolder);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", ScoreboardCommand.getFirstTargetName(collection)), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", collection.size()), true);
        }
        return collection.size();
    }

    private static int resetScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        for (ScoreHolder scoreHolder : collection) {
            serverScoreboard.resetSinglePlayerScore(scoreHolder, objective);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.specific.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(collection)), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", objective.getFormattedDisplayName(), collection.size()), true);
        }
        return collection.size();
    }

    private static int setScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, int n) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        for (ScoreHolder scoreHolder : collection) {
            serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective).set(n);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(collection), n), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.multiple", objective.getFormattedDisplayName(), collection.size(), n), true);
        }
        return n * collection.size();
    }

    private static int setScoreDisplay(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, @Nullable Component component) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        for (ScoreHolder scoreHolder : collection) {
            serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective).display(component);
        }
        if (component == null) {
            if (collection.size() == 1) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.clear.success.single", ScoreboardCommand.getFirstTargetName(collection), objective.getFormattedDisplayName()), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.clear.success.multiple", collection.size(), objective.getFormattedDisplayName()), true);
            }
        } else if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.set.success.single", component, ScoreboardCommand.getFirstTargetName(collection), objective.getFormattedDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.set.success.multiple", component, collection.size(), objective.getFormattedDisplayName()), true);
        }
        return collection.size();
    }

    private static int setScoreNumberFormat(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, @Nullable NumberFormat numberFormat) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        for (ScoreHolder scoreHolder : collection) {
            serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective).numberFormatOverride(numberFormat);
        }
        if (numberFormat == null) {
            if (collection.size() == 1) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.clear.success.single", ScoreboardCommand.getFirstTargetName(collection), objective.getFormattedDisplayName()), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.clear.success.multiple", collection.size(), objective.getFormattedDisplayName()), true);
            }
        } else if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.set.success.single", ScoreboardCommand.getFirstTargetName(collection), objective.getFormattedDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.set.success.multiple", collection.size(), objective.getFormattedDisplayName()), true);
        }
        return collection.size();
    }

    private static int addScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, int n) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        int n2 = 0;
        for (ScoreHolder scoreHolder : collection) {
            ScoreAccess scoreAccess = serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective);
            scoreAccess.set(scoreAccess.get() + n);
            n2 += scoreAccess.get();
        }
        if (collection.size() == 1) {
            int n3 = n2;
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.single", n, objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(collection), n3), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.multiple", n, objective.getFormattedDisplayName(), collection.size()), true);
        }
        return n2;
    }

    private static int removeScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, int n) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        int n2 = 0;
        for (ScoreHolder scoreHolder : collection) {
            ScoreAccess scoreAccess = serverScoreboard.getOrCreatePlayerScore(scoreHolder, objective);
            scoreAccess.set(scoreAccess.get() - n);
            n2 += scoreAccess.get();
        }
        if (collection.size() == 1) {
            int n3 = n2;
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.remove.success.single", n, objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(collection), n3), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.remove.success.multiple", n, objective.getFormattedDisplayName(), collection.size()), true);
        }
        return n2;
    }

    private static int listTrackedPlayers(CommandSourceStack commandSourceStack) {
        Collection<ScoreHolder> collection = commandSourceStack.getServer().getScoreboard().getTrackedPlayers();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.success", collection.size(), ComponentUtils.formatList(collection, ScoreHolder::getFeedbackDisplayName)), false);
        }
        return collection.size();
    }

    private static int listTrackedPlayerScores(CommandSourceStack commandSourceStack, ScoreHolder scoreHolder) {
        Object2IntMap<Objective> object2IntMap = commandSourceStack.getServer().getScoreboard().listPlayerScores(scoreHolder);
        if (object2IntMap.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", scoreHolder.getFeedbackDisplayName()), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.success", scoreHolder.getFeedbackDisplayName(), object2IntMap.size()), false);
            Object2IntMaps.fastForEach(object2IntMap, entry -> commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.entry", ((Objective)entry.getKey()).getFormattedDisplayName(), entry.getIntValue()), false));
        }
        return object2IntMap.size();
    }

    private static int clearDisplaySlot(CommandSourceStack commandSourceStack, DisplaySlot displaySlot) throws CommandSyntaxException {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        if (serverScoreboard.getDisplayObjective(displaySlot) == null) {
            throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
        }
        ((Scoreboard)serverScoreboard).setDisplayObjective(displaySlot, null);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.cleared", displaySlot.getSerializedName()), true);
        return 0;
    }

    private static int setDisplaySlot(CommandSourceStack commandSourceStack, DisplaySlot displaySlot, Objective objective) throws CommandSyntaxException {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        if (serverScoreboard.getDisplayObjective(displaySlot) == objective) {
            throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
        }
        ((Scoreboard)serverScoreboard).setDisplayObjective(displaySlot, objective);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.set", displaySlot.getSerializedName(), objective.getDisplayName()), true);
        return 0;
    }

    private static int setDisplayName(CommandSourceStack commandSourceStack, Objective objective, Component component) {
        if (!objective.getDisplayName().equals(component)) {
            objective.setDisplayName(component);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayname", objective.getName(), objective.getFormattedDisplayName()), true);
        }
        return 0;
    }

    private static int setDisplayAutoUpdate(CommandSourceStack commandSourceStack, Objective objective, boolean bl) {
        if (objective.displayAutoUpdate() != bl) {
            objective.setDisplayAutoUpdate(bl);
            if (bl) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.enable", objective.getName(), objective.getFormattedDisplayName()), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.disable", objective.getName(), objective.getFormattedDisplayName()), true);
            }
        }
        return 0;
    }

    private static int setObjectiveFormat(CommandSourceStack commandSourceStack, Objective objective, @Nullable NumberFormat numberFormat) {
        objective.setNumberFormat(numberFormat);
        if (numberFormat != null) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.set", objective.getName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.clear", objective.getName()), true);
        }
        return 0;
    }

    private static int setRenderType(CommandSourceStack commandSourceStack, Objective objective, ObjectiveCriteria.RenderType renderType) {
        if (objective.getRenderType() != renderType) {
            objective.setRenderType(renderType);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.rendertype", objective.getFormattedDisplayName()), true);
        }
        return 0;
    }

    private static int removeObjective(CommandSourceStack commandSourceStack, Objective objective) {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        serverScoreboard.removeObjective(objective);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.remove.success", objective.getFormattedDisplayName()), true);
        return serverScoreboard.getObjectives().size();
    }

    private static int addObjective(CommandSourceStack commandSourceStack, String string, ObjectiveCriteria objectiveCriteria, Component component) throws CommandSyntaxException {
        ServerScoreboard serverScoreboard = commandSourceStack.getServer().getScoreboard();
        if (serverScoreboard.getObjective(string) != null) {
            throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
        }
        serverScoreboard.addObjective(string, objectiveCriteria, component, objectiveCriteria.getDefaultRenderType(), false, null);
        Objective objective = serverScoreboard.getObjective(string);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.add.success", objective.getFormattedDisplayName()), true);
        return serverScoreboard.getObjectives().size();
    }

    private static int listObjectives(CommandSourceStack commandSourceStack) {
        Collection<Objective> collection = commandSourceStack.getServer().getScoreboard().getObjectives();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.empty"), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.success", collection.size(), ComponentUtils.formatList(collection, Objective::getFormattedDisplayName)), false);
        }
        return collection.size();
    }

    @FunctionalInterface
    public static interface NumberFormatCommandExecutor {
        public int run(CommandContext<CommandSourceStack> var1, @Nullable NumberFormat var2) throws CommandSyntaxException;
    }
}

