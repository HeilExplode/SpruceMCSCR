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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
    public static final int MAX_ALLOWED_ITEMSTACKS = 100;

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("give").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("targets", EntityArgument.players()).then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item(commandBuildContext)).executes(commandContext -> GiveCommand.giveItem((CommandSourceStack)commandContext.getSource(), ItemArgument.getItem(commandContext, "item"), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), 1))).then(Commands.argument("count", IntegerArgumentType.integer((int)1)).executes(commandContext -> GiveCommand.giveItem((CommandSourceStack)commandContext.getSource(), ItemArgument.getItem(commandContext, "item"), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"count")))))));
    }

    private static int giveItem(CommandSourceStack commandSourceStack, ItemInput itemInput, Collection<ServerPlayer> collection, int n) throws CommandSyntaxException {
        ItemStack itemStack = itemInput.createItemStack(1, false);
        int n2 = itemStack.getMaxStackSize();
        int n3 = n2 * 100;
        if (n > n3) {
            commandSourceStack.sendFailure(Component.translatable("commands.give.failed.toomanyitems", n3, itemStack.getDisplayName()));
            return 0;
        }
        for (ServerPlayer serverPlayer : collection) {
            int n4 = n;
            while (n4 > 0) {
                ItemEntity itemEntity;
                int n5 = Math.min(n2, n4);
                n4 -= n5;
                ItemStack itemStack2 = itemInput.createItemStack(n5, false);
                boolean bl = serverPlayer.getInventory().add(itemStack2);
                if (!bl || !itemStack2.isEmpty()) {
                    itemEntity = serverPlayer.drop(itemStack2, false);
                    if (itemEntity == null) continue;
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(serverPlayer.getUUID());
                    continue;
                }
                itemEntity = serverPlayer.drop(itemStack, false);
                if (itemEntity != null) {
                    itemEntity.makeFakeItem();
                }
                serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.give.success.single", n, itemStack.getDisplayName(), ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.give.success.single", n, itemStack.getDisplayName(), collection.size()), true);
        }
        return collection.size();
    }
}

