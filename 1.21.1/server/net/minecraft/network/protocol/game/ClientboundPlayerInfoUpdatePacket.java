/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.Multimap
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket> STREAM_CODEC = Packet.codec(ClientboundPlayerInfoUpdatePacket::write, ClientboundPlayerInfoUpdatePacket::new);
    private final EnumSet<Action> actions;
    private final List<Entry> entries;

    public ClientboundPlayerInfoUpdatePacket(EnumSet<Action> enumSet, Collection<ServerPlayer> collection) {
        this.actions = enumSet;
        this.entries = collection.stream().map(Entry::new).toList();
    }

    public ClientboundPlayerInfoUpdatePacket(Action action, ServerPlayer serverPlayer) {
        this.actions = EnumSet.of(action);
        this.entries = List.of(new Entry(serverPlayer));
    }

    public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> collection) {
        EnumSet<Action[]> enumSet = EnumSet.of(Action.ADD_PLAYER, new Action[]{Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME});
        return new ClientboundPlayerInfoUpdatePacket(enumSet, collection);
    }

    private ClientboundPlayerInfoUpdatePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.actions = registryFriendlyByteBuf.readEnumSet(Action.class);
        this.entries = registryFriendlyByteBuf.readList(friendlyByteBuf -> {
            EntryBuilder entryBuilder = new EntryBuilder(friendlyByteBuf.readUUID());
            for (Action action : this.actions) {
                action.reader.read(entryBuilder, (RegistryFriendlyByteBuf)((Object)friendlyByteBuf));
            }
            return entryBuilder.build();
        });
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeEnumSet(this.actions, Action.class);
        registryFriendlyByteBuf.writeCollection(this.entries, (friendlyByteBuf, entry) -> {
            friendlyByteBuf.writeUUID(entry.profileId());
            for (Action action : this.actions) {
                action.writer.write((RegistryFriendlyByteBuf)((Object)friendlyByteBuf), (Entry)entry);
            }
        });
    }

    @Override
    public PacketType<ClientboundPlayerInfoUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerInfoUpdate(this);
    }

    public EnumSet<Action> actions() {
        return this.actions;
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public List<Entry> newEntries() {
        return this.actions.contains((Object)Action.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public static final class Entry
    extends Record {
        private final UUID profileId;
        @Nullable
        private final GameProfile profile;
        private final boolean listed;
        private final int latency;
        private final GameType gameMode;
        @Nullable
        private final Component displayName;
        @Nullable
        final RemoteChatSession.Data chatSession;

        Entry(ServerPlayer serverPlayer) {
            this(serverPlayer.getUUID(), serverPlayer.getGameProfile(), true, serverPlayer.connection.latency(), serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName(), Optionull.map(serverPlayer.getChatSession(), RemoteChatSession::asData));
        }

        public Entry(UUID uUID, @Nullable GameProfile gameProfile, boolean bl, int n, GameType gameType, @Nullable Component component, @Nullable RemoteChatSession.Data data) {
            this.profileId = uUID;
            this.profile = gameProfile;
            this.listed = bl;
            this.latency = n;
            this.gameMode = gameType;
            this.displayName = component;
            this.chatSession = data;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "chatSession"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "chatSession"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "chatSession"}, this, object);
        }

        public UUID profileId() {
            return this.profileId;
        }

        @Nullable
        public GameProfile profile() {
            return this.profile;
        }

        public boolean listed() {
            return this.listed;
        }

        public int latency() {
            return this.latency;
        }

        public GameType gameMode() {
            return this.gameMode;
        }

        @Nullable
        public Component displayName() {
            return this.displayName;
        }

        @Nullable
        public RemoteChatSession.Data chatSession() {
            return this.chatSession;
        }
    }

    public static enum Action {
        ADD_PLAYER((entryBuilder, registryFriendlyByteBuf) -> {
            GameProfile gameProfile = new GameProfile(entryBuilder.profileId, registryFriendlyByteBuf.readUtf(16));
            gameProfile.getProperties().putAll((Multimap)ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(registryFriendlyByteBuf));
            entryBuilder.profile = gameProfile;
        }, (registryFriendlyByteBuf, entry) -> {
            GameProfile gameProfile = Objects.requireNonNull(entry.profile());
            registryFriendlyByteBuf.writeUtf(gameProfile.getName(), 16);
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(registryFriendlyByteBuf, gameProfile.getProperties());
        }),
        INITIALIZE_CHAT((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.chatSession = registryFriendlyByteBuf.readNullable(RemoteChatSession.Data::read);
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeNullable(entry.chatSession, RemoteChatSession.Data::write)),
        UPDATE_GAME_MODE((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.gameMode = GameType.byId(registryFriendlyByteBuf.readVarInt());
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.gameMode().getId())),
        UPDATE_LISTED((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.listed = registryFriendlyByteBuf.readBoolean();
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeBoolean(entry.listed())),
        UPDATE_LATENCY((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.latency = registryFriendlyByteBuf.readVarInt();
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.latency())),
        UPDATE_DISPLAY_NAME((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.displayName = FriendlyByteBuf.readNullable(registryFriendlyByteBuf, ComponentSerialization.TRUSTED_STREAM_CODEC);
        }, (registryFriendlyByteBuf, entry) -> FriendlyByteBuf.writeNullable(registryFriendlyByteBuf, entry.displayName(), ComponentSerialization.TRUSTED_STREAM_CODEC));

        final Reader reader;
        final Writer writer;

        private Action(Reader reader, Writer writer) {
            this.reader = reader;
            this.writer = writer;
        }

        public static interface Reader {
            public void read(EntryBuilder var1, RegistryFriendlyByteBuf var2);
        }

        public static interface Writer {
            public void write(RegistryFriendlyByteBuf var1, Entry var2);
        }
    }

    static class EntryBuilder {
        final UUID profileId;
        @Nullable
        GameProfile profile;
        boolean listed;
        int latency;
        GameType gameMode = GameType.DEFAULT_MODE;
        @Nullable
        Component displayName;
        @Nullable
        RemoteChatSession.Data chatSession;

        EntryBuilder(UUID uUID) {
            this.profileId = uUID;
        }

        Entry build() {
            return new Entry(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession);
        }
    }
}

