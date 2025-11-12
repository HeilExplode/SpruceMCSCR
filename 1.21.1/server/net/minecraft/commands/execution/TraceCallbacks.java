/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution;

import net.minecraft.resources.ResourceLocation;

public interface TraceCallbacks
extends AutoCloseable {
    public void onCommand(int var1, String var2);

    public void onReturn(int var1, String var2, int var3);

    public void onError(String var1);

    public void onCall(int var1, ResourceLocation var2, int var3);

    @Override
    public void close();
}

