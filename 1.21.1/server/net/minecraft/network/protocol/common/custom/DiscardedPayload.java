/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DiscardedPayload(ResourceLocation id) implements CustomPacketPayload
{
    public static <T extends FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(ResourceLocation resourceLocation, int n) {
        return CustomPacketPayload.codec((T discardedPayload, B friendlyByteBuf) -> {}, (B friendlyByteBuf) -> {
            int n2 = friendlyByteBuf.readableBytes();
            if (n2 < 0 || n2 > n) {
                throw new IllegalArgumentException("Payload may not be larger than " + n + " bytes");
            }
            friendlyByteBuf.skipBytes(n2);
            return new DiscardedPayload(resourceLocation);
        });
    }

    public CustomPacketPayload.Type<DiscardedPayload> type() {
        return new CustomPacketPayload.Type<DiscardedPayload>(this.id);
    }
}

