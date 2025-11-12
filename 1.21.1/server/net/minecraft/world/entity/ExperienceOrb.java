/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ExperienceOrb
extends Entity {
    private static final int LIFETIME = 6000;
    private static final int ENTITY_SCAN_PERIOD = 20;
    private static final int MAX_FOLLOW_DIST = 8;
    private static final int ORB_GROUPS_PER_AREA = 40;
    private static final double ORB_MERGE_DISTANCE = 0.5;
    private int age;
    private int health = 5;
    private int value;
    private int count = 1;
    private Player followingPlayer;

    public ExperienceOrb(Level level, double d, double d2, double d3, int n) {
        this((EntityType<? extends ExperienceOrb>)EntityType.EXPERIENCE_ORB, level);
        this.setPos(d, d2, d3);
        this.setYRot((float)(this.random.nextDouble() * 360.0));
        this.setDeltaMovement((this.random.nextDouble() * (double)0.2f - (double)0.1f) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * (double)0.2f - (double)0.1f) * 2.0);
        this.value = n;
    }

    public ExperienceOrb(EntityType<? extends ExperienceOrb> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        Vec3 vec3;
        double d;
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else {
            this.applyGravity();
        }
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }
        if (this.tickCount % 20 == 1) {
            this.scanForEntities();
        }
        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }
        if (this.followingPlayer != null && (d = (vec3 = new Vec3(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(), this.followingPlayer.getZ() - this.getZ())).lengthSqr()) < 64.0) {
            double d2 = 1.0 - Math.sqrt(d) / 8.0;
            this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d2 * d2 * 0.1)));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        float f = 0.98f;
        if (this.onGround()) {
            f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98f;
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.98, f));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));
        }
        ++this.age;
        if (this.age >= 6000) {
            this.discard();
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999f);
    }

    private void scanForEntities() {
        if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0) {
            this.followingPlayer = this.level().getNearestPlayer(this, 8.0);
        }
        if (this.level() instanceof ServerLevel) {
            List<ExperienceOrb> list = this.level().getEntities(EntityTypeTest.forClass(ExperienceOrb.class), this.getBoundingBox().inflate(0.5), this::canMerge);
            for (ExperienceOrb experienceOrb : list) {
                this.merge(experienceOrb);
            }
        }
    }

    public static void award(ServerLevel serverLevel, Vec3 vec3, int n) {
        while (n > 0) {
            int n2 = ExperienceOrb.getExperienceValue(n);
            n -= n2;
            if (ExperienceOrb.tryMergeToExisting(serverLevel, vec3, n2)) continue;
            serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, vec3.x(), vec3.y(), vec3.z(), n2));
        }
    }

    private static boolean tryMergeToExisting(ServerLevel serverLevel, Vec3 vec3, int n) {
        AABB aABB = AABB.ofSize(vec3, 1.0, 1.0, 1.0);
        int n2 = serverLevel.getRandom().nextInt(40);
        List<ExperienceOrb> list = serverLevel.getEntities(EntityTypeTest.forClass(ExperienceOrb.class), aABB, experienceOrb -> ExperienceOrb.canMerge(experienceOrb, n2, n));
        if (!list.isEmpty()) {
            ExperienceOrb experienceOrb2 = list.get(0);
            ++experienceOrb2.count;
            experienceOrb2.age = 0;
            return true;
        }
        return false;
    }

    private boolean canMerge(ExperienceOrb experienceOrb) {
        return experienceOrb != this && ExperienceOrb.canMerge(experienceOrb, this.getId(), this.value);
    }

    private static boolean canMerge(ExperienceOrb experienceOrb, int n, int n2) {
        return !experienceOrb.isRemoved() && (experienceOrb.getId() - n) % 40 == 0 && experienceOrb.value == n2;
    }

    private void merge(ExperienceOrb experienceOrb) {
        this.count += experienceOrb.count;
        this.age = Math.min(this.age, experienceOrb.age);
        experienceOrb.discard();
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)0.99f, Math.min(vec3.y + (double)5.0E-4f, (double)0.06f), vec3.z * (double)0.99f);
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (this.level().isClientSide) {
            return true;
        }
        this.markHurt();
        this.health = (int)((float)this.health - f);
        if (this.health <= 0) {
            this.discard();
        }
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putShort("Health", (short)this.health);
        compoundTag.putShort("Age", (short)this.age);
        compoundTag.putShort("Value", (short)this.value);
        compoundTag.putInt("Count", this.count);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.health = compoundTag.getShort("Health");
        this.age = compoundTag.getShort("Age");
        this.value = compoundTag.getShort("Value");
        this.count = Math.max(compoundTag.getInt("Count"), 1);
    }

    @Override
    public void playerTouch(Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (player.takeXpDelay == 0) {
            player.takeXpDelay = 2;
            player.take(this, 1);
            int n = this.repairPlayerItems(serverPlayer, this.value);
            if (n > 0) {
                player.giveExperiencePoints(n);
            }
            --this.count;
            if (this.count == 0) {
                this.discard();
            }
        }
    }

    private int repairPlayerItems(ServerPlayer serverPlayer, int n) {
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, serverPlayer, ItemStack::isDamaged);
        if (optional.isPresent()) {
            int n2;
            ItemStack itemStack = optional.get().itemStack();
            int n3 = EnchantmentHelper.modifyDurabilityToRepairFromXp(serverPlayer.serverLevel(), itemStack, n);
            int n4 = Math.min(n3, itemStack.getDamageValue());
            itemStack.setDamageValue(itemStack.getDamageValue() - n4);
            if (n4 > 0 && (n2 = n - n4 * n / n3) > 0) {
                return this.repairPlayerItems(serverPlayer, n2);
            }
            return 0;
        }
        return n;
    }

    public int getValue() {
        return this.value;
    }

    public int getIcon() {
        if (this.value >= 2477) {
            return 10;
        }
        if (this.value >= 1237) {
            return 9;
        }
        if (this.value >= 617) {
            return 8;
        }
        if (this.value >= 307) {
            return 7;
        }
        if (this.value >= 149) {
            return 6;
        }
        if (this.value >= 73) {
            return 5;
        }
        if (this.value >= 37) {
            return 4;
        }
        if (this.value >= 17) {
            return 3;
        }
        if (this.value >= 7) {
            return 2;
        }
        if (this.value >= 3) {
            return 1;
        }
        return 0;
    }

    public static int getExperienceValue(int n) {
        if (n >= 2477) {
            return 2477;
        }
        if (n >= 1237) {
            return 1237;
        }
        if (n >= 617) {
            return 617;
        }
        if (n >= 307) {
            return 307;
        }
        if (n >= 149) {
            return 149;
        }
        if (n >= 73) {
            return 73;
        }
        if (n >= 37) {
            return 37;
        }
        if (n >= 17) {
            return 17;
        }
        if (n >= 7) {
            return 7;
        }
        if (n >= 3) {
            return 3;
        }
        return 1;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddExperienceOrbPacket(this, serverEntity);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }
}

