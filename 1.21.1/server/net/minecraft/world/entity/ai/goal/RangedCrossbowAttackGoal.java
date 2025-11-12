/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class RangedCrossbowAttackGoal<T extends Monster & CrossbowAttackMob>
extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public RangedCrossbowAttackGoal(T t, double d, float f) {
        this.mob = t;
        this.speedModifier = d;
        this.attackRadiusSqr = f * f;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return ((LivingEntity)this.mob).isHolding(Items.CROSSBOW);
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !((Mob)this.mob).getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return ((Mob)this.mob).getTarget() != null && ((Mob)this.mob).getTarget().isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        ((Mob)this.mob).setAggressive(false);
        ((Mob)this.mob).setTarget(null);
        this.seeTime = 0;
        if (((LivingEntity)this.mob).isUsingItem()) {
            ((LivingEntity)this.mob).stopUsingItem();
            ((CrossbowAttackMob)this.mob).setChargingCrossbow(false);
            ((LivingEntity)this.mob).getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean bl;
        boolean bl2;
        LivingEntity livingEntity = ((Mob)this.mob).getTarget();
        if (livingEntity == null) {
            return;
        }
        boolean bl3 = ((Mob)this.mob).getSensing().hasLineOfSight(livingEntity);
        boolean bl4 = bl2 = this.seeTime > 0;
        if (bl3 != bl2) {
            this.seeTime = 0;
        }
        this.seeTime = bl3 ? ++this.seeTime : --this.seeTime;
        double d = ((Entity)this.mob).distanceToSqr(livingEntity);
        boolean bl5 = bl = (d > (double)this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
        if (bl) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                ((Mob)this.mob).getNavigation().moveTo(livingEntity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(((Entity)this.mob).getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            ((Mob)this.mob).getNavigation().stop();
        }
        ((Mob)this.mob).getLookControl().setLookAt(livingEntity, 30.0f, 30.0f);
        if (this.crossbowState == CrossbowState.UNCHARGED) {
            if (!bl) {
                ((LivingEntity)this.mob).startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
                this.crossbowState = CrossbowState.CHARGING;
                ((CrossbowAttackMob)this.mob).setChargingCrossbow(true);
            }
        } else if (this.crossbowState == CrossbowState.CHARGING) {
            ItemStack itemStack;
            int n;
            if (!((LivingEntity)this.mob).isUsingItem()) {
                this.crossbowState = CrossbowState.UNCHARGED;
            }
            if ((n = ((LivingEntity)this.mob).getTicksUsingItem()) >= CrossbowItem.getChargeDuration(itemStack = ((LivingEntity)this.mob).getUseItem(), this.mob)) {
                ((LivingEntity)this.mob).releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = 20 + ((Entity)this.mob).getRandom().nextInt(20);
                ((CrossbowAttackMob)this.mob).setChargingCrossbow(false);
            }
        } else if (this.crossbowState == CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && bl3) {
            ((RangedAttackMob)this.mob).performRangedAttack(livingEntity, 1.0f);
            this.crossbowState = CrossbowState.UNCHARGED;
        }
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;

    }
}

