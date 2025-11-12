/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.Queues
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.Bootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelOutboundHandler
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.ChannelPromise
 *  io.netty.channel.DefaultEventLoopGroup
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.channel.epoll.Epoll
 *  io.netty.channel.epoll.EpollEventLoopGroup
 *  io.netty.channel.epoll.EpollSocketChannel
 *  io.netty.channel.local.LocalChannel
 *  io.netty.channel.local.LocalServerChannel
 *  io.netty.channel.nio.NioEventLoopGroup
 *  io.netty.channel.socket.nio.NioSocketChannel
 *  io.netty.handler.flow.FlowControlHandler
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  io.netty.handler.timeout.TimeoutException
 *  io.netty.util.concurrent.GenericFutureListener
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 *  org.slf4j.Logger
 *  org.slf4j.Marker
 *  org.slf4j.MarkerFactory
 */
package net.minecraft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.CipherDecoder;
import net.minecraft.network.CipherEncoder;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.CompressionEncoder;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.MonitorFrameDecoder;
import net.minecraft.network.NoOpFrameDecoder;
import net.minecraft.network.NoOpFrameEncoder;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Connection
extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float AVERAGE_PACKETS_SMOOTHING = 0.75f;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker ROOT_MARKER = MarkerFactory.getMarker((String)"NETWORK");
    public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker((String)"NETWORK_PACKETS"), marker -> marker.add(ROOT_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker((String)"PACKET_RECEIVED"), marker -> marker.add(PACKET_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker((String)"PACKET_SENT"), marker -> marker.add(PACKET_MARKER));
    public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build()));
    public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build()));
    public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(() -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build()));
    private static final ProtocolInfo<ServerHandshakePacketListener> INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND;
    private final PacketFlow receiving;
    private volatile boolean sendLoginDisconnect = true;
    private final Queue<Consumer<Connection>> pendingActions = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    @Nullable
    private volatile PacketListener disconnectListener;
    @Nullable
    private volatile PacketListener packetListener;
    @Nullable
    private DisconnectionDetails disconnectionDetails;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;
    @Nullable
    private volatile DisconnectionDetails delayedDisconnect;
    @Nullable
    BandwidthDebugMonitor bandwidthDebugMonitor;

    public Connection(PacketFlow packetFlow) {
        this.receiving = packetFlow;
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        this.channel = channelHandlerContext.channel();
        this.address = this.channel.remoteAddress();
        if (this.delayedDisconnect != null) {
            this.disconnect(this.delayedDisconnect);
        }
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        this.disconnect(Component.translatable("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        if (throwable instanceof SkipPacketException) {
            LOGGER.debug("Skipping packet due to errors", throwable.getCause());
            return;
        }
        boolean bl = !this.handlingFault;
        this.handlingFault = true;
        if (!this.channel.isOpen()) {
            return;
        }
        if (throwable instanceof TimeoutException) {
            LOGGER.debug("Timeout", throwable);
            this.disconnect(Component.translatable("disconnect.timeout"));
        } else {
            MutableComponent mutableComponent = Component.translatable("disconnect.genericReason", "Internal Exception: " + String.valueOf(throwable));
            PacketListener packetListener = this.packetListener;
            DisconnectionDetails disconnectionDetails = packetListener != null ? packetListener.createDisconnectionInfo(mutableComponent, throwable) : new DisconnectionDetails(mutableComponent);
            if (bl) {
                LOGGER.debug("Failed to sent packet", throwable);
                if (this.getSending() == PacketFlow.CLIENTBOUND) {
                    Packet<ClientLoginPacketListener> packet = this.sendLoginDisconnect ? new ClientboundLoginDisconnectPacket(mutableComponent) : new ClientboundDisconnectPacket(mutableComponent);
                    this.send(packet, PacketSendListener.thenRun(() -> this.disconnect(disconnectionDetails)));
                } else {
                    this.disconnect(disconnectionDetails);
                }
                this.setReadOnly();
            } else {
                LOGGER.debug("Double fault", throwable);
                this.disconnect(disconnectionDetails);
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        if (!this.channel.isOpen()) {
            return;
        }
        PacketListener packetListener = this.packetListener;
        if (packetListener == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
        }
        if (packetListener.shouldHandleMessage(packet)) {
            try {
                Connection.genericsFtw(packet, packetListener);
            }
            catch (RunningOnDifferentThreadException runningOnDifferentThreadException) {
            }
            catch (RejectedExecutionException rejectedExecutionException) {
                this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
            }
            catch (ClassCastException classCastException) {
                LOGGER.error("Received {} that couldn't be processed", packet.getClass(), (Object)classCastException);
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
            }
            ++this.receivedPackets;
        }
    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetListener) {
        packet.handle(packetListener);
    }

    private void validateListener(ProtocolInfo<?> protocolInfo, PacketListener packetListener) {
        Validate.notNull((Object)packetListener, (String)"packetListener", (Object[])new Object[0]);
        PacketFlow packetFlow = packetListener.flow();
        if (packetFlow != this.receiving) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + String.valueOf((Object)this.receiving) + ", but listener is " + String.valueOf((Object)packetFlow));
        }
        ConnectionProtocol connectionProtocol = packetListener.protocol();
        if (protocolInfo.id() != connectionProtocol) {
            throw new IllegalStateException("Listener protocol (" + String.valueOf((Object)connectionProtocol) + ") does not match requested one " + String.valueOf(protocolInfo));
        }
    }

    private static void syncAfterConfigurationChange(ChannelFuture channelFuture) {
        try {
            channelFuture.syncUninterruptibly();
        }
        catch (Exception exception) {
            if (exception instanceof ClosedChannelException) {
                LOGGER.info("Connection closed during protocol change");
                return;
            }
            throw exception;
        }
    }

    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocolInfo, T t) {
        this.validateListener(protocolInfo, t);
        if (protocolInfo.flow() != this.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf((Object)protocolInfo.id()));
        }
        this.packetListener = t;
        this.disconnectListener = null;
        UnconfiguredPipelineHandler.InboundConfigurationTask inboundConfigurationTask = UnconfiguredPipelineHandler.setupInboundProtocol(protocolInfo);
        BundlerInfo bundlerInfo = protocolInfo.bundlerInfo();
        if (bundlerInfo != null) {
            PacketBundlePacker packetBundlePacker = new PacketBundlePacker(bundlerInfo);
            inboundConfigurationTask = inboundConfigurationTask.andThen(channelHandlerContext -> channelHandlerContext.pipeline().addAfter("decoder", "bundler", (ChannelHandler)packetBundlePacker));
        }
        Connection.syncAfterConfigurationChange(this.channel.writeAndFlush((Object)inboundConfigurationTask));
    }

    public void setupOutboundProtocol(ProtocolInfo<?> protocolInfo) {
        if (protocolInfo.flow() != this.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf((Object)protocolInfo.id()));
        }
        UnconfiguredPipelineHandler.OutboundConfigurationTask outboundConfigurationTask = UnconfiguredPipelineHandler.setupOutboundProtocol(protocolInfo);
        BundlerInfo bundlerInfo = protocolInfo.bundlerInfo();
        if (bundlerInfo != null) {
            PacketBundleUnpacker packetBundleUnpacker = new PacketBundleUnpacker(bundlerInfo);
            outboundConfigurationTask = outboundConfigurationTask.andThen(channelHandlerContext -> channelHandlerContext.pipeline().addAfter("encoder", "unbundler", (ChannelHandler)packetBundleUnpacker));
        }
        boolean bl = protocolInfo.id() == ConnectionProtocol.LOGIN;
        Connection.syncAfterConfigurationChange(this.channel.writeAndFlush((Object)outboundConfigurationTask.andThen(channelHandlerContext -> {
            this.sendLoginDisconnect = bl;
        })));
    }

    public void setListenerForServerboundHandshake(PacketListener packetListener) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        }
        if (this.receiving != PacketFlow.SERVERBOUND || packetListener.flow() != PacketFlow.SERVERBOUND || packetListener.protocol() != INITIAL_PROTOCOL.id()) {
            throw new IllegalStateException("Invalid initial listener");
        }
        this.packetListener = packetListener;
    }

    public void initiateServerboundStatusConnection(String string, int n, ClientStatusPacketListener clientStatusPacketListener) {
        this.initiateServerboundConnection(string, n, StatusProtocols.SERVERBOUND, StatusProtocols.CLIENTBOUND, clientStatusPacketListener, ClientIntent.STATUS);
    }

    public void initiateServerboundPlayConnection(String string, int n, ClientLoginPacketListener clientLoginPacketListener) {
        this.initiateServerboundConnection(string, n, LoginProtocols.SERVERBOUND, LoginProtocols.CLIENTBOUND, clientLoginPacketListener, ClientIntent.LOGIN);
    }

    public <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundPlayConnection(String string, int n, ProtocolInfo<S> protocolInfo, ProtocolInfo<C> protocolInfo2, C c, boolean bl) {
        this.initiateServerboundConnection(string, n, protocolInfo, protocolInfo2, c, bl ? ClientIntent.TRANSFER : ClientIntent.LOGIN);
    }

    private <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundConnection(String string, int n, ProtocolInfo<S> protocolInfo, ProtocolInfo<C> protocolInfo2, C c, ClientIntent clientIntent) {
        if (protocolInfo.id() != protocolInfo2.id()) {
            throw new IllegalStateException("Mismatched initial protocols");
        }
        this.disconnectListener = c;
        this.runOnceConnected(connection -> {
            this.setupInboundProtocol(protocolInfo2, c);
            connection.sendPacket(new ClientIntentionPacket(SharedConstants.getCurrentVersion().getProtocolVersion(), string, n, clientIntent), null, true);
            this.setupOutboundProtocol(protocolInfo);
        });
    }

    public void send(Packet<?> packet) {
        this.send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener) {
        this.send(packet, packetSendListener, true);
    }

    public void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket(packet, packetSendListener, bl);
        } else {
            this.pendingActions.add(connection -> connection.sendPacket(packet, packetSendListener, bl));
        }
    }

    public void runOnceConnected(Consumer<Connection> consumer) {
        if (this.isConnected()) {
            this.flushQueue();
            consumer.accept(this);
        } else {
            this.pendingActions.add(consumer);
        }
    }

    private void sendPacket(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
        ++this.sentPackets;
        if (this.channel.eventLoop().inEventLoop()) {
            this.doSendPacket(packet, packetSendListener, bl);
        } else {
            this.channel.eventLoop().execute(() -> this.doSendPacket(packet, packetSendListener, bl));
        }
    }

    private void doSendPacket(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
        ChannelFuture channelFuture;
        ChannelFuture channelFuture2 = channelFuture = bl ? this.channel.writeAndFlush(packet) : this.channel.write(packet);
        if (packetSendListener != null) {
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    packetSendListener.onSuccess();
                } else {
                    Packet<?> packet = packetSendListener.onFailure();
                    if (packet != null) {
                        ChannelFuture channelFuture = this.channel.writeAndFlush(packet);
                        channelFuture.addListener((GenericFutureListener)ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }
            });
        }
        channelFuture.addListener((GenericFutureListener)ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void flushChannel() {
        if (this.isConnected()) {
            this.flush();
        } else {
            this.pendingActions.add(Connection::flush);
        }
    }

    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void flushQueue() {
        if (this.channel == null || !this.channel.isOpen()) {
            return;
        }
        Queue<Consumer<Connection>> queue = this.pendingActions;
        synchronized (queue) {
            Consumer<Connection> consumer;
            while ((consumer = this.pendingActions.poll()) != null) {
                consumer.accept(this);
            }
        }
    }

    public void tick() {
        this.flushQueue();
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof TickablePacketListener) {
            TickablePacketListener tickablePacketListener = (TickablePacketListener)packetListener;
            tickablePacketListener.tick();
        }
        if (!this.isConnected() && !this.disconnectionHandled) {
            this.handleDisconnection();
        }
        if (this.channel != null) {
            this.channel.flush();
        }
        if (this.tickCount++ % 20 == 0) {
            this.tickSecond();
        }
        if (this.bandwidthDebugMonitor != null) {
            this.bandwidthDebugMonitor.tick();
        }
    }

    protected void tickSecond() {
        this.averageSentPackets = Mth.lerp(0.75f, this.sentPackets, this.averageSentPackets);
        this.averageReceivedPackets = Mth.lerp(0.75f, this.receivedPackets, this.averageReceivedPackets);
        this.sentPackets = 0;
        this.receivedPackets = 0;
    }

    public SocketAddress getRemoteAddress() {
        return this.address;
    }

    public String getLoggableAddress(boolean bl) {
        if (this.address == null) {
            return "local";
        }
        if (bl) {
            return this.address.toString();
        }
        return "IP hidden";
    }

    public void disconnect(Component component) {
        this.disconnect(new DisconnectionDetails(component));
    }

    public void disconnect(DisconnectionDetails disconnectionDetails) {
        if (this.channel == null) {
            this.delayedDisconnect = disconnectionDetails;
        }
        if (this.isConnected()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectionDetails = disconnectionDetails;
        }
    }

    public boolean isMemoryConnection() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public PacketFlow getReceiving() {
        return this.receiving;
    }

    public PacketFlow getSending() {
        return this.receiving.getOpposite();
    }

    public static Connection connectToServer(InetSocketAddress inetSocketAddress, boolean bl, @Nullable LocalSampleLogger localSampleLogger) {
        Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        if (localSampleLogger != null) {
            connection.setBandwidthLogger(localSampleLogger);
        }
        ChannelFuture channelFuture = Connection.connect(inetSocketAddress, bl, connection);
        channelFuture.syncUninterruptibly();
        return connection;
    }

    public static ChannelFuture connect(InetSocketAddress inetSocketAddress, boolean bl, final Connection connection) {
        EventLoopGroup eventLoopGroup;
        Class<NioSocketChannel> clazz;
        if (Epoll.isAvailable() && bl) {
            clazz = EpollSocketChannel.class;
            eventLoopGroup = (EventLoopGroup)NETWORK_EPOLL_WORKER_GROUP.get();
        } else {
            clazz = NioSocketChannel.class;
            eventLoopGroup = (EventLoopGroup)NETWORK_WORKER_GROUP.get();
        }
        return ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(eventLoopGroup)).handler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                Connection.configureSerialization(channelPipeline, PacketFlow.CLIENTBOUND, false, connection.bandwidthDebugMonitor);
                connection.configurePacketHandler(channelPipeline);
            }
        })).channel(clazz)).connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
    }

    private static String outboundHandlerName(boolean bl) {
        return bl ? "encoder" : "outbound_config";
    }

    private static String inboundHandlerName(boolean bl) {
        return bl ? "decoder" : "inbound_config";
    }

    public void configurePacketHandler(ChannelPipeline channelPipeline) {
        channelPipeline.addLast("hackfix", (ChannelHandler)new ChannelOutboundHandlerAdapter(this){

            public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
                super.write(channelHandlerContext, object, channelPromise);
            }
        }).addLast("packet_handler", (ChannelHandler)this);
    }

    public static void configureSerialization(ChannelPipeline channelPipeline, PacketFlow packetFlow, boolean bl, @Nullable BandwidthDebugMonitor bandwidthDebugMonitor) {
        PacketFlow packetFlow2 = packetFlow.getOpposite();
        boolean bl2 = packetFlow == PacketFlow.SERVERBOUND;
        boolean bl3 = packetFlow2 == PacketFlow.SERVERBOUND;
        channelPipeline.addLast("splitter", (ChannelHandler)Connection.createFrameDecoder(bandwidthDebugMonitor, bl)).addLast(new ChannelHandler[]{new FlowControlHandler()}).addLast(Connection.inboundHandlerName(bl2), bl2 ? new PacketDecoder<ServerHandshakePacketListener>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Inbound()).addLast("prepender", (ChannelHandler)Connection.createFrameEncoder(bl)).addLast(Connection.outboundHandlerName(bl3), bl3 ? new PacketEncoder<ServerHandshakePacketListener>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Outbound());
    }

    private static ChannelOutboundHandler createFrameEncoder(boolean bl) {
        return bl ? new NoOpFrameEncoder() : new Varint21LengthFieldPrepender();
    }

    private static ChannelInboundHandler createFrameDecoder(@Nullable BandwidthDebugMonitor bandwidthDebugMonitor, boolean bl) {
        if (!bl) {
            return new Varint21FrameDecoder(bandwidthDebugMonitor);
        }
        if (bandwidthDebugMonitor != null) {
            return new MonitorFrameDecoder(bandwidthDebugMonitor);
        }
        return new NoOpFrameDecoder();
    }

    public static void configureInMemoryPipeline(ChannelPipeline channelPipeline, PacketFlow packetFlow) {
        Connection.configureSerialization(channelPipeline, packetFlow, true, null);
    }

    public static Connection connectToLocalServer(SocketAddress socketAddress) {
        final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group((EventLoopGroup)LOCAL_WORKER_GROUP.get())).handler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel channel) {
                ChannelPipeline channelPipeline = channel.pipeline();
                Connection.configureInMemoryPipeline(channelPipeline, PacketFlow.CLIENTBOUND);
                connection.configurePacketHandler(channelPipeline);
            }
        })).channel(LocalChannel.class)).connect(socketAddress).syncUninterruptibly();
        return connection;
    }

    public void setEncryptionKey(Cipher cipher, Cipher cipher2) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", (ChannelHandler)new CipherDecoder(cipher));
        this.channel.pipeline().addBefore("prepender", "encrypt", (ChannelHandler)new CipherEncoder(cipher2));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isConnecting() {
        return this.channel == null;
    }

    @Nullable
    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public DisconnectionDetails getDisconnectionDetails() {
        return this.disconnectionDetails;
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }
    }

    public void setupCompression(int n, boolean bl) {
        if (n >= 0) {
            Object object;
            ChannelHandler channelHandler = this.channel.pipeline().get("decompress");
            if (channelHandler instanceof CompressionDecoder) {
                object = (CompressionDecoder)channelHandler;
                ((CompressionDecoder)((Object)object)).setThreshold(n, bl);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", (ChannelHandler)new CompressionDecoder(n, bl));
            }
            channelHandler = this.channel.pipeline().get("compress");
            if (channelHandler instanceof CompressionEncoder) {
                object = (CompressionEncoder)channelHandler;
                ((CompressionEncoder)((Object)object)).setThreshold(n);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", (ChannelHandler)new CompressionEncoder(n));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }
            if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void handleDisconnection() {
        PacketListener packetListener;
        if (this.channel == null || this.channel.isOpen()) {
            return;
        }
        if (this.disconnectionHandled) {
            LOGGER.warn("handleDisconnection() called twice");
            return;
        }
        this.disconnectionHandled = true;
        PacketListener packetListener2 = this.getPacketListener();
        PacketListener packetListener3 = packetListener = packetListener2 != null ? packetListener2 : this.disconnectListener;
        if (packetListener != null) {
            DisconnectionDetails disconnectionDetails = Objects.requireNonNullElseGet(this.getDisconnectionDetails(), () -> new DisconnectionDetails(Component.translatable("multiplayer.disconnect.generic")));
            packetListener.onDisconnect(disconnectionDetails);
        }
    }

    public float getAverageReceivedPackets() {
        return this.averageReceivedPackets;
    }

    public float getAverageSentPackets() {
        return this.averageSentPackets;
    }

    public void setBandwidthLogger(LocalSampleLogger localSampleLogger) {
        this.bandwidthDebugMonitor = new BandwidthDebugMonitor(localSampleLogger);
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        this.channelRead0(channelHandlerContext, (Packet)object);
    }
}

