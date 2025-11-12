/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.network.protocol.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiAddedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiRemovedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiTicketCountDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.common.custom.VillageSectionsDebugPayload;
import net.minecraft.network.protocol.common.custom.WorldGenAttemptDebugPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ClientCommonPacketListener>
{
    private static final int MAX_PAYLOAD_SIZE = 0x100000;
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> GAMEPLAY_STREAM_CODEC = CustomPacketPayload.codec((ResourceLocation resourceLocation) -> DiscardedPayload.codec(resourceLocation, 0x100000), Util.make(Lists.newArrayList((Object[])new CustomPacketPayload.TypeAndCodec[]{new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, BrandPayload>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, BeeDebugPayload>(BeeDebugPayload.TYPE, BeeDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, BrainDebugPayload>(BrainDebugPayload.TYPE, BrainDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, BreezeDebugPayload>(BreezeDebugPayload.TYPE, BreezeDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, GameEventDebugPayload>(GameEventDebugPayload.TYPE, GameEventDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, GameEventListenerDebugPayload>(GameEventListenerDebugPayload.TYPE, GameEventListenerDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, GameTestAddMarkerDebugPayload>(GameTestAddMarkerDebugPayload.TYPE, GameTestAddMarkerDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, GameTestClearMarkersDebugPayload>(GameTestClearMarkersDebugPayload.TYPE, GameTestClearMarkersDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, GoalDebugPayload>(GoalDebugPayload.TYPE, GoalDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, HiveDebugPayload>(HiveDebugPayload.TYPE, HiveDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, NeighborUpdatesDebugPayload>(NeighborUpdatesDebugPayload.TYPE, NeighborUpdatesDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, PathfindingDebugPayload>(PathfindingDebugPayload.TYPE, PathfindingDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, PoiAddedDebugPayload>(PoiAddedDebugPayload.TYPE, PoiAddedDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, PoiRemovedDebugPayload>(PoiRemovedDebugPayload.TYPE, PoiRemovedDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, PoiTicketCountDebugPayload>(PoiTicketCountDebugPayload.TYPE, PoiTicketCountDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, RaidsDebugPayload>(RaidsDebugPayload.TYPE, RaidsDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, StructuresDebugPayload>(StructuresDebugPayload.TYPE, StructuresDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, VillageSectionsDebugPayload>(VillageSectionsDebugPayload.TYPE, VillageSectionsDebugPayload.STREAM_CODEC), new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, WorldGenAttemptDebugPayload>(WorldGenAttemptDebugPayload.TYPE, WorldGenAttemptDebugPayload.STREAM_CODEC)}), arrayList -> {})).map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomPayloadPacket> CONFIG_STREAM_CODEC = CustomPacketPayload.codec((ResourceLocation resourceLocation) -> DiscardedPayload.codec(resourceLocation, 0x100000), List.of(new CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, BrandPayload>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC))).map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);

    @Override
    public PacketType<ClientboundCustomPayloadPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD;
    }

    @Override
    public void handle(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.handleCustomPayload(this);
    }
}

