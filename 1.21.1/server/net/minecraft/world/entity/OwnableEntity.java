/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.EntityGetter;

public interface OwnableEntity {
    @Nullable
    public UUID getOwnerUUID();

    public EntityGetter level();

    @Nullable
    default public LivingEntity getOwner() {
        UUID uUID = this.getOwnerUUID();
        if (uUID == null) {
            return null;
        }
        return this.level().getPlayerByUUID(uUID);
    }
}

