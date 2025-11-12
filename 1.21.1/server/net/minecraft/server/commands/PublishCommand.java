/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class PublishCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.publish.failed"));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.publish.alreadyPublished", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("publish").requires(commandSourceStack -> commandSourceStack.hasPermission(4))).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), false, null))).then(((RequiredArgumentBuilder)Commands.argument("allowCommands", BoolArgumentType.bool()).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool((CommandContext)commandContext, (String)"allowCommands"), null))).then(((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode()).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool((CommandContext)commandContext, (String)"allowCommands"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode")))).then(Commands.argument("port", IntegerArgumentType.integer((int)0, (int)65535)).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"port"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"allowCommands"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode")))))));
    }

    private static int publish(CommandSourceStack commandSourceStack, int n, boolean bl, @Nullable GameType gameType) throws CommandSyntaxException {
        if (commandSourceStack.getServer().isPublished()) {
            throw ERROR_ALREADY_PUBLISHED.create((Object)commandSourceStack.getServer().getPort());
        }
        if (!commandSourceStack.getServer().publishServer(gameType, bl, n)) {
            throw ERROR_FAILED.create();
        }
        commandSourceStack.sendSuccess(() -> PublishCommand.getSuccessMessage(n), true);
        return n;
    }

    public static MutableComponent getSuccessMessage(int n) {
        MutableComponent mutableComponent = ComponentUtils.copyOnClickText(String.valueOf(n));
        return Component.translatable("commands.publish.started", mutableComponent);
    }
}

