/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
    private static final SimpleCommandExceptionType ERROR_KICKING_OWNER = new SimpleCommandExceptionType((Message)Component.translatable("commands.kick.owner.failed"));
    private static final SimpleCommandExceptionType ERROR_SINGLEPLAYER = new SimpleCommandExceptionType((Message)Component.translatable("commands.kick.singleplayer.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("kick").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> KickCommand.kickPlayers((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), Component.translatable("multiplayer.disconnect.kicked")))).then(Commands.argument("reason", MessageArgument.message()).executes(commandContext -> KickCommand.kickPlayers((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), MessageArgument.getMessage((CommandContext<CommandSourceStack>)commandContext, "reason"))))));
    }

    private static int kickPlayers(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component) throws CommandSyntaxException {
        if (!commandSourceStack.getServer().isPublished()) {
            throw ERROR_SINGLEPLAYER.create();
        }
        int n = 0;
        for (ServerPlayer serverPlayer : collection) {
            if (commandSourceStack.getServer().isSingleplayerOwner(serverPlayer.getGameProfile())) continue;
            serverPlayer.connection.disconnect(component);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.kick.success", serverPlayer.getDisplayName(), component), true);
            ++n;
        }
        if (n == 0) {
            throw ERROR_KICKING_OWNER.create();
        }
        return n;
    }
}

