/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.exceptions.AuthenticationUnavailableException
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl
implements ServerLoginPacketListener,
TickablePacketListener {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_LOGIN = 600;
    private final byte[] challenge;
    final MinecraftServer server;
    final Connection connection;
    private volatile State state = State.HELLO;
    private int tick;
    @Nullable
    String requestedUsername;
    @Nullable
    private GameProfile authenticatedProfile;
    private final String serverId = "";
    private final boolean transferred;

    public ServerLoginPacketListenerImpl(MinecraftServer minecraftServer, Connection connection, boolean bl) {
        this.server = minecraftServer;
        this.connection = connection;
        this.challenge = Ints.toByteArray((int)RandomSource.create().nextInt());
        this.transferred = bl;
    }

    @Override
    public void tick() {
        if (this.state == State.VERIFYING) {
            this.verifyLoginAndFinishConnectionSetup(Objects.requireNonNull(this.authenticatedProfile));
        }
        if (this.state == State.WAITING_FOR_DUPE_DISCONNECT && !this.isPlayerAlreadyInWorld(Objects.requireNonNull(this.authenticatedProfile))) {
            this.finishLoginAndWaitForClient(this.authenticatedProfile);
        }
        if (this.tick++ == 600) {
            this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
        }
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void disconnect(Component component) {
        try {
            LOGGER.info("Disconnecting {}: {}", (Object)this.getUserName(), (Object)component.getString());
            this.connection.send(new ClientboundLoginDisconnectPacket(component));
            this.connection.disconnect(component);
        }
        catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", (Throwable)exception);
        }
    }

    private boolean isPlayerAlreadyInWorld(GameProfile gameProfile) {
        return this.server.getPlayerList().getPlayer(gameProfile.getId()) != null;
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        LOGGER.info("{} lost connection: {}", (Object)this.getUserName(), (Object)disconnectionDetails.reason().getString());
    }

    public String getUserName() {
        String string = this.connection.getLoggableAddress(this.server.logIPs());
        if (this.requestedUsername != null) {
            return this.requestedUsername + " (" + string + ")";
        }
        return string;
    }

    @Override
    public void handleHello(ServerboundHelloPacket serverboundHelloPacket) {
        Validate.validState((this.state == State.HELLO ? 1 : 0) != 0, (String)"Unexpected hello packet", (Object[])new Object[0]);
        Validate.validState((boolean)StringUtil.isValidPlayerName(serverboundHelloPacket.name()), (String)"Invalid characters in username", (Object[])new Object[0]);
        this.requestedUsername = serverboundHelloPacket.name();
        GameProfile gameProfile = this.server.getSingleplayerProfile();
        if (gameProfile != null && this.requestedUsername.equalsIgnoreCase(gameProfile.getName())) {
            this.startClientVerification(gameProfile);
            return;
        }
        if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            this.state = State.KEY;
            this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge, true));
        } else {
            this.startClientVerification(UUIDUtil.createOfflineProfile(this.requestedUsername));
        }
    }

    void startClientVerification(GameProfile gameProfile) {
        this.authenticatedProfile = gameProfile;
        this.state = State.VERIFYING;
    }

    private void verifyLoginAndFinishConnectionSetup(GameProfile gameProfile) {
        PlayerList playerList = this.server.getPlayerList();
        Component component = playerList.canPlayerLogin(this.connection.getRemoteAddress(), gameProfile);
        if (component != null) {
            this.disconnect(component);
        } else {
            boolean bl;
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true)));
            }
            if (bl = playerList.disconnectAllPlayersWithProfile(gameProfile)) {
                this.state = State.WAITING_FOR_DUPE_DISCONNECT;
            } else {
                this.finishLoginAndWaitForClient(gameProfile);
            }
        }
    }

    private void finishLoginAndWaitForClient(GameProfile gameProfile) {
        this.state = State.PROTOCOL_SWITCHING;
        this.connection.send(new ClientboundGameProfilePacket(gameProfile, true));
    }

    @Override
    public void handleKey(ServerboundKeyPacket serverboundKeyPacket) {
        String string;
        Object object;
        Validate.validState((this.state == State.KEY ? 1 : 0) != 0, (String)"Unexpected key packet", (Object[])new Object[0]);
        try {
            object = this.server.getKeyPair().getPrivate();
            if (!serverboundKeyPacket.isChallengeValid(this.challenge, (PrivateKey)object)) {
                throw new IllegalStateException("Protocol error");
            }
            SecretKey secretKey = serverboundKeyPacket.getSecretKey((PrivateKey)object);
            Cipher cipher = Crypt.getCipher(2, secretKey);
            Cipher cipher2 = Crypt.getCipher(1, secretKey);
            string = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretKey)).toString(16);
            this.state = State.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher2);
        }
        catch (CryptException cryptException) {
            throw new IllegalStateException("Protocol error", cryptException);
        }
        object = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()){

            @Override
            public void run() {
                String string2 = Objects.requireNonNull(ServerLoginPacketListenerImpl.this.requestedUsername, "Player name not initialized");
                try {
                    ProfileResult profileResult = ServerLoginPacketListenerImpl.this.server.getSessionService().hasJoinedServer(string2, string, this.getAddress());
                    if (profileResult != null) {
                        GameProfile gameProfile = profileResult.profile();
                        LOGGER.info("UUID of player {} is {}", (Object)gameProfile.getName(), (Object)gameProfile.getId());
                        ServerLoginPacketListenerImpl.this.startClientVerification(gameProfile);
                    } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile(string2));
                    } else {
                        ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        LOGGER.error("Username '{}' tried to join with an invalid session", (Object)string2);
                    }
                }
                catch (AuthenticationUnavailableException authenticationUnavailableException) {
                    if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                        LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile(string2));
                    }
                    ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                    LOGGER.error("Couldn't verify username because servers are unavailable");
                }
            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketAddress = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
                return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && socketAddress instanceof InetSocketAddress ? ((InetSocketAddress)socketAddress).getAddress() : null;
            }
        };
        ((Thread)object).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        ((Thread)object).start();
    }

    @Override
    public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket serverboundCustomQueryAnswerPacket) {
        this.disconnect(ServerCommonPacketListenerImpl.DISCONNECT_UNEXPECTED_QUERY);
    }

    @Override
    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundLoginAcknowledgedPacket) {
        Validate.validState((this.state == State.PROTOCOL_SWITCHING ? 1 : 0) != 0, (String)"Unexpected login acknowledgement packet", (Object[])new Object[0]);
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(Objects.requireNonNull(this.authenticatedProfile), this.transferred);
        ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = new ServerConfigurationPacketListenerImpl(this.server, this.connection, commonListenerCookie);
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, serverConfigurationPacketListenerImpl);
        serverConfigurationPacketListenerImpl.startConfiguration();
        this.state = State.ACCEPTED;
    }

    @Override
    public void fillListenerSpecificCrashDetails(CrashReport crashReport, CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Login phase", () -> this.state.toString());
    }

    @Override
    public void handleCookieResponse(ServerboundCookieResponsePacket serverboundCookieResponsePacket) {
        this.disconnect(ServerCommonPacketListenerImpl.DISCONNECT_UNEXPECTED_QUERY);
    }

    static enum State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        VERIFYING,
        WAITING_FOR_DUPE_DISCONNECT,
        PROTOCOL_SWITCHING,
        ACCEPTED;

    }
}

