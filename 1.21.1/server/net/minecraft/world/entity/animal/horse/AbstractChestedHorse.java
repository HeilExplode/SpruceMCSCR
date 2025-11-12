/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractChestedHorse
extends AbstractHorse {
    private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);
    private final EntityDimensions babyDimensions;

    protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
        this.canGallop = false;
        this.babyDimensions = entityType.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, entityType.getHeight() - 0.15625f, 0.0f)).scale(0.5f);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AbstractChestedHorse.generateMaxHealth(randomSource::nextInt));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_CHEST, false);
    }

    public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
        return AbstractChestedHorse.createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.175f).add(Attributes.JUMP_STRENGTH, 0.5);
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_ID_CHEST);
    }

    public void setChest(boolean bl) {
        this.entityData.set(DATA_ID_CHEST, bl);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? this.babyDimensions : super.getDefaultDimensions(pose);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.hasChest()) {
            if (!((Level)this.level()).isClientSide) {
                this.spawnAtLocation(Blocks.CHEST);
            }
            this.setChest(false);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            ListTag listTag = new ListTag();
            for (int i = 1; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemStack = this.inventory.getItem(i);
                if (itemStack.isEmpty()) continue;
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putByte("Slot", (byte)(i - 1));
                listTag.add(itemStack.save(this.registryAccess(), compoundTag2));
            }
            compoundTag.put("Items", listTag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setChest(compoundTag.getBoolean("ChestedHorse"));
        this.createInventory();
        if (this.hasChest()) {
            ListTag listTag = compoundTag.getList("Items", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                int n = compoundTag2.getByte("Slot") & 0xFF;
                if (n >= this.inventory.getContainerSize() - 1) continue;
                this.inventory.setItem(n + 1, ItemStack.parse(this.registryAccess(), compoundTag2).orElse(ItemStack.EMPTY));
            }
        }
        this.syncSaddleToClients();
    }

    @Override
    public SlotAccess getSlot(int n) {
        if (n == 499) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return AbstractChestedHorse.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    if (itemStack.isEmpty()) {
                        if (AbstractChestedHorse.this.hasChest()) {
                            AbstractChestedHorse.this.setChest(false);
                            AbstractChestedHorse.this.createInventory();
                        }
                        return true;
                    }
                    if (itemStack.is(Items.CHEST)) {
                        if (!AbstractChestedHorse.this.hasChest()) {
                            AbstractChestedHorse.this.setChest(true);
                            AbstractChestedHorse.this.createInventory();
                        }
                        return true;
                    }
                    return false;
                }
            };
        }
        return super.getSlot(n);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        boolean bl;
        boolean bl2 = bl = !this.isBaby() && this.isTamed() && player.isSecondaryUseActive();
        if (this.isVehicle() || bl) {
            return super.mobInteract(player, interactionHand);
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty()) {
            if (this.isFood(itemStack)) {
                return this.fedFood(player, itemStack);
            }
            if (!this.isTamed()) {
                this.makeMad();
                return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide);
            }
            if (!this.hasChest() && itemStack.is(Items.CHEST)) {
                this.equipChest(player, itemStack);
                return InteractionResult.sidedSuccess(((Level)this.level()).isClientSide);
            }
        }
        return super.mobInteract(player, interactionHand);
    }

    private void equipChest(Player player, ItemStack itemStack) {
        this.setChest(true);
        this.playChestEquipsSound();
        itemStack.consume(1, player);
        this.createInventory();
    }

    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }
}

