/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;

public interface TraceableEntity {
    @Nullable
    public Entity getOwner();
}

