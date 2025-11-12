/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
    public static final Codec<CacheableFunction> CODEC = ResourceLocation.CODEC.xmap(CacheableFunction::new, CacheableFunction::getId);
    private final ResourceLocation id;
    private boolean resolved;
    private Optional<CommandFunction<CommandSourceStack>> function = Optional.empty();

    public CacheableFunction(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    public Optional<CommandFunction<CommandSourceStack>> get(ServerFunctionManager serverFunctionManager) {
        if (!this.resolved) {
            this.function = serverFunctionManager.get(this.id);
            this.resolved = true;
        }
        return this.function;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof CacheableFunction)) return false;
        CacheableFunction cacheableFunction = (CacheableFunction)object;
        if (!this.getId().equals(cacheableFunction.getId())) return false;
        return true;
    }
}

