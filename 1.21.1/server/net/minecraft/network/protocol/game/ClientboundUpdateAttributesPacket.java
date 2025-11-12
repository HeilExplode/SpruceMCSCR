/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundUpdateAttributesPacket::getEntityId, AttributeSnapshot.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundUpdateAttributesPacket::getValues, ClientboundUpdateAttributesPacket::new);
    private final int entityId;
    private final List<AttributeSnapshot> attributes;

    public ClientboundUpdateAttributesPacket(int n, Collection<AttributeInstance> collection) {
        this.entityId = n;
        this.attributes = Lists.newArrayList();
        for (AttributeInstance attributeInstance : collection) {
            this.attributes.add(new AttributeSnapshot(attributeInstance.getAttribute(), attributeInstance.getBaseValue(), attributeInstance.getModifiers()));
        }
    }

    private ClientboundUpdateAttributesPacket(int n, List<AttributeSnapshot> list) {
        this.entityId = n;
        this.attributes = list;
    }

    @Override
    public PacketType<ClientboundUpdateAttributesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_ATTRIBUTES;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateAttributes(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public List<AttributeSnapshot> getValues() {
        return this.attributes;
    }

    public record AttributeSnapshot(Holder<Attribute> attribute, double base, Collection<AttributeModifier> modifiers) {
        public static final StreamCodec<ByteBuf, AttributeModifier> MODIFIER_STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, AttributeModifier::id, ByteBufCodecs.DOUBLE, AttributeModifier::amount, AttributeModifier.Operation.STREAM_CODEC, AttributeModifier::operation, AttributeModifier::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, AttributeSnapshot> STREAM_CODEC = StreamCodec.composite(Attribute.STREAM_CODEC, AttributeSnapshot::attribute, ByteBufCodecs.DOUBLE, AttributeSnapshot::base, MODIFIER_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), AttributeSnapshot::modifiers, AttributeSnapshot::new);
    }
}

