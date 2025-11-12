/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelDuplexHandler
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandler
 *  io.netty.channel.ChannelOutboundHandler
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPromise
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  io.netty.util.ReferenceCountUtil
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;

public class UnconfiguredPipelineHandler {
    public static <T extends PacketListener> InboundConfigurationTask setupInboundProtocol(ProtocolInfo<T> protocolInfo) {
        return UnconfiguredPipelineHandler.setupInboundHandler(new PacketDecoder<T>(protocolInfo));
    }

    private static InboundConfigurationTask setupInboundHandler(ChannelInboundHandler channelInboundHandler) {
        return channelHandlerContext -> {
            channelHandlerContext.pipeline().replace(channelHandlerContext.name(), "decoder", (ChannelHandler)channelInboundHandler);
            channelHandlerContext.channel().config().setAutoRead(true);
        };
    }

    public static <T extends PacketListener> OutboundConfigurationTask setupOutboundProtocol(ProtocolInfo<T> protocolInfo) {
        return UnconfiguredPipelineHandler.setupOutboundHandler(new PacketEncoder<T>(protocolInfo));
    }

    private static OutboundConfigurationTask setupOutboundHandler(ChannelOutboundHandler channelOutboundHandler) {
        return channelHandlerContext -> channelHandlerContext.pipeline().replace(channelHandlerContext.name(), "encoder", (ChannelHandler)channelOutboundHandler);
    }

    @FunctionalInterface
    public static interface InboundConfigurationTask {
        public void run(ChannelHandlerContext var1);

        default public InboundConfigurationTask andThen(InboundConfigurationTask inboundConfigurationTask) {
            return channelHandlerContext -> {
                this.run(channelHandlerContext);
                inboundConfigurationTask.run(channelHandlerContext);
            };
        }
    }

    @FunctionalInterface
    public static interface OutboundConfigurationTask {
        public void run(ChannelHandlerContext var1);

        default public OutboundConfigurationTask andThen(OutboundConfigurationTask outboundConfigurationTask) {
            return channelHandlerContext -> {
                this.run(channelHandlerContext);
                outboundConfigurationTask.run(channelHandlerContext);
            };
        }
    }

    public static class Outbound
    extends ChannelOutboundHandlerAdapter {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
            if (object instanceof Packet) {
                ReferenceCountUtil.release((Object)object);
                throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + String.valueOf(object));
            }
            if (object instanceof OutboundConfigurationTask) {
                OutboundConfigurationTask outboundConfigurationTask = (OutboundConfigurationTask)object;
                try {
                    outboundConfigurationTask.run(channelHandlerContext);
                }
                finally {
                    ReferenceCountUtil.release((Object)object);
                }
                channelPromise.setSuccess();
            } else {
                channelHandlerContext.write(object, channelPromise);
            }
        }
    }

    public static class Inbound
    extends ChannelDuplexHandler {
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
            if (object instanceof ByteBuf || object instanceof Packet) {
                ReferenceCountUtil.release((Object)object);
                throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + String.valueOf(object));
            }
            channelHandlerContext.fireChannelRead(object);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
            if (object instanceof InboundConfigurationTask) {
                InboundConfigurationTask inboundConfigurationTask = (InboundConfigurationTask)object;
                try {
                    inboundConfigurationTask.run(channelHandlerContext);
                }
                finally {
                    ReferenceCountUtil.release((Object)object);
                }
                channelPromise.setSuccess();
            } else {
                channelHandlerContext.write(object, channelPromise);
            }
        }
    }
}

