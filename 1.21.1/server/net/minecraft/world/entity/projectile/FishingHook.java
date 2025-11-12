/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.projectile;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class FishingHook
extends Projectile {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RandomSource syncronizedRandom = RandomSource.create();
    private boolean biting;
    private int outOfWaterTime;
    private static final int MAX_OUT_OF_WATER_TIME = 10;
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater = true;
    @Nullable
    private Entity hookedIn;
    private FishHookState currentState = FishHookState.FLYING;
    private final int luck;
    private final int lureSpeed;

    private FishingHook(EntityType<? extends FishingHook> entityType, Level level, int n, int n2) {
        super((EntityType<? extends Projectile>)entityType, level);
        this.noCulling = true;
        this.luck = Math.max(0, n);
        this.lureSpeed = Math.max(0, n2);
    }

    public FishingHook(EntityType<? extends FishingHook> entityType, Level level) {
        this(entityType, level, 0, 0);
    }

    public FishingHook(Player player, Level level, int n, int n2) {
        this(EntityType.FISHING_BOBBER, level, n, n2);
        this.setOwner(player);
        float f = player.getXRot();
        float f2 = player.getYRot();
        float f3 = Mth.cos(-f2 * ((float)Math.PI / 180) - (float)Math.PI);
        float f4 = Mth.sin(-f2 * ((float)Math.PI / 180) - (float)Math.PI);
        float f5 = -Mth.cos(-f * ((float)Math.PI / 180));
        float f6 = Mth.sin(-f * ((float)Math.PI / 180));
        double d = player.getX() - (double)f4 * 0.3;
        double d2 = player.getEyeY();
        double d3 = player.getZ() - (double)f3 * 0.3;
        this.moveTo(d, d2, d3, f2, f);
        Vec3 vec3 = new Vec3(-f4, Mth.clamp(-(f6 / f5), -5.0f, 5.0f), -f3);
        double d4 = vec3.length();
        vec3 = vec3.multiply(0.6 / d4 + this.random.triangle(0.5, 0.0103365), 0.6 / d4 + this.random.triangle(0.5, 0.0103365), 0.6 / d4 + this.random.triangle(0.5, 0.0103365));
        this.setDeltaMovement(vec3);
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HOOKED_ENTITY, 0);
        builder.define(DATA_BITING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_HOOKED_ENTITY.equals(entityDataAccessor)) {
            int n = this.getEntityData().get(DATA_HOOKED_ENTITY);
            Entity entity = this.hookedIn = n > 0 ? this.level().getEntity(n - 1) : null;
        }
        if (DATA_BITING.equals(entityDataAccessor)) {
            this.biting = this.getEntityData().get(DATA_BITING);
            if (this.biting) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.4f * Mth.nextFloat(this.syncronizedRandom, 0.6f, 1.0f), this.getDeltaMovement().z);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = 64.0;
        return d < 4096.0;
    }

    @Override
    public void lerpTo(double d, double d2, double d3, float f, float f2, int n) {
    }

    @Override
    public void tick() {
        boolean bl;
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level().getGameTime());
        super.tick();
        Player player = this.getPlayerOwner();
        if (player == null) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide && this.shouldStopFishing(player)) {
            return;
        }
        if (this.onGround()) {
            ++this.life;
            if (this.life >= 1200) {
                this.discard();
                return;
            }
        } else {
            this.life = 0;
        }
        float f = 0.0f;
        BlockPos blockPos = this.blockPosition();
        FluidState fluidState = this.level().getFluidState(blockPos);
        if (fluidState.is(FluidTags.WATER)) {
            f = fluidState.getHeight(this.level(), blockPos);
        }
        boolean bl2 = bl = f > 0.0f;
        if (this.currentState == FishHookState.FLYING) {
            if (this.hookedIn != null) {
                this.setDeltaMovement(Vec3.ZERO);
                this.currentState = FishHookState.HOOKED_IN_ENTITY;
                return;
            }
            if (bl) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                this.currentState = FishHookState.BOBBING;
                return;
            }
            this.checkCollision();
        } else {
            if (this.currentState == FishHookState.HOOKED_IN_ENTITY) {
                if (this.hookedIn != null) {
                    if (this.hookedIn.isRemoved() || this.hookedIn.level().dimension() != this.level().dimension()) {
                        this.setHookedEntity(null);
                        this.currentState = FishHookState.FLYING;
                    } else {
                        this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
                    }
                }
                return;
            }
            if (this.currentState == FishHookState.BOBBING) {
                Vec3 vec3 = this.getDeltaMovement();
                double d = this.getY() + vec3.y - (double)blockPos.getY() - (double)f;
                if (Math.abs(d) < 0.01) {
                    d += Math.signum(d) * 0.1;
                }
                this.setDeltaMovement(vec3.x * 0.9, vec3.y - d * (double)this.random.nextFloat() * 0.2, vec3.z * 0.9);
                this.openWater = this.nibble > 0 || this.timeUntilHooked > 0 ? this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater(blockPos) : true;
                if (bl) {
                    this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                    if (this.biting) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.1 * (double)this.syncronizedRandom.nextFloat() * (double)this.syncronizedRandom.nextFloat(), 0.0));
                    }
                    if (!this.level().isClientSide) {
                        this.catchingFish(blockPos);
                    }
                } else {
                    this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                }
            }
        }
        if (!fluidState.is(FluidTags.WATER)) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.updateRotation();
        if (this.currentState == FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        double d = 0.92;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
        this.reapplyPosition();
    }

    private boolean shouldStopFishing(Player player) {
        ItemStack itemStack = player.getMainHandItem();
        ItemStack itemStack2 = player.getOffhandItem();
        boolean bl = itemStack.is(Items.FISHING_ROD);
        boolean bl2 = itemStack2.is(Items.FISHING_ROD);
        if (player.isRemoved() || !player.isAlive() || !bl && !bl2 || this.distanceToSqr(player) > 1024.0) {
            this.discard();
            return true;
        }
        return false;
    }

    private void checkCollision() {
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.hitTargetOrDeflectSelf(hitResult);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (!this.level().isClientSide) {
            this.setHookedEntity(entityHitResult.getEntity());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(blockHitResult.distanceTo(this)));
    }

    private void setHookedEntity(@Nullable Entity entity) {
        this.hookedIn = entity;
        this.getEntityData().set(DATA_HOOKED_ENTITY, entity == null ? 0 : entity.getId() + 1);
    }

    private void catchingFish(BlockPos blockPos) {
        ServerLevel serverLevel = (ServerLevel)this.level();
        int n = 1;
        BlockPos blockPos2 = blockPos.above();
        if (this.random.nextFloat() < 0.25f && this.level().isRainingAt(blockPos2)) {
            ++n;
        }
        if (this.random.nextFloat() < 0.5f && !this.level().canSeeSky(blockPos2)) {
            --n;
        }
        if (this.nibble > 0) {
            --this.nibble;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.getEntityData().set(DATA_BITING, false);
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= n;
            if (this.timeUntilHooked > 0) {
                double d;
                double d2;
                this.fishAngle += (float)this.random.triangle(0.0, 9.188);
                float f = this.fishAngle * ((float)Math.PI / 180);
                float f2 = Mth.sin(f);
                float f3 = Mth.cos(f);
                double d3 = this.getX() + (double)(f2 * (float)this.timeUntilHooked * 0.1f);
                BlockState blockState = serverLevel.getBlockState(BlockPos.containing(d3, (d2 = (double)((float)Mth.floor(this.getY()) + 1.0f)) - 1.0, d = this.getZ() + (double)(f3 * (float)this.timeUntilHooked * 0.1f)));
                if (blockState.is(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15f) {
                        serverLevel.sendParticles(ParticleTypes.BUBBLE, d3, d2 - (double)0.1f, d, 1, f2, 0.1, f3, 0.0);
                    }
                    float f4 = f2 * 0.04f;
                    float f5 = f3 * 0.04f;
                    serverLevel.sendParticles(ParticleTypes.FISHING, d3, d2, d, 0, f5, 0.01, -f4, 1.0);
                    serverLevel.sendParticles(ParticleTypes.FISHING, d3, d2, d, 0, -f5, 0.01, f4, 1.0);
                }
            } else {
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                double d = this.getY() + 0.5;
                serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), d, this.getZ(), (int)(1.0f + this.getBbWidth() * 20.0f), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2f);
                serverLevel.sendParticles(ParticleTypes.FISHING, this.getX(), d, this.getZ(), (int)(1.0f + this.getBbWidth() * 20.0f), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2f);
                this.nibble = Mth.nextInt(this.random, 20, 40);
                this.getEntityData().set(DATA_BITING, true);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured -= n;
            float f = 0.15f;
            if (this.timeUntilLured < 20) {
                f += (float)(20 - this.timeUntilLured) * 0.05f;
            } else if (this.timeUntilLured < 40) {
                f += (float)(40 - this.timeUntilLured) * 0.02f;
            } else if (this.timeUntilLured < 60) {
                f += (float)(60 - this.timeUntilLured) * 0.01f;
            }
            if (this.random.nextFloat() < f) {
                double d;
                double d4;
                float f6 = Mth.nextFloat(this.random, 0.0f, 360.0f) * ((float)Math.PI / 180);
                float f7 = Mth.nextFloat(this.random, 25.0f, 60.0f);
                double d5 = this.getX() + (double)(Mth.sin(f6) * f7) * 0.1;
                BlockState blockState = serverLevel.getBlockState(BlockPos.containing(d5, (d4 = (double)((float)Mth.floor(this.getY()) + 1.0f)) - 1.0, d = this.getZ() + (double)(Mth.cos(f6) * f7) * 0.1));
                if (blockState.is(Blocks.WATER)) {
                    serverLevel.sendParticles(ParticleTypes.SPLASH, d5, d4, d, 2 + this.random.nextInt(2), 0.1f, 0.0, 0.1f, 0.0);
                }
            }
            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0f, 360.0f);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        } else {
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
            this.timeUntilLured -= this.lureSpeed;
        }
    }

    private boolean calculateOpenWater(BlockPos blockPos) {
        OpenWaterType openWaterType = OpenWaterType.INVALID;
        for (int i = -1; i <= 2; ++i) {
            OpenWaterType openWaterType2 = this.getOpenWaterTypeForArea(blockPos.offset(-2, i, -2), blockPos.offset(2, i, 2));
            switch (openWaterType2.ordinal()) {
                case 2: {
                    return false;
                }
                case 0: {
                    if (openWaterType != OpenWaterType.INVALID) break;
                    return false;
                }
                case 1: {
                    if (openWaterType != OpenWaterType.ABOVE_WATER) break;
                    return false;
                }
            }
            openWaterType = openWaterType2;
        }
        return true;
    }

    private OpenWaterType getOpenWaterTypeForArea(BlockPos blockPos, BlockPos blockPos2) {
        return BlockPos.betweenClosedStream(blockPos, blockPos2).map(this::getOpenWaterTypeForBlock).reduce((openWaterType, openWaterType2) -> openWaterType == openWaterType2 ? openWaterType : OpenWaterType.INVALID).orElse(OpenWaterType.INVALID);
    }

    private OpenWaterType getOpenWaterTypeForBlock(BlockPos blockPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.isAir() || blockState.is(Blocks.LILY_PAD)) {
            return OpenWaterType.ABOVE_WATER;
        }
        FluidState fluidState = blockState.getFluidState();
        if (fluidState.is(FluidTags.WATER) && fluidState.isSource() && blockState.getCollisionShape(this.level(), blockPos).isEmpty()) {
            return OpenWaterType.INSIDE_WATER;
        }
        return OpenWaterType.INVALID;
    }

    public boolean isOpenWaterFishing() {
        return this.openWater;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    public int retrieve(ItemStack itemStack) {
        Player player = this.getPlayerOwner();
        if (this.level().isClientSide || player == null || this.shouldStopFishing(player)) {
            return 0;
        }
        int n = 0;
        if (this.hookedIn != null) {
            this.pullEntity(this.hookedIn);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, itemStack, this, Collections.emptyList());
            this.level().broadcastEntityEvent(this, (byte)31);
            n = this.hookedIn instanceof ItemEntity ? 3 : 5;
        } else if (this.nibble > 0) {
            LootParams lootParams = new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, itemStack).withParameter(LootContextParams.THIS_ENTITY, this).withLuck((float)this.luck + player.getLuck()).create(LootContextParamSets.FISHING);
            LootTable lootTable = this.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
            ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, itemStack, this, (Collection<ItemStack>)objectArrayList);
            for (ItemStack itemStack2 : objectArrayList) {
                ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack2);
                double d = player.getX() - this.getX();
                double d2 = player.getY() - this.getY();
                double d3 = player.getZ() - this.getZ();
                double d4 = 0.1;
                itemEntity.setDeltaMovement(d * 0.1, d2 * 0.1 + Math.sqrt(Math.sqrt(d * d + d2 * d2 + d3 * d3)) * 0.08, d3 * 0.1);
                this.level().addFreshEntity(itemEntity);
                player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
                if (!itemStack2.is(ItemTags.FISHES)) continue;
                player.awardStat(Stats.FISH_CAUGHT, 1);
            }
            n = 1;
        }
        if (this.onGround()) {
            n = 2;
        }
        this.discard();
        return n;
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 31 && this.level().isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }
        super.handleEntityEvent(by);
    }

    protected void pullEntity(Entity entity) {
        Entity entity2 = this.getOwner();
        if (entity2 == null) {
            return;
        }
        Vec3 vec3 = new Vec3(entity2.getX() - this.getX(), entity2.getY() - this.getY(), entity2.getZ() - this.getZ()).scale(0.1);
        entity.setDeltaMovement(entity.getDeltaMovement().add(vec3));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        this.updateOwnerInfo(null);
        super.remove(removalReason);
    }

    @Override
    public void onClientRemoval() {
        this.updateOwnerInfo(null);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.updateOwnerInfo(this);
    }

    private void updateOwnerInfo(@Nullable FishingHook fishingHook) {
        Player player = this.getPlayerOwner();
        if (player != null) {
            player.fishing = fishingHook;
        }
    }

    @Nullable
    public Player getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof Player ? (Player)entity : null;
    }

    @Nullable
    public Entity getHookedIn() {
        return this.hookedIn;
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket((Entity)this, serverEntity, entity == null ? this.getId() : entity.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        if (this.getPlayerOwner() == null) {
            int n = clientboundAddEntityPacket.getData();
            LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", (Object)this.level().getEntity(n), (Object)n);
            this.kill();
        }
    }

    static enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;

    }

    static enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;

    }
}

