/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundExplodePacket> STREAM_CODEC = Packet.codec(ClientboundExplodePacket::write, ClientboundExplodePacket::new);
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ParticleOptions smallExplosionParticles;
    private final ParticleOptions largeExplosionParticles;
    private final Explosion.BlockInteraction blockInteraction;
    private final Holder<SoundEvent> explosionSound;

    public ClientboundExplodePacket(double d, double d2, double d3, float f, List<BlockPos> list, @Nullable Vec3 vec3, Explosion.BlockInteraction blockInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.power = f;
        this.toBlow = Lists.newArrayList(list);
        this.explosionSound = holder;
        if (vec3 != null) {
            this.knockbackX = (float)vec3.x;
            this.knockbackY = (float)vec3.y;
            this.knockbackZ = (float)vec3.z;
        } else {
            this.knockbackX = 0.0f;
            this.knockbackY = 0.0f;
            this.knockbackZ = 0.0f;
        }
        this.blockInteraction = blockInteraction;
        this.smallExplosionParticles = particleOptions;
        this.largeExplosionParticles = particleOptions2;
    }

    private ClientboundExplodePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.x = registryFriendlyByteBuf.readDouble();
        this.y = registryFriendlyByteBuf.readDouble();
        this.z = registryFriendlyByteBuf.readDouble();
        this.power = registryFriendlyByteBuf.readFloat();
        int n = Mth.floor(this.x);
        int n2 = Mth.floor(this.y);
        int n3 = Mth.floor(this.z);
        this.toBlow = registryFriendlyByteBuf.readList(friendlyByteBuf -> {
            int n4 = friendlyByteBuf.readByte() + n;
            int n5 = friendlyByteBuf.readByte() + n2;
            int n6 = friendlyByteBuf.readByte() + n3;
            return new BlockPos(n4, n5, n6);
        });
        this.knockbackX = registryFriendlyByteBuf.readFloat();
        this.knockbackY = registryFriendlyByteBuf.readFloat();
        this.knockbackZ = registryFriendlyByteBuf.readFloat();
        this.blockInteraction = registryFriendlyByteBuf.readEnum(Explosion.BlockInteraction.class);
        this.smallExplosionParticles = (ParticleOptions)ParticleTypes.STREAM_CODEC.decode(registryFriendlyByteBuf);
        this.largeExplosionParticles = (ParticleOptions)ParticleTypes.STREAM_CODEC.decode(registryFriendlyByteBuf);
        this.explosionSound = (Holder)SoundEvent.STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeDouble(this.x);
        registryFriendlyByteBuf.writeDouble(this.y);
        registryFriendlyByteBuf.writeDouble(this.z);
        registryFriendlyByteBuf.writeFloat(this.power);
        int n = Mth.floor(this.x);
        int n2 = Mth.floor(this.y);
        int n3 = Mth.floor(this.z);
        registryFriendlyByteBuf.writeCollection(this.toBlow, (friendlyByteBuf, blockPos) -> {
            int n4 = blockPos.getX() - n;
            int n5 = blockPos.getY() - n2;
            int n6 = blockPos.getZ() - n3;
            friendlyByteBuf.writeByte(n4);
            friendlyByteBuf.writeByte(n5);
            friendlyByteBuf.writeByte(n6);
        });
        registryFriendlyByteBuf.writeFloat(this.knockbackX);
        registryFriendlyByteBuf.writeFloat(this.knockbackY);
        registryFriendlyByteBuf.writeFloat(this.knockbackZ);
        registryFriendlyByteBuf.writeEnum(this.blockInteraction);
        ParticleTypes.STREAM_CODEC.encode(registryFriendlyByteBuf, this.smallExplosionParticles);
        ParticleTypes.STREAM_CODEC.encode(registryFriendlyByteBuf, this.largeExplosionParticles);
        SoundEvent.STREAM_CODEC.encode(registryFriendlyByteBuf, this.explosionSound);
    }

    @Override
    public PacketType<ClientboundExplodePacket> type() {
        return GamePacketTypes.CLIENTBOUND_EXPLODE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public Holder<SoundEvent> getExplosionSound() {
        return this.explosionSound;
    }
}

