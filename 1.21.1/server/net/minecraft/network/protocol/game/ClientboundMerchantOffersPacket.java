/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMerchantOffersPacket> STREAM_CODEC = Packet.codec(ClientboundMerchantOffersPacket::write, ClientboundMerchantOffersPacket::new);
    private final int containerId;
    private final MerchantOffers offers;
    private final int villagerLevel;
    private final int villagerXp;
    private final boolean showProgress;
    private final boolean canRestock;

    public ClientboundMerchantOffersPacket(int n, MerchantOffers merchantOffers, int n2, int n3, boolean bl, boolean bl2) {
        this.containerId = n;
        this.offers = merchantOffers.copy();
        this.villagerLevel = n2;
        this.villagerXp = n3;
        this.showProgress = bl;
        this.canRestock = bl2;
    }

    private ClientboundMerchantOffersPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.containerId = registryFriendlyByteBuf.readVarInt();
        this.offers = (MerchantOffers)MerchantOffers.STREAM_CODEC.decode(registryFriendlyByteBuf);
        this.villagerLevel = registryFriendlyByteBuf.readVarInt();
        this.villagerXp = registryFriendlyByteBuf.readVarInt();
        this.showProgress = registryFriendlyByteBuf.readBoolean();
        this.canRestock = registryFriendlyByteBuf.readBoolean();
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeVarInt(this.containerId);
        MerchantOffers.STREAM_CODEC.encode(registryFriendlyByteBuf, this.offers);
        registryFriendlyByteBuf.writeVarInt(this.villagerLevel);
        registryFriendlyByteBuf.writeVarInt(this.villagerXp);
        registryFriendlyByteBuf.writeBoolean(this.showProgress);
        registryFriendlyByteBuf.writeBoolean(this.canRestock);
    }

    @Override
    public PacketType<ClientboundMerchantOffersPacket> type() {
        return GamePacketTypes.CLIENTBOUND_MERCHANT_OFFERS;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMerchantOffers(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public MerchantOffers getOffers() {
        return this.offers;
    }

    public int getVillagerLevel() {
        return this.villagerLevel;
    }

    public int getVillagerXp() {
        return this.villagerXp;
    }

    public boolean showProgress() {
        return this.showProgress;
    }

    public boolean canRestock() {
        return this.canRestock;
    }
}

