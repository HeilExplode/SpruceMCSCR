/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveBackToVillageGoal
extends RandomStrollGoal {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public MoveBackToVillageGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
        super(pathfinderMob, d, 10, bl);
    }

    @Override
    public boolean canUse() {
        BlockPos blockPos;
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        if (serverLevel.isVillage(blockPos = this.mob.blockPosition())) {
            return false;
        }
        return super.canUse();
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        BlockPos blockPos;
        SectionPos sectionPos;
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        SectionPos sectionPos2 = BehaviorUtils.findSectionClosestToVillage(serverLevel, sectionPos = SectionPos.of(blockPos = this.mob.blockPosition()), 2);
        if (sectionPos2 != sectionPos) {
            return DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(sectionPos2.center()), 1.5707963705062866);
        }
        return null;
    }
}

