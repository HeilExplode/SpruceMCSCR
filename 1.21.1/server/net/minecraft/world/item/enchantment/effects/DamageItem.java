/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record DamageItem(LevelBasedValue amount) implements EnchantmentEntityEffect
{
    public static final MapCodec<DamageItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LevelBasedValue.CODEC.fieldOf("amount").forGetter(damageItem -> damageItem.amount)).apply((Applicative)instance, DamageItem::new));

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        ServerPlayer serverPlayer;
        LivingEntity livingEntity = enchantedItemInUse.owner();
        ServerPlayer serverPlayer2 = livingEntity instanceof ServerPlayer ? (serverPlayer = (ServerPlayer)livingEntity) : null;
        enchantedItemInUse.itemStack().hurtAndBreak((int)this.amount.calculate(n), serverLevel, serverPlayer2, enchantedItemInUse.onBreak());
    }

    public MapCodec<DamageItem> codec() {
        return CODEC;
    }
}

