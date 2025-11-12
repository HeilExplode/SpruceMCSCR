/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.entity.EquipmentUser;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public abstract class Mob
extends LivingEntity
implements EquipmentUser,
Leashable,
Targeting {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    private static final int MOB_FLAG_NO_AI = 1;
    private static final int MOB_FLAG_LEFTHANDED = 2;
    private static final int MOB_FLAG_AGGRESSIVE = 4;
    protected static final int PICKUP_REACH = 1;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
    public static final float MAX_WEARING_ARMOR_CHANCE = 0.15f;
    public static final float MAX_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25f;
    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085f;
    public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0f;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04f) - (double)0.6f;
    protected static final ResourceLocation RANDOM_SPAWN_BONUS_ID = ResourceLocation.withDefaultNamespace("random_spawn_bonus");
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    @Nullable
    private LivingEntity target;
    private final Sensing sensing;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    protected final float[] handDropChances = new float[2];
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    protected final float[] armorDropChances = new float[4];
    private ItemStack bodyArmorItem = ItemStack.EMPTY;
    protected float bodyArmorDropChance;
    private boolean canPickUpLoot;
    private boolean persistenceRequired;
    private final Map<PathType, Float> pathfindingMalus = Maps.newEnumMap(PathType.class);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;
    @Nullable
    private Leashable.LeashData leashData;
    private BlockPos restrictCenter = BlockPos.ZERO;
    private float restrictRadius = -1.0f;

    protected Mob(EntityType<? extends Mob> entityType, Level level) {
        super((EntityType<? extends LivingEntity>)entityType, level);
        this.goalSelector = new GoalSelector(level.getProfilerSupplier());
        this.targetSelector = new GoalSelector(level.getProfilerSupplier());
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = this.createBodyControl();
        this.navigation = this.createNavigation(level);
        this.sensing = new Sensing(this);
        Arrays.fill(this.armorDropChances, 0.085f);
        Arrays.fill(this.handDropChances, 0.085f);
        this.bodyArmorDropChance = 0.085f;
        if (level != null && !level.isClientSide) {
            this.registerGoals();
        }
    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
    }

    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(PathType pathType) {
        Object object;
        Entity entity = this.getControlledVehicle();
        Object object2 = entity instanceof Mob && ((Mob)(object = (Mob)entity)).shouldPassengersInheritMalus() ? object : this;
        object = ((Mob)object2).pathfindingMalus.get((Object)pathType);
        return object == null ? pathType.getMalus() : ((Float)object).floatValue();
    }

    public void setPathfindingMalus(PathType pathType, float f) {
        this.pathfindingMalus.put(pathType, Float.valueOf(f));
    }

    public void onPathfindingStart() {
    }

    public void onPathfindingDone() {
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return mob.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return mob.getNavigation();
        }
        return this.navigation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (this.isNoAi()) return null;
        if (!(entity instanceof Mob)) return null;
        Mob mob = (Mob)entity;
        if (!entity.canControlVehicle()) return null;
        Mob mob2 = mob;
        return mob2;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.target;
    }

    @Nullable
    protected final LivingEntity getTargetFromBrain() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity livingEntity) {
        this.target = livingEntity;
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return entityType != EntityType.GHAST;
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return false;
    }

    public void ate() {
        this.gameEvent(GameEvent.EAT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOB_FLAGS_ID, (byte)0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        this.makeSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.level().getProfiler().push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.resetAmbientSoundTime();
            this.playAmbientSound();
        }
        this.level().getProfiler().pop();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.resetAmbientSoundTime();
        super.playHurtSound(damageSource);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    protected int getBaseExperienceReward() {
        if (this.xpReward > 0) {
            int n;
            int n2 = this.xpReward;
            for (n = 0; n < this.armorItems.size(); ++n) {
                if (this.armorItems.get(n).isEmpty() || !(this.armorDropChances[n] <= 1.0f)) continue;
                n2 += 1 + this.random.nextInt(3);
            }
            for (n = 0; n < this.handItems.size(); ++n) {
                if (this.handItems.get(n).isEmpty() || !(this.handDropChances[n] <= 1.0f)) continue;
                n2 += 1 + this.random.nextInt(3);
            }
            if (!this.bodyArmorItem.isEmpty() && this.bodyArmorDropChance <= 1.0f) {
                n2 += 1 + this.random.nextInt(3);
            }
            return n2;
        }
        return this.xpReward;
    }

    public void spawnAnim() {
        if (this.level().isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double d2 = this.random.nextGaussian() * 0.02;
                double d3 = this.random.nextGaussian() * 0.02;
                double d4 = 10.0;
                this.level().addParticle(ParticleTypes.POOF, this.getX(1.0) - d * 10.0, this.getRandomY() - d2 * 10.0, this.getRandomZ(1.0) - d3 * 10.0, d, d2, d3);
            }
        } else {
            this.level().broadcastEntityEvent(this, (byte)20);
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 20) {
            this.spawnAnim();
        } else {
            super.handleEntityEvent(by);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 5 == 0) {
            this.updateControlFlags();
        }
    }

    protected void updateControlFlags() {
        boolean bl = !(this.getControllingPassenger() instanceof Mob);
        boolean bl2 = !(this.getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, bl);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, bl && bl2);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, bl);
    }

    @Override
    protected float tickHeadTurn(float f, float f2) {
        this.bodyRotationControl.clientTick();
        return f2;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("CanPickUpLoot", this.canPickUpLoot());
        compoundTag.putBoolean("PersistenceRequired", this.persistenceRequired);
        ListTag listTag = new ListTag();
        for (ItemStack object2 : this.armorItems) {
            if (!object2.isEmpty()) {
                listTag.add(object2.save(this.registryAccess()));
                continue;
            }
            listTag.add(new CompoundTag());
        }
        compoundTag.put("ArmorItems", listTag);
        ListTag listTag2 = new ListTag();
        for (float f : this.armorDropChances) {
            listTag2.add(FloatTag.valueOf(f));
        }
        compoundTag.put("ArmorDropChances", listTag2);
        ListTag listTag3 = new ListTag();
        for (ItemStack itemStack : this.handItems) {
            if (!itemStack.isEmpty()) {
                listTag3.add(itemStack.save(this.registryAccess()));
                continue;
            }
            listTag3.add(new CompoundTag());
        }
        compoundTag.put("HandItems", listTag3);
        ListTag listTag4 = new ListTag();
        for (float f : this.handDropChances) {
            listTag4.add(FloatTag.valueOf(f));
        }
        compoundTag.put("HandDropChances", listTag4);
        if (!this.bodyArmorItem.isEmpty()) {
            compoundTag.put("body_armor_item", this.bodyArmorItem.save(this.registryAccess()));
            compoundTag.putFloat("body_armor_drop_chance", this.bodyArmorDropChance);
        }
        this.writeLeashData(compoundTag, this.leashData);
        compoundTag.putBoolean("LeftHanded", this.isLeftHanded());
        if (this.lootTable != null) {
            compoundTag.putString("DeathLootTable", this.lootTable.location().toString());
            if (this.lootTableSeed != 0L) {
                compoundTag.putLong("DeathLootTableSeed", this.lootTableSeed);
            }
        }
        if (this.isNoAi()) {
            compoundTag.putBoolean("NoAI", this.isNoAi());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        CompoundTag compoundTag2;
        int n;
        ListTag listTag;
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("CanPickUpLoot", 1)) {
            this.setCanPickUpLoot(compoundTag.getBoolean("CanPickUpLoot"));
        }
        this.persistenceRequired = compoundTag.getBoolean("PersistenceRequired");
        if (compoundTag.contains("ArmorItems", 9)) {
            listTag = compoundTag.getList("ArmorItems", 10);
            for (n = 0; n < this.armorItems.size(); ++n) {
                compoundTag2 = listTag.getCompound(n);
                this.armorItems.set(n, ItemStack.parseOptional(this.registryAccess(), compoundTag2));
            }
        }
        if (compoundTag.contains("ArmorDropChances", 9)) {
            listTag = compoundTag.getList("ArmorDropChances", 5);
            for (n = 0; n < listTag.size(); ++n) {
                this.armorDropChances[n] = listTag.getFloat(n);
            }
        }
        if (compoundTag.contains("HandItems", 9)) {
            listTag = compoundTag.getList("HandItems", 10);
            for (n = 0; n < this.handItems.size(); ++n) {
                compoundTag2 = listTag.getCompound(n);
                this.handItems.set(n, ItemStack.parseOptional(this.registryAccess(), compoundTag2));
            }
        }
        if (compoundTag.contains("HandDropChances", 9)) {
            listTag = compoundTag.getList("HandDropChances", 5);
            for (n = 0; n < listTag.size(); ++n) {
                this.handDropChances[n] = listTag.getFloat(n);
            }
        }
        if (compoundTag.contains("body_armor_item", 10)) {
            this.bodyArmorItem = ItemStack.parse(this.registryAccess(), compoundTag.getCompound("body_armor_item")).orElse(ItemStack.EMPTY);
            this.bodyArmorDropChance = compoundTag.getFloat("body_armor_drop_chance");
        } else {
            this.bodyArmorItem = ItemStack.EMPTY;
        }
        this.leashData = this.readLeashData(compoundTag);
        this.setLeftHanded(compoundTag.getBoolean("LeftHanded"));
        if (compoundTag.contains("DeathLootTable", 8)) {
            this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(compoundTag.getString("DeathLootTable")));
            this.lootTableSeed = compoundTag.getLong("DeathLootTableSeed");
        }
        this.setNoAi(compoundTag.getBoolean("NoAI"));
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean bl) {
        super.dropFromLootTable(damageSource, bl);
        this.lootTable = null;
    }

    @Override
    public final ResourceKey<LootTable> getLootTable() {
        return this.lootTable == null ? this.getDefaultLootTable() : this.lootTable;
    }

    protected ResourceKey<LootTable> getDefaultLootTable() {
        return super.getLootTable();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setZza(float f) {
        this.zza = f;
    }

    public void setYya(float f) {
        this.yya = f;
    }

    public void setXxa(float f) {
        this.xxa = f;
    }

    @Override
    public void setSpeed(float f) {
        super.setSpeed(f);
        this.setZza(f);
    }

    public void stopInPlace() {
        this.getNavigation().stop();
        this.setXxa(0.0f);
        this.setYya(0.0f);
        this.setSpeed(0.0f);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.level().getProfiler().push("looting");
        if (!this.level().isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            Vec3i vec3i = this.getPickupReach();
            List<ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
            for (ItemEntity itemEntity : list) {
                if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty() || itemEntity.hasPickUpDelay() || !this.wantsToPickUp(itemEntity.getItem())) continue;
                this.pickUpItem(itemEntity);
            }
        }
        this.level().getProfiler().pop();
    }

    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack itemStack2 = this.equipItemIfPossible(itemStack.copy());
        if (!itemStack2.isEmpty()) {
            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemStack2.getCount());
            itemStack.shrink(itemStack2.getCount());
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public ItemStack equipItemIfPossible(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
        boolean bl = this.canReplaceCurrentItem(itemStack, itemStack2);
        if (equipmentSlot.isArmor() && !bl) {
            equipmentSlot = EquipmentSlot.MAINHAND;
            itemStack2 = this.getItemBySlot(equipmentSlot);
            bl = itemStack2.isEmpty();
        }
        if (bl && this.canHoldItem(itemStack)) {
            double d = this.getEquipmentDropChance(equipmentSlot);
            if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.spawnAtLocation(itemStack2);
            }
            ItemStack itemStack3 = equipmentSlot.limit(itemStack);
            this.setItemSlotAndDropWhenKilled(equipmentSlot, itemStack3);
            return itemStack3;
        }
        return ItemStack.EMPTY;
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.setItemSlot(equipmentSlot, itemStack);
        this.setGuaranteedDrop(equipmentSlot);
        this.persistenceRequired = true;
    }

    public void setGuaranteedDrop(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case HAND: {
                this.handDropChances[equipmentSlot.getIndex()] = 2.0f;
                break;
            }
            case HUMANOID_ARMOR: {
                this.armorDropChances[equipmentSlot.getIndex()] = 2.0f;
                break;
            }
            case ANIMAL_ARMOR: {
                this.bodyArmorDropChance = 2.0f;
            }
        }
    }

    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.getItem() instanceof SwordItem) {
            double d;
            if (!(itemStack2.getItem() instanceof SwordItem)) {
                return true;
            }
            double d2 = this.getApproximateAttackDamageWithItem(itemStack);
            if (d2 != (d = this.getApproximateAttackDamageWithItem(itemStack2))) {
                return d2 > d;
            }
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof BowItem && itemStack2.getItem() instanceof BowItem) {
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof CrossbowItem && itemStack2.getItem() instanceof CrossbowItem) {
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        Item item = itemStack.getItem();
        if (item instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem)item;
            if (EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                return false;
            }
            if (!(itemStack2.getItem() instanceof ArmorItem)) {
                return true;
            }
            item = (ArmorItem)itemStack2.getItem();
            if (armorItem.getDefense() != ((ArmorItem)item).getDefense()) {
                return armorItem.getDefense() > ((ArmorItem)item).getDefense();
            }
            if (armorItem.getToughness() != ((ArmorItem)item).getToughness()) {
                return armorItem.getToughness() > ((ArmorItem)item).getToughness();
            }
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof DiggerItem) {
            if (itemStack2.getItem() instanceof BlockItem) {
                return true;
            }
            if (itemStack2.getItem() instanceof DiggerItem) {
                double d;
                double d3 = this.getApproximateAttackDamageWithItem(itemStack);
                if (d3 != (d = this.getApproximateAttackDamageWithItem(itemStack2))) {
                    return d3 > d;
                }
                return this.canReplaceEqualItem(itemStack, itemStack2);
            }
        }
        return false;
    }

    private double getApproximateAttackDamageWithItem(ItemStack itemStack) {
        ItemAttributeModifiers itemAttributeModifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return itemAttributeModifiers.compute(this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), EquipmentSlot.MAINHAND);
    }

    public boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.getDamageValue() < itemStack2.getDamageValue()) {
            return true;
        }
        return Mob.hasAnyComponentExceptDamage(itemStack) && !Mob.hasAnyComponentExceptDamage(itemStack2);
    }

    private static boolean hasAnyComponentExceptDamage(ItemStack itemStack) {
        DataComponentMap dataComponentMap = itemStack.getComponents();
        int n = dataComponentMap.size();
        return n > 1 || n == 1 && !dataComponentMap.has(DataComponents.DAMAGE);
    }

    public boolean canHoldItem(ItemStack itemStack) {
        return true;
    }

    public boolean wantsToPickUp(ItemStack itemStack) {
        return this.canHoldItem(itemStack);
    }

    public boolean removeWhenFarAway(double d) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return this.isPassenger();
    }

    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
            this.noActionTime = 0;
            return;
        }
        Player player = this.level().getNearestPlayer(this, -1.0);
        if (player != null) {
            int n;
            int n2;
            double d = player.distanceToSqr(this);
            if (d > (double)(n2 = (n = this.getType().getCategory().getDespawnDistance()) * n) && this.removeWhenFarAway(d)) {
                this.discard();
            }
            int n3 = this.getType().getCategory().getNoDespawnDistance();
            int n4 = n3 * n3;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d > (double)n4 && this.removeWhenFarAway(d)) {
                this.discard();
            } else if (d < (double)n4) {
                this.noActionTime = 0;
            }
        }
    }

    @Override
    protected final void serverAiStep() {
        ++this.noActionTime;
        ProfilerFiller profilerFiller = this.level().getProfiler();
        profilerFiller.push("sensing");
        this.sensing.tick();
        profilerFiller.pop();
        int n = this.tickCount + this.getId();
        if (n % 2 == 0 || this.tickCount <= 1) {
            profilerFiller.push("targetSelector");
            this.targetSelector.tick();
            profilerFiller.pop();
            profilerFiller.push("goalSelector");
            this.goalSelector.tick();
            profilerFiller.pop();
        } else {
            profilerFiller.push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            profilerFiller.pop();
            profilerFiller.push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            profilerFiller.pop();
        }
        profilerFiller.push("navigation");
        this.navigation.tick();
        profilerFiller.pop();
        profilerFiller.push("mob tick");
        this.customServerAiStep();
        profilerFiller.pop();
        profilerFiller.push("controls");
        profilerFiller.push("move");
        this.moveControl.tick();
        profilerFiller.popPush("look");
        this.lookControl.tick();
        profilerFiller.popPush("jump");
        this.jumpControl.tick();
        profilerFiller.pop();
        profilerFiller.pop();
        this.sendDebugPackets();
    }

    protected void sendDebugPackets() {
        DebugPackets.sendGoalSelector(this.level(), this, this.goalSelector);
    }

    protected void customServerAiStep() {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    protected void clampHeadRotationToBody() {
        float f = this.getMaxHeadYRot();
        float f2 = this.getYHeadRot();
        float f3 = Mth.wrapDegrees(this.yBodyRot - f2);
        float f4 = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - f2), -f, f);
        float f5 = f2 + f3 - f4;
        this.setYHeadRot(f5);
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity entity, float f, float f2) {
        double d;
        double d2 = entity.getX() - this.getX();
        double d3 = entity.getZ() - this.getZ();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            d = livingEntity.getEyeY() - this.getEyeY();
        } else {
            d = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double d4 = Math.sqrt(d2 * d2 + d3 * d3);
        float f3 = (float)(Mth.atan2(d3, d2) * 57.2957763671875) - 90.0f;
        float f4 = (float)(-(Mth.atan2(d, d4) * 57.2957763671875));
        this.setXRot(this.rotlerp(this.getXRot(), f4, f2));
        this.setYRot(this.rotlerp(this.getYRot(), f3, f));
    }

    private float rotlerp(float f, float f2, float f3) {
        float f4 = Mth.wrapDegrees(f2 - f);
        if (f4 > f3) {
            f4 = f3;
        }
        if (f4 < -f3) {
            f4 = -f3;
        }
        return f + f4;
    }

    public static boolean checkMobSpawnRules(EntityType<? extends Mob> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2 = blockPos.below();
        return mobSpawnType == MobSpawnType.SPAWNER || levelAccessor.getBlockState(blockPos2).isValidSpawn(levelAccessor, blockPos2, entityType);
    }

    public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return !levelReader.containsAnyLiquid(this.getBoundingBox()) && levelReader.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int n) {
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return this.getComfortableFallDistance(0.0f);
        }
        int n = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if ((n -= (3 - this.level().getDifficulty().getId()) * 4) < 0) {
            n = 0;
        }
        return this.getComfortableFallDistance(n);
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    public ItemStack getBodyArmorItem() {
        return this.bodyArmorItem;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return equipmentSlot != EquipmentSlot.BODY;
    }

    public boolean isWearingBodyArmor() {
        return !this.getItemBySlot(EquipmentSlot.BODY).isEmpty();
    }

    public boolean isBodyArmorItem(ItemStack itemStack) {
        return false;
    }

    public void setBodyArmorItem(ItemStack itemStack) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.BODY, itemStack);
    }

    @Override
    public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
        if (this.bodyArmorItem.isEmpty()) {
            return this.armorItems;
        }
        return Iterables.concat(this.armorItems, List.of(this.bodyArmorItem));
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot.getType()) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.Type.HAND -> this.handItems.get(equipmentSlot.getIndex());
            case EquipmentSlot.Type.HUMANOID_ARMOR -> this.armorItems.get(equipmentSlot.getIndex());
            case EquipmentSlot.Type.ANIMAL_ARMOR -> this.bodyArmorItem;
        };
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
                break;
            }
            case ANIMAL_ARMOR: {
                ItemStack itemStack2 = this.bodyArmorItem;
                this.bodyArmorItem = itemStack;
                this.onEquipItem(equipmentSlot, itemStack2, itemStack);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            float f = this.getEquipmentDropChance(equipmentSlot);
            if (f == 0.0f) continue;
            boolean bl2 = f > 1.0f;
            Object object = damageSource.getEntity();
            if (object instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)object;
                object = this.level();
                if (object instanceof ServerLevel) {
                    ServerLevel serverLevel2 = (ServerLevel)object;
                    f = EnchantmentHelper.processEquipmentDropChance(serverLevel2, livingEntity, damageSource, f);
                }
            }
            if (itemStack.isEmpty() || EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) || !bl && !bl2 || !(this.random.nextFloat() < f)) continue;
            if (!bl2 && itemStack.isDamageableItem()) {
                itemStack.setDamageValue(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
            }
            this.spawnAtLocation(itemStack);
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
    }

    protected float getEquipmentDropChance(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot.getType()) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.Type.HAND -> this.handDropChances[equipmentSlot.getIndex()];
            case EquipmentSlot.Type.HUMANOID_ARMOR -> this.armorDropChances[equipmentSlot.getIndex()];
            case EquipmentSlot.Type.ANIMAL_ARMOR -> this.bodyArmorDropChance;
        };
    }

    public void dropPreservedEquipment() {
        this.dropPreservedEquipment(itemStack -> true);
    }

    public Set<EquipmentSlot> dropPreservedEquipment(Predicate<ItemStack> predicate) {
        HashSet<EquipmentSlot> hashSet = new HashSet<EquipmentSlot>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (itemStack.isEmpty()) continue;
            if (!predicate.test(itemStack)) {
                hashSet.add(equipmentSlot);
                continue;
            }
            double d = this.getEquipmentDropChance(equipmentSlot);
            if (!(d > 1.0)) continue;
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            this.spawnAtLocation(itemStack);
        }
        return hashSet;
    }

    private LootParams createEquipmentParams(ServerLevel serverLevel) {
        return new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.EQUIPMENT);
    }

    public void equip(EquipmentTable equipmentTable) {
        this.equip(equipmentTable.lootTable(), equipmentTable.slotDropChances());
    }

    public void equip(ResourceKey<LootTable> resourceKey, Map<EquipmentSlot, Float> map) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.equip(resourceKey, this.createEquipmentParams(serverLevel), map);
        }
    }

    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        if (randomSource.nextFloat() < 0.15f * difficultyInstance.getSpecialMultiplier()) {
            float f;
            int n = randomSource.nextInt(2);
            float f2 = f = this.level().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            if (randomSource.nextFloat() < 0.095f) {
                ++n;
            }
            if (randomSource.nextFloat() < 0.095f) {
                ++n;
            }
            if (randomSource.nextFloat() < 0.095f) {
                ++n;
            }
            boolean bl = true;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Item item;
                if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (!bl && randomSource.nextFloat() < f) break;
                bl = false;
                if (!itemStack.isEmpty() || (item = Mob.getEquipmentForSlot(equipmentSlot, n)) == null) continue;
                this.setItemSlot(equipmentSlot, new ItemStack(item));
            }
        }
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int n) {
        switch (equipmentSlot) {
            case HEAD: {
                if (n == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (n == 1) {
                    return Items.GOLDEN_HELMET;
                }
                if (n == 2) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (n == 3) {
                    return Items.IRON_HELMET;
                }
                if (n == 4) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if (n == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (n == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (n == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (n == 3) {
                    return Items.IRON_CHESTPLATE;
                }
                if (n == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if (n == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (n == 1) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (n == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (n == 3) {
                    return Items.IRON_LEGGINGS;
                }
                if (n == 4) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if (n == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (n == 1) {
                    return Items.GOLDEN_BOOTS;
                }
                if (n == 2) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (n == 3) {
                    return Items.IRON_BOOTS;
                }
                if (n != 4) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantSpawnedArmor(serverLevelAccessor, randomSource, equipmentSlot, difficultyInstance);
        }
    }

    protected void enchantSpawnedWeapon(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.enchantSpawnedEquipment(serverLevelAccessor, EquipmentSlot.MAINHAND, randomSource, 0.25f, difficultyInstance);
    }

    protected void enchantSpawnedArmor(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, EquipmentSlot equipmentSlot, DifficultyInstance difficultyInstance) {
        this.enchantSpawnedEquipment(serverLevelAccessor, equipmentSlot, randomSource, 0.5f, difficultyInstance);
    }

    private void enchantSpawnedEquipment(ServerLevelAccessor serverLevelAccessor, EquipmentSlot equipmentSlot, RandomSource randomSource, float f, DifficultyInstance difficultyInstance) {
        ItemStack itemStack = this.getItemBySlot(equipmentSlot);
        if (!itemStack.isEmpty() && randomSource.nextFloat() < f * difficultyInstance.getSpecialMultiplier()) {
            EnchantmentHelper.enchantItemFromProvider(itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource);
            this.setItemSlot(equipmentSlot, itemStack);
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        AttributeInstance attributeInstance = Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE));
        if (!attributeInstance.hasModifier(RANDOM_SPAWN_BONUS_ID)) {
            attributeInstance.addPermanentModifier(new AttributeModifier(RANDOM_SPAWN_BONUS_ID, randomSource.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.setLeftHanded(randomSource.nextFloat() < 0.05f);
        return spawnGroupData;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    @Override
    public void setDropChance(EquipmentSlot equipmentSlot, float f) {
        switch (equipmentSlot.getType()) {
            case HAND: {
                this.handDropChances[equipmentSlot.getIndex()] = f;
                break;
            }
            case HUMANOID_ARMOR: {
                this.armorDropChances[equipmentSlot.getIndex()] = f;
                break;
            }
            case ANIMAL_ARMOR: {
                this.bodyArmorDropChance = f;
            }
        }
    }

    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean bl) {
        this.canPickUpLoot = bl;
    }

    @Override
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        return this.getItemBySlot(equipmentSlot).isEmpty() && this.canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override
    public final InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (!this.isAlive()) {
            return InteractionResult.PASS;
        }
        InteractionResult interactionResult = this.checkAndHandleImportantInteractions(player, interactionHand);
        if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionResult;
        }
        InteractionResult interactionResult2 = super.interact(player, interactionHand);
        if (interactionResult2 != InteractionResult.PASS) {
            return interactionResult2;
        }
        interactionResult = this.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionResult;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionHand) {
        Object object;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.NAME_TAG) && ((InteractionResult)((Object)(object = itemStack.interactLivingEntity(player, this, interactionHand)))).consumesAction()) {
            return object;
        }
        if (itemStack.getItem() instanceof SpawnEggItem) {
            if (this.level() instanceof ServerLevel) {
                object = (SpawnEggItem)itemStack.getItem();
                Optional<Mob> optional = ((SpawnEggItem)object).spawnOffspringFromSpawnEgg(player, this, this.getType(), (ServerLevel)this.level(), this.position(), itemStack);
                optional.ifPresent(mob -> this.onOffspringSpawnedFromEgg(player, (Mob)mob));
                return optional.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
    }

    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    public boolean isWithinRestriction() {
        return this.isWithinRestriction(this.blockPosition());
    }

    public boolean isWithinRestriction(BlockPos blockPos) {
        if (this.restrictRadius == -1.0f) {
            return true;
        }
        return this.restrictCenter.distSqr(blockPos) < (double)(this.restrictRadius * this.restrictRadius);
    }

    public void restrictTo(BlockPos blockPos, int n) {
        this.restrictCenter = blockPos;
        this.restrictRadius = n;
    }

    public BlockPos getRestrictCenter() {
        return this.restrictCenter;
    }

    public float getRestrictRadius() {
        return this.restrictRadius;
    }

    public void clearRestriction() {
        this.restrictRadius = -1.0f;
    }

    public boolean hasRestriction() {
        return this.restrictRadius != -1.0f;
    }

    @Nullable
    public <T extends Mob> T convertTo(EntityType<T> entityType, boolean bl) {
        if (this.isRemoved()) {
            return null;
        }
        Mob mob = (Mob)entityType.create(this.level());
        if (mob == null) {
            return null;
        }
        mob.copyPosition(this);
        mob.setBaby(this.isBaby());
        mob.setNoAi(this.isNoAi());
        if (this.hasCustomName()) {
            mob.setCustomName(this.getCustomName());
            mob.setCustomNameVisible(this.isCustomNameVisible());
        }
        if (this.isPersistenceRequired()) {
            mob.setPersistenceRequired();
        }
        mob.setInvulnerable(this.isInvulnerable());
        if (bl) {
            mob.setCanPickUpLoot(this.canPickUpLoot());
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                mob.setItemSlot(equipmentSlot, itemStack.copyAndClear());
                mob.setDropChance(equipmentSlot, this.getEquipmentDropChance(equipmentSlot));
            }
        }
        this.level().addFreshEntity(mob);
        if (this.isPassenger()) {
            Entity entity = this.getVehicle();
            this.stopRiding();
            mob.startRiding(entity, true);
        }
        this.discard();
        return (T)mob;
    }

    @Override
    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void dropLeash(boolean bl, boolean bl2) {
        Leashable.super.dropLeash(bl, bl2);
        if (this.getLeashData() == null) {
            this.clearRestriction();
        }
    }

    @Override
    public void leashTooFarBehaviour() {
        Leashable.super.leashTooFarBehaviour();
        this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Enemy);
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl) {
        boolean bl2 = super.startRiding(entity, bl);
        if (bl2 && this.isLeashed()) {
            this.dropLeash(true, true);
        }
        return bl2;
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isNoAi();
    }

    public void setNoAi(boolean bl) {
        byte by = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(by | 1) : (byte)(by & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean bl) {
        byte by = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(by | 2) : (byte)(by & 0xFFFFFFFD));
    }

    public void setAggressive(boolean bl) {
        byte by = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(by | 4) : (byte)(by & 0xFFFFFFFB));
    }

    public boolean isNoAi() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
    }

    public boolean isAggressive() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
    }

    public void setBaby(boolean bl) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public boolean isWithinMeleeAttackRange(LivingEntity livingEntity) {
        return this.getAttackBoundingBox().intersects(livingEntity.getHitbox());
    }

    protected AABB getAttackBoundingBox() {
        AABB aABB;
        Entity entity = this.getVehicle();
        if (entity != null) {
            AABB aABB2 = entity.getBoundingBox();
            AABB aABB3 = this.getBoundingBox();
            aABB = new AABB(Math.min(aABB3.minX, aABB2.minX), aABB3.minY, Math.min(aABB3.minZ, aABB2.minZ), Math.max(aABB3.maxX, aABB2.maxX), aABB3.maxY, Math.max(aABB3.maxZ, aABB2.maxZ));
        } else {
            aABB = this.getBoundingBox();
        }
        return aABB.inflate(DEFAULT_ATTACK_REACH, 0.0, DEFAULT_ATTACK_REACH);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean bl;
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        DamageSource damageSource = this.damageSources().mobAttack(this);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            f = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, f);
        }
        if (bl = entity.hurt(damageSource, f)) {
            Level level2;
            Object object;
            float f2 = this.getKnockback(entity, damageSource);
            if (f2 > 0.0f && entity instanceof LivingEntity) {
                object = (LivingEntity)entity;
                ((LivingEntity)object).knockback(f2 * 0.5f, Mth.sin(this.getYRot() * ((float)Math.PI / 180)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180)));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if ((level2 = this.level()) instanceof ServerLevel) {
                object = (ServerLevel)level2;
                EnchantmentHelper.doPostAttackEffects((ServerLevel)object, entity, damageSource);
            }
            this.setLastHurtMob(entity);
            this.playAttackSound();
        }
        return bl;
    }

    protected void playAttackSound() {
    }

    protected boolean isSunBurnTick() {
        if (this.level().isDay() && !this.level().isClientSide) {
            boolean bl;
            float f = this.getLightLevelDependentMagicValue();
            BlockPos blockPos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean bl2 = bl = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
            if (f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.level().canSeeSky(blockPos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid(tagKey);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void removeFreeWill() {
        this.removeAllGoals(goal -> true);
        this.getBrain().removeAllBehaviors();
    }

    public void removeAllGoals(Predicate<Goal> predicate) {
        this.goalSelector.removeAllGoals(predicate);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        this.getAllSlots().forEach(itemStack -> {
            if (!itemStack.isEmpty()) {
                itemStack.setCount(0);
            }
        });
    }

    @Override
    @Nullable
    public ItemStack getPickResult() {
        SpawnEggItem spawnEggItem = SpawnEggItem.byId(this.getType());
        if (spawnEggItem == null) {
            return null;
        }
        return new ItemStack(spawnEggItem);
    }
}

