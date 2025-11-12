/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;

public class WardenSpawnTrackerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("warden_spawn_tracker").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("clear").executes(commandContext -> WardenSpawnTrackerCommand.resetTracker((CommandSourceStack)commandContext.getSource(), (Collection<? extends Player>)ImmutableList.of((Object)((CommandSourceStack)commandContext.getSource()).getPlayerOrException()))))).then(Commands.literal("set").then(Commands.argument("warning_level", IntegerArgumentType.integer((int)0, (int)4)).executes(commandContext -> WardenSpawnTrackerCommand.setWarningLevel((CommandSourceStack)commandContext.getSource(), (Collection<? extends Player>)ImmutableList.of((Object)((CommandSourceStack)commandContext.getSource()).getPlayerOrException()), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"warning_level"))))));
    }

    private static int setWarningLevel(CommandSourceStack commandSourceStack, Collection<? extends Player> collection, int n) {
        for (Player player : collection) {
            player.getWardenSpawnTracker().ifPresent(wardenSpawnTracker -> wardenSpawnTracker.setWarningLevel(n));
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.single", ((Player)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.multiple", collection.size()), true);
        }
        return collection.size();
    }

    private static int resetTracker(CommandSourceStack commandSourceStack, Collection<? extends Player> collection) {
        for (Player player : collection) {
            player.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.single", ((Player)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", collection.size()), true);
        }
        return collection.size();
    }
}

