/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal
extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;

    public HurtByTargetGoal(PathfinderMob pathfinderMob, Class<?> ... classArray) {
        super(pathfinderMob, true);
        this.toIgnoreDamage = classArray;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int n = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingEntity = this.mob.getLastHurtByMob();
        if (n == this.timestamp || livingEntity == null) {
            return false;
        }
        if (livingEntity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            return false;
        }
        for (Class<?> clazz : this.toIgnoreDamage) {
            if (!clazz.isAssignableFrom(livingEntity.getClass())) continue;
            return false;
        }
        return this.canAttack(livingEntity, HURT_BY_TARGETING);
    }

    public HurtByTargetGoal setAlertOthers(Class<?> ... classArray) {
        this.alertSameType = true;
        this.toIgnoreAlert = classArray;
        return this;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double d = this.getFollowDistance();
        AABB aABB = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d, 10.0, d);
        List<Entity> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), aABB, EntitySelector.NO_SPECTATORS);
        for (Mob mob : list) {
            if (this.mob == mob || mob.getTarget() != null || this.mob instanceof TamableAnimal && ((TamableAnimal)this.mob).getOwner() != ((TamableAnimal)mob).getOwner() || mob.isAlliedTo(this.mob.getLastHurtByMob())) continue;
            if (this.toIgnoreAlert != null) {
                boolean bl = false;
                for (Class<?> clazz : this.toIgnoreAlert) {
                    if (mob.getClass() != clazz) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
            }
            this.alertOther(mob, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob mob, LivingEntity livingEntity) {
        mob.setTarget(livingEntity);
    }
}

