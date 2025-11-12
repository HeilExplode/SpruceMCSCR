/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.scores.PlayerTeam;

public class ClientboundSetPlayerTeamPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetPlayerTeamPacket> STREAM_CODEC = Packet.codec(ClientboundSetPlayerTeamPacket::write, ClientboundSetPlayerTeamPacket::new);
    private static final int METHOD_ADD = 0;
    private static final int METHOD_REMOVE = 1;
    private static final int METHOD_CHANGE = 2;
    private static final int METHOD_JOIN = 3;
    private static final int METHOD_LEAVE = 4;
    private static final int MAX_VISIBILITY_LENGTH = 40;
    private static final int MAX_COLLISION_LENGTH = 40;
    private final int method;
    private final String name;
    private final Collection<String> players;
    private final Optional<Parameters> parameters;

    private ClientboundSetPlayerTeamPacket(String string, int n, Optional<Parameters> optional, Collection<String> collection) {
        this.name = string;
        this.method = n;
        this.parameters = optional;
        this.players = ImmutableList.copyOf(collection);
    }

    public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam playerTeam, boolean bl) {
        return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), bl ? 0 : 2, Optional.of(new Parameters(playerTeam)), bl ? playerTeam.getPlayers() : ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam playerTeam) {
        return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), 1, Optional.empty(), (Collection<String>)ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam playerTeam, String string, Action action) {
        return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), action == Action.ADD ? 3 : 4, Optional.empty(), (Collection<String>)ImmutableList.of((Object)string));
    }

    private ClientboundSetPlayerTeamPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.name = registryFriendlyByteBuf.readUtf();
        this.method = registryFriendlyByteBuf.readByte();
        this.parameters = ClientboundSetPlayerTeamPacket.shouldHaveParameters(this.method) ? Optional.of(new Parameters(registryFriendlyByteBuf)) : Optional.empty();
        this.players = ClientboundSetPlayerTeamPacket.shouldHavePlayerList(this.method) ? registryFriendlyByteBuf.readList(FriendlyByteBuf::readUtf) : ImmutableList.of();
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeUtf(this.name);
        registryFriendlyByteBuf.writeByte(this.method);
        if (ClientboundSetPlayerTeamPacket.shouldHaveParameters(this.method)) {
            this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)).write(registryFriendlyByteBuf);
        }
        if (ClientboundSetPlayerTeamPacket.shouldHavePlayerList(this.method)) {
            registryFriendlyByteBuf.writeCollection(this.players, FriendlyByteBuf::writeUtf);
        }
    }

    private static boolean shouldHavePlayerList(int n) {
        return n == 0 || n == 3 || n == 4;
    }

    private static boolean shouldHaveParameters(int n) {
        return n == 0 || n == 2;
    }

    @Nullable
    public Action getPlayerAction() {
        return switch (this.method) {
            case 0, 3 -> Action.ADD;
            case 4 -> Action.REMOVE;
            default -> null;
        };
    }

    @Nullable
    public Action getTeamAction() {
        return switch (this.method) {
            case 0 -> Action.ADD;
            case 1 -> Action.REMOVE;
            default -> null;
        };
    }

    @Override
    public PacketType<ClientboundSetPlayerTeamPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetPlayerTeamPacket(this);
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getPlayers() {
        return this.players;
    }

    public Optional<Parameters> getParameters() {
        return this.parameters;
    }

    public static class Parameters {
        private final Component displayName;
        private final Component playerPrefix;
        private final Component playerSuffix;
        private final String nametagVisibility;
        private final String collisionRule;
        private final ChatFormatting color;
        private final int options;

        public Parameters(PlayerTeam playerTeam) {
            this.displayName = playerTeam.getDisplayName();
            this.options = playerTeam.packOptions();
            this.nametagVisibility = playerTeam.getNameTagVisibility().name;
            this.collisionRule = playerTeam.getCollisionRule().name;
            this.color = playerTeam.getColor();
            this.playerPrefix = playerTeam.getPlayerPrefix();
            this.playerSuffix = playerTeam.getPlayerSuffix();
        }

        public Parameters(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this.displayName = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
            this.options = registryFriendlyByteBuf.readByte();
            this.nametagVisibility = registryFriendlyByteBuf.readUtf(40);
            this.collisionRule = registryFriendlyByteBuf.readUtf(40);
            this.color = registryFriendlyByteBuf.readEnum(ChatFormatting.class);
            this.playerPrefix = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
            this.playerSuffix = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
        }

        public Component getDisplayName() {
            return this.displayName;
        }

        public int getOptions() {
            return this.options;
        }

        public ChatFormatting getColor() {
            return this.color;
        }

        public String getNametagVisibility() {
            return this.nametagVisibility;
        }

        public String getCollisionRule() {
            return this.collisionRule;
        }

        public Component getPlayerPrefix() {
            return this.playerPrefix;
        }

        public Component getPlayerSuffix() {
            return this.playerSuffix;
        }

        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.displayName);
            registryFriendlyByteBuf.writeByte(this.options);
            registryFriendlyByteBuf.writeUtf(this.nametagVisibility);
            registryFriendlyByteBuf.writeUtf(this.collisionRule);
            registryFriendlyByteBuf.writeEnum(this.color);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.playerPrefix);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.playerSuffix);
        }
    }

    public static enum Action {
        ADD,
        REMOVE;

    }
}

