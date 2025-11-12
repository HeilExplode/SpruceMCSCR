/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.Level;

public abstract class SpellcasterIllager
extends AbstractIllager {
    private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(SpellcasterIllager.class, EntityDataSerializers.BYTE);
    protected int spellCastingTickCount;
    private IllagerSpell currentSpell = IllagerSpell.NONE;

    protected SpellcasterIllager(EntityType<? extends SpellcasterIllager> entityType, Level level) {
        super((EntityType<? extends AbstractIllager>)entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPELL_CASTING_ID, (byte)0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.spellCastingTickCount = compoundTag.getInt("SpellTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("SpellTicks", this.spellCastingTickCount);
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        if (this.isCelebrating()) {
            return AbstractIllager.IllagerArmPose.CELEBRATING;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    public boolean isCastingSpell() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_SPELL_CASTING_ID) > 0;
        }
        return this.spellCastingTickCount > 0;
    }

    public void setIsCastingSpell(IllagerSpell illagerSpell) {
        this.currentSpell = illagerSpell;
        this.entityData.set(DATA_SPELL_CASTING_ID, (byte)illagerSpell.id);
    }

    protected IllagerSpell getCurrentSpell() {
        if (!this.level().isClientSide) {
            return this.currentSpell;
        }
        return IllagerSpell.byId(this.entityData.get(DATA_SPELL_CASTING_ID).byteValue());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.spellCastingTickCount > 0) {
            --this.spellCastingTickCount;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.isCastingSpell()) {
            IllagerSpell illagerSpell = this.getCurrentSpell();
            float f = (float)illagerSpell.spellColor[0];
            float f2 = (float)illagerSpell.spellColor[1];
            float f3 = (float)illagerSpell.spellColor[2];
            float f4 = this.yBodyRot * ((float)Math.PI / 180) + Mth.cos((float)this.tickCount * 0.6662f) * 0.25f;
            float f5 = Mth.cos(f4);
            float f6 = Mth.sin(f4);
            double d = 0.6 * (double)this.getScale();
            double d2 = 1.8 * (double)this.getScale();
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, f, f2, f3), this.getX() + (double)f5 * d, this.getY() + d2, this.getZ() + (double)f6 * d, 0.0, 0.0, 0.0);
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, f, f2, f3), this.getX() - (double)f5 * d, this.getY() + d2, this.getZ() - (double)f6 * d, 0.0, 0.0, 0.0);
        }
    }

    protected int getSpellCastingTime() {
        return this.spellCastingTickCount;
    }

    protected abstract SoundEvent getCastingSoundEvent();

    protected static enum IllagerSpell {
        NONE(0, 0.0, 0.0, 0.0),
        SUMMON_VEX(1, 0.7, 0.7, 0.8),
        FANGS(2, 0.4, 0.3, 0.35),
        WOLOLO(3, 0.7, 0.5, 0.2),
        DISAPPEAR(4, 0.3, 0.3, 0.8),
        BLINDNESS(5, 0.1, 0.1, 0.2);

        private static final IntFunction<IllagerSpell> BY_ID;
        final int id;
        final double[] spellColor;

        private IllagerSpell(int n2, double d, double d2, double d3) {
            this.id = n2;
            this.spellColor = new double[]{d, d2, d3};
        }

        public static IllagerSpell byId(int n) {
            return BY_ID.apply(n);
        }

        static {
            BY_ID = ByIdMap.continuous(illagerSpell -> illagerSpell.id, IllagerSpell.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        }
    }

    protected abstract class SpellcasterUseSpellGoal
    extends Goal {
        protected int attackWarmupDelay;
        protected int nextAttackTickCount;

        protected SpellcasterUseSpellGoal() {
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = SpellcasterIllager.this.getTarget();
            if (livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            if (SpellcasterIllager.this.isCastingSpell()) {
                return false;
            }
            return SpellcasterIllager.this.tickCount >= this.nextAttackTickCount;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = SpellcasterIllager.this.getTarget();
            return livingEntity != null && livingEntity.isAlive() && this.attackWarmupDelay > 0;
        }

        @Override
        public void start() {
            this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
            SpellcasterIllager.this.spellCastingTickCount = this.getCastingTime();
            this.nextAttackTickCount = SpellcasterIllager.this.tickCount + this.getCastingInterval();
            SoundEvent soundEvent = this.getSpellPrepareSound();
            if (soundEvent != null) {
                SpellcasterIllager.this.playSound(soundEvent, 1.0f, 1.0f);
            }
            SpellcasterIllager.this.setIsCastingSpell(this.getSpell());
        }

        @Override
        public void tick() {
            --this.attackWarmupDelay;
            if (this.attackWarmupDelay == 0) {
                this.performSpellCasting();
                SpellcasterIllager.this.playSound(SpellcasterIllager.this.getCastingSoundEvent(), 1.0f, 1.0f);
            }
        }

        protected abstract void performSpellCasting();

        protected int getCastWarmupTime() {
            return 20;
        }

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEvent getSpellPrepareSound();

        protected abstract IllagerSpell getSpell();
    }

    protected class SpellcasterCastingSpellGoal
    extends Goal {
        public SpellcasterCastingSpellGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return SpellcasterIllager.this.getSpellCastingTime() > 0;
        }

        @Override
        public void start() {
            super.start();
            SpellcasterIllager.this.navigation.stop();
        }

        @Override
        public void stop() {
            super.stop();
            SpellcasterIllager.this.setIsCastingSpell(IllagerSpell.NONE);
        }

        @Override
        public void tick() {
            if (SpellcasterIllager.this.getTarget() != null) {
                SpellcasterIllager.this.getLookControl().setLookAt(SpellcasterIllager.this.getTarget(), SpellcasterIllager.this.getMaxHeadYRot(), SpellcasterIllager.this.getMaxHeadXRot());
            }
        }
    }
}

