/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("say").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("message", MessageArgument.message()).executes(commandContext -> {
            MessageArgument.resolveChatMessage((CommandContext<CommandSourceStack>)commandContext, "message", playerChatMessage -> {
                CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
                PlayerList playerList = commandSourceStack.getServer().getPlayerList();
                playerList.broadcastChatMessage((PlayerChatMessage)playerChatMessage, commandSourceStack, ChatType.bind(ChatType.SAY_COMMAND, commandSourceStack));
            });
            return 1;
        })));
    }
}

