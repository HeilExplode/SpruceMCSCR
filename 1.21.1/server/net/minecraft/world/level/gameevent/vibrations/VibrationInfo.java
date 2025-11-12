/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record VibrationInfo(Holder<GameEvent> gameEvent, float distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity) {
    public static final Codec<VibrationInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GameEvent.CODEC.fieldOf("game_event").forGetter(VibrationInfo::gameEvent), (App)Codec.floatRange((float)0.0f, (float)Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationInfo::distance), (App)Vec3.CODEC.fieldOf("pos").forGetter(VibrationInfo::pos), (App)UUIDUtil.CODEC.lenientOptionalFieldOf("source").forGetter(vibrationInfo -> Optional.ofNullable(vibrationInfo.uuid())), (App)UUIDUtil.CODEC.lenientOptionalFieldOf("projectile_owner").forGetter(vibrationInfo -> Optional.ofNullable(vibrationInfo.projectileOwnerUuid()))).apply((Applicative)instance, (holder, f, vec3, optional, optional2) -> new VibrationInfo((Holder<GameEvent>)holder, f.floatValue(), (Vec3)vec3, optional.orElse(null), optional2.orElse(null))));

    public VibrationInfo(Holder<GameEvent> holder, float f, Vec3 vec3, @Nullable UUID uUID, @Nullable UUID uUID2) {
        this(holder, f, vec3, uUID, uUID2, null);
    }

    public VibrationInfo(Holder<GameEvent> holder, float f, Vec3 vec3, @Nullable Entity entity) {
        this(holder, f, vec3, entity == null ? null : entity.getUUID(), VibrationInfo.getProjectileOwner(entity), entity);
    }

    @Nullable
    private static UUID getProjectileOwner(@Nullable Entity entity) {
        Projectile projectile;
        if (entity instanceof Projectile && (projectile = (Projectile)entity).getOwner() != null) {
            return projectile.getOwner().getUUID();
        }
        return null;
    }

    public Optional<Entity> getEntity(ServerLevel serverLevel) {
        return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(serverLevel::getEntity));
    }

    public Optional<Entity> getProjectileOwner(ServerLevel serverLevel) {
        return this.getEntity(serverLevel).filter(entity -> entity instanceof Projectile).map(entity -> (Projectile)entity).map(Projectile::getOwner).or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(serverLevel::getEntity));
    }
}

