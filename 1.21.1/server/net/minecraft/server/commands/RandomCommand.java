/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;

public class RandomCommand {
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_LARGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.random.error.range_too_large"));
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_SMALL = new SimpleCommandExceptionType((Message)Component.translatable("commands.random.error.range_too_small"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("random").then(RandomCommand.drawRandomValueTree("value", false))).then(RandomCommand.drawRandomValueTree("roll", true))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reset").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)Commands.literal("*").executes(commandContext -> RandomCommand.resetAllSequences((CommandSourceStack)commandContext.getSource()))).then(((RequiredArgumentBuilder)Commands.argument("seed", IntegerArgumentType.integer()).executes(commandContext -> RandomCommand.resetAllSequencesAndSetNewDefaults((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed"), true, true))).then(((RequiredArgumentBuilder)Commands.argument("includeWorldSeed", BoolArgumentType.bool()).executes(commandContext -> RandomCommand.resetAllSequencesAndSetNewDefaults((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"includeWorldSeed"), true))).then(Commands.argument("includeSequenceId", BoolArgumentType.bool()).executes(commandContext -> RandomCommand.resetAllSequencesAndSetNewDefaults((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"includeWorldSeed"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"includeSequenceId")))))))).then(((RequiredArgumentBuilder)Commands.argument("sequence", ResourceLocationArgument.id()).suggests(RandomCommand::suggestRandomSequence).executes(commandContext -> RandomCommand.resetSequence((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sequence")))).then(((RequiredArgumentBuilder)Commands.argument("seed", IntegerArgumentType.integer()).executes(commandContext -> RandomCommand.resetSequence((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sequence"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed"), true, true))).then(((RequiredArgumentBuilder)Commands.argument("includeWorldSeed", BoolArgumentType.bool()).executes(commandContext -> RandomCommand.resetSequence((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sequence"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"includeWorldSeed"), true))).then(Commands.argument("includeSequenceId", BoolArgumentType.bool()).executes(commandContext -> RandomCommand.resetSequence((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sequence"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seed"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"includeWorldSeed"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"includeSequenceId")))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> drawRandomValueTree(String string, boolean bl) {
        return (LiteralArgumentBuilder)Commands.literal(string).then(((RequiredArgumentBuilder)Commands.argument("range", RangeArgument.intRange()).executes(commandContext -> RandomCommand.randomSample((CommandSourceStack)commandContext.getSource(), RangeArgument.Ints.getRange((CommandContext<CommandSourceStack>)commandContext, "range"), null, bl))).then(((RequiredArgumentBuilder)Commands.argument("sequence", ResourceLocationArgument.id()).suggests(RandomCommand::suggestRandomSequence).requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> RandomCommand.randomSample((CommandSourceStack)commandContext.getSource(), RangeArgument.Ints.getRange((CommandContext<CommandSourceStack>)commandContext, "range"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sequence"), bl))));
    }

    private static CompletableFuture<Suggestions> suggestRandomSequence(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
        ArrayList arrayList = Lists.newArrayList();
        ((CommandSourceStack)commandContext.getSource()).getLevel().getRandomSequences().forAllSequences((resourceLocation, randomSequence) -> arrayList.add(resourceLocation.toString()));
        return SharedSuggestionProvider.suggest(arrayList, suggestionsBuilder);
    }

    private static int randomSample(CommandSourceStack commandSourceStack, MinMaxBounds.Ints ints, @Nullable ResourceLocation resourceLocation, boolean bl) throws CommandSyntaxException {
        RandomSource randomSource = resourceLocation != null ? commandSourceStack.getLevel().getRandomSequence(resourceLocation) : commandSourceStack.getLevel().getRandom();
        int n = ints.min().orElse(Integer.MIN_VALUE);
        int n2 = ints.max().orElse(Integer.MAX_VALUE);
        long l = (long)n2 - (long)n;
        if (l == 0L) {
            throw ERROR_RANGE_TOO_SMALL.create();
        }
        if (l >= Integer.MAX_VALUE) {
            throw ERROR_RANGE_TOO_LARGE.create();
        }
        int n3 = Mth.randomBetweenInclusive(randomSource, n, n2);
        if (bl) {
            commandSourceStack.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("commands.random.roll", commandSourceStack.getDisplayName(), n3, n, n2), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.sample.success", n3), false);
        }
        return n3;
    }

    private static int resetSequence(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation) throws CommandSyntaxException {
        commandSourceStack.getLevel().getRandomSequences().reset(resourceLocation);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg(resourceLocation)), false);
        return 1;
    }

    private static int resetSequence(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, int n, boolean bl, boolean bl2) throws CommandSyntaxException {
        commandSourceStack.getLevel().getRandomSequences().reset(resourceLocation, n, bl, bl2);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg(resourceLocation)), false);
        return 1;
    }

    private static int resetAllSequences(CommandSourceStack commandSourceStack) {
        int n = commandSourceStack.getLevel().getRandomSequences().clear();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", n), false);
        return n;
    }

    private static int resetAllSequencesAndSetNewDefaults(CommandSourceStack commandSourceStack, int n, boolean bl, boolean bl2) {
        RandomSequences randomSequences = commandSourceStack.getLevel().getRandomSequences();
        randomSequences.setSeedDefaults(n, bl, bl2);
        int n2 = randomSequences.clear();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", n2), false);
        return n2;
    }
}

