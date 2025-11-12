/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ClientboundPlaceGhostRecipePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundPlaceGhostRecipePacket> STREAM_CODEC = Packet.codec(ClientboundPlaceGhostRecipePacket::write, ClientboundPlaceGhostRecipePacket::new);
    private final int containerId;
    private final ResourceLocation recipe;

    public ClientboundPlaceGhostRecipePacket(int n, RecipeHolder<?> recipeHolder) {
        this.containerId = n;
        this.recipe = recipeHolder.id();
    }

    private ClientboundPlaceGhostRecipePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readByte();
        this.recipe = friendlyByteBuf.readResourceLocation();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeResourceLocation(this.recipe);
    }

    @Override
    public PacketType<ClientboundPlaceGhostRecipePacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLACE_GHOST_RECIPE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlaceRecipe(this);
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    public int getContainerId() {
        return this.containerId;
    }
}

