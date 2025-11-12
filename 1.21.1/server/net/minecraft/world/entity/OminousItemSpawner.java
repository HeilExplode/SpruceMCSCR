/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public class OminousItemSpawner
extends Entity {
    private static final int SPAWN_ITEM_DELAY_MIN = 60;
    private static final int SPAWN_ITEM_DELAY_MAX = 120;
    private static final String TAG_SPAWN_ITEM_AFTER_TICKS = "spawn_item_after_ticks";
    private static final String TAG_ITEM = "item";
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(OminousItemSpawner.class, EntityDataSerializers.ITEM_STACK);
    public static final int TICKS_BEFORE_ABOUT_TO_SPAWN_SOUND = 36;
    private long spawnItemAfterTicks;

    public OminousItemSpawner(EntityType<? extends OminousItemSpawner> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public static OminousItemSpawner create(Level level, ItemStack itemStack) {
        OminousItemSpawner ominousItemSpawner = new OminousItemSpawner((EntityType<? extends OminousItemSpawner>)EntityType.OMINOUS_ITEM_SPAWNER, level);
        ominousItemSpawner.spawnItemAfterTicks = level.random.nextIntBetweenInclusive(60, 120);
        ominousItemSpawner.setItem(itemStack);
        return ominousItemSpawner;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.tickClient();
            return;
        }
        this.tickServer();
    }

    private void tickServer() {
        if ((long)this.tickCount == this.spawnItemAfterTicks - 36L) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundSource.NEUTRAL);
        }
        if ((long)this.tickCount >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill();
        }
    }

    private void tickClient() {
        if (this.level().getGameTime() % 5L == 0L) {
            this.addParticles();
        }
    }

    private void spawnItem() {
        Entity entity;
        Level level = this.level();
        ItemStack itemStack = this.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        Object object = itemStack.getItem();
        if (object instanceof ProjectileItem) {
            ProjectileItem projectileItem = (ProjectileItem)object;
            object = Direction.DOWN;
            Projectile projectile = projectileItem.asProjectile(level, this.position(), itemStack, (Direction)object);
            projectile.setOwner(this);
            ProjectileItem.DispenseConfig dispenseConfig = projectileItem.createDispenseConfig();
            projectileItem.shoot(projectile, ((Direction)object).getStepX(), ((Direction)object).getStepY(), ((Direction)object).getStepZ(), dispenseConfig.power(), dispenseConfig.uncertainty());
            dispenseConfig.overrideDispenseEvent().ifPresent(n -> level.levelEvent(n, this.blockPosition(), 0));
            entity = projectile;
        } else {
            entity = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), itemStack);
        }
        level.addFreshEntity(entity);
        level.levelEvent(3021, this.blockPosition(), 1);
        level.gameEvent(entity, GameEvent.ENTITY_PLACE, this.position());
        this.setItem(ItemStack.EMPTY);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        ItemStack itemStack = compoundTag.contains(TAG_ITEM, 10) ? ItemStack.parse(this.registryAccess(), compoundTag.getCompound(TAG_ITEM)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.setItem(itemStack);
        this.spawnItemAfterTicks = compoundTag.getLong(TAG_SPAWN_ITEM_AFTER_TICKS);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (!this.getItem().isEmpty()) {
            compoundTag.put(TAG_ITEM, this.getItem().save(this.registryAccess()).copy());
        }
        compoundTag.putLong(TAG_SPAWN_ITEM_AFTER_TICKS, this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity entity) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public void addParticles() {
        Vec3 vec3 = this.position();
        int n = this.random.nextIntBetweenInclusive(1, 3);
        for (int i = 0; i < n; ++i) {
            double d = 0.4;
            Vec3 vec32 = new Vec3(this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3 vec33 = vec3.vectorTo(vec32);
            this.level().addParticle(ParticleTypes.OMINOUS_SPAWNING, vec3.x(), vec3.y(), vec3.z(), vec33.x(), vec33.y(), vec33.z());
        }
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    private void setItem(ItemStack itemStack) {
        this.getEntityData().set(DATA_ITEM, itemStack);
    }
}

