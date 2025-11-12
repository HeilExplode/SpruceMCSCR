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
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record ApplyMobEffect(HolderSet<MobEffect> toApply, LevelBasedValue minDuration, LevelBasedValue maxDuration, LevelBasedValue minAmplifier, LevelBasedValue maxAmplifier) implements EnchantmentEntityEffect
{
    public static final MapCodec<ApplyMobEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyMobEffect::toApply), (App)LevelBasedValue.CODEC.fieldOf("min_duration").forGetter(ApplyMobEffect::minDuration), (App)LevelBasedValue.CODEC.fieldOf("max_duration").forGetter(ApplyMobEffect::maxDuration), (App)LevelBasedValue.CODEC.fieldOf("min_amplifier").forGetter(ApplyMobEffect::minAmplifier), (App)LevelBasedValue.CODEC.fieldOf("max_amplifier").forGetter(ApplyMobEffect::maxAmplifier)).apply((Applicative)instance, ApplyMobEffect::new));

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        LivingEntity livingEntity;
        RandomSource randomSource;
        Optional<Holder<MobEffect>> optional;
        if (entity instanceof LivingEntity && (optional = this.toApply.getRandomElement(randomSource = (livingEntity = (LivingEntity)entity).getRandom())).isPresent()) {
            int n2 = Math.round(Mth.randomBetween(randomSource, this.minDuration.calculate(n), this.maxDuration.calculate(n)) * 20.0f);
            int n3 = Math.max(0, Math.round(Mth.randomBetween(randomSource, this.minAmplifier.calculate(n), this.maxAmplifier.calculate(n))));
            livingEntity.addEffect(new MobEffectInstance(optional.get(), n2, n3));
        }
    }

    public MapCodec<ApplyMobEffect> codec() {
        return CODEC;
    }
}

