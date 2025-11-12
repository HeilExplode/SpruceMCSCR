/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record MobEffectsPredicate(Map<Holder<MobEffect>, MobEffectInstancePredicate> effectMap) {
    public static final Codec<MobEffectsPredicate> CODEC = Codec.unboundedMap(MobEffect.CODEC, MobEffectInstancePredicate.CODEC).xmap(MobEffectsPredicate::new, MobEffectsPredicate::effectMap);

    public boolean matches(Entity entity) {
        LivingEntity livingEntity;
        return entity instanceof LivingEntity && this.matches((livingEntity = (LivingEntity)entity).getActiveEffectsMap());
    }

    public boolean matches(LivingEntity livingEntity) {
        return this.matches(livingEntity.getActiveEffectsMap());
    }

    public boolean matches(Map<Holder<MobEffect>, MobEffectInstance> map) {
        for (Map.Entry<Holder<MobEffect>, MobEffectInstancePredicate> entry : this.effectMap.entrySet()) {
            MobEffectInstance mobEffectInstance = map.get(entry.getKey());
            if (entry.getValue().matches(mobEffectInstance)) continue;
            return false;
        }
        return true;
    }

    public record MobEffectInstancePredicate(MinMaxBounds.Ints amplifier, MinMaxBounds.Ints duration, Optional<Boolean> ambient, Optional<Boolean> visible) {
        public static final Codec<MobEffectInstancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", (Object)MinMaxBounds.Ints.ANY).forGetter(MobEffectInstancePredicate::amplifier), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", (Object)MinMaxBounds.Ints.ANY).forGetter(MobEffectInstancePredicate::duration), (App)Codec.BOOL.optionalFieldOf("ambient").forGetter(MobEffectInstancePredicate::ambient), (App)Codec.BOOL.optionalFieldOf("visible").forGetter(MobEffectInstancePredicate::visible)).apply((Applicative)instance, MobEffectInstancePredicate::new));

        public MobEffectInstancePredicate() {
            this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
        }

        public boolean matches(@Nullable MobEffectInstance mobEffectInstance) {
            if (mobEffectInstance == null) {
                return false;
            }
            if (!this.amplifier.matches(mobEffectInstance.getAmplifier())) {
                return false;
            }
            if (!this.duration.matches(mobEffectInstance.getDuration())) {
                return false;
            }
            if (this.ambient.isPresent() && this.ambient.get().booleanValue() != mobEffectInstance.isAmbient()) {
                return false;
            }
            return !this.visible.isPresent() || this.visible.get().booleanValue() == mobEffectInstance.isVisible();
        }
    }

    public static class Builder {
        private final ImmutableMap.Builder<Holder<MobEffect>, MobEffectInstancePredicate> effectMap = ImmutableMap.builder();

        public static Builder effects() {
            return new Builder();
        }

        public Builder and(Holder<MobEffect> holder) {
            this.effectMap.put(holder, (Object)new MobEffectInstancePredicate());
            return this;
        }

        public Builder and(Holder<MobEffect> holder, MobEffectInstancePredicate mobEffectInstancePredicate) {
            this.effectMap.put(holder, (Object)mobEffectInstancePredicate);
            return this;
        }

        public Optional<MobEffectsPredicate> build() {
            return Optional.of(new MobEffectsPredicate((Map<Holder<MobEffect>, MobEffectInstancePredicate>)this.effectMap.build()));
        }
    }
}

