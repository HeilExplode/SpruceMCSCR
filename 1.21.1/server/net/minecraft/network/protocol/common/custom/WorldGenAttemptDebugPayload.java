/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WorldGenAttemptDebugPayload(BlockPos pos, float scale, float red, float green, float blue, float alpha) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, WorldGenAttemptDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(WorldGenAttemptDebugPayload::write, WorldGenAttemptDebugPayload::new);
    public static final CustomPacketPayload.Type<WorldGenAttemptDebugPayload> TYPE = CustomPacketPayload.createType("debug/worldgen_attempt");

    private WorldGenAttemptDebugPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeFloat(this.scale);
        friendlyByteBuf.writeFloat(this.red);
        friendlyByteBuf.writeFloat(this.green);
        friendlyByteBuf.writeFloat(this.blue);
        friendlyByteBuf.writeFloat(this.alpha);
    }

    public CustomPacketPayload.Type<WorldGenAttemptDebugPayload> type() {
        return TYPE;
    }
}

