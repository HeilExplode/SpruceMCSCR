/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GoalDebugPayload(int entityId, BlockPos pos, List<DebugGoal> goals) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, GoalDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GoalDebugPayload::write, GoalDebugPayload::new);
    public static final CustomPacketPayload.Type<GoalDebugPayload> TYPE = CustomPacketPayload.createType("debug/goal_selector");

    private GoalDebugPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readInt(), friendlyByteBuf.readBlockPos(), friendlyByteBuf.readList(DebugGoal::new));
    }

    private void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeInt(this.entityId);
        friendlyByteBuf2.writeBlockPos(this.pos);
        friendlyByteBuf2.writeCollection(this.goals, (friendlyByteBuf, debugGoal) -> debugGoal.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
    }

    public CustomPacketPayload.Type<GoalDebugPayload> type() {
        return TYPE;
    }

    public record DebugGoal(int priority, boolean isRunning, String name) {
        public DebugGoal(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readInt(), friendlyByteBuf.readBoolean(), friendlyByteBuf.readUtf(255));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeInt(this.priority);
            friendlyByteBuf.writeBoolean(this.isRunning);
            friendlyByteBuf.writeUtf(this.name);
        }
    }
}

