/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

public record PacketType<T extends Packet<?>>(PacketFlow flow, ResourceLocation id) {
    @Override
    public String toString() {
        return this.flow.id() + "/" + String.valueOf(this.id);
    }
}

