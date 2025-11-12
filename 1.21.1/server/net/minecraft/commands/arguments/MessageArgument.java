/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

public class MessageArgument
implements SignedArgument<Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    static final Dynamic2CommandExceptionType TOO_LONG = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("argument.message.too_long", object, object2));

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Message message = (Message)commandContext.getArgument(string, Message.class);
        return message.resolveComponent((CommandSourceStack)commandContext.getSource());
    }

    public static void resolveChatMessage(CommandContext<CommandSourceStack> commandContext, String string, Consumer<PlayerChatMessage> consumer) throws CommandSyntaxException {
        Message message = (Message)commandContext.getArgument(string, Message.class);
        CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
        Component component = message.resolveComponent(commandSourceStack);
        CommandSigningContext commandSigningContext = commandSourceStack.getSigningContext();
        PlayerChatMessage playerChatMessage = commandSigningContext.getArgument(string);
        if (playerChatMessage != null) {
            MessageArgument.resolveSignedMessage(consumer, commandSourceStack, playerChatMessage.withUnsignedContent(component));
        } else {
            MessageArgument.resolveDisguisedMessage(consumer, commandSourceStack, PlayerChatMessage.system(message.text).withUnsignedContent(component));
        }
    }

    private static void resolveSignedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        CompletableFuture<FilteredText> completableFuture = MessageArgument.filterPlainText(commandSourceStack, playerChatMessage);
        Component component = minecraftServer.getChatDecorator().decorate(commandSourceStack.getPlayer(), playerChatMessage.decoratedContent());
        commandSourceStack.getChatMessageChainer().append(completableFuture, filteredText -> {
            PlayerChatMessage playerChatMessage2 = playerChatMessage.withUnsignedContent(component).filter(filteredText.mask());
            consumer.accept(playerChatMessage2);
        });
    }

    private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
        ChatDecorator chatDecorator = commandSourceStack.getServer().getChatDecorator();
        Component component = chatDecorator.decorate(commandSourceStack.getPlayer(), playerChatMessage.decoratedContent());
        consumer.accept(playerChatMessage.withUnsignedContent(component));
    }

    private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
        ServerPlayer serverPlayer = commandSourceStack.getPlayer();
        if (serverPlayer != null && playerChatMessage.hasSignatureFrom(serverPlayer.getUUID())) {
            return serverPlayer.getTextFilter().processStreamMessage(playerChatMessage.signedContent());
        }
        return CompletableFuture.completedFuture(FilteredText.passThrough(playerChatMessage.signedContent()));
    }

    public Message parse(StringReader stringReader) throws CommandSyntaxException {
        return Message.parseText(stringReader, true);
    }

    public <S> Message parse(StringReader stringReader, @Nullable S s) throws CommandSyntaxException {
        return Message.parseText(stringReader, EntitySelectorParser.allowSelectors(s));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader, @Nullable Object object) throws CommandSyntaxException {
        return this.parse(stringReader, object);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static final class Message
    extends Record {
        final String text;
        private final Part[] parts;

        public Message(String string, Part[] partArray) {
            this.text = string;
            this.parts = partArray;
        }

        Component resolveComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            return this.toComponent(commandSourceStack, EntitySelectorParser.allowSelectors(commandSourceStack));
        }

        public Component toComponent(CommandSourceStack commandSourceStack, boolean bl) throws CommandSyntaxException {
            if (this.parts.length == 0 || !bl) {
                return Component.literal(this.text);
            }
            MutableComponent mutableComponent = Component.literal(this.text.substring(0, this.parts[0].start()));
            int n = this.parts[0].start();
            for (Part part : this.parts) {
                Component component = part.toComponent(commandSourceStack);
                if (n < part.start()) {
                    mutableComponent.append(this.text.substring(n, part.start()));
                }
                mutableComponent.append(component);
                n = part.end();
            }
            if (n < this.text.length()) {
                mutableComponent.append(this.text.substring(n));
            }
            return mutableComponent;
        }

        public static Message parseText(StringReader stringReader, boolean bl) throws CommandSyntaxException {
            if (stringReader.getRemainingLength() > 256) {
                throw TOO_LONG.create((Object)stringReader.getRemainingLength(), (Object)256);
            }
            String string = stringReader.getRemaining();
            if (!bl) {
                stringReader.setCursor(stringReader.getTotalLength());
                return new Message(string, new Part[0]);
            }
            ArrayList arrayList = Lists.newArrayList();
            int n = stringReader.getCursor();
            while (stringReader.canRead()) {
                if (stringReader.peek() == '@') {
                    EntitySelector entitySelector;
                    int n2 = stringReader.getCursor();
                    try {
                        EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader, true);
                        entitySelector = entitySelectorParser.parse();
                    }
                    catch (CommandSyntaxException commandSyntaxException) {
                        if (commandSyntaxException.getType() == EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE || commandSyntaxException.getType() == EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                            stringReader.setCursor(n2 + 1);
                            continue;
                        }
                        throw commandSyntaxException;
                    }
                    arrayList.add(new Part(n2 - n, stringReader.getCursor() - n, entitySelector));
                    continue;
                }
                stringReader.skip();
            }
            return new Message(string, arrayList.toArray(new Part[0]));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Message.class, "text;parts", "text", "parts"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Message.class, "text;parts", "text", "parts"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Message.class, "text;parts", "text", "parts"}, this, object);
        }

        public String text() {
            return this.text;
        }

        public Part[] parts() {
            return this.parts;
        }
    }

    public record Part(int start, int end, EntitySelector selector) {
        public Component toComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            return EntitySelector.joinNames(this.selector.findEntities(commandSourceStack));
        }
    }
}

