/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;

public interface Portal {
    default public int getPortalTransitionTime(ServerLevel serverLevel, Entity entity) {
        return 0;
    }

    @Nullable
    public DimensionTransition getPortalDestination(ServerLevel var1, Entity var2, BlockPos var3);

    default public Transition getLocalTransition() {
        return Transition.NONE;
    }

    public static enum Transition {
        CONFUSION,
        NONE;

    }
}

