/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBossEventPacket> STREAM_CODEC = Packet.codec(ClientboundBossEventPacket::write, ClientboundBossEventPacket::new);
    private static final int FLAG_DARKEN = 1;
    private static final int FLAG_MUSIC = 2;
    private static final int FLAG_FOG = 4;
    private final UUID id;
    private final Operation operation;
    static final Operation REMOVE_OPERATION = new Operation(){

        @Override
        public OperationType getType() {
            return OperationType.REMOVE;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.remove(uUID);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        }
    };

    private ClientboundBossEventPacket(UUID uUID, Operation operation) {
        this.id = uUID;
        this.operation = operation;
    }

    private ClientboundBossEventPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.id = registryFriendlyByteBuf.readUUID();
        OperationType operationType = registryFriendlyByteBuf.readEnum(OperationType.class);
        this.operation = operationType.reader.decode(registryFriendlyByteBuf);
    }

    public static ClientboundBossEventPacket createAddPacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new AddOperation(bossEvent));
    }

    public static ClientboundBossEventPacket createRemovePacket(UUID uUID) {
        return new ClientboundBossEventPacket(uUID, REMOVE_OPERATION);
    }

    public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdateProgressOperation(bossEvent.getProgress()));
    }

    public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdateNameOperation(bossEvent.getName()));
    }

    public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdateStyleOperation(bossEvent.getColor(), bossEvent.getOverlay()));
    }

    public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdatePropertiesOperation(bossEvent.shouldDarkenScreen(), bossEvent.shouldPlayBossMusic(), bossEvent.shouldCreateWorldFog()));
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeUUID(this.id);
        registryFriendlyByteBuf.writeEnum(this.operation.getType());
        this.operation.write(registryFriendlyByteBuf);
    }

    static int encodeProperties(boolean bl, boolean bl2, boolean bl3) {
        int n = 0;
        if (bl) {
            n |= 1;
        }
        if (bl2) {
            n |= 2;
        }
        if (bl3) {
            n |= 4;
        }
        return n;
    }

    @Override
    public PacketType<ClientboundBossEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BOSS_EVENT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBossUpdate(this);
    }

    public void dispatch(Handler handler) {
        this.operation.dispatch(this.id, handler);
    }

    static interface Operation {
        public OperationType getType();

        public void dispatch(UUID var1, Handler var2);

        public void write(RegistryFriendlyByteBuf var1);
    }

    static enum OperationType {
        ADD(AddOperation::new),
        REMOVE(registryFriendlyByteBuf -> REMOVE_OPERATION),
        UPDATE_PROGRESS(UpdateProgressOperation::new),
        UPDATE_NAME(UpdateNameOperation::new),
        UPDATE_STYLE(UpdateStyleOperation::new),
        UPDATE_PROPERTIES(UpdatePropertiesOperation::new);

        final StreamDecoder<RegistryFriendlyByteBuf, Operation> reader;

        private OperationType(StreamDecoder<RegistryFriendlyByteBuf, Operation> streamDecoder) {
            this.reader = streamDecoder;
        }
    }

    static class AddOperation
    implements Operation {
        private final Component name;
        private final float progress;
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        AddOperation(BossEvent bossEvent) {
            this.name = bossEvent.getName();
            this.progress = bossEvent.getProgress();
            this.color = bossEvent.getColor();
            this.overlay = bossEvent.getOverlay();
            this.darkenScreen = bossEvent.shouldDarkenScreen();
            this.playMusic = bossEvent.shouldPlayBossMusic();
            this.createWorldFog = bossEvent.shouldCreateWorldFog();
        }

        private AddOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this.name = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
            this.progress = registryFriendlyByteBuf.readFloat();
            this.color = registryFriendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
            this.overlay = registryFriendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
            short s = registryFriendlyByteBuf.readUnsignedByte();
            this.darkenScreen = (s & 1) > 0;
            this.playMusic = (s & 2) > 0;
            this.createWorldFog = (s & 4) > 0;
        }

        @Override
        public OperationType getType() {
            return OperationType.ADD;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.add(uUID, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.name);
            registryFriendlyByteBuf.writeFloat(this.progress);
            registryFriendlyByteBuf.writeEnum(this.color);
            registryFriendlyByteBuf.writeEnum(this.overlay);
            registryFriendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    record UpdateProgressOperation(float progress) implements Operation
    {
        private UpdateProgressOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this(registryFriendlyByteBuf.readFloat());
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_PROGRESS;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateProgress(uUID, this.progress);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeFloat(this.progress);
        }
    }

    record UpdateNameOperation(Component name) implements Operation
    {
        private UpdateNameOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this((Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf));
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_NAME;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateName(uUID, this.name);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.name);
        }
    }

    static class UpdateStyleOperation
    implements Operation {
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;

        UpdateStyleOperation(BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
            this.color = bossBarColor;
            this.overlay = bossBarOverlay;
        }

        private UpdateStyleOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this.color = registryFriendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
            this.overlay = registryFriendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_STYLE;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateStyle(uUID, this.color, this.overlay);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeEnum(this.color);
            registryFriendlyByteBuf.writeEnum(this.overlay);
        }
    }

    static class UpdatePropertiesOperation
    implements Operation {
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        UpdatePropertiesOperation(boolean bl, boolean bl2, boolean bl3) {
            this.darkenScreen = bl;
            this.playMusic = bl2;
            this.createWorldFog = bl3;
        }

        private UpdatePropertiesOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            short s = registryFriendlyByteBuf.readUnsignedByte();
            this.darkenScreen = (s & 1) > 0;
            this.playMusic = (s & 2) > 0;
            this.createWorldFog = (s & 4) > 0;
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_PROPERTIES;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateProperties(uUID, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    public static interface Handler {
        default public void add(UUID uUID, Component component, float f, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl, boolean bl2, boolean bl3) {
        }

        default public void remove(UUID uUID) {
        }

        default public void updateProgress(UUID uUID, float f) {
        }

        default public void updateName(UUID uUID, Component component) {
        }

        default public void updateStyle(UUID uUID, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
        }

        default public void updateProperties(UUID uUID, boolean bl, boolean bl2, boolean bl3) {
        }
    }
}

