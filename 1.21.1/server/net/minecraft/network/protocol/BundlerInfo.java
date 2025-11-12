/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public interface BundlerInfo {
    public static final int BUNDLE_SIZE_LIMIT = 4096;

    public static <T extends PacketListener, P extends BundlePacket<? super T>> BundlerInfo createForPacket(final PacketType<P> packetType, final Function<Iterable<Packet<? super T>>, P> function, final BundleDelimiterPacket<? super T> bundleDelimiterPacket) {
        return new BundlerInfo(){

            @Override
            public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.type() == packetType) {
                    BundlePacket bundlePacket = (BundlePacket)packet;
                    consumer.accept(bundleDelimiterPacket);
                    bundlePacket.subPackets().forEach(consumer);
                    consumer.accept(bundleDelimiterPacket);
                } else {
                    consumer.accept(packet);
                }
            }

            @Override
            @Nullable
            public Bundler startPacketBundling(Packet<?> packet) {
                if (packet == bundleDelimiterPacket) {
                    return new Bundler(){
                        private final List<Packet<? super T>> bundlePackets = new ArrayList();

                        @Override
                        @Nullable
                        public Packet<?> addPacket(Packet<?> packet) {
                            if (packet == bundleDelimiterPacket) {
                                return (Packet)function.apply(this.bundlePackets);
                            }
                            Packet<?> packet2 = packet;
                            if (this.bundlePackets.size() >= 4096) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            }
                            this.bundlePackets.add(packet2);
                            return null;
                        }
                    };
                }
                return null;
            }
        };
    }

    public void unbundlePacket(Packet<?> var1, Consumer<Packet<?>> var2);

    @Nullable
    public Bundler startPacketBundling(Packet<?> var1);

    public static interface Bundler {
        @Nullable
        public Packet<?> addPacket(Packet<?> var1);
    }
}

