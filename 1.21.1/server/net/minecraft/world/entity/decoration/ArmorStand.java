/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.decoration;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStand
extends LivingEntity {
    public static final int WOBBLE_TIME = 5;
    private static final boolean ENABLE_ARMS = true;
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0f, 0.0f, -10.0f);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0f, 0.0f, 10.0f);
    private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0f, 0.0f, -1.0f);
    private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0f, 0.0f, 1.0f);
    private static final EntityDimensions MARKER_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5f).withEyeHeight(0.9875f);
    private static final double FEET_OFFSET = 0.1;
    private static final double CHEST_OFFSET = 0.9;
    private static final double LEGS_OFFSET = 0.4;
    private static final double HEAD_OFFSET = 1.6;
    public static final int DISABLE_TAKING_OFFSET = 8;
    public static final int DISABLE_PUTTING_OFFSET = 16;
    public static final int CLIENT_FLAG_SMALL = 1;
    public static final int CLIENT_FLAG_SHOW_ARMS = 4;
    public static final int CLIENT_FLAG_NO_BASEPLATE = 8;
    public static final int CLIENT_FLAG_MARKER = 16;
    public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    private static final Predicate<Entity> RIDABLE_MINECARTS = entity -> entity instanceof AbstractMinecart && ((AbstractMinecart)entity).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    private boolean invisible;
    public long lastHit;
    private int disabledSlots;
    private Rotations headPose = DEFAULT_HEAD_POSE;
    private Rotations bodyPose = DEFAULT_BODY_POSE;
    private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
    private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;
    private Rotations leftLegPose = DEFAULT_LEFT_LEG_POSE;
    private Rotations rightLegPose = DEFAULT_RIGHT_LEG_POSE;

    public ArmorStand(EntityType<? extends ArmorStand> entityType, Level level) {
        super((EntityType<? extends LivingEntity>)entityType, level);
    }

    public ArmorStand(Level level, double d, double d2, double d3) {
        this((EntityType<? extends ArmorStand>)EntityType.ARMOR_STAND, level);
        this.setPos(d, d2, d3);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ArmorStand.createLivingAttributes().add(Attributes.STEP_HEIGHT, 0.0);
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        super.refreshDimensions();
        this.setPos(d, d2, d3);
    }

    private boolean hasPhysics() {
        return !this.isMarker() && !this.isNoGravity();
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && this.hasPhysics();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLIENT_FLAGS, (byte)0);
        builder.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
        builder.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
        builder.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
        builder.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
        builder.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
        builder.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case HAND: {
                return this.handItems.get(equipmentSlot.getIndex());
            }
            case HUMANOID_ARMOR: {
                return this.armorItems.get(equipmentSlot.getIndex());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return equipmentSlot != EquipmentSlot.BODY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.verifyEquippedItem(itemStack);
        switch (equipmentSlot.getType()) {
            case HAND: {
                this.onEquipItem(equipmentSlot, this.handItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
                break;
            }
            case HUMANOID_ARMOR: {
                this.onEquipItem(equipmentSlot, this.armorItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
            }
        }
    }

    @Override
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        return this.getItemBySlot(equipmentSlot).isEmpty() && !this.isDisabled(equipmentSlot);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ListTag listTag = new ListTag();
        for (ItemStack object : this.armorItems) {
            listTag.add(object.saveOptional(this.registryAccess()));
        }
        compoundTag.put("ArmorItems", listTag);
        ListTag listTag2 = new ListTag();
        for (ItemStack itemStack : this.handItems) {
            listTag2.add(itemStack.saveOptional(this.registryAccess()));
        }
        compoundTag.put("HandItems", listTag2);
        compoundTag.putBoolean("Invisible", this.isInvisible());
        compoundTag.putBoolean("Small", this.isSmall());
        compoundTag.putBoolean("ShowArms", this.isShowArms());
        compoundTag.putInt("DisabledSlots", this.disabledSlots);
        compoundTag.putBoolean("NoBasePlate", this.isNoBasePlate());
        if (this.isMarker()) {
            compoundTag.putBoolean("Marker", this.isMarker());
        }
        compoundTag.put("Pose", this.writePose());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        CompoundTag compoundTag2;
        int n;
        Tag tag;
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("ArmorItems", 9)) {
            tag = compoundTag.getList("ArmorItems", 10);
            for (n = 0; n < this.armorItems.size(); ++n) {
                compoundTag2 = tag.getCompound(n);
                this.armorItems.set(n, ItemStack.parseOptional(this.registryAccess(), compoundTag2));
            }
        }
        if (compoundTag.contains("HandItems", 9)) {
            tag = compoundTag.getList("HandItems", 10);
            for (n = 0; n < this.handItems.size(); ++n) {
                compoundTag2 = tag.getCompound(n);
                this.handItems.set(n, ItemStack.parseOptional(this.registryAccess(), compoundTag2));
            }
        }
        this.setInvisible(compoundTag.getBoolean("Invisible"));
        this.setSmall(compoundTag.getBoolean("Small"));
        this.setShowArms(compoundTag.getBoolean("ShowArms"));
        this.disabledSlots = compoundTag.getInt("DisabledSlots");
        this.setNoBasePlate(compoundTag.getBoolean("NoBasePlate"));
        this.setMarker(compoundTag.getBoolean("Marker"));
        this.noPhysics = !this.hasPhysics();
        tag = compoundTag.getCompound("Pose");
        this.readPose((CompoundTag)tag);
    }

    private void readPose(CompoundTag compoundTag) {
        ListTag listTag = compoundTag.getList("Head", 5);
        this.setHeadPose(listTag.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(listTag));
        ListTag listTag2 = compoundTag.getList("Body", 5);
        this.setBodyPose(listTag2.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(listTag2));
        ListTag listTag3 = compoundTag.getList("LeftArm", 5);
        this.setLeftArmPose(listTag3.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(listTag3));
        ListTag listTag4 = compoundTag.getList("RightArm", 5);
        this.setRightArmPose(listTag4.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(listTag4));
        ListTag listTag5 = compoundTag.getList("LeftLeg", 5);
        this.setLeftLegPose(listTag5.isEmpty() ? DEFAULT_LEFT_LEG_POSE : new Rotations(listTag5));
        ListTag listTag6 = compoundTag.getList("RightLeg", 5);
        this.setRightLegPose(listTag6.isEmpty() ? DEFAULT_RIGHT_LEG_POSE : new Rotations(listTag6));
    }

    private CompoundTag writePose() {
        CompoundTag compoundTag = new CompoundTag();
        if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
            compoundTag.put("Head", this.headPose.save());
        }
        if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
            compoundTag.put("Body", this.bodyPose.save());
        }
        if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
            compoundTag.put("LeftArm", this.leftArmPose.save());
        }
        if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
            compoundTag.put("RightArm", this.rightArmPose.save());
        }
        if (!DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
            compoundTag.put("LeftLeg", this.leftLegPose.save());
        }
        if (!DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
            compoundTag.put("RightLeg", this.rightLegPose.save());
        }
        return compoundTag;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);
        for (Entity entity : list) {
            if (!(this.distanceToSqr(entity) <= 0.2)) continue;
            entity.push(this);
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (this.isMarker() || itemStack.is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (player.isSpectator()) {
            return InteractionResult.SUCCESS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        if (itemStack.isEmpty()) {
            EquipmentSlot equipmentSlot2;
            EquipmentSlot equipmentSlot3 = this.getClickedSlot(vec3);
            EquipmentSlot equipmentSlot4 = equipmentSlot2 = this.isDisabled(equipmentSlot3) ? equipmentSlot : equipmentSlot3;
            if (this.hasItemInSlot(equipmentSlot2) && this.swapItem(player, equipmentSlot2, itemStack, interactionHand)) {
                return InteractionResult.SUCCESS;
            }
        } else {
            if (this.isDisabled(equipmentSlot)) {
                return InteractionResult.FAIL;
            }
            if (equipmentSlot.getType() == EquipmentSlot.Type.HAND && !this.isShowArms()) {
                return InteractionResult.FAIL;
            }
            if (this.swapItem(player, equipmentSlot, itemStack, interactionHand)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private EquipmentSlot getClickedSlot(Vec3 vec3) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        boolean bl = this.isSmall();
        double d = vec3.y / (double)(this.getScale() * this.getAgeScale());
        EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
        if (d >= 0.1) {
            double d2 = bl ? 0.8 : 0.45;
            if (d < 0.1 + d2 && this.hasItemInSlot(equipmentSlot2)) {
                return EquipmentSlot.FEET;
            }
        }
        double d3 = bl ? 0.3 : 0.0;
        if (d >= 0.9 + d3) {
            double d4 = bl ? 1.0 : 0.7;
            if (d < 0.9 + d4 && this.hasItemInSlot(EquipmentSlot.CHEST)) {
                return EquipmentSlot.CHEST;
            }
        }
        if (d >= 0.4) {
            double d5 = bl ? 1.0 : 0.8;
            if (d < 0.4 + d5 && this.hasItemInSlot(EquipmentSlot.LEGS)) {
                return EquipmentSlot.LEGS;
            }
        }
        if (d >= 1.6 && this.hasItemInSlot(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        }
        if (this.hasItemInSlot(EquipmentSlot.MAINHAND)) return equipmentSlot;
        if (!this.hasItemInSlot(EquipmentSlot.OFFHAND)) return equipmentSlot;
        return EquipmentSlot.OFFHAND;
    }

    private boolean isDisabled(EquipmentSlot equipmentSlot) {
        return (this.disabledSlots & 1 << equipmentSlot.getFilterFlag()) != 0 || equipmentSlot.getType() == EquipmentSlot.Type.HAND && !this.isShowArms();
    }

    private boolean swapItem(Player player, EquipmentSlot equipmentSlot, ItemStack itemStack, InteractionHand interactionHand) {
        ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
        if (!itemStack2.isEmpty() && (this.disabledSlots & 1 << equipmentSlot.getFilterFlag() + 8) != 0) {
            return false;
        }
        if (itemStack2.isEmpty() && (this.disabledSlots & 1 << equipmentSlot.getFilterFlag() + 16) != 0) {
            return false;
        }
        if (player.hasInfiniteMaterials() && itemStack2.isEmpty() && !itemStack.isEmpty()) {
            this.setItemSlot(equipmentSlot, itemStack.copyWithCount(1));
            return true;
        }
        if (!itemStack.isEmpty() && itemStack.getCount() > 1) {
            if (!itemStack2.isEmpty()) {
                return false;
            }
            this.setItemSlot(equipmentSlot, itemStack.split(1));
            return true;
        }
        this.setItemSlot(equipmentSlot, itemStack);
        player.setItemInHand(interactionHand, itemStack2);
        return true;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isRemoved()) {
            return false;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill();
            return false;
        }
        if (this.isInvulnerableTo(damageSource) || this.invisible || this.isMarker()) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            this.brokenByAnything(serverLevel, damageSource);
            this.kill();
            return false;
        }
        if (damageSource.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
            if (this.isOnFire()) {
                this.causeDamage(serverLevel, damageSource, 0.15f);
            } else {
                this.igniteForSeconds(5.0f);
            }
            return false;
        }
        if (damageSource.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5f) {
            this.causeDamage(serverLevel, damageSource, 4.0f);
            return false;
        }
        boolean bl = damageSource.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
        boolean bl2 = damageSource.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
        if (!bl && !bl2) {
            return false;
        }
        Entity entity = damageSource.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (!player.getAbilities().mayBuild) {
                return false;
            }
        }
        if (damageSource.isCreativePlayer()) {
            this.playBrokenSound();
            this.showBreakingParticles();
            this.kill();
            return true;
        }
        long l = serverLevel.getGameTime();
        if (l - this.lastHit <= 5L || bl2) {
            this.brokenByPlayer(serverLevel, damageSource);
            this.showBreakingParticles();
            this.kill();
        } else {
            serverLevel.broadcastEntityEvent(this, (byte)32);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
            this.lastHit = l;
        }
        return true;
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 32) {
            if (this.level().isClientSide) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3f, 1.0f, false);
                this.lastHit = this.level().getGameTime();
            }
        } else {
            super.handleEntityEvent(by);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d2) || d2 == 0.0) {
            d2 = 4.0;
        }
        return d < (d2 *= 64.0) * d2;
    }

    private void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.6666666666666666), this.getZ(), 10, this.getBbWidth() / 4.0f, this.getBbHeight() / 4.0f, this.getBbWidth() / 4.0f, 0.05);
        }
    }

    private void causeDamage(ServerLevel serverLevel, DamageSource damageSource, float f) {
        float f2 = this.getHealth();
        if ((f2 -= f) <= 0.5f) {
            this.brokenByAnything(serverLevel, damageSource);
            this.kill();
        } else {
            this.setHealth(f2);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        }
    }

    private void brokenByPlayer(ServerLevel serverLevel, DamageSource damageSource) {
        ItemStack itemStack = new ItemStack(Items.ARMOR_STAND);
        itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        Block.popResource(this.level(), this.blockPosition(), itemStack);
        this.brokenByAnything(serverLevel, damageSource);
    }

    private void brokenByAnything(ServerLevel serverLevel, DamageSource damageSource) {
        ItemStack itemStack;
        int n;
        this.playBrokenSound();
        this.dropAllDeathLoot(serverLevel, damageSource);
        for (n = 0; n < this.handItems.size(); ++n) {
            itemStack = this.handItems.get(n);
            if (itemStack.isEmpty()) continue;
            Block.popResource(this.level(), this.blockPosition().above(), itemStack);
            this.handItems.set(n, ItemStack.EMPTY);
        }
        for (n = 0; n < this.armorItems.size(); ++n) {
            itemStack = this.armorItems.get(n);
            if (itemStack.isEmpty()) continue;
            Block.popResource(this.level(), this.blockPosition().above(), itemStack);
            this.armorItems.set(n, ItemStack.EMPTY);
        }
    }

    private void playBrokenSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0f, 1.0f);
    }

    @Override
    protected float tickHeadTurn(float f, float f2) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
        return 0.0f;
    }

    @Override
    public void travel(Vec3 vec3) {
        if (!this.hasPhysics()) {
            return;
        }
        super.travel(vec3);
    }

    @Override
    public void setYBodyRot(float f) {
        this.yBodyRotO = this.yRotO = f;
        this.yHeadRotO = this.yHeadRot = f;
    }

    @Override
    public void setYHeadRot(float f) {
        this.yBodyRotO = this.yRotO = f;
        this.yHeadRotO = this.yHeadRot = f;
    }

    @Override
    public void tick() {
        Rotations rotations;
        Rotations rotations2;
        Rotations rotations3;
        Rotations rotations4;
        Rotations rotations5;
        super.tick();
        Rotations rotations6 = this.entityData.get(DATA_HEAD_POSE);
        if (!this.headPose.equals(rotations6)) {
            this.setHeadPose(rotations6);
        }
        if (!this.bodyPose.equals(rotations5 = this.entityData.get(DATA_BODY_POSE))) {
            this.setBodyPose(rotations5);
        }
        if (!this.leftArmPose.equals(rotations4 = this.entityData.get(DATA_LEFT_ARM_POSE))) {
            this.setLeftArmPose(rotations4);
        }
        if (!this.rightArmPose.equals(rotations3 = this.entityData.get(DATA_RIGHT_ARM_POSE))) {
            this.setRightArmPose(rotations3);
        }
        if (!this.leftLegPose.equals(rotations2 = this.entityData.get(DATA_LEFT_LEG_POSE))) {
            this.setLeftLegPose(rotations2);
        }
        if (!this.rightLegPose.equals(rotations = this.entityData.get(DATA_RIGHT_LEG_POSE))) {
            this.setRightLegPose(rotations);
        }
    }

    @Override
    protected void updateInvisibilityStatus() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean bl) {
        this.invisible = bl;
        super.setInvisible(bl);
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return this.isInvisible();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        if (this.isMarker()) {
            return PushReaction.IGNORE;
        }
        return super.getPistonPushReaction();
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return this.isMarker();
    }

    private void setSmall(boolean bl) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 1, bl));
    }

    public boolean isSmall() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 1) != 0;
    }

    public void setShowArms(boolean bl) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 4, bl));
    }

    public boolean isShowArms() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
    }

    public void setNoBasePlate(boolean bl) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 8, bl));
    }

    public boolean isNoBasePlate() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 8) != 0;
    }

    private void setMarker(boolean bl) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 16, bl));
    }

    public boolean isMarker() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 0x10) != 0;
    }

    private byte setBit(byte by, int n, boolean bl) {
        by = bl ? (byte)(by | n) : (byte)(by & ~n);
        return by;
    }

    public void setHeadPose(Rotations rotations) {
        this.headPose = rotations;
        this.entityData.set(DATA_HEAD_POSE, rotations);
    }

    public void setBodyPose(Rotations rotations) {
        this.bodyPose = rotations;
        this.entityData.set(DATA_BODY_POSE, rotations);
    }

    public void setLeftArmPose(Rotations rotations) {
        this.leftArmPose = rotations;
        this.entityData.set(DATA_LEFT_ARM_POSE, rotations);
    }

    public void setRightArmPose(Rotations rotations) {
        this.rightArmPose = rotations;
        this.entityData.set(DATA_RIGHT_ARM_POSE, rotations);
    }

    public void setLeftLegPose(Rotations rotations) {
        this.leftLegPose = rotations;
        this.entityData.set(DATA_LEFT_LEG_POSE, rotations);
    }

    public void setRightLegPose(Rotations rotations) {
        this.rightLegPose = rotations;
        this.entityData.set(DATA_RIGHT_LEG_POSE, rotations);
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    public Rotations getBodyPose() {
        return this.bodyPose;
    }

    public Rotations getLeftArmPose() {
        return this.leftArmPose;
    }

    public Rotations getRightArmPose() {
        return this.rightArmPose;
    }

    public Rotations getLeftLegPose() {
        return this.leftLegPose;
    }

    public Rotations getRightLegPose() {
        return this.rightLegPose;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isMarker();
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return entity instanceof Player && !this.level().mayInteract((Player)entity, this.blockPosition());
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_CLIENT_FLAGS.equals(entityDataAccessor)) {
            this.refreshDimensions();
            this.blocksBuilding = !this.isMarker();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getDimensionsMarker(this.isMarker());
    }

    private EntityDimensions getDimensionsMarker(boolean bl) {
        if (bl) {
            return MARKER_DIMENSIONS;
        }
        return this.isBaby() ? BABY_DIMENSIONS : this.getType().getDimensions();
    }

    @Override
    public Vec3 getLightProbePosition(float f) {
        if (this.isMarker()) {
            AABB aABB = this.getDimensionsMarker(false).makeBoundingBox(this.position());
            BlockPos blockPos = this.blockPosition();
            int n = Integer.MIN_VALUE;
            for (BlockPos blockPos2 : BlockPos.betweenClosed(BlockPos.containing(aABB.minX, aABB.minY, aABB.minZ), BlockPos.containing(aABB.maxX, aABB.maxY, aABB.maxZ))) {
                int n2 = Math.max(this.level().getBrightness(LightLayer.BLOCK, blockPos2), this.level().getBrightness(LightLayer.SKY, blockPos2));
                if (n2 == 15) {
                    return Vec3.atCenterOf(blockPos2);
                }
                if (n2 <= n) continue;
                n = n2;
                blockPos = blockPos2.immutable();
            }
            return Vec3.atCenterOf(blockPos);
        }
        return super.getLightProbePosition(f);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return !this.isInvisible() && !this.isMarker();
    }
}

