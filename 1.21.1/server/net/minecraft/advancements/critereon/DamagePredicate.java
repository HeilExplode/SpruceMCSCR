/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public record DamagePredicate(MinMaxBounds.Doubles dealtDamage, MinMaxBounds.Doubles takenDamage, Optional<EntityPredicate> sourceEntity, Optional<Boolean> blocked, Optional<DamageSourcePredicate> type) {
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("dealt", (Object)MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::dealtDamage), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("taken", (Object)MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::takenDamage), (App)EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamagePredicate::sourceEntity), (App)Codec.BOOL.optionalFieldOf("blocked").forGetter(DamagePredicate::blocked), (App)DamageSourcePredicate.CODEC.optionalFieldOf("type").forGetter(DamagePredicate::type)).apply((Applicative)instance, DamagePredicate::new));

    public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float f2, boolean bl) {
        if (!this.dealtDamage.matches(f)) {
            return false;
        }
        if (!this.takenDamage.matches(f2)) {
            return false;
        }
        if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches(serverPlayer, damageSource.getEntity())) {
            return false;
        }
        if (this.blocked.isPresent() && this.blocked.get() != bl) {
            return false;
        }
        return !this.type.isPresent() || this.type.get().matches(serverPlayer, damageSource);
    }

    public static class Builder {
        private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> blocked = Optional.empty();
        private Optional<DamageSourcePredicate> type = Optional.empty();

        public static Builder damageInstance() {
            return new Builder();
        }

        public Builder dealtDamage(MinMaxBounds.Doubles doubles) {
            this.dealtDamage = doubles;
            return this;
        }

        public Builder takenDamage(MinMaxBounds.Doubles doubles) {
            this.takenDamage = doubles;
            return this;
        }

        public Builder sourceEntity(EntityPredicate entityPredicate) {
            this.sourceEntity = Optional.of(entityPredicate);
            return this;
        }

        public Builder blocked(Boolean bl) {
            this.blocked = Optional.of(bl);
            return this;
        }

        public Builder type(DamageSourcePredicate damageSourcePredicate) {
            this.type = Optional.of(damageSourcePredicate);
            return this;
        }

        public Builder type(DamageSourcePredicate.Builder builder) {
            this.type = Optional.of(builder.build());
            return this;
        }

        public DamagePredicate build() {
            return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
        }
    }
}

