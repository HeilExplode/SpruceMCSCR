/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface CrossbowAttackMob
extends RangedAttackMob {
    public void setChargingCrossbow(boolean var1);

    @Nullable
    public LivingEntity getTarget();

    public void onCrossbowAttackPerformed();

    default public void performCrossbowAttack(LivingEntity livingEntity, float f) {
        InteractionHand interactionHand = ProjectileUtil.getWeaponHoldingHand(livingEntity, Items.CROSSBOW);
        ItemStack itemStack = livingEntity.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (item instanceof CrossbowItem) {
            CrossbowItem crossbowItem = (CrossbowItem)item;
            crossbowItem.performShooting(livingEntity.level(), livingEntity, interactionHand, itemStack, f, 14 - livingEntity.level().getDifficulty().getId() * 4, this.getTarget());
        }
        this.onCrossbowAttackPerformed();
    }
}

