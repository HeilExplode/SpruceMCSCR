/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ServerboundContainerClickPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundContainerClickPacket> STREAM_CODEC = Packet.codec(ServerboundContainerClickPacket::write, ServerboundContainerClickPacket::new);
    private static final int MAX_SLOT_COUNT = 128;
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ItemStack>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(Int2ObjectOpenHashMap::new, ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue), ItemStack.OPTIONAL_STREAM_CODEC, 128);
    private final int containerId;
    private final int stateId;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final ItemStack carriedItem;
    private final Int2ObjectMap<ItemStack> changedSlots;

    public ServerboundContainerClickPacket(int n, int n2, int n3, int n4, ClickType clickType, ItemStack itemStack, Int2ObjectMap<ItemStack> int2ObjectMap) {
        this.containerId = n;
        this.stateId = n2;
        this.slotNum = n3;
        this.buttonNum = n4;
        this.clickType = clickType;
        this.carriedItem = itemStack;
        this.changedSlots = Int2ObjectMaps.unmodifiable(int2ObjectMap);
    }

    private ServerboundContainerClickPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.containerId = registryFriendlyByteBuf.readByte();
        this.stateId = registryFriendlyByteBuf.readVarInt();
        this.slotNum = registryFriendlyByteBuf.readShort();
        this.buttonNum = registryFriendlyByteBuf.readByte();
        this.clickType = registryFriendlyByteBuf.readEnum(ClickType.class);
        this.changedSlots = Int2ObjectMaps.unmodifiable((Int2ObjectMap)((Int2ObjectMap)SLOTS_STREAM_CODEC.decode(registryFriendlyByteBuf)));
        this.carriedItem = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeByte(this.containerId);
        registryFriendlyByteBuf.writeVarInt(this.stateId);
        registryFriendlyByteBuf.writeShort(this.slotNum);
        registryFriendlyByteBuf.writeByte(this.buttonNum);
        registryFriendlyByteBuf.writeEnum(this.clickType);
        SLOTS_STREAM_CODEC.encode(registryFriendlyByteBuf, this.changedSlots);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, this.carriedItem);
    }

    @Override
    public PacketType<ServerboundContainerClickPacket> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_CLICK;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleContainerClick(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public Int2ObjectMap<ItemStack> getChangedSlots() {
        return this.changedSlots;
    }

    public ClickType getClickType() {
        return this.clickType;
    }

    public int getStateId() {
        return this.stateId;
    }
}

