/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.damagesource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0f;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0f;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0f;
    public static final float MIN_ARMOR_RATIO = 0.2f;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(LivingEntity livingEntity, float f, DamageSource damageSource, float f2, float f3) {
        float f4;
        Level level;
        float f5 = 2.0f + f3 / 4.0f;
        float f6 = Mth.clamp(f2 - f / f5, f2 * 0.2f, 20.0f);
        float f7 = f6 / 25.0f;
        ItemStack itemStack = damageSource.getWeaponItem();
        if (itemStack != null && (level = livingEntity.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            f4 = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverLevel, itemStack, livingEntity, damageSource, f7), 0.0f, 1.0f);
        } else {
            f4 = f7;
        }
        float f8 = 1.0f - f4;
        return f * f8;
    }

    public static float getDamageAfterMagicAbsorb(float f, float f2) {
        float f3 = Mth.clamp(f2, 0.0f, 20.0f);
        return f * (1.0f - f3 / 25.0f);
    }
}

