/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.world.entity.decoration;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

public class ItemFrame
extends HangingEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
    public static final int NUM_ROTATIONS = 8;
    private static final float DEPTH = 0.0625f;
    private static final float WIDTH = 0.75f;
    private static final float HEIGHT = 0.75f;
    private float dropChance = 1.0f;
    private boolean fixed;

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level) {
        super((EntityType<? extends HangingEntity>)entityType, level);
    }

    public ItemFrame(Level level, BlockPos blockPos, Direction direction) {
        this(EntityType.ITEM_FRAME, level, blockPos, direction);
    }

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level, BlockPos blockPos, Direction direction) {
        super((EntityType<? extends HangingEntity>)entityType, level, blockPos);
        this.setDirection(direction);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_ROTATION, 0);
    }

    @Override
    protected void setDirection(Direction direction) {
        Validate.notNull((Object)direction);
        this.direction = direction;
        if (direction.getAxis().isHorizontal()) {
            this.setXRot(0.0f);
            this.setYRot(this.direction.get2DDataValue() * 90);
        } else {
            this.setXRot(-90 * direction.getAxisDirection().getStep());
            this.setYRot(0.0f);
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos blockPos, Direction direction) {
        float f = 0.46875f;
        Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, -0.46875);
        Direction.Axis axis = direction.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : 0.75;
        double d2 = axis == Direction.Axis.Y ? 0.0625 : 0.75;
        double d3 = axis == Direction.Axis.Z ? 0.0625 : 0.75;
        return AABB.ofSize(vec3, d, d2, d3);
    }

    @Override
    public boolean survives() {
        if (this.fixed) {
            return true;
        }
        if (!this.level().noCollision(this)) {
            return false;
        }
        BlockState blockState = this.level().getBlockState(this.pos.relative(this.direction.getOpposite()));
        if (!(blockState.isSolid() || this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(blockState))) {
            return false;
        }
        return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        if (!this.fixed) {
            super.move(moverType, vec3);
        }
    }

    @Override
    public void push(double d, double d2, double d3) {
        if (!this.fixed) {
            super.push(d, d2, d3);
        }
    }

    @Override
    public void kill() {
        this.removeFramedMap(this.getItem());
        super.kill();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.fixed) {
            if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.isCreativePlayer()) {
                return super.hurt(damageSource, f);
            }
            return false;
        }
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!damageSource.is(DamageTypeTags.IS_EXPLOSION) && !this.getItem().isEmpty()) {
            if (!this.level().isClientSide) {
                this.dropItem(damageSource.getEntity(), false);
                this.gameEvent(GameEvent.BLOCK_CHANGE, damageSource.getEntity());
                this.playSound(this.getRemoveItemSound(), 1.0f, 1.0f);
            }
            return true;
        }
        return super.hurt(damageSource, f);
    }

    public SoundEvent getRemoveItemSound() {
        return SoundEvents.ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = 16.0;
        return d < (d2 *= 64.0 * ItemFrame.getViewScale()) * d2;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        this.playSound(this.getBreakSound(), 1.0f, 1.0f);
        this.dropItem(entity, true);
        this.gameEvent(GameEvent.BLOCK_CHANGE, entity);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ITEM_FRAME_BREAK;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(this.getPlaceSound(), 1.0f, 1.0f);
    }

    public SoundEvent getPlaceSound() {
        return SoundEvents.ITEM_FRAME_PLACE;
    }

    private void dropItem(@Nullable Entity entity, boolean bl) {
        Player player;
        if (this.fixed) {
            return;
        }
        ItemStack itemStack = this.getItem();
        this.setItem(ItemStack.EMPTY);
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (entity == null) {
                this.removeFramedMap(itemStack);
            }
            return;
        }
        if (entity instanceof Player && (player = (Player)entity).hasInfiniteMaterials()) {
            this.removeFramedMap(itemStack);
            return;
        }
        if (bl) {
            this.spawnAtLocation(this.getFrameItemStack());
        }
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            this.removeFramedMap(itemStack);
            if (this.random.nextFloat() < this.dropChance) {
                this.spawnAtLocation(itemStack);
            }
        }
    }

    private void removeFramedMap(ItemStack itemStack) {
        MapItemSavedData mapItemSavedData;
        MapId mapId = this.getFramedMapId(itemStack);
        if (mapId != null && (mapItemSavedData = MapItem.getSavedData(mapId, this.level())) != null) {
            mapItemSavedData.removedFromFrame(this.pos, this.getId());
            mapItemSavedData.setDirty(true);
        }
        itemStack.setEntityRepresentation(null);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    @Nullable
    public MapId getFramedMapId(ItemStack itemStack) {
        return itemStack.get(DataComponents.MAP_ID);
    }

    public boolean hasFramedMap() {
        return this.getItem().has(DataComponents.MAP_ID);
    }

    public void setItem(ItemStack itemStack) {
        this.setItem(itemStack, true);
    }

    public void setItem(ItemStack itemStack, boolean bl) {
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(1);
        }
        this.onItemChanged(itemStack);
        this.getEntityData().set(DATA_ITEM, itemStack);
        if (!itemStack.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0f, 1.0f);
        }
        if (bl && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public SlotAccess getSlot(int n) {
        if (n == 0) {
            return SlotAccess.of(this::getItem, this::setItem);
        }
        return super.getSlot(n);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (entityDataAccessor.equals(DATA_ITEM)) {
            this.onItemChanged(this.getItem());
        }
    }

    private void onItemChanged(ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getFrame() != this) {
            itemStack.setEntityRepresentation(this);
        }
        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int n) {
        this.setRotation(n, true);
    }

    private void setRotation(int n, boolean bl) {
        this.getEntityData().set(DATA_ROTATION, n % 8);
        if (bl && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (!this.getItem().isEmpty()) {
            compoundTag.put("Item", this.getItem().save(this.registryAccess()));
            compoundTag.putByte("ItemRotation", (byte)this.getRotation());
            compoundTag.putFloat("ItemDropChance", this.dropChance);
        }
        compoundTag.putByte("Facing", (byte)this.direction.get3DDataValue());
        compoundTag.putBoolean("Invisible", this.isInvisible());
        compoundTag.putBoolean("Fixed", this.fixed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        ItemStack itemStack;
        Object object;
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Item", 10)) {
            object = compoundTag.getCompound("Item");
            itemStack = ItemStack.parse(this.registryAccess(), (Tag)object).orElse(ItemStack.EMPTY);
        } else {
            itemStack = ItemStack.EMPTY;
        }
        object = this.getItem();
        if (!((ItemStack)object).isEmpty() && !ItemStack.matches(itemStack, (ItemStack)object)) {
            this.removeFramedMap((ItemStack)object);
        }
        this.setItem(itemStack, false);
        if (!itemStack.isEmpty()) {
            this.setRotation(compoundTag.getByte("ItemRotation"), false);
            if (compoundTag.contains("ItemDropChance", 99)) {
                this.dropChance = compoundTag.getFloat("ItemDropChance");
            }
        }
        this.setDirection(Direction.from3DDataValue(compoundTag.getByte("Facing")));
        this.setInvisible(compoundTag.getBoolean("Invisible"));
        this.fixed = compoundTag.getBoolean("Fixed");
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        boolean bl;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl2 = !this.getItem().isEmpty();
        boolean bl3 = bl = !itemStack.isEmpty();
        if (this.fixed) {
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide) {
            return bl2 || bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!bl2) {
            if (bl && !this.isRemoved()) {
                MapItemSavedData mapItemSavedData;
                if (itemStack.is(Items.FILLED_MAP) && (mapItemSavedData = MapItem.getSavedData(itemStack, this.level())) != null && mapItemSavedData.isTrackedCountOverLimit(256)) {
                    return InteractionResult.FAIL;
                }
                this.setItem(itemStack);
                this.gameEvent(GameEvent.BLOCK_CHANGE, player);
                itemStack.consume(1, player);
            }
        } else {
            this.playSound(this.getRotateItemSound(), 1.0f, 1.0f);
            this.setRotation(this.getRotation() + 1);
            this.gameEvent(GameEvent.BLOCK_CHANGE, player);
        }
        return InteractionResult.CONSUME;
    }

    public SoundEvent getRotateItemSound() {
        return SoundEvents.ITEM_FRAME_ROTATE_ITEM;
    }

    public int getAnalogOutput() {
        if (this.getItem().isEmpty()) {
            return 0;
        }
        return this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack itemStack = this.getItem();
        if (itemStack.isEmpty()) {
            return this.getFrameItemStack();
        }
        return itemStack.copy();
    }

    protected ItemStack getFrameItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        Direction direction = this.getDirection();
        int n = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + this.getRotation() * 45 + n);
    }
}

