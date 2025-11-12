/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.functions;

import java.util.List;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.ResourceLocation;

public interface InstantiatedFunction<T> {
    public ResourceLocation id();

    public List<UnboundEntryAction<T>> entries();
}

