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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EntityFlagsPredicate(Optional<Boolean> isOnGround, Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isFlying, Optional<Boolean> isBaby) {
    public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagsPredicate::isOnGround), (App)Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagsPredicate::isOnFire), (App)Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagsPredicate::isCrouching), (App)Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagsPredicate::isSprinting), (App)Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagsPredicate::isSwimming), (App)Codec.BOOL.optionalFieldOf("is_flying").forGetter(EntityFlagsPredicate::isFlying), (App)Codec.BOOL.optionalFieldOf("is_baby").forGetter(EntityFlagsPredicate::isBaby)).apply((Applicative)instance, EntityFlagsPredicate::new));

    /*
     * Unable to fully structure code
     */
    public boolean matches(Entity var1_1) {
        block9: {
            if (this.isOnGround.isPresent() && var1_1.onGround() != this.isOnGround.get().booleanValue()) {
                return false;
            }
            if (this.isOnFire.isPresent() && var1_1.isOnFire() != this.isOnFire.get().booleanValue()) {
                return false;
            }
            if (this.isCrouching.isPresent() && var1_1.isCrouching() != this.isCrouching.get().booleanValue()) {
                return false;
            }
            if (this.isSprinting.isPresent() && var1_1.isSprinting() != this.isSprinting.get().booleanValue()) {
                return false;
            }
            if (this.isSwimming.isPresent() && var1_1.isSwimming() != this.isSwimming.get().booleanValue()) {
                return false;
            }
            if (!this.isFlying.isPresent()) break block9;
            if (!(var1_1 instanceof LivingEntity)) ** GOTO lbl-1000
            var4_2 = (LivingEntity)var1_1;
            if (var4_2.isFallFlying()) ** GOTO lbl-1000
            if (var4_2 instanceof Player) {
                var3_3 = (Player)var4_2;
                ** if (!var3_3.getAbilities().flying) goto lbl-1000
            }
            ** GOTO lbl-1000
lbl-1000:
            // 2 sources

            {
                v0 = true;
                ** GOTO lbl22
            }
lbl-1000:
            // 3 sources

            {
                v0 = var2_4 = false;
            }
lbl22:
            // 2 sources

            if (var2_4 != this.isFlying.get()) {
                return false;
            }
        }
        return this.isBaby.isPresent() == false || var1_1 instanceof LivingEntity == false || (var2_5 = (LivingEntity)var1_1).isBaby() == this.isBaby.get().booleanValue();
    }

    public static class Builder {
        private Optional<Boolean> isOnGround = Optional.empty();
        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isFlying = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();

        public static Builder flags() {
            return new Builder();
        }

        public Builder setOnGround(Boolean bl) {
            this.isOnGround = Optional.of(bl);
            return this;
        }

        public Builder setOnFire(Boolean bl) {
            this.isOnFire = Optional.of(bl);
            return this;
        }

        public Builder setCrouching(Boolean bl) {
            this.isCrouching = Optional.of(bl);
            return this;
        }

        public Builder setSprinting(Boolean bl) {
            this.isSprinting = Optional.of(bl);
            return this;
        }

        public Builder setSwimming(Boolean bl) {
            this.isSwimming = Optional.of(bl);
            return this;
        }

        public Builder setIsFlying(Boolean bl) {
            this.isFlying = Optional.of(bl);
            return this;
        }

        public Builder setIsBaby(Boolean bl) {
            this.isBaby = Optional.of(bl);
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby);
        }
    }
}

