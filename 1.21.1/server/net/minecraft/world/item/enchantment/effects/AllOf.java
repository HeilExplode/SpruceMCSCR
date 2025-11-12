/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.phys.Vec3;

public interface AllOf {
    public static <T, A extends T> MapCodec<A> codec(Codec<T> codec, Function<List<T>, A> function, Function<A, List<T>> function2) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)codec.listOf().fieldOf("effects").forGetter(function2)).apply((Applicative)instance, function));
    }

    public static EntityEffects entityEffects(EnchantmentEntityEffect ... enchantmentEntityEffectArray) {
        return new EntityEffects(List.of(enchantmentEntityEffectArray));
    }

    public static LocationBasedEffects locationBasedEffects(EnchantmentLocationBasedEffect ... enchantmentLocationBasedEffectArray) {
        return new LocationBasedEffects(List.of(enchantmentLocationBasedEffectArray));
    }

    public static ValueEffects valueEffects(EnchantmentValueEffect ... enchantmentValueEffectArray) {
        return new ValueEffects(List.of(enchantmentValueEffectArray));
    }

    public record EntityEffects(List<EnchantmentEntityEffect> effects) implements EnchantmentEntityEffect
    {
        public static final MapCodec<EntityEffects> CODEC = AllOf.codec(EnchantmentEntityEffect.CODEC, EntityEffects::new, EntityEffects::effects);

        @Override
        public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
            for (EnchantmentEntityEffect enchantmentEntityEffect : this.effects) {
                enchantmentEntityEffect.apply(serverLevel, n, enchantedItemInUse, entity, vec3);
            }
        }

        public MapCodec<EntityEffects> codec() {
            return CODEC;
        }
    }

    public record LocationBasedEffects(List<EnchantmentLocationBasedEffect> effects) implements EnchantmentLocationBasedEffect
    {
        public static final MapCodec<LocationBasedEffects> CODEC = AllOf.codec(EnchantmentLocationBasedEffect.CODEC, LocationBasedEffects::new, LocationBasedEffects::effects);

        @Override
        public void onChangedBlock(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, boolean bl) {
            for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : this.effects) {
                enchantmentLocationBasedEffect.onChangedBlock(serverLevel, n, enchantedItemInUse, entity, vec3, bl);
            }
        }

        @Override
        public void onDeactivated(EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, int n) {
            for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : this.effects) {
                enchantmentLocationBasedEffect.onDeactivated(enchantedItemInUse, entity, vec3, n);
            }
        }

        public MapCodec<LocationBasedEffects> codec() {
            return CODEC;
        }
    }

    public record ValueEffects(List<EnchantmentValueEffect> effects) implements EnchantmentValueEffect
    {
        public static final MapCodec<ValueEffects> CODEC = AllOf.codec(EnchantmentValueEffect.CODEC, ValueEffects::new, ValueEffects::effects);

        @Override
        public float process(int n, RandomSource randomSource, float f) {
            for (EnchantmentValueEffect enchantmentValueEffect : this.effects) {
                f = enchantmentValueEffect.process(n, randomSource, f);
            }
            return f;
        }

        public MapCodec<ValueEffects> codec() {
            return CODEC;
        }
    }
}

