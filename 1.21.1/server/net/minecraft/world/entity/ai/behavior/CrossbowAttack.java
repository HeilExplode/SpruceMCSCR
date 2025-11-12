/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class CrossbowAttack<E extends Mob, T extends LivingEntity>
extends Behavior<E> {
    private static final int TIMEOUT = 1200;
    private int attackDelay;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;

    public CrossbowAttack() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        LivingEntity livingEntity = CrossbowAttack.getAttackTarget(e);
        return ((LivingEntity)e).isHolding(Items.CROSSBOW) && BehaviorUtils.canSee(e, livingEntity) && BehaviorUtils.isWithinAttackRange(e, livingEntity, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E e, long l) {
        return ((LivingEntity)e).getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(serverLevel, e);
    }

    @Override
    protected void tick(ServerLevel serverLevel, E e, long l) {
        LivingEntity livingEntity = CrossbowAttack.getAttackTarget(e);
        this.lookAtTarget((Mob)e, livingEntity);
        this.crossbowAttack(e, livingEntity);
    }

    @Override
    protected void stop(ServerLevel serverLevel, E e, long l) {
        if (((LivingEntity)e).isUsingItem()) {
            ((LivingEntity)e).stopUsingItem();
        }
        if (((LivingEntity)e).isHolding(Items.CROSSBOW)) {
            ((CrossbowAttackMob)e).setChargingCrossbow(false);
            ((LivingEntity)e).getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    private void crossbowAttack(E e, LivingEntity livingEntity) {
        if (this.crossbowState == CrossbowState.UNCHARGED) {
            ((LivingEntity)e).startUsingItem(ProjectileUtil.getWeaponHoldingHand(e, Items.CROSSBOW));
            this.crossbowState = CrossbowState.CHARGING;
            ((CrossbowAttackMob)e).setChargingCrossbow(true);
        } else if (this.crossbowState == CrossbowState.CHARGING) {
            ItemStack itemStack;
            int n;
            if (!((LivingEntity)e).isUsingItem()) {
                this.crossbowState = CrossbowState.UNCHARGED;
            }
            if ((n = ((LivingEntity)e).getTicksUsingItem()) >= CrossbowItem.getChargeDuration(itemStack = ((LivingEntity)e).getUseItem(), e)) {
                ((LivingEntity)e).releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = 20 + ((Entity)e).getRandom().nextInt(20);
                ((CrossbowAttackMob)e).setChargingCrossbow(false);
            }
        } else if (this.crossbowState == CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK) {
            ((RangedAttackMob)e).performRangedAttack(livingEntity, 1.0f);
            this.crossbowState = CrossbowState.UNCHARGED;
        }
    }

    private void lookAtTarget(Mob mob, LivingEntity livingEntity) {
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
    }

    private static LivingEntity getAttackTarget(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((Mob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((Mob)livingEntity), l);
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;

    }
}

