/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.PlayerTeam;

public abstract class TargetGoal
extends Goal {
    private static final int EMPTY_REACH_CACHE = 0;
    private static final int CAN_REACH_CACHE = 1;
    private static final int CANT_REACH_CACHE = 2;
    protected final Mob mob;
    protected final boolean mustSee;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    private int unseenTicks;
    @Nullable
    protected LivingEntity targetMob;
    protected int unseenMemoryTicks = 60;

    public TargetGoal(Mob mob, boolean bl) {
        this(mob, bl, false);
    }

    public TargetGoal(Mob mob, boolean bl, boolean bl2) {
        this.mob = mob;
        this.mustSee = bl;
        this.mustReach = bl2;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            livingEntity = this.targetMob;
        }
        if (livingEntity == null) {
            return false;
        }
        if (!this.mob.canAttack(livingEntity)) {
            return false;
        }
        PlayerTeam playerTeam = this.mob.getTeam();
        PlayerTeam playerTeam2 = livingEntity.getTeam();
        if (playerTeam != null && playerTeam2 == playerTeam) {
            return false;
        }
        double d = this.getFollowDistance();
        if (this.mob.distanceToSqr(livingEntity) > d * d) {
            return false;
        }
        if (this.mustSee) {
            if (this.mob.getSensing().hasLineOfSight(livingEntity)) {
                this.unseenTicks = 0;
            } else if (++this.unseenTicks > TargetGoal.reducedTickDelay(this.unseenMemoryTicks)) {
                return false;
            }
        }
        this.mob.setTarget(livingEntity);
        return true;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void start() {
        this.reachCache = 0;
        this.reachCacheTime = 0;
        this.unseenTicks = 0;
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
        this.targetMob = null;
    }

    protected boolean canAttack(@Nullable LivingEntity livingEntity, TargetingConditions targetingConditions) {
        if (livingEntity == null) {
            return false;
        }
        if (!targetingConditions.test(this.mob, livingEntity)) {
            return false;
        }
        if (!this.mob.isWithinRestriction(livingEntity.blockPosition())) {
            return false;
        }
        if (this.mustReach) {
            if (--this.reachCacheTime <= 0) {
                this.reachCache = 0;
            }
            if (this.reachCache == 0) {
                int n = this.reachCache = this.canReach(livingEntity) ? 1 : 2;
            }
            if (this.reachCache == 2) {
                return false;
            }
        }
        return true;
    }

    private boolean canReach(LivingEntity livingEntity) {
        int n;
        this.reachCacheTime = TargetGoal.reducedTickDelay(10 + this.mob.getRandom().nextInt(5));
        Path path = this.mob.getNavigation().createPath(livingEntity, 0);
        if (path == null) {
            return false;
        }
        Node node = path.getEndNode();
        if (node == null) {
            return false;
        }
        int n2 = node.x - livingEntity.getBlockX();
        return (double)(n2 * n2 + (n = node.z - livingEntity.getBlockZ()) * n) <= 2.25;
    }

    public TargetGoal setUnseenMemoryTicks(int n) {
        this.unseenMemoryTicks = n;
        return this;
    }
}

