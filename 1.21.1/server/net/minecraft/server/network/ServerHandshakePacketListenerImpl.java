/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;

public class ServerHandshakePacketListenerImpl
implements ServerHandshakePacketListener {
    private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
    private final MinecraftServer server;
    private final Connection connection;

    public ServerHandshakePacketListenerImpl(MinecraftServer minecraftServer, Connection connection) {
        this.server = minecraftServer;
        this.connection = connection;
    }

    @Override
    public void handleIntention(ClientIntentionPacket clientIntentionPacket) {
        switch (clientIntentionPacket.intention()) {
            case LOGIN: {
                this.beginLogin(clientIntentionPacket, false);
                break;
            }
            case STATUS: {
                ServerStatus serverStatus = this.server.getStatus();
                this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
                if (this.server.repliesToStatus() && serverStatus != null) {
                    this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new ServerStatusPacketListenerImpl(serverStatus, this.connection));
                    break;
                }
                this.connection.disconnect(IGNORE_STATUS_REASON);
                break;
            }
            case TRANSFER: {
                if (!this.server.acceptsTransfers()) {
                    this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
                    MutableComponent mutableComponent = Component.translatable("multiplayer.disconnect.transfers_disabled");
                    this.connection.send(new ClientboundLoginDisconnectPacket(mutableComponent));
                    this.connection.disconnect(mutableComponent);
                    break;
                }
                this.beginLogin(clientIntentionPacket, true);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + String.valueOf((Object)clientIntentionPacket.intention()));
            }
        }
    }

    private void beginLogin(ClientIntentionPacket clientIntentionPacket, boolean bl) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        if (clientIntentionPacket.protocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
            MutableComponent mutableComponent = clientIntentionPacket.protocolVersion() < 754 ? Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName()) : Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
            this.connection.send(new ClientboundLoginDisconnectPacket(mutableComponent));
            this.connection.disconnect(mutableComponent);
        } else {
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(this.server, this.connection, bl));
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}

