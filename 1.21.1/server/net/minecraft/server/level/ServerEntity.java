/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private static final double TOLERANCE_LEVEL_POSITION = 7.62939453125E-6;
    public static final int FORCED_POS_UPDATE_PERIOD = 60;
    private static final int FORCED_TELEPORT_PERIOD = 400;
    private final ServerLevel level;
    private final Entity entity;
    private final int updateInterval;
    private final boolean trackDelta;
    private final Consumer<Packet<?>> broadcast;
    private final VecDeltaCodec positionCodec = new VecDeltaCodec();
    private int lastSentYRot;
    private int lastSentXRot;
    private int lastSentYHeadRot;
    private Vec3 lastSentMovement;
    private int tickCount;
    private int teleportDelay;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean wasRiding;
    private boolean wasOnGround;
    @Nullable
    private List<SynchedEntityData.DataValue<?>> trackedDataValues;

    public ServerEntity(ServerLevel serverLevel, Entity entity, int n, boolean bl, Consumer<Packet<?>> consumer) {
        this.level = serverLevel;
        this.broadcast = consumer;
        this.entity = entity;
        this.updateInterval = n;
        this.trackDelta = bl;
        this.positionCodec.setBase(entity.trackingPosition());
        this.lastSentMovement = entity.getDeltaMovement();
        this.lastSentYRot = Mth.floor(entity.getYRot() * 256.0f / 360.0f);
        this.lastSentXRot = Mth.floor(entity.getXRot() * 256.0f / 360.0f);
        this.lastSentYHeadRot = Mth.floor(entity.getYHeadRot() * 256.0f / 360.0f);
        this.wasOnGround = entity.onGround();
        this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
    }

    public void sendChanges() {
        Object object;
        Object object2;
        List<Entity> list = this.entity.getPassengers();
        if (!list.equals(this.lastPassengers)) {
            this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
            ServerEntity.removedPassengers(list, this.lastPassengers).forEach(entity -> {
                if (entity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    serverPlayer.connection.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
                }
            });
            this.lastPassengers = list;
        }
        if ((object2 = this.entity) instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)object2;
            if (this.tickCount % 10 == 0) {
                MapItemSavedData mapItemSavedData;
                object2 = itemFrame.getItem();
                if (((ItemStack)object2).getItem() instanceof MapItem && (mapItemSavedData = MapItem.getSavedData((MapId)(object = object2.get(DataComponents.MAP_ID)), (Level)this.level)) != null) {
                    for (ServerPlayer serverPlayer : this.level.players()) {
                        mapItemSavedData.tickCarriedBy(serverPlayer, (ItemStack)object2);
                        Packet<?> packet = mapItemSavedData.getUpdatePacket((MapId)object, serverPlayer);
                        if (packet == null) continue;
                        serverPlayer.connection.send(packet);
                    }
                }
                this.sendDirtyEntityData();
            }
        }
        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            int n;
            if (this.entity.isPassenger()) {
                boolean bl;
                n = Mth.floor(this.entity.getYRot() * 256.0f / 360.0f);
                int n2 = Mth.floor(this.entity.getXRot() * 256.0f / 360.0f);
                boolean bl2 = bl = Math.abs(n - this.lastSentYRot) >= 1 || Math.abs(n2 - this.lastSentXRot) >= 1;
                if (bl) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)n, (byte)n2, this.entity.onGround()));
                    this.lastSentYRot = n;
                    this.lastSentXRot = n2;
                }
                this.positionCodec.setBase(this.entity.trackingPosition());
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                Vec3 vec3;
                double d;
                boolean bl;
                ++this.teleportDelay;
                n = Mth.floor(this.entity.getYRot() * 256.0f / 360.0f);
                int n3 = Mth.floor(this.entity.getXRot() * 256.0f / 360.0f);
                object = this.entity.trackingPosition();
                boolean bl3 = this.positionCodec.delta((Vec3)object).lengthSqr() >= 7.62939453125E-6;
                Object object3 = null;
                boolean bl4 = bl3 || this.tickCount % 60 == 0;
                boolean bl5 = Math.abs(n - this.lastSentYRot) >= 1 || Math.abs(n3 - this.lastSentXRot) >= 1;
                boolean bl6 = false;
                boolean bl7 = false;
                long l = this.positionCodec.encodeX((Vec3)object);
                long l2 = this.positionCodec.encodeY((Vec3)object);
                long l3 = this.positionCodec.encodeZ((Vec3)object);
                boolean bl8 = bl = l < -32768L || l > 32767L || l2 < -32768L || l2 > 32767L || l3 < -32768L || l3 > 32767L;
                if (bl || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
                    this.wasOnGround = this.entity.onGround();
                    this.teleportDelay = 0;
                    object3 = new ClientboundTeleportEntityPacket(this.entity);
                    bl6 = true;
                    bl7 = true;
                } else if (bl4 && bl5 || this.entity instanceof AbstractArrow) {
                    object3 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)l, (short)l2, (short)l3, (byte)n, (byte)n3, this.entity.onGround());
                    bl6 = true;
                    bl7 = true;
                } else if (bl4) {
                    object3 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)l, (short)l2, (short)l3, this.entity.onGround());
                    bl6 = true;
                } else if (bl5) {
                    object3 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)n, (byte)n3, this.entity.onGround());
                    bl7 = true;
                }
                if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0 && ((d = (vec3 = this.entity.getDeltaMovement()).distanceToSqr(this.lastSentMovement)) > 1.0E-7 || d > 0.0 && vec3.lengthSqr() == 0.0)) {
                    this.lastSentMovement = vec3;
                    Entity entity2 = this.entity;
                    if (entity2 instanceof AbstractHurtingProjectile) {
                        AbstractHurtingProjectile abstractHurtingProjectile = (AbstractHurtingProjectile)entity2;
                        this.broadcast.accept(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)List.of(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement), new ClientboundProjectilePowerPacket(abstractHurtingProjectile.getId(), abstractHurtingProjectile.accelerationPower))));
                    } else {
                        this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
                    }
                }
                if (object3 != null) {
                    this.broadcast.accept((Packet<?>)object3);
                }
                this.sendDirtyEntityData();
                if (bl6) {
                    this.positionCodec.setBase((Vec3)object);
                }
                if (bl7) {
                    this.lastSentYRot = n;
                    this.lastSentXRot = n3;
                }
                this.wasRiding = false;
            }
            n = Mth.floor(this.entity.getYHeadRot() * 256.0f / 360.0f);
            if (Math.abs(n - this.lastSentYHeadRot) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)n));
                this.lastSentYHeadRot = n;
            }
            this.entity.hasImpulse = false;
        }
        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.entity.hurtMarked = false;
            this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
        }
    }

    private static Stream<Entity> removedPassengers(List<Entity> list, List<Entity> list2) {
        return list2.stream().filter(entity -> !list.contains(entity));
    }

    public void removePairing(ServerPlayer serverPlayer) {
        this.entity.stopSeenByPlayer(serverPlayer);
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void addPairing(ServerPlayer serverPlayer) {
        ArrayList<Packet<? super ClientGamePacketListener>> arrayList = new ArrayList<Packet<? super ClientGamePacketListener>>();
        this.sendPairingData(serverPlayer, arrayList::add);
        serverPlayer.connection.send(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)arrayList));
        this.entity.startSeenByPlayer(serverPlayer);
    }

    public void sendPairingData(ServerPlayer serverPlayer, Consumer<Packet<ClientGamePacketListener>> consumer) {
        Object object;
        Object object2;
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket(this);
        consumer.accept(packet);
        if (this.trackedDataValues != null) {
            consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
        }
        boolean bl = this.trackDelta;
        if (this.entity instanceof LivingEntity) {
            object2 = ((LivingEntity)this.entity).getAttributes().getSyncableAttributes();
            if (!object2.isEmpty()) {
                consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), (Collection<AttributeInstance>)object2));
            }
            if (((LivingEntity)this.entity).isFallFlying()) {
                bl = true;
            }
        }
        if (bl && !(this.entity instanceof LivingEntity)) {
            consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
        }
        if (this.entity instanceof LivingEntity) {
            object2 = Lists.newArrayList();
            object = EquipmentSlot.values();
            int n = ((EquipmentSlot[])object).length;
            for (int i = 0; i < n; ++i) {
                EquipmentSlot equipmentSlot = object[i];
                ItemStack itemStack = ((LivingEntity)this.entity).getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                object2.add(Pair.of((Object)equipmentSlot, (Object)itemStack.copy()));
            }
            if (!object2.isEmpty()) {
                consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), (List<Pair<EquipmentSlot, ItemStack>>)object2));
            }
        }
        if (!this.entity.getPassengers().isEmpty()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity.isPassenger()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }
        if ((object = this.entity) instanceof Leashable && (object2 = (Leashable)object).isLeashed()) {
            consumer.accept(new ClientboundSetEntityLinkPacket(this.entity, object2.getLeashHolder()));
        }
    }

    public Vec3 getPositionBase() {
        return this.positionCodec.getBase();
    }

    public Vec3 getLastSentMovement() {
        return this.lastSentMovement;
    }

    public float getLastSentXRot() {
        return (float)(this.lastSentXRot * 360) / 256.0f;
    }

    public float getLastSentYRot() {
        return (float)(this.lastSentYRot * 360) / 256.0f;
    }

    public float getLastSentYHeadRot() {
        return (float)(this.lastSentYHeadRot * 360) / 256.0f;
    }

    private void sendDirtyEntityData() {
        SynchedEntityData synchedEntityData = this.entity.getEntityData();
        List<SynchedEntityData.DataValue<?>> list = synchedEntityData.packDirty();
        if (list != null) {
            this.trackedDataValues = synchedEntityData.getNonDefaultValues();
            this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
        }
        if (this.entity instanceof LivingEntity) {
            Set<AttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getAttributesToSync();
            if (!set.isEmpty()) {
                this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), set));
            }
            set.clear();
        }
    }

    private void broadcastAndSend(Packet<?> packet) {
        this.broadcast.accept(packet);
        if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(packet);
        }
    }
}

