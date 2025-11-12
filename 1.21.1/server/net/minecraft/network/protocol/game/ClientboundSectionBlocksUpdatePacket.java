/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.shorts.ShortIterator
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSectionBlocksUpdatePacket> STREAM_CODEC = Packet.codec(ClientboundSectionBlocksUpdatePacket::write, ClientboundSectionBlocksUpdatePacket::new);
    private static final int POS_IN_SECTION_BITS = 12;
    private final SectionPos sectionPos;
    private final short[] positions;
    private final BlockState[] states;

    public ClientboundSectionBlocksUpdatePacket(SectionPos sectionPos, ShortSet shortSet, LevelChunkSection levelChunkSection) {
        this.sectionPos = sectionPos;
        int n = shortSet.size();
        this.positions = new short[n];
        this.states = new BlockState[n];
        int n2 = 0;
        ShortIterator shortIterator = shortSet.iterator();
        while (shortIterator.hasNext()) {
            short s;
            this.positions[n2] = s = ((Short)shortIterator.next()).shortValue();
            this.states[n2] = levelChunkSection.getBlockState(SectionPos.sectionRelativeX(s), SectionPos.sectionRelativeY(s), SectionPos.sectionRelativeZ(s));
            ++n2;
        }
    }

    private ClientboundSectionBlocksUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
        this.sectionPos = SectionPos.of(friendlyByteBuf.readLong());
        int n = friendlyByteBuf.readVarInt();
        this.positions = new short[n];
        this.states = new BlockState[n];
        for (int i = 0; i < n; ++i) {
            long l = friendlyByteBuf.readVarLong();
            this.positions[i] = (short)(l & 0xFFFL);
            this.states[i] = Block.BLOCK_STATE_REGISTRY.byId((int)(l >>> 12));
        }
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.sectionPos.asLong());
        friendlyByteBuf.writeVarInt(this.positions.length);
        for (int i = 0; i < this.positions.length; ++i) {
            friendlyByteBuf.writeVarLong((long)Block.getId(this.states[i]) << 12 | (long)this.positions[i]);
        }
    }

    @Override
    public PacketType<ClientboundSectionBlocksUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SECTION_BLOCKS_UPDATE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleChunkBlocksUpdate(this);
    }

    public void runUpdates(BiConsumer<BlockPos, BlockState> biConsumer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < this.positions.length; ++i) {
            short s = this.positions[i];
            mutableBlockPos.set(this.sectionPos.relativeToBlockX(s), this.sectionPos.relativeToBlockY(s), this.sectionPos.relativeToBlockZ(s));
            biConsumer.accept(mutableBlockPos, this.states[i]);
        }
    }
}

