/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
    public static final ChatDecorator PLAIN = (serverPlayer, component) -> component;

    public Component decorate(@Nullable ServerPlayer var1, Component var2);
}

