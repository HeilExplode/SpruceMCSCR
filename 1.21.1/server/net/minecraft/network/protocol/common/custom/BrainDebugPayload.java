/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BrainDebugPayload(BrainDump brainDump) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, BrainDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BrainDebugPayload::write, BrainDebugPayload::new);
    public static final CustomPacketPayload.Type<BrainDebugPayload> TYPE = CustomPacketPayload.createType("debug/brain");

    private BrainDebugPayload(FriendlyByteBuf friendlyByteBuf) {
        this(new BrainDump(friendlyByteBuf));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        this.brainDump.write(friendlyByteBuf);
    }

    public CustomPacketPayload.Type<BrainDebugPayload> type() {
        return TYPE;
    }

    public record BrainDump(UUID uuid, int id, String name, String profession, int xp, float health, float maxHealth, Vec3 pos, String inventory, @Nullable Path path, boolean wantsGolem, int angerLevel, List<String> activities, List<String> behaviors, List<String> memories, List<String> gossips, Set<BlockPos> pois, Set<BlockPos> potentialPois) {
        public BrainDump(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readUUID(), friendlyByteBuf.readInt(), friendlyByteBuf.readUtf(), friendlyByteBuf.readUtf(), friendlyByteBuf.readInt(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readVec3(), friendlyByteBuf.readUtf(), friendlyByteBuf.readNullable(Path::createFromStream), friendlyByteBuf.readBoolean(), friendlyByteBuf.readInt(), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readCollection(HashSet::new, BlockPos.STREAM_CODEC), friendlyByteBuf.readCollection(HashSet::new, BlockPos.STREAM_CODEC));
        }

        public void write(FriendlyByteBuf friendlyByteBuf2) {
            friendlyByteBuf2.writeUUID(this.uuid);
            friendlyByteBuf2.writeInt(this.id);
            friendlyByteBuf2.writeUtf(this.name);
            friendlyByteBuf2.writeUtf(this.profession);
            friendlyByteBuf2.writeInt(this.xp);
            friendlyByteBuf2.writeFloat(this.health);
            friendlyByteBuf2.writeFloat(this.maxHealth);
            friendlyByteBuf2.writeVec3(this.pos);
            friendlyByteBuf2.writeUtf(this.inventory);
            friendlyByteBuf2.writeNullable(this.path, (friendlyByteBuf, path) -> path.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)));
            friendlyByteBuf2.writeBoolean(this.wantsGolem);
            friendlyByteBuf2.writeInt(this.angerLevel);
            friendlyByteBuf2.writeCollection(this.activities, FriendlyByteBuf::writeUtf);
            friendlyByteBuf2.writeCollection(this.behaviors, FriendlyByteBuf::writeUtf);
            friendlyByteBuf2.writeCollection(this.memories, FriendlyByteBuf::writeUtf);
            friendlyByteBuf2.writeCollection(this.gossips, FriendlyByteBuf::writeUtf);
            friendlyByteBuf2.writeCollection(this.pois, BlockPos.STREAM_CODEC);
            friendlyByteBuf2.writeCollection(this.potentialPois, BlockPos.STREAM_CODEC);
        }

        public boolean hasPoi(BlockPos blockPos) {
            return this.pois.contains(blockPos);
        }

        public boolean hasPotentialPoi(BlockPos blockPos) {
            return this.potentialPois.contains(blockPos);
        }
    }
}

