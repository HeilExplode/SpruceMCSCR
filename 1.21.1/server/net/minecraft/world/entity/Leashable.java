/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface Leashable {
    public static final String LEASH_TAG = "leash";
    public static final double LEASH_TOO_FAR_DIST = 10.0;
    public static final double LEASH_ELASTIC_DIST = 6.0;

    @Nullable
    public LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canHaveALeashAttachedToIt() {
        return this.canBeLeashed() && !this.isLeashed();
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setDelayedLeashHolderId(int n) {
        this.setLeashData(new LeashData(n));
        Leashable.dropLeash((Entity)((Object)this), false, false);
    }

    @Nullable
    default public LeashData readLeashData(CompoundTag compoundTag) {
        Either either;
        if (compoundTag.contains(LEASH_TAG, 10)) {
            return new LeashData((Either<UUID, BlockPos>)Either.left((Object)compoundTag.getCompound(LEASH_TAG).getUUID("UUID")));
        }
        if (compoundTag.contains(LEASH_TAG, 11) && (either = (Either)NbtUtils.readBlockPos(compoundTag, LEASH_TAG).map(Either::right).orElse(null)) != null) {
            return new LeashData((Either<UUID, BlockPos>)either);
        }
        return null;
    }

    default public void writeLeashData(CompoundTag compoundTag, @Nullable LeashData leashData) {
        if (leashData == null) {
            return;
        }
        Either either = leashData.delayedLeashInfo;
        Entity entity = leashData.leashHolder;
        if (entity instanceof LeashFenceKnotEntity) {
            LeashFenceKnotEntity leashFenceKnotEntity = (LeashFenceKnotEntity)entity;
            either = Either.right((Object)leashFenceKnotEntity.getPos());
        } else if (leashData.leashHolder != null) {
            either = Either.left((Object)leashData.leashHolder.getUUID());
        }
        if (either == null) {
            return;
        }
        compoundTag.put(LEASH_TAG, (Tag)either.map(uUID -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", (UUID)uUID);
            return compoundTag;
        }, NbtUtils::writeBlockPos));
    }

    private static <E extends Entity> void restoreLeashFromSave(E e, LeashData leashData) {
        Object object;
        if (leashData.delayedLeashInfo != null && (object = e.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            object = leashData.delayedLeashInfo.left();
            Optional optional = leashData.delayedLeashInfo.right();
            if (((Optional)object).isPresent()) {
                Entity entity = serverLevel.getEntity((UUID)((Optional)object).get());
                if (entity != null) {
                    Leashable.setLeashedTo(e, entity, true);
                    return;
                }
            } else if (optional.isPresent()) {
                Leashable.setLeashedTo(e, LeashFenceKnotEntity.getOrCreateKnot(serverLevel, (BlockPos)optional.get()), true);
                return;
            }
            if (e.tickCount > 100) {
                e.spawnAtLocation(Items.LEAD);
                ((Leashable)((Object)e)).setLeashData(null);
            }
        }
    }

    default public void dropLeash(boolean bl, boolean bl2) {
        Leashable.dropLeash((Entity)((Object)this), bl, bl2);
    }

    private static <E extends Entity> void dropLeash(E e, boolean bl, boolean bl2) {
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData != null && leashData.leashHolder != null) {
            Level level;
            ((Leashable)((Object)e)).setLeashData(null);
            if (!e.level().isClientSide && bl2) {
                e.spawnAtLocation(Items.LEAD);
            }
            if (bl && (level = e.level()) instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.getChunkSource().broadcast(e, new ClientboundSetEntityLinkPacket(e, null));
            }
        }
    }

    public static <E extends Entity> void tickLeash(E e) {
        Entity entity;
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData != null && leashData.delayedLeashInfo != null) {
            Leashable.restoreLeashFromSave(e, leashData);
        }
        if (leashData == null || leashData.leashHolder == null) {
            return;
        }
        if (!e.isAlive() || !leashData.leashHolder.isAlive()) {
            Leashable.dropLeash(e, true, true);
        }
        if ((entity = ((Leashable)((Object)e)).getLeashHolder()) != null && entity.level() == e.level()) {
            float f = e.distanceTo(entity);
            if (!((Leashable)((Object)e)).handleLeashAtDistance(entity, f)) {
                return;
            }
            if ((double)f > 10.0) {
                ((Leashable)((Object)e)).leashTooFarBehaviour();
            } else if ((double)f > 6.0) {
                ((Leashable)((Object)e)).elasticRangeLeashBehaviour(entity, f);
                e.checkSlowFallDistance();
            } else {
                ((Leashable)((Object)e)).closeRangeLeashBehaviour(entity);
            }
        }
    }

    default public boolean handleLeashAtDistance(Entity entity, float f) {
        return true;
    }

    default public void leashTooFarBehaviour() {
        this.dropLeash(true, true);
    }

    default public void closeRangeLeashBehaviour(Entity entity) {
    }

    default public void elasticRangeLeashBehaviour(Entity entity, float f) {
        Leashable.legacyElasticRangeLeashBehaviour((Entity)((Object)this), entity, f);
    }

    private static <E extends Entity> void legacyElasticRangeLeashBehaviour(E e, Entity entity, float f) {
        double d = (entity.getX() - e.getX()) / (double)f;
        double d2 = (entity.getY() - e.getY()) / (double)f;
        double d3 = (entity.getZ() - e.getZ()) / (double)f;
        e.setDeltaMovement(e.getDeltaMovement().add(Math.copySign(d * d * 0.4, d), Math.copySign(d2 * d2 * 0.4, d2), Math.copySign(d3 * d3 * 0.4, d3)));
    }

    default public void setLeashedTo(Entity entity, boolean bl) {
        Leashable.setLeashedTo((Entity)((Object)this), entity, bl);
    }

    private static <E extends Entity> void setLeashedTo(E e, Entity entity, boolean bl) {
        Level level;
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData == null) {
            leashData = new LeashData(entity);
            ((Leashable)((Object)e)).setLeashData(leashData);
        } else {
            leashData.setLeashHolder(entity);
        }
        if (bl && (level = e.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.getChunkSource().broadcast(e, new ClientboundSetEntityLinkPacket(e, entity));
        }
        if (e.isPassenger()) {
            e.stopRiding();
        }
    }

    @Nullable
    default public Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    @Nullable
    private static <E extends Entity> Entity getLeashHolder(E e) {
        Entity entity;
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData == null) {
            return null;
        }
        if (leashData.delayedLeashHolderId != 0 && e.level().isClientSide && (entity = e.level().getEntity(leashData.delayedLeashHolderId)) instanceof Entity) {
            Entity entity2 = entity;
            leashData.setLeashHolder(entity2);
        }
        return leashData.leashHolder;
    }

    public static final class LeashData {
        int delayedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPos> delayedLeashInfo;

        LeashData(Either<UUID, BlockPos> either) {
            this.delayedLeashInfo = either;
        }

        LeashData(Entity entity) {
            this.leashHolder = entity;
        }

        LeashData(int n) {
            this.delayedLeashHolderId = n;
        }

        public void setLeashHolder(Entity entity) {
            this.leashHolder = entity;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }
}

