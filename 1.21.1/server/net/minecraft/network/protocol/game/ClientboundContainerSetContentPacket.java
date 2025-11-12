/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetContentPacket> STREAM_CODEC = Packet.codec(ClientboundContainerSetContentPacket::write, ClientboundContainerSetContentPacket::new);
    private final int containerId;
    private final int stateId;
    private final List<ItemStack> items;
    private final ItemStack carriedItem;

    public ClientboundContainerSetContentPacket(int n, int n2, NonNullList<ItemStack> nonNullList, ItemStack itemStack) {
        this.containerId = n;
        this.stateId = n2;
        this.items = NonNullList.withSize(nonNullList.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            this.items.set(i, nonNullList.get(i).copy());
        }
        this.carriedItem = itemStack.copy();
    }

    private ClientboundContainerSetContentPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.containerId = registryFriendlyByteBuf.readUnsignedByte();
        this.stateId = registryFriendlyByteBuf.readVarInt();
        this.items = (List)ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(registryFriendlyByteBuf);
        this.carriedItem = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeByte(this.containerId);
        registryFriendlyByteBuf.writeVarInt(this.stateId);
        ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(registryFriendlyByteBuf, this.items);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, this.carriedItem);
    }

    @Override
    public PacketType<ClientboundContainerSetContentPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerContent(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public int getStateId() {
        return this.stateId;
    }
}

