/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractArrow
extends Projectile {
    private static final double ARROW_BASE_DAMAGE = 2.0;
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final int FLAG_CRIT = 1;
    private static final int FLAG_NOPHYSICS = 2;
    @Nullable
    private BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime;
    public Pickup pickup = Pickup.DISALLOWED;
    public int shakeTime;
    private int life;
    private double baseDamage = 2.0;
    private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
    @Nullable
    private IntOpenHashSet piercingIgnoreEntityIds;
    @Nullable
    private List<Entity> piercedAndKilledEntities;
    private ItemStack pickupItemStack = this.getDefaultPickupItem();
    @Nullable
    private ItemStack firedFromWeapon = null;

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, double d, double d2, double d3, Level level, ItemStack itemStack, @Nullable ItemStack itemStack2) {
        this(entityType, level);
        this.pickupItemStack = itemStack.copy();
        this.setCustomName(itemStack.get(DataComponents.CUSTOM_NAME));
        Unit unit = itemStack.remove(DataComponents.INTANGIBLE_PROJECTILE);
        if (unit != null) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
        this.setPos(d, d2, d3);
        if (itemStack2 != null && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (itemStack2.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an arrow");
            }
            this.firedFromWeapon = itemStack2.copy();
            int n = EnchantmentHelper.getPiercingCount(serverLevel, itemStack2, this.pickupItemStack);
            if (n > 0) {
                this.setPierceLevel((byte)n);
            }
            EnchantmentHelper.onProjectileSpawned(serverLevel, itemStack2, this, item -> {
                this.firedFromWeapon = null;
            });
        }
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, LivingEntity livingEntity, Level level, ItemStack itemStack, @Nullable ItemStack itemStack2) {
        this(entityType, livingEntity.getX(), livingEntity.getEyeY() - (double)0.1f, livingEntity.getZ(), level, itemStack, itemStack2);
        this.setOwner(livingEntity);
    }

    public void setSoundEvent(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d2)) {
            d2 = 1.0;
        }
        return d < (d2 *= 64.0 * AbstractArrow.getViewScale()) * d2;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_FLAGS, (byte)0);
        builder.define(PIERCE_LEVEL, (byte)0);
    }

    @Override
    public void shoot(double d, double d2, double d3, float f, float f2) {
        super.shoot(d, d2, d3, f, f2);
        this.life = 0;
    }

    @Override
    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
        this.setPos(d, d2, d3);
        this.setRot(f, f2);
    }

    @Override
    public void lerpMotion(double d, double d2, double d3) {
        super.lerpMotion(d, d2, d3);
        this.life = 0;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void tick() {
        Vec3 vec3;
        Object object;
        super.tick();
        boolean bl = this.isNoPhysics();
        Vec3 vec32 = this.getDeltaMovement();
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double d = vec32.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vec32.x, vec32.z) * 57.2957763671875));
            this.setXRot((float)(Mth.atan2(vec32.y, d) * 57.2957763671875));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        BlockPos blockPos = this.blockPosition();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (!(blockState.isAir() || bl || ((VoxelShape)(object = blockState.getCollisionShape(this.level(), blockPos))).isEmpty())) {
            vec3 = this.position();
            for (AABB object2 : ((VoxelShape)object).toAabbs()) {
                if (!object2.move(blockPos).contains(vec3)) continue;
                this.inGround = true;
                break;
            }
        }
        if (this.shakeTime > 0) {
            --this.shakeTime;
        }
        if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }
        if (this.inGround && !bl) {
            if (this.lastState != blockState && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level().isClientSide) {
                this.tickDespawn();
            }
            ++this.inGroundTime;
            return;
        }
        this.inGroundTime = 0;
        object = this.position();
        vec3 = ((Vec3)object).add(vec32);
        Object object3 = this.level().clip(new ClipContext((Vec3)object, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (((HitResult)object3).getType() != HitResult.Type.MISS) {
            vec3 = ((HitResult)object3).getLocation();
        }
        while (!this.isRemoved()) {
            void var8_13;
            Object object2;
            EntityHitResult entityHitResult = this.findHitEntity((Vec3)object, vec3);
            if (entityHitResult != null) {
                object3 = entityHitResult;
            }
            if (object3 != null && ((HitResult)object3).getType() == HitResult.Type.ENTITY) {
                object2 = ((EntityHitResult)object3).getEntity();
                Entity entity = this.getOwner();
                if (object2 instanceof Player && entity instanceof Player && !((Player)entity).canHarmPlayer((Player)object2)) {
                    object3 = null;
                    Object var8_12 = null;
                }
            }
            if (object3 != null && !bl) {
                object2 = this.hitTargetOrDeflectSelf((HitResult)object3);
                this.hasImpulse = true;
                if (object2 != ProjectileDeflection.NONE) break;
            }
            if (var8_13 == null || this.getPierceLevel() <= 0) break;
            object3 = null;
        }
        vec32 = this.getDeltaMovement();
        double d = vec32.x;
        double d2 = vec32.y;
        double d3 = vec32.z;
        if (this.isCritArrow()) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.CRIT, this.getX() + d * (double)i / 4.0, this.getY() + d2 * (double)i / 4.0, this.getZ() + d3 * (double)i / 4.0, -d, -d2 + 0.2, -d3);
            }
        }
        double d4 = this.getX() + d;
        double d5 = this.getY() + d2;
        double d6 = this.getZ() + d3;
        double d7 = vec32.horizontalDistance();
        if (bl) {
            this.setYRot((float)(Mth.atan2(-d, -d3) * 57.2957763671875));
        } else {
            this.setYRot((float)(Mth.atan2(d, d3) * 57.2957763671875));
        }
        this.setXRot((float)(Mth.atan2(d2, d7) * 57.2957763671875));
        this.setXRot(AbstractArrow.lerpRotation(this.xRotO, this.getXRot()));
        this.setYRot(AbstractArrow.lerpRotation(this.yRotO, this.getYRot()));
        float f = 0.99f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f2 = 0.25f;
                this.level().addParticle(ParticleTypes.BUBBLE, d4 - d * 0.25, d5 - d2 * 0.25, d6 - d3 * 0.25, d, d2, d3);
            }
            f = this.getWaterInertia();
        }
        this.setDeltaMovement(vec32.scale(f));
        if (!bl) {
            this.applyGravity();
        }
        this.setPos(d4, d5, d6);
        this.checkInsideBlocks();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    private boolean shouldFall() {
        return this.inGround && this.level().noCollision(new AABB(this.position(), this.position()).inflate(0.06));
    }

    private void startFalling() {
        this.inGround = false;
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        super.move(moverType, vec3);
        if (moverType != MoverType.SELF && this.shouldFall()) {
            this.startFalling();
        }
    }

    protected void tickDespawn() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
        }
    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }
        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Object object;
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        float f = (float)this.getDeltaMovement().length();
        double d = this.baseDamage;
        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.damageSources().arrow(this, entity2 != null ? entity2 : this);
        if (this.getWeaponItem() != null && (object = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            d = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, (float)d);
        }
        int n = Mth.ceil(Mth.clamp((double)f * d, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity((int)5);
            }
            if (this.piercingIgnoreEntityIds.size() < this.getPierceLevel() + 1) {
                this.piercingIgnoreEntityIds.add(entity.getId());
            } else {
                this.discard();
                return;
            }
        }
        if (this.isCritArrow()) {
            long l = this.random.nextInt(n / 2 + 2);
            n = (int)Math.min(l + (long)n, Integer.MAX_VALUE);
        }
        if (entity2 instanceof LivingEntity) {
            object = (LivingEntity)entity2;
            ((LivingEntity)object).setLastHurtMob(entity);
        }
        boolean bl = entity.getType() == EntityType.ENDERMAN;
        int n2 = entity.getRemainingFireTicks();
        if (this.isOnFire() && !bl) {
            entity.igniteForSeconds(5.0f);
        }
        if (entity.hurt(damageSource, n)) {
            if (bl) {
                return;
            }
            if (entity instanceof LivingEntity) {
                Object object2;
                LivingEntity livingEntity = (LivingEntity)entity;
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingEntity.setArrowCount(livingEntity.getArrowCount() + 1);
                }
                this.doKnockback(livingEntity, damageSource);
                Level level = this.level();
                if (level instanceof ServerLevel) {
                    object2 = (ServerLevel)level;
                    EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel)object2, livingEntity, damageSource, this.getWeaponItem());
                }
                this.doPostHurtEffects(livingEntity);
                if (livingEntity != entity2 && livingEntity instanceof Player && entity2 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)entity2).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0f));
                }
                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingEntity);
                }
                if (!this.level().isClientSide && entity2 instanceof ServerPlayer) {
                    object2 = (ServerPlayer)entity2;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger((ServerPlayer)object2, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger((ServerPlayer)object2, Arrays.asList(entity));
                    }
                }
            }
            this.playSound(this.soundEvent, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(n2);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1f);
                }
                this.discard();
            }
        }
    }

    protected void doKnockback(LivingEntity livingEntity, DamageSource damageSource) {
        float f;
        Level level;
        if (this.firedFromWeapon != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            f = EnchantmentHelper.modifyKnockback(serverLevel, this.firedFromWeapon, livingEntity, damageSource, 0.0f);
        } else {
            f = 0.0f;
        }
        double d = f;
        if (d > 0.0) {
            double d2 = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(d * 0.6 * d2);
            if (vec3.lengthSqr() > 0.0) {
                livingEntity.push(vec3.x, 0.1, vec3.z);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        Object object;
        this.lastState = this.level().getBlockState(blockHitResult.getBlockPos());
        super.onHitBlock(blockHitResult);
        Vec3 vec3 = blockHitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        ItemStack itemStack = this.getWeaponItem();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            object = (ServerLevel)level;
            if (itemStack != null) {
                this.hitBlockEnchantmentEffects((ServerLevel)object, blockHitResult, itemStack);
            }
        }
        object = vec3.normalize().scale(0.05f);
        this.setPosRaw(this.getX() - ((Vec3)object).x, this.getY() - ((Vec3)object).y, this.getZ() - ((Vec3)object).z);
        this.playSound(this.getHitGroundSoundEvent(), 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
        this.inGround = true;
        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setSoundEvent(SoundEvents.ARROW_HIT);
        this.resetPiercedEntities();
    }

    protected void hitBlockEnchantmentEffects(ServerLevel serverLevel, BlockHitResult blockHitResult, ItemStack itemStack) {
        LivingEntity livingEntity;
        Vec3 vec3 = blockHitResult.getBlockPos().clampLocationWithin(blockHitResult.getLocation());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock(serverLevel, itemStack, entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null, this, null, vec3, serverLevel.getBlockState(blockHitResult.getBlockPos()), item -> {
            this.firedFromWeapon = null;
        });
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.firedFromWeapon;
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    protected final SoundEvent getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected void doPostHurtEffects(LivingEntity livingEntity) {
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec32) {
        return ProjectileUtil.getEntityHitResult(this.level(), this, vec3, vec32, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putShort("life", (short)this.life);
        if (this.lastState != null) {
            compoundTag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }
        compoundTag.putByte("shake", (byte)this.shakeTime);
        compoundTag.putBoolean("inGround", this.inGround);
        compoundTag.putByte("pickup", (byte)this.pickup.ordinal());
        compoundTag.putDouble("damage", this.baseDamage);
        compoundTag.putBoolean("crit", this.isCritArrow());
        compoundTag.putByte("PierceLevel", this.getPierceLevel());
        compoundTag.putString("SoundEvent", BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent).toString());
        compoundTag.put("item", this.pickupItemStack.save(this.registryAccess()));
        if (this.firedFromWeapon != null) {
            compoundTag.put("weapon", this.firedFromWeapon.save(this.registryAccess(), new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.life = compoundTag.getShort("life");
        if (compoundTag.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound("inBlockState"));
        }
        this.shakeTime = compoundTag.getByte("shake") & 0xFF;
        this.inGround = compoundTag.getBoolean("inGround");
        if (compoundTag.contains("damage", 99)) {
            this.baseDamage = compoundTag.getDouble("damage");
        }
        this.pickup = Pickup.byOrdinal(compoundTag.getByte("pickup"));
        this.setCritArrow(compoundTag.getBoolean("crit"));
        this.setPierceLevel(compoundTag.getByte("PierceLevel"));
        if (compoundTag.contains("SoundEvent", 8)) {
            this.soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse(compoundTag.getString("SoundEvent"))).orElse(this.getDefaultHitGroundSoundEvent());
        }
        if (compoundTag.contains("item", 10)) {
            this.setPickupItemStack(ItemStack.parse(this.registryAccess(), compoundTag.getCompound("item")).orElse(this.getDefaultPickupItem()));
        } else {
            this.setPickupItemStack(this.getDefaultPickupItem());
        }
        this.firedFromWeapon = compoundTag.contains("weapon", 10) ? (ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("weapon")).orElse(null) : null;
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        Pickup pickup;
        super.setOwner(entity);
        Entity entity2 = entity;
        int n = 0;
        block4: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Player.class, OminousItemSpawner.class}, (Object)entity2, n)) {
                case 0: {
                    Player player = (Player)entity2;
                    if (this.pickup != Pickup.DISALLOWED) {
                        n = 1;
                        continue block4;
                    }
                    pickup = Pickup.ALLOWED;
                    break block4;
                }
                case 1: {
                    OminousItemSpawner ominousItemSpawner = (OminousItemSpawner)entity2;
                    pickup = Pickup.DISALLOWED;
                    break block4;
                }
                default: {
                    pickup = this.pickup;
                    break block4;
                }
            }
            break;
        }
        this.pickup = pickup;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide || !this.inGround && !this.isNoPhysics() || this.shakeTime > 0) {
            return;
        }
        if (this.tryPickup(player)) {
            player.take(this, 1);
            this.discard();
        }
    }

    protected boolean tryPickup(Player player) {
        return switch (this.pickup.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 1 -> player.getInventory().add(this.getPickupItem());
            case 2 -> player.hasInfiniteMaterials();
        };
    }

    protected ItemStack getPickupItem() {
        return this.pickupItemStack.copy();
    }

    protected abstract ItemStack getDefaultPickupItem();

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    public ItemStack getPickupItemStackOrigin() {
        return this.pickupItemStack;
    }

    public void setBaseDamage(double d) {
        this.baseDamage = d;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    @Override
    public boolean isAttackable() {
        return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    public void setCritArrow(boolean bl) {
        this.setFlag(1, bl);
    }

    private void setPierceLevel(byte by) {
        this.entityData.set(PIERCE_LEVEL, by);
    }

    private void setFlag(int n, boolean bl) {
        byte by = this.entityData.get(ID_FLAGS);
        if (bl) {
            this.entityData.set(ID_FLAGS, (byte)(by | n));
        } else {
            this.entityData.set(ID_FLAGS, (byte)(by & ~n));
        }
    }

    protected void setPickupItemStack(ItemStack itemStack) {
        this.pickupItemStack = !itemStack.isEmpty() ? itemStack : this.getDefaultPickupItem();
    }

    public boolean isCritArrow() {
        byte by = this.entityData.get(ID_FLAGS);
        return (by & 1) != 0;
    }

    public boolean shotFromCrossbow() {
        return this.firedFromWeapon != null && this.firedFromWeapon.is(Items.CROSSBOW);
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    public void setBaseDamageFromMob(float f) {
        this.setBaseDamage((double)(f * 2.0f) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11, 0.57425));
    }

    protected float getWaterInertia() {
        return 0.6f;
    }

    public void setNoPhysics(boolean bl) {
        this.noPhysics = bl;
        this.setFlag(2, bl);
    }

    public boolean isNoPhysics() {
        if (!this.level().isClientSide) {
            return this.noPhysics;
        }
        return (this.entityData.get(ID_FLAGS) & 2) != 0;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.inGround;
    }

    @Override
    public SlotAccess getSlot(int n) {
        if (n == 0) {
            return SlotAccess.of(this::getPickupItemStackOrigin, this::setPickupItemStack);
        }
        return super.getSlot(n);
    }

    public static enum Pickup {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;


        public static Pickup byOrdinal(int n) {
            if (n < 0 || n > Pickup.values().length) {
                n = 0;
            }
            return Pickup.values()[n];
        }
    }
}

