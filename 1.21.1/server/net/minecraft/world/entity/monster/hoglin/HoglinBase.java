/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
    public static final int ATTACK_ANIMATION_DURATION = 10;

    public int getAttackAnimationRemainingTicks();

    public static boolean hurtAndThrowTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        float f = (float)livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f2 = !livingEntity.isBaby() && (int)f > 0 ? f / 2.0f + (float)livingEntity.level().random.nextInt((int)f) : f;
        DamageSource damageSource = livingEntity.damageSources().mobAttack(livingEntity);
        boolean bl = livingEntity2.hurt(damageSource, f2);
        if (bl) {
            Level level = livingEntity.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity2, damageSource);
            }
            if (!livingEntity.isBaby()) {
                HoglinBase.throwTarget(livingEntity, livingEntity2);
            }
        }
        return bl;
    }

    public static void throwTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        double d;
        double d2 = livingEntity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double d3 = d2 - (d = livingEntity2.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if (d3 <= 0.0) {
            return;
        }
        double d4 = livingEntity2.getX() - livingEntity.getX();
        double d5 = livingEntity2.getZ() - livingEntity.getZ();
        float f = livingEntity.level().random.nextInt(21) - 10;
        double d6 = d3 * (double)(livingEntity.level().random.nextFloat() * 0.5f + 0.2f);
        Vec3 vec3 = new Vec3(d4, 0.0, d5).normalize().scale(d6).yRot(f);
        double d7 = d3 * (double)livingEntity.level().random.nextFloat() * 0.5;
        livingEntity2.push(vec3.x, d7, vec3.z);
        livingEntity2.hurtMarked = true;
    }
}

