/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.MessageToMessageDecoder
 *  javax.annotation.Nullable
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundlePacker
extends MessageToMessageDecoder<Packet<?>> {
    private final BundlerInfo bundlerInfo;
    @Nullable
    private BundlerInfo.Bundler currentBundler;

    public PacketBundlePacker(BundlerInfo bundlerInfo) {
        this.bundlerInfo = bundlerInfo;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
        if (this.currentBundler != null) {
            PacketBundlePacker.verifyNonTerminalPacket(packet);
            Packet<?> packet2 = this.currentBundler.addPacket(packet);
            if (packet2 != null) {
                this.currentBundler = null;
                list.add(packet2);
            }
        } else {
            BundlerInfo.Bundler bundler = this.bundlerInfo.startPacketBundling(packet);
            if (bundler != null) {
                PacketBundlePacker.verifyNonTerminalPacket(packet);
                this.currentBundler = bundler;
            } else {
                list.add(packet);
                if (packet.isTerminal()) {
                    channelHandlerContext.pipeline().remove(channelHandlerContext.name());
                }
            }
        }
    }

    private static void verifyNonTerminalPacket(Packet<?> packet) {
        if (packet.isTerminal()) {
            throw new DecoderException("Terminal message received in bundle");
        }
    }

    protected /* synthetic */ void decode(ChannelHandlerContext channelHandlerContext, Object object, List list) throws Exception {
        this.decode(channelHandlerContext, (Packet)object, (List<Object>)list);
    }
}

