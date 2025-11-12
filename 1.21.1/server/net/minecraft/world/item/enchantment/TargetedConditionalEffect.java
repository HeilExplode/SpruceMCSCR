/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TargetedConditionalEffect<T>(EnchantmentTarget enchanted, EnchantmentTarget affected, T effect, Optional<LootItemCondition> requirements) {
    public static <S> Codec<TargetedConditionalEffect<S>> codec(Codec<S> codec, LootContextParamSet lootContextParamSet) {
        return RecordCodecBuilder.create(instance -> instance.group((App)EnchantmentTarget.CODEC.fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted), (App)EnchantmentTarget.CODEC.fieldOf("affected").forGetter(TargetedConditionalEffect::affected), (App)codec.fieldOf("effect").forGetter(TargetedConditionalEffect::effect), (App)ConditionalEffect.conditionCodec(lootContextParamSet).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)).apply((Applicative)instance, TargetedConditionalEffect::new));
    }

    public static <S> Codec<TargetedConditionalEffect<S>> equipmentDropsCodec(Codec<S> codec, LootContextParamSet lootContextParamSet) {
        return RecordCodecBuilder.create(instance -> instance.group((App)EnchantmentTarget.CODEC.validate(enchantmentTarget -> enchantmentTarget != EnchantmentTarget.DAMAGING_ENTITY ? DataResult.success((Object)enchantmentTarget) : DataResult.error(() -> "enchanted must be attacker or victim")).fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted), (App)codec.fieldOf("effect").forGetter(TargetedConditionalEffect::effect), (App)ConditionalEffect.conditionCodec(lootContextParamSet).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)).apply((Applicative)instance, (enchantmentTarget, object, optional) -> new TargetedConditionalEffect<Object>((EnchantmentTarget)enchantmentTarget, EnchantmentTarget.VICTIM, object, (Optional<LootItemCondition>)optional)));
    }

    public boolean matches(LootContext lootContext) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(lootContext);
    }
}

