/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TitleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("title").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.literal("clear").executes(commandContext -> TitleCommand.clearTitle((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"))))).then(Commands.literal("reset").executes(commandContext -> TitleCommand.resetTitle((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"))))).then(Commands.literal("title").then(Commands.argument("title", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> TitleCommand.showTitle((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "title"), "title", ClientboundSetTitleTextPacket::new))))).then(Commands.literal("subtitle").then(Commands.argument("title", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> TitleCommand.showTitle((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "title"), "subtitle", ClientboundSetSubtitleTextPacket::new))))).then(Commands.literal("actionbar").then(Commands.argument("title", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> TitleCommand.showTitle((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "title"), "actionbar", ClientboundSetActionBarTextPacket::new))))).then(Commands.literal("times").then(Commands.argument("fadeIn", TimeArgument.time()).then(Commands.argument("stay", TimeArgument.time()).then(Commands.argument("fadeOut", TimeArgument.time()).executes(commandContext -> TitleCommand.setTimes((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"fadeIn"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"stay"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"fadeOut")))))))));
    }

    private static int clearTitle(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection) {
        ClientboundClearTitlesPacket clientboundClearTitlesPacket = new ClientboundClearTitlesPacket(false);
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(clientboundClearTitlesPacket);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.cleared.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.cleared.multiple", collection.size()), true);
        }
        return collection.size();
    }

    private static int resetTitle(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection) {
        ClientboundClearTitlesPacket clientboundClearTitlesPacket = new ClientboundClearTitlesPacket(true);
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(clientboundClearTitlesPacket);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.reset.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.reset.multiple", collection.size()), true);
        }
        return collection.size();
    }

    private static int showTitle(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component, String string, Function<Component, Packet<?>> function) throws CommandSyntaxException {
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(function.apply(ComponentUtils.updateForEntity(commandSourceStack, component, (Entity)serverPlayer, 0)));
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.show." + string + ".single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.show." + string + ".multiple", collection.size()), true);
        }
        return collection.size();
    }

    private static int setTimes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, int n, int n2, int n3) {
        ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket = new ClientboundSetTitlesAnimationPacket(n, n2, n3);
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(clientboundSetTitlesAnimationPacket);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.times.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.title.times.multiple", collection.size()), true);
        }
        return collection.size();
    }
}

