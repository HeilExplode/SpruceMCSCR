/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.commands;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.chat.PlayerChatMessage;

public interface CommandSigningContext {
    public static final CommandSigningContext ANONYMOUS = new CommandSigningContext(){

        @Override
        @Nullable
        public PlayerChatMessage getArgument(String string) {
            return null;
        }
    };

    @Nullable
    public PlayerChatMessage getArgument(String var1);

    public record SignedArguments(Map<String, PlayerChatMessage> arguments) implements CommandSigningContext
    {
        @Override
        @Nullable
        public PlayerChatMessage getArgument(String string) {
            return this.arguments.get(string);
        }
    }
}

