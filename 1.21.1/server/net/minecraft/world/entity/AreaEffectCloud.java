/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud
extends Entity
implements TraceableEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private static final float MAX_RADIUS = 32.0f;
    private static final float MINIMAL_RADIUS = 0.5f;
    private static final float DEFAULT_RADIUS = 3.0f;
    public static final float DEFAULT_WIDTH = 6.0f;
    public static final float HEIGHT = 0.5f;
    private PotionContents potionContents = PotionContents.EMPTY;
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = 600;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public AreaEffectCloud(Level level, double d, double d2, double d3) {
        this((EntityType<? extends AreaEffectCloud>)EntityType.AREA_EFFECT_CLOUD, level);
        this.setPos(d, d2, d3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, Float.valueOf(3.0f));
        builder.define(DATA_WAITING, false);
        builder.define(DATA_PARTICLE, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1));
    }

    public void setRadius(float f) {
        if (!this.level().isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Float.valueOf(Mth.clamp(f, 0.0f, 32.0f)));
        }
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        super.refreshDimensions();
        this.setPos(d, d2, d3);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS).floatValue();
    }

    public void setPotionContents(PotionContents potionContents) {
        this.potionContents = potionContents;
        this.updateColor();
    }

    private void updateColor() {
        ParticleOptions particleOptions = this.entityData.get(DATA_PARTICLE);
        if (particleOptions instanceof ColorParticleOption) {
            ColorParticleOption colorParticleOption = (ColorParticleOption)particleOptions;
            int n = this.potionContents.equals(PotionContents.EMPTY) ? 0 : this.potionContents.getColor();
            this.entityData.set(DATA_PARTICLE, ColorParticleOption.create(colorParticleOption.getType(), FastColor.ARGB32.opaque(n)));
        }
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        this.setPotionContents(this.potionContents.withEffectAdded(mobEffectInstance));
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    public void setParticle(ParticleOptions particleOptions) {
        this.getEntityData().set(DATA_PARTICLE, particleOptions);
    }

    protected void setWaiting(boolean bl) {
        this.getEntityData().set(DATA_WAITING, bl);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int n) {
        this.duration = n;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void tick() {
        block23: {
            float f;
            block24: {
                boolean bl;
                boolean bl2;
                block22: {
                    void object2;
                    float f2;
                    int n2;
                    super.tick();
                    bl2 = this.isWaiting();
                    f = this.getRadius();
                    if (!this.level().isClientSide) break block22;
                    if (bl2 && this.random.nextBoolean()) {
                        return;
                    }
                    ParticleOptions particleOptions = this.getParticle();
                    if (bl2) {
                        n2 = 2;
                        f2 = 0.2f;
                    } else {
                        n2 = Mth.ceil((float)Math.PI * f * f);
                        f2 = f;
                    }
                    boolean i = false;
                    while (object2 < n2) {
                        float f3 = this.random.nextFloat() * ((float)Math.PI * 2);
                        float f4 = Mth.sqrt(this.random.nextFloat()) * f2;
                        double d = this.getX() + (double)(Mth.cos(f3) * f4);
                        double d2 = this.getY();
                        double d3 = this.getZ() + (double)(Mth.sin(f3) * f4);
                        if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                            if (bl2 && this.random.nextBoolean()) {
                                this.level().addAlwaysVisibleParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1), d, d2, d3, 0.0, 0.0, 0.0);
                            } else {
                                this.level().addAlwaysVisibleParticle(particleOptions, d, d2, d3, 0.0, 0.0, 0.0);
                            }
                        } else if (bl2) {
                            this.level().addAlwaysVisibleParticle(particleOptions, d, d2, d3, 0.0, 0.0, 0.0);
                        } else {
                            this.level().addAlwaysVisibleParticle(particleOptions, d, d2, d3, (0.5 - this.random.nextDouble()) * 0.15, 0.01f, (0.5 - this.random.nextDouble()) * 0.15);
                        }
                        ++object2;
                    }
                    break block23;
                }
                if (this.tickCount >= this.waitTime + this.duration) {
                    this.discard();
                    return;
                }
                boolean bl3 = bl = this.tickCount < this.waitTime;
                if (bl2 != bl) {
                    this.setWaiting(bl);
                }
                if (bl) {
                    return;
                }
                if (this.radiusPerTick != 0.0f) {
                    if ((f += this.radiusPerTick) < 0.5f) {
                        this.discard();
                        return;
                    }
                    this.setRadius(f);
                }
                if (this.tickCount % 5 != 0) break block23;
                this.victims.entrySet().removeIf(entry -> this.tickCount >= (Integer)entry.getValue());
                if (this.potionContents.hasEffects()) break block24;
                this.victims.clear();
                break block23;
            }
            ArrayList arrayList = Lists.newArrayList();
            if (this.potionContents.potion().isPresent()) {
                for (MobEffectInstance mobEffectInstance : this.potionContents.potion().get().value().getEffects()) {
                    arrayList.add(new MobEffectInstance(mobEffectInstance.getEffect(), mobEffectInstance.mapDuration(n -> n / 4), mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
                }
            }
            arrayList.addAll(this.potionContents.customEffects());
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
            if (list.isEmpty()) break block23;
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                double d;
                double d4;
                double d5;
                LivingEntity livingEntity = (LivingEntity)iterator.next();
                if (this.victims.containsKey(livingEntity) || !livingEntity.isAffectedByPotions()) continue;
                if (arrayList.stream().noneMatch(livingEntity::canBeAffected) || !((d5 = (d4 = livingEntity.getX() - this.getX()) * d4 + (d = livingEntity.getZ() - this.getZ()) * d) <= (double)(f * f))) continue;
                this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);
                for (MobEffectInstance mobEffectInstance : arrayList) {
                    if (mobEffectInstance.getEffect().value().isInstantenous()) {
                        mobEffectInstance.getEffect().value().applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), 0.5);
                        continue;
                    }
                    livingEntity.addEffect(new MobEffectInstance(mobEffectInstance), this);
                }
                if (this.radiusOnUse != 0.0f) {
                    if ((f += this.radiusOnUse) < 0.5f) {
                        this.discard();
                        return;
                    }
                    this.setRadius(f);
                }
                if (this.durationOnUse == 0) continue;
                this.duration += this.durationOnUse;
                if (this.duration > 0) continue;
                this.discard();
                return;
            }
        }
    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int n) {
        this.durationOnUse = n;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int n) {
        this.waitTime = n;
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity;
        this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        Entity entity;
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel && (entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID)) instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
        }
        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.tickCount = compoundTag.getInt("Age");
        this.duration = compoundTag.getInt("Duration");
        this.waitTime = compoundTag.getInt("WaitTime");
        this.reapplicationDelay = compoundTag.getInt("ReapplicationDelay");
        this.durationOnUse = compoundTag.getInt("DurationOnUse");
        this.radiusOnUse = compoundTag.getFloat("RadiusOnUse");
        this.radiusPerTick = compoundTag.getFloat("RadiusPerTick");
        this.setRadius(compoundTag.getFloat("Radius"));
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
        RegistryOps<Tag> registryOps = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (compoundTag.contains("Particle", 10)) {
            ParticleTypes.CODEC.parse(registryOps, (Object)compoundTag.get("Particle")).resultOrPartial(string -> LOGGER.warn("Failed to parse area effect cloud particle options: '{}'", string)).ifPresent(this::setParticle);
        }
        if (compoundTag.contains("potion_contents")) {
            PotionContents.CODEC.parse(registryOps, (Object)compoundTag.get("potion_contents")).resultOrPartial(string -> LOGGER.warn("Failed to parse area effect cloud potions: '{}'", string)).ifPresent(this::setPotionContents);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Age", this.tickCount);
        compoundTag.putInt("Duration", this.duration);
        compoundTag.putInt("WaitTime", this.waitTime);
        compoundTag.putInt("ReapplicationDelay", this.reapplicationDelay);
        compoundTag.putInt("DurationOnUse", this.durationOnUse);
        compoundTag.putFloat("RadiusOnUse", this.radiusOnUse);
        compoundTag.putFloat("RadiusPerTick", this.radiusPerTick);
        compoundTag.putFloat("Radius", this.getRadius());
        RegistryOps<Tag> registryOps = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        compoundTag.put("Particle", (Tag)ParticleTypes.CODEC.encodeStart(registryOps, (Object)this.getParticle()).getOrThrow());
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (!this.potionContents.equals(PotionContents.EMPTY)) {
            Tag tag = (Tag)PotionContents.CODEC.encodeStart(registryOps, (Object)this.potionContents).getOrThrow();
            compoundTag.put("potion_contents", tag);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_RADIUS.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0f, 0.5f);
    }

    @Override
    @Nullable
    public /* synthetic */ Entity getOwner() {
        return this.getOwner();
    }
}

