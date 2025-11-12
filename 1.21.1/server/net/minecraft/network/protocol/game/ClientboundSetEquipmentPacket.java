/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetEquipmentPacket> STREAM_CODEC = Packet.codec(ClientboundSetEquipmentPacket::write, ClientboundSetEquipmentPacket::new);
    private static final byte CONTINUE_MASK = -128;
    private final int entity;
    private final List<Pair<EquipmentSlot, ItemStack>> slots;

    public ClientboundSetEquipmentPacket(int n, List<Pair<EquipmentSlot, ItemStack>> list) {
        this.entity = n;
        this.slots = list;
    }

    private ClientboundSetEquipmentPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        byte by;
        this.entity = registryFriendlyByteBuf.readVarInt();
        EquipmentSlot[] equipmentSlotArray = EquipmentSlot.values();
        this.slots = Lists.newArrayList();
        do {
            by = registryFriendlyByteBuf.readByte();
            EquipmentSlot equipmentSlot = equipmentSlotArray[by & 0x7F];
            ItemStack itemStack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
            this.slots.add((Pair<EquipmentSlot, ItemStack>)Pair.of((Object)equipmentSlot, (Object)itemStack));
        } while ((by & 0xFFFFFF80) != 0);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeVarInt(this.entity);
        int n = this.slots.size();
        for (int i = 0; i < n; ++i) {
            Pair<EquipmentSlot, ItemStack> pair = this.slots.get(i);
            EquipmentSlot equipmentSlot = (EquipmentSlot)pair.getFirst();
            boolean bl = i != n - 1;
            int n2 = equipmentSlot.ordinal();
            registryFriendlyByteBuf.writeByte(bl ? n2 | 0xFFFFFF80 : n2);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, (ItemStack)pair.getSecond());
        }
    }

    @Override
    public PacketType<ClientboundSetEquipmentPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_EQUIPMENT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEquipment(this);
    }

    public int getEntity() {
        return this.entity;
    }

    public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
        return this.slots;
    }
}

