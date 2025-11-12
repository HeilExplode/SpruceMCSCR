/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomQueryPayload {
    public ResourceLocation id();

    public void write(FriendlyByteBuf var1);
}

